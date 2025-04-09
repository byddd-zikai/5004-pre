import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Provides stock price prediction algorithms.
 * This class implements various prediction methods as pure functions,
 * ensuring thread safety and immutability of inputs and outputs.
 */
public final class StockPredictor {

  private StockPredictor() {
    // Private constructor to prevent instantiation
  }

  /**
   * Calculates simple moving average for the given price series.
   *
   * @param prices Historical stock prices, ordered from most recent to oldest
   * @param window Size of the moving window (must be positive and <= prices size)
   * @return Unmodifiable list of moving average values
   * @throws IllegalArgumentException if prices is null, empty, or window is invalid
   */
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

  /**
   * Predicts future price using simplified linear regression.
   *
   * @param prices Historical stock prices, ordered from most recent to oldest
   * @return Predicted future price
   * @throws IllegalArgumentException if prices is null or empty
   */
  public static double predictLinearRegression(List<Double> prices) {
    if (prices == null || prices.isEmpty()) {
      throw new IllegalArgumentException("Prices list cannot be null or empty");
    }

    double lastPrice = prices.get(0); // Most recent price is first in list
    return lastPrice * 1.05; // Simplified prediction: 5% increase
  }

  private static void validateInput(List<Double> prices, int window) {
    if (prices == null) {
      throw new IllegalArgumentException
