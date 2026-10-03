import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;


public class RowCalculatorBig {

    public static class Result {
        public BigDecimal sum;
        public int count;
    }

    public static Result calculateSum(BigDecimal x, int k) {
        MathContext mc = new MathContext(k + 20, RoundingMode.HALF_UP);

        BigInteger tenPowK = BigInteger.TEN.pow(k);
        BigDecimal eps = BigDecimal.ONE.divide(new BigDecimal(tenPowK), mc);

        BigDecimal term = x.negate();
        BigDecimal sum = BigDecimal.ZERO;
        BigInteger n = BigInteger.ONE;
        int count = 0;

        while (term.abs().compareTo(eps) >= 0) {
            sum = sum.add(term, mc);
            count++;

            n = n.add(BigInteger.ONE);
            BigInteger nMinusOne = n.subtract(BigInteger.ONE);

            term = term.multiply(x, mc)
                       .multiply(new BigDecimal(nMinusOne))
                       .divide(new BigDecimal(n), mc);
        }

        Result result = new Result();
        result.sum = sum;
        result.count = count;
        return result;
    }
}