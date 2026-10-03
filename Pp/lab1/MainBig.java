import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Formatter;
import java.util.Locale;

public class MainBig {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Введите x из диапазона [-1; 1): ");
        BigDecimal x = new BigDecimal(reader.readLine().trim());

        System.out.print("Введите k (натуральное число, точность = 10^-k): ");
        int k = Integer.parseInt(reader.readLine().trim());

        if (x.compareTo(BigDecimal.ONE.negate()) < 0 || x.compareTo(BigDecimal.ONE) >= 0) {
            System.out.println("Ошибка: x должен принадлежать [-1; 1), иначе ряд расходится.");
            return;
        }
        if (k <= 0) {
            System.out.println("Ошибка: k должно быть натуральным числом.");
            return;
        }

        RowCalculatorBig.Result result = RowCalculatorBig.calculateSum(x, k);

        double etalon = Math.log(1 - x.doubleValue());
        BigDecimal diff = result.sum.subtract(BigDecimal.valueOf(etalon)).abs();

        int width = 22;
        int precision = k + 1;

        Formatter fmt = new Formatter(System.out, Locale.US);

        fmt.format("%n--- Результаты вычисления ряда ln(1-x) (BigDecimal) ---%n");
        fmt.format("Число слагаемых:   %d%n", result.count);
        fmt.format("Сумма ряда:        %" + width + "." + precision + "f%n", result.sum);
        fmt.format("Эталон Math.log:   %" + width + "." + precision + "f%n", etalon);
        fmt.format("Разница:           %" + width + "." + precision + "f%n", diff);

        fmt.format("%n--- Демонстрация флагов форматирования (0 , + - # ( ) ---%n");
        fmt.format("Флаг '+' :     %+." + precision + "f%n", diff);
        fmt.format("Флаг '-' :      [%-" + width + "." + precision + "f]%n", result.sum);
        fmt.format("Флаг '0' :       %0" + width + "." + precision + "f%n",
                BigDecimal.valueOf(etalon));
        fmt.format("Флаг '(' : %(." + precision + "f%n", result.sum);
        fmt.format("Флаг ',' :        %,." + precision + "f%n",
                result.sum.multiply(BigDecimal.valueOf(1_000_000)));

        long sumRounded = Math.abs(Math.round(result.sum.doubleValue() * 1000));
        long xRounded = Math.abs(Math.round(x.doubleValue() * 1000));
        fmt.format("%n|сумма*1000| (округл.) в 8-ричном виде: %#o, в 16-ричном виде: %#X%n",
                sumRounded, sumRounded);
        fmt.format("|x*1000| (округл.) в 8-ричном виде:     %#o, в 16-ричном виде: %#X%n",
                xRounded, xRounded);

        fmt.flush();

        System.out.print(String.format(Locale.US, "%nРазница, выведенная через print(): %"
                + width + "." + precision + "f%n", diff));
    }
}