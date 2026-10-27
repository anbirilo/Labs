import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static List<String> readFromFile(String fileName) throws IOException {
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

    public static void writeToFile(List<String> array, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for(String line : array) {
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rewriteCorrect(List<String> rawData, List<String> correctData, List<String> errors) {
        for (String line : rawData) {
            if (isValid(line)) {
                correctData.add(line);
            } else {
                errors.add(line);
            }
        }
    }

    public static boolean isValid(String line) {
        return line.matches("[A-Z][a-z]+;\\d{1,3};\\d+");
    }

    public static void fillStruct(List<String> array, List<Employee> empl) {
        for(String line : array) {
            String[] element = line.split(";");
            Employee e = new Employee();
            e.name = element[0];
            e.age = Integer.parseInt(element[1]);
            e.salary = new BigInteger(element[2]);
            empl.add(e);
        }
    }

    public static class Employee {
        public String name;
        public int age;
        public BigInteger salary;
    }

    public static List<String> convert(List<Employee> empl) {
        List<String> a = new ArrayList<>();
        for (Employee e : empl) {
            String line = e.name + "; " + e.age + "; " + e.salary;
            a.add(line);
        }
        return a;
    }

    public static void main(String args[]) {
        try {
            String fileName = "employees.txt";
            
            List<String> rawArray = readFromFile(fileName);
            
            List<String> correctArray = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            rewriteCorrect(rawArray, correctArray, errors);
            
            List<Employee> empl = new ArrayList<>();
            fillStruct(correctArray, empl);
            
            empl.sort((e1, e2) -> {
                int salaryComp = e2.salary.compareTo(e1.salary); 
                if (salaryComp != 0) {
                    return salaryComp; 
                }
                int ageComp = Integer.compare(e1.age, e2.age);
                if (ageComp != 0) {
                    return ageComp;
                }
                return e1.name.compareTo(e2.name);
            });
            
            List<String> fa = convert(empl);
            
            writeToFile(fa, "output.txt");
            writeToFile(errors, "errors.txt");
            
            StringBuilder text = new StringBuilder("SUccess!");
            System.out.printf("Text: %s\n", text.toString());
            System.out.printf("Reversed: %s\n", text.reverse().toString());
            text.setCharAt(1, 'u');
            System.out.printf("Chacnged: %s\n", text.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
