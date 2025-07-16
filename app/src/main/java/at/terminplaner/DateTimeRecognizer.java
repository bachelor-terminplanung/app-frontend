package at.terminplaner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
                    "\\b" +
                            "(?<!\\d[\\.\\-/])" + // there is no ".", "-" and "/" allowed before
                            "(?:[0-1]?\\d|2[0-3])" + // hours from 0 to 23
                            "(?:" + // for next block
                            "[:\\^h]" + // followed by ":", "^", or "h"
                            "\\d{2}" + // 2 digits for minutes
                            "|\\.\\d{2}\\s?Uhr" + // "." with 2 digits and optional space before Uhr (e.g. 4.30 Uhr)
                            "|\\sUhr(?:\\s\\d{1,2})?" + // space Uhr and optional space and 1-2 digits
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
            return formatTime(input);
        }
    }
    private static String formatDate(String date) {
        List<String> monthNamePatterns = Arrays.asList(
                "d. MMMM yyyy",  // 20. März 2020
                "d MMMM yyyy",   // 20 März 2020
                "d. MMMM",       // 16. Juni
                "d MMMM"         // 16 Juni
        );

        for (String pattern : monthNamePatterns) {
            try {
                String adjusted = date.trim().replace(",","").trim();

                if (!pattern.contains("yyyy")) {
                    adjusted += " " + LocalDate.now().getYear();
                    pattern += " yyyy";
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDate parsedDate = LocalDate.parse(adjusted, formatter);
                return parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }

        List<String> patterns = Arrays.asList(
                "yyyy-MM-dd", "dd.MM.yyyy", "dd-MM-yyyy", "dd/MM/yyyy",
                "d.M.yyyy", "d-M-yyyy", "d/M/yyyy",
                "dd.MM.yy", "dd-MM-yy", "dd/MM/yy",
                "d.M.yy", "d-M-yy", "d/M/yy",
                "d.M", "d-M", "d/M"
        );

        for (String pattern : patterns) {
            try {
                String adjustedDate = date;

                // there is no year -> add current year
                if (pattern.equals("d.M") || pattern.equals("d-M") || pattern.equals("d/M")) {

                    if (isValidDayAndMonth(date) == 1) {
                        return null;
                    }

                    adjustedDate += "." + LocalDate.now().getYear();
                    pattern += ".yyyy";
                }

                // add century e.g. 22 -> 2022
                if (pattern.equals("d.M.yy") || pattern.equals("d-M-yy") || pattern.equals("d/M/yy")
                        || pattern.equals("dd.MM.yy") || pattern.equals("dd-MM-yy") || pattern.equals("dd/MM/yy")) {

                    if (isValidDayAndMonth(date) == 1) {
                        return null;
                    }

                    String[] parts = date.split("[.\\-/]");
                    int year = Integer.parseInt(parts[2]);
                    int fullYear = (LocalDate.now().getYear() / 100) * 100 + year;
                    adjustedDate = parts[0] + "." + parts[1] + "." + fullYear;
                    pattern = pattern.replace("yy", "yyyy");
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDate parsedDate = LocalDate.parse(adjustedDate, formatter);

                if (parsedDate.getYear() < 1900 || parsedDate.getYear() > 2100) {
                    return null;
                }

                if (parsedDate.getDayOfMonth() == 29 && parsedDate.getMonthValue() == 2 && !parsedDate.isLeapYear()) {
                    return null;
                }

                return parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception ignored) {}
        }

        return null;
    }
    private static String formatTime(String time) {

        time = time.trim().replaceAll("\\s+", " ");

        // "4.30 Uhr" → "4:30"
        if (time.matches("\\d{1,2}\\.\\d{2}\\s?Uhr")) {
            time = time.replace(".", ":").replace(" Uhr", "");
        }

        time = time.replace(" Uhr ", ":").replace(" Uhr", ":00");
        if (time.matches("\\d{1,2} Uhr \\d{1,2}")) {
            time = time.replace(" Uhr ", ":");
        }

        List<String> patterns = Arrays.asList(
                "H:mm",        // 9:30, 10:00
                "HH:mm",       // 09:30, 10:00
                "H.mm 'Uhr'",  // 4.30 Uhr
                "HH.mm 'Uhr'", // 04.30 Uhr
                "H'h'mm",      // 3h00
                "HH'h'mm",     // 13h05
                "H^mm",        // 3^00
                "HH^mm",       // 13^15
                "H':00'",      // 9 Uhr → 9:00
                "HH':00'",     // 10 Uhr → 10:00
                "H:mm:ss",
                "HH:mm:ss"
        );

        for (String pattern : patterns) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(pattern);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                return outputFormatter.format(inputFormatter.parse(time));
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private static int isValidDayAndMonth (String date) {
        String[] parts = date.split("[.\\-/]");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        if (day < 1 || day > 31 || month < 1 || month > 12) {
            return 1;
        }
        return 0;
    }
}
