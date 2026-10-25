import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
public class Main {
    public static List<String> readFromFile(String fileName) {
        List<String> array = new ArrayList<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            while((line = reader.readLine()) != null) {
                array.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }
    public static void writeToFile(List<String> array) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            for (String line : array) {
                writer.write(line);
                writer.newLine();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    public static List<String> correctUsers(List<String> array) {
        List<String> correctUsers = new ArrayList<>();
        for (String line : array) {
            String[] parts = line.split(" ");
            if (loginMatches(parts[0]) && numberMatches(parts[1])) {
                correctUsers.add(line);
            }
        }
        return correctUsers;
    }
    public static boolean loginMatches(String line) {
        return line.matches("[A-Za-z]\\w{2,15}");
    }
    public static boolean numberMatches(String line) {
        return line.matches("\\+375\\d{9}");
    }
    public static void main(String[] args) {
        List<String> array = new ArrayList<>();
        array = readFromFile("users.txt");
        List<String> correctUsers = new ArrayList<>();
        correctUsers = correctUsers(array);
        writeToFile(correctUsers);
        
    }
}