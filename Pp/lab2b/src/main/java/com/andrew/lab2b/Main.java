package com.andrew.lab2b;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
//17 07:11:25 abc123x 42 hello
    private static final String OUTPUT_FILE_NAME = "report.txt";

    public static void main(String[] args) {

        try (BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Enter the first string (lexemes, base-8 numbers, possibly dates DD:MM:YY):");
            String line1 = consoleIn.readLine();

            System.out.println("Enter the second string (delimiter characters, no duplicates, no ':'):");
            String line2 = consoleIn.readLine();
            if (line1 == null || line2 == null) {
                System.err.println("Error: could not read both lines (input interrupted).");
                return;
            }

            LexemeProcessor processor = new LexemeProcessor();
            ProcessingResult result = processor.process(line1, line2);

            String report = result.buildReport(line1);

            System.out.println();
            System.out.println("RESULT:");
            System.out.println(report);

            writeToFile(OUTPUT_FILE_NAME, report);

            System.out.println("Reading file \"" + OUTPUT_FILE_NAME + "\" back");
            printFile(OUTPUT_FILE_NAME);

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    private static void writeToFile(String fileName, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }
        System.out.println("Results saved to file \"" + fileName + "\"");
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