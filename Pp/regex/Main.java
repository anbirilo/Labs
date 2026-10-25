import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String line = "Температура: -5, потом 12, потом -30 и 7";
        String template = "-?\\d+";
        Pattern pattern = Pattern.compile(template);
        Matcher m = pattern.matcher(line);
        int sum = 0;
        while(m.find()) {
            int number = Integer.parseInt(m.group());
            System.out.printf("%d\n", number);
            sum += number;
        }
        System.out.printf("sum: %d\n", sum);

    }
}