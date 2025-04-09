import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public final class StockPredictor {

  private StockPredictor() {
  }

  public static List<Double> predictMovingAverage(List<Double> prices, int window) {
    validateInput(prices, window);

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
    if (prices == null || prices.isEmpty()) {
      throw new IllegalArgumentException("Prices list cannot be null or empty");
    }

    SimpleRegression regression = new SimpleRegression();
    for (int i = 0; i < prices.size(); i++) {
      regression.addData(i, prices.get(i));
    }

    double currentPrice = prices.get(0);
    double nextDayPrice = regression.predict(prices.size());
    double twoDaysLaterPrice = regression.predict(prices.size() + 1);

    return List.of(currentPrice, nextDayPrice, twoDaysLaterPrice);
  }


  public static List<Double> predictExponentialSmoothing(List<Double> prices, double alpha) {
    if (prices == null || prices.isEmpty()) {
      throw new IllegalArgumentException("Prices list cannot be null or empty");
    }
    if (alpha <= 0 || alpha >= 1) {
      throw new IllegalArgumentException("Alpha must be between 0 and 1");
    }

    List<Double> smoothed = new ArrayList<>();
    double lastSmoothed = prices.get(0);
    smoothed.add(lastSmoothed);

    for (int i = 1; i < prices.size(); i++) {
      lastSmoothed = alpha * prices.get(i) + (1 - alpha) * lastSmoothed;
      smoothed.add(lastSmoothed);
    }

    double nextValue = alpha * prices.get(prices.size()-1) + (1 - alpha) * lastSmoothed;
    smoothed.add(nextValue);

    return Collections.unmodifiableList(smoothed);
  }

  private static void validateInput(List<Double> prices, int window) {
    if (prices == null) {
      throw new IllegalArgumentException("Prices list cannot be null");
    }
    if (prices.isEmpty()) {
      throw new IllegalArgumentException("Prices list cannot be empty");
    }
    if (window <= 0) {
      throw new IllegalArgumentException("Window size must be positive");
    }
    if (window > prices.size()) {
      throw new IllegalArgumentException(
          "Window size cannot be larger than prices list size");
    }
  }
}
