public class RowCalculator {

    public static class Result {
        public double sum;
        public int count;
    }

    public static Result calculateSum(double x, int k) {
        double eps = Math.pow(10, -k);

        double term = -x;
        double sum = 0.0;
        int n = 1;
        int count = 0;

        while (Math.abs(term) >= eps) {
            sum += term;
            count++;
            n++;
            term = term * x * (n - 1) / n;
        }

        Result result = new Result();
        result.sum = sum;
        result.count = count;
        return result;
    }
}