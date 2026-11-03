import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            
            List<String> array = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                // Разделяем строку по одному или нескольким пробелам
                String[] tokens = line.trim().split("\\s+");
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        array.add(token);
                    }
                }
            }

            // 2. Создать второй List и записать объекты BigDecimal, вычислить произведение
            List<BigDecimal> arrayBig = new ArrayList<>();
            BigDecimal mul = BigDecimal.ONE; // Начальное значение для произведения = 1
            
            for (String numStr : array) {
                BigDecimal number = new BigDecimal(numStr);
                arrayBig.add(number);
                mul = mul.multiply(number);
            }

            // 3. Найти числа, начинающиеся на 5 и состоящие только из цифр 3-7
            List<String> matchedNumbers = new ArrayList<>();
            // Регулярное выражение: ^5[3-7]*$ (начинается с 5, далее только цифры от 3 до 7)
            String regex = "^5[3-7]*$";
            for (String numStr : array) {
                if (numStr.matches(regex)) {
                    matchedNumbers.add(numStr);
                }
            }

            String res1 = "1. Строковый список: " + array;
            String res2 = "2. Список BigDecimal: " + arrayBig;
            String res3 = "   Произведение элементов: " + mul;
            String res4 = "3. Числа по регулярному выражению (нач. на 5, цифры 3-7): " + matchedNumbers;

            System.out.println(res1);
            System.out.println(res2);
            System.out.println(res3);
            System.out.println(res4);

            writer.write(res1 + "\n");
            writer.write(res2 + "\n");
            writer.write(res3 + "\n");
            writer.write(res4 + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
