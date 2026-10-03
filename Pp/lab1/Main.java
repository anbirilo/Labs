import java.util.Formatter;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in).useLocale(Locale.US);

        System.out.print("Введите x из диапазона [-1; 1): ");
        double x = scanner.nextDouble();

        System.out.print("Введите k (натуральное число, точность = 10^-k): ");
        int k = scanner.nextInt();

        if (x < -1.0 || x >= 1.0) {
            System.out.println("Ошибка: x должен принадлежать [-1; 1), иначе ряд расходится.");
            return;
        }
        if (k <= 0) {
            System.out.println("Ошибка: k должно быть натуральным числом.");
            return;
        }

        RowCalculator.Result result = RowCalculator.calculateSum(x, k);

        double etalon = Math.log(1 - x);
        double diff = Math.abs(result.sum - etalon);

        int width = 18;
        int precision = k + 1;

        Formatter fmt = new Formatter(System.out, Locale.US);

        fmt.format("%n--- Результаты вычисления ряда ln(1-x) ---%n");
        fmt.format("Число слагаемых:   %d%n", result.count);
        fmt.format("Сумма ряда:        %" + width + "." + precision + "f%n", result.sum);
        fmt.format("Эталон Math.log:   %" + width + "." + precision + "f%n", etalon);
        fmt.format("Разница:           %" + width + "." + precision + "f%n", diff);

        fmt.format("%n--- Демонстрация флагов форматирования (0 , + - # ( ) ---%n");
        fmt.format("Флаг '+' :     %+." + precision + "f%n", diff);
        fmt.format("Флаг '-' :      [%-" + width + "." + precision + "f]%n", result.sum);
        fmt.format("Флаг '0' :       %0" + width + "." + precision + "f%n", etalon);
        fmt.format("Флаг '(' : %(." + precision + "f%n", result.sum);
        fmt.format("Флаг ',' :        %,." + precision + "f%n", result.sum * 1_000_000);

        long sumRounded = Math.abs(Math.round(result.sum * 1000));
        long xRounded = Math.abs(Math.round(x * 1000));
        fmt.format("%n|сумма*1000| (округл.) в 8-ричном виде: %#o, в 16-ричном виде: %#X%n",
                sumRounded, sumRounded);
        fmt.format("|x*1000| (округл.) в 8-ричном виде:     %#o, в 16-ричном виде: %#X%n",
                xRounded, xRounded);

        fmt.flush();

        System.out.print(String.format(Locale.US, "%nРазница, выведенная через print(): %"
                + width + "." + precision + "f%n", diff));
    }
}