import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    private static final String OUTPUT_FILE_NAME = "report.txt";

    public static void main(String[] args) {

        try (BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Введите первую строку (лексемы, числа 8-й с/с, возможно даты ДД:ММ:ГГ):");
            String line1 = consoleIn.readLine();

            System.out.println("Введите вторую строку (символы-разделители, без повторов, без ':'):");
            String line2 = consoleIn.readLine();
            if (line1 == null || line2 == null) {
                System.err.println("Ошибка: не удалось считать обе строки (ввод прерван).");
                return;
            }

            LexemeProcessor processor = new LexemeProcessor();
            ProcessingResult result = processor.process(line1, line2);

            String report = result.buildReport(line1);

            System.out.println();
            System.out.println("===== РЕЗУЛЬТАТ =====");
            System.out.println(report);

            writeToFile(OUTPUT_FILE_NAME, report);

            System.out.println("Чтение файла \"" + OUTPUT_FILE_NAME + "\" обратно");
            printFile(OUTPUT_FILE_NAME);

        } catch (IOException e) {
            System.err.println("Ошибка ввода/вывода: " + e.getMessage());
        }
    }

    private static void writeToFile(String fileName, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }
        System.out.println("Результаты сохранены в файл \"" + fileName + "\"");
    }

    private static void printFile(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}