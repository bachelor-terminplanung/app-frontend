package at.terminplaner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeRecognizer {

    public enum Type {
        DATE, TIME
    }

    public static List<String> getRecognicedDatesOrTimes(String detectedText, Type type) {
        List<String> results = new ArrayList<>();
        Pattern pattern = null;

        if (type == Type.DATE) {
            pattern = Pattern.compile(
                    "\\b(?:" + // word limit for start
                            "\\d{1,2}" + // 1 or 2 numbers are allowed
                            "[.\\-/]" + // followed by ".", "-" or "/"
                            "\\d{1,2}" +
                            "(?:[.\\-/]\\d{2,4})?" +  // optional year with 2 or 4 numbers
                            // for dates like 4.3, 13-01-2023, 29/02/11
                            "|" +
                            "\\d{4}" + // 4 numbers for year
                            "[.\\-/]" +
                            "\\d{1,2}" + // month
                            "[.\\-/]" +
                            "\\d{1,2}" + // day
                            ")\\b" + // word limit for end
                            "(?!" + // Negative Lookahead → the expression must not follow
                            "\\s?" + // optional space
                            "(Uhr|h|:|\\^))"
                    // for dates like 2020-12-12, 2017/12/16
            );
        } else if (type == Type.TIME) {
            pattern = Pattern.compile(
                    "\\b(?<!\\d[\\.\\-/])(?:[0-1]?\\d|2[0-3])(?:" +
                            "[:\\^h]\\d{2}" +
                            "|\\.\\d{2}\\s?Uhr" +
                            "|\\sUhr(?:\\s\\d{1,2})?" +
                            ")\\b"
            );
        }

        Matcher matcher = pattern.matcher(detectedText);
        while (matcher.find()) {
            String raw = matcher.group();
            String formatted = tryFormat(raw, type);
            if (formatted != null) {
                results.add(formatted);
            } else {
                results.add("invalid: " + raw);
            }
        }

        return results;
    }
    private static String tryFormat(String input, Type type) {
        if (type == Type.DATE) {
            return formatDate(input);
        } else {
            return "time";
        }
    }
    private static String formatDate (String date) {
        // d, M can either be 1 or 2 numbers
        List<String> patterns = Arrays.asList(
                "yyyy-MM-dd", "d.M.yyyy", "d-M-yyyy", "d/M/yyyy",
                "d.M.yy", "d-M-yy", "d/M/yy",
                "d.M", "d-M", "d/M"
        );

        for (String pattern : patterns) {
            try {
                String adjustedDate = date;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

                // there is no year -> add current year
                if (pattern.equals("d.M") || pattern.equals("d-M") || pattern.equals("d/M")) {
                    adjustedDate += "." + LocalDate.now().getYear();
                    formatter = DateTimeFormatter.ofPattern(pattern + ".yyyy");
                }

                // add century e.g. 22 -> 2022
                if (pattern.contains("yy") && !pattern.contains("yyyy")) {
                    String[] parts = date.split("[.\\-/]");
                    int year = Integer.parseInt(parts[2]);
                    int currentCentury = (LocalDate.now().getYear() / 100) * 100;
                    if (year < 100) {
                        adjustedDate = parts[0] + "-" + parts[1] + "-" + (currentCentury + year);
                        formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    }
                }

                LocalDate parsedDate = LocalDate.parse(adjustedDate, formatter);

                // leap year test
                if (parsedDate.getDayOfMonth() == 29 && parsedDate.getMonthValue() == 2 && !parsedDate.isLeapYear()) {
                    return null;
                }

                return parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
        return date;
    }
}
