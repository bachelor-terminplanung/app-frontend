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
            pattern = Pattern.compile("\\b\\d{1,2}[.\\-/]\\d{1,2}([.\\-/](\\d{2}|\\d{4}))?\\b(?!\\s?(Uhr|h|H|:|\\^))");
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
