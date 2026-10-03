public class ProcessingResult {

    private final String[] allTokens;

    private final int[] octalNumbers;

    private final String[] sortedDates;

    private final String stringAfterInsert;

    private final String finalString;

    private final String removedSubstring;

    private final String reversedOriginal;

    private final String percentOfOctalTokens;

    private final String sumAsCurrency;

    private final String firstDateFormatted;

    private final String[] tokensViaRegexSplit;

    private final String lastDelimiterInfo;

    public ProcessingResult(String[] allTokens,
                             int[] octalNumbers,
                             String[] sortedDates,
                             String stringAfterInsert,
                             String finalString,
                             String removedSubstring,
                             String reversedOriginal,
                             String percentOfOctalTokens,
                             String sumAsCurrency,
                             String firstDateFormatted,
                             String[] tokensViaRegexSplit,
                             String lastDelimiterInfo) {
        this.allTokens = allTokens;
        this.octalNumbers = octalNumbers;
        this.sortedDates = sortedDates;
        this.stringAfterInsert = stringAfterInsert;
        this.finalString = finalString;
        this.removedSubstring = removedSubstring;
        this.reversedOriginal = reversedOriginal;
        this.percentOfOctalTokens = percentOfOctalTokens;
        this.sumAsCurrency = sumAsCurrency;
        this.firstDateFormatted = firstDateFormatted;
        this.tokensViaRegexSplit = tokensViaRegexSplit;
        this.lastDelimiterInfo = lastDelimiterInfo;
    }

    public String[] getAllTokens() { return allTokens; }
    public int[] getOctalNumbers() { return octalNumbers; }
    public String[] getSortedDates() { return sortedDates; }
    public String getStringAfterInsert() { return stringAfterInsert; }
    public String getFinalString() { return finalString; }
    public String getRemovedSubstring() { return removedSubstring; }
    public String getReversedOriginal() { return reversedOriginal; }
    public String getPercentOfOctalTokens() { return percentOfOctalTokens; }
    public String getSumAsCurrency() { return sumAsCurrency; }
    public String getFirstDateFormatted() { return firstDateFormatted; }
    public String[] getTokensViaRegexSplit() { return tokensViaRegexSplit; }
    public String getLastDelimiterInfo() { return lastDelimiterInfo; }

    public String buildReport(String originalLine1) {
        StringBuilder report = new StringBuilder();

        report.append(String.format("Исходная строка:          \"%s\"%n", originalLine1));
        report.append(String.format("Лексемы (StringTokenizer): %s%n", java.util.Arrays.toString(allTokens)));
        report.append(String.format("Лексемы (String.split):    %s%n", java.util.Arrays.toString(tokensViaRegexSplit)));
        report.append(String.format("Диагностика (String.lastIndexOf): %s%n", lastDelimiterInfo));
        report.append(String.format("Числа 8-й с/с (10-я с/с):  %s%n", java.util.Arrays.toString(octalNumbers)));
        report.append(String.format("Найденные даты (ДД:ММ:ГГ), отсортированы: %s%n", java.util.Arrays.toString(sortedDates)));
        report.append(String.format("Первая дата в формате DateFormat/Formatter: %s%n", firstDateFormatted));
        report.append(String.format("Строка после вставки случайного числа: \"%s\"%n", stringAfterInsert));
        report.append(String.format("Удаленная подстрока (цифра...лат.буква): \"%s\"%n", removedSubstring));
        report.append(String.format("Итоговая строка:           \"%s\"%n", finalString));
        report.append(String.format("Исходная строка задом наперед (StringBuilder.reverse): \"%s\"%n", reversedOriginal));
        report.append(String.format("Доля чисел 8-й с/с среди лексем (NumberFormat, percent): %s%n", percentOfOctalTokens));
        report.append(String.format("Сумма чисел 8-й с/с как денежная сумма (NumberFormat, currency): %s%n", sumAsCurrency));

        return report.toString();
    }
}