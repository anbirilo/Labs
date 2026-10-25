import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void printWithNumbers(List<String> array) {
        int i = 1;
        for (String line : array) {
            System.out.printf("%d: %s\n", i, line);
            i++;
        }
        System.out.printf("Количество: %d\n", array.size());
    }
    public static void longestString(List<String> array) {
        String longest = array.get(0);

        for(String line : array) {
            if (line.length() > longest.length()) {
                longest = line;
            }
        }
        System.out.printf("Самое длинное слово: %s\n", longest);
    }

    public static void main(String[] args) {
        String line;
        List<String> array = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            while((line = reader.readLine()) != null) {
                array.add(line);
            }
            printWithNumbers(array);
            longestString(array);
            
        } catch(IOException err) {
            err.printStackTrace();
        }
    }
}