package at.terminplaner;

import java.util.ArrayList;
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

        Matcher matchedResult = pattern.matcher(detectedText);
        while (matchedResult.find()) {
            results.add(matchedResult.group());
        }

        return results;
    }
}
