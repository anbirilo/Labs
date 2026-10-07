package com.andrew.lab2b;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LexemeProcessor {

    private static final String DATE_TOKEN_REGEX = "\\d{2}:\\d{2}:\\d{2}";
    private static final Pattern DATE_PATTERN = Pattern.compile(DATE_TOKEN_REGEX);

    private final SimpleDateFormat dateFormat;

    private final Random random = new Random();

    public LexemeProcessor() {
        dateFormat = new SimpleDateFormat("dd:MM:yy", Locale.forLanguageTag("ru-RU"));
        dateFormat.setLenient(false);
    }

    public ProcessingResult process(String source, String delimiters) {

        if (delimiters.indexOf(':') >= 0) {
            System.out.println("WARNING: the ':' character is included in the delimiter list - " +
                    "dates in DD:MM:YY format will be split apart and not recognized.");
        }

        String[] tokens = tokenize(source, delimiters);
        String[] tokensViaSplit = splitWithRegexFallback(source, delimiters);

        int[] octalNumbers = new int[0];
        String[] dateTokens = new String[0];

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }

            char firstChar = token.charAt(0);
            boolean looksLikeNumber = Character.isDigit(firstChar) || firstChar == '-' || firstChar == '+';

            Integer octalValue = looksLikeNumber ? tryParseOctal(token) : null;

            if (octalValue != null) {
                octalNumbers = Arrays.copyOf(octalNumbers, octalNumbers.length + 1);
                octalNumbers[octalNumbers.length - 1] = octalValue;
            } else if (isValidDateToken(token)) {
                dateTokens = Arrays.copyOf(dateTokens, dateTokens.length + 1);
                dateTokens[dateTokens.length - 1] = token;
            }
        }

        Arrays.sort(octalNumbers);

        Comparator<String> dateComparator = (a, b) -> {
            try {
                Date da = dateFormat.parse(a);
                Date db = dateFormat.parse(b);
                return da.compareTo(db);
            } catch (ParseException e) {
                return 0;
            }
        };
        Arrays.sort(dateTokens, dateComparator);

        String stringAfterInsert = insertRandomAfterDates(source, tokens);

        StringBuilder workingString = new StringBuilder(stringAfterInsert);
        String removedSubstring = removeShortestDigitToLatinSubstring(workingString);

        String finalString = workingString.toString().replaceAll(" {2,}", " ").trim();

        String reversedOriginal = new StringBuilder(source).reverse().toString();

        Locale ruLocale = Locale.forLanguageTag("ru-RU");
        NumberFormat percentFormat = NumberFormat.getPercentInstance(ruLocale);
        percentFormat.setMinimumFractionDigits(1);
        double share = tokens.length == 0 ? 0.0 : (double) octalNumbers.length / countNonEmpty(tokens);
        String percentStr = percentFormat.format(share);

        long sum = 0;
        for (int v : octalNumbers) {
            sum += v;
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(ruLocale);
        String currencyStr = currencyFormat.format(sum);

        String firstDateFormatted = "(no dates found)";
        if (dateTokens.length > 0) {
            try {
                Date d = dateFormat.parse(dateTokens[0]);
                DateFormat pretty = DateFormat.getDateInstance(DateFormat.LONG, ruLocale);
                String viaFormatter = String.format(ruLocale, "%1$td %1$tB %1$tY", d);
                firstDateFormatted = pretty.format(d) + "  /  Formatter: " + viaFormatter;
            } catch (ParseException e) {
                firstDateFormatted = "(formatting error)";
            }
        }

        String lastDelimiterInfo = lastDelimiterDiagnostics(source, delimiters);

        return new ProcessingResult(
                tokens,
                octalNumbers,
                dateTokens,
                stringAfterInsert,
                finalString,
                removedSubstring,
                reversedOriginal,
                percentStr,
                currencyStr,
                firstDateFormatted,
                tokensViaSplit,
                lastDelimiterInfo
        );
    }


    private String[] tokenize(String source, String delimiters) {
        StringTokenizer st = new StringTokenizer(source, delimiters);
        List<String> list = new ArrayList<>();
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list.toArray(new String[0]);
    }

    private String[] splitWithRegexFallback(String source, String delimiters) {
        String naiveRegex = "[" + delimiters + "]+";
        try {
            Pattern.compile(naiveRegex);
            return source.split(naiveRegex);
        } catch (PatternSyntaxException ex) {
            StringBuilder safe = new StringBuilder();
            for (char c : delimiters.toCharArray()) {
                safe.append(Pattern.quote(String.valueOf(c))).append('|');
            }
            if (safe.length() > 0) {
                safe.setLength(safe.length() - 1);
            }
            String safeRegex = "(?:" + safe + ")+";
            return source.split(safeRegex);
        }
    }

    private Integer tryParseOctal(String token) {
        try {
            return Integer.parseInt(token, 8);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isValidDateToken(String token) {
        Matcher matcher = DATE_PATTERN.matcher(token);
        if (!matcher.matches()) {
            return false;
        }
        try {
            dateFormat.parse(token);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private String insertRandomAfterDates(String source, String[] tokens) {
        List<Integer> insertPositions = new ArrayList<>();
        int cursor = 0;
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            int idx = source.indexOf(token, cursor);
            if (idx < 0) {
                continue;
            }
            if (isValidDateToken(token)) {
                insertPositions.add(idx + token.length());
            }
            cursor = idx + token.length();
        }

        StringBuilder sb = new StringBuilder(source);

        if (insertPositions.isEmpty()) {
            int mid = sb.length() / 2;
            sb.insert(mid, " " + random.nextInt(1000) + " ");
            return sb.toString();
        }

        insertPositions.sort(Comparator.reverseOrder());
        for (int pos : insertPositions) {
            sb.insert(pos, " " + random.nextInt(1000) + " ");
        }
        return sb.toString();
    }

    private String removeShortestDigitToLatinSubstring(StringBuilder sb) {
        String text = sb.toString();
        int bestStart = -1, bestEnd = -1, bestLen = Integer.MAX_VALUE;

        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                continue;
            }
            for (int j = i + 1; j < text.length(); j++) {
                char c = text.charAt(j);
                if (isLatinLetter(c)) {
                    int len = j - i + 1;
                    if (len < bestLen) {
                        bestLen = len;
                        bestStart = i;
                        bestEnd = j + 1;
                    }
                    break;
                }
            }
        }

        if (bestStart == -1) {
            return "(no matching substring found)";
        }

        String removed = sb.substring(bestStart, bestEnd);
        sb.delete(bestStart, bestEnd);
        return removed;
    }

    private boolean isLatinLetter(char c) {
        char[] buf = new char[1];
        String.valueOf(c).getChars(0, 1, buf, 0);
        char ch = buf[0];
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    private String lastDelimiterDiagnostics(String source, String delimiters) {
        int bestPos = -1;
        char bestChar = '\0';
        for (int i = 0; i < delimiters.length(); i++) {
            char d = delimiters.charAt(i);
            int pos = source.lastIndexOf(d);
            if (pos > bestPos) {
                bestPos = pos;
                bestChar = d;
            }
        }
        if (bestPos == -1) {
            return "none of the delimiters from the second string appear in the first string";
        }
        return String.format("rightmost delimiter by position - character '%c' at index %d", bestChar, bestPos);
    }

    private int countNonEmpty(String[] arr) {
        int n = 0;
        for (String s : arr) {
            if (!s.isEmpty()) {
                n++;
            }
        }
        return n;
    }
}