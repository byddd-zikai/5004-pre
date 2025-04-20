import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public final class StockPredictor {

  private StockPredictor() {}

  public static List<Double> predictMovingAverage(List<Double> prices, int window) {
    if (prices == null || prices.isEmpty() || window <= 0 || window > prices.size()) {
      throw new IllegalArgumentException("Invalid prices or window size");
    }
    List<Double> movingAverages = new ArrayList<>();
    for (int i = 0; i <= prices.size() - window; i++) {
      double sum = 0;
      for (int j = i; j < i + window; j++) {
        sum += prices.get(j);
      }
      movingAverages.add(sum / window);
    }
    return Collections.unmodifiableList(movingAverages);
  }

  public static List<Double> predictLinearRegression(List<Double> prices) {
    if (prices == null || prices.size() < 5) {
      throw new IllegalArgumentException("Need at least 5 data points for regression");
    }

    int window = 5;
    List<Double> windowPrices = prices.subList(prices.size() - window, prices.size());

    SimpleRegression regression = new SimpleRegression(true);
    for (int i = 0; i < window; i++) {
      regression.addData(i, windowPrices.get(i));
    }

    double currentPrice = windowPrices.get(window - 1);
    double nextDayPrice = regression.predict(window);

    return List.of(currentPrice, nextDayPrice);
  }
}

