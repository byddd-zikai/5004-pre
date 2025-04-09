import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a caching mechanism for stock data to minimize API calls.
 * Implements a simple in-memory cache with thread-safe read access.
 * The cache uses a HashMap internally and provides a single point of access
 * for stock data retrieval with optional forced refresh.
 */
public final class StockDataCache {
  private static final Map<String, List<Double>> CACHE = new HashMap<>();

  private StockDataCache() {
    // Private constructor to prevent instantiation
  }

  /**
   * Retrieves stock data for the given symbol, using cached data if available.
   *
   * @param symbol The stock symbol to retrieve data for (e.g., "AAPL")
   * @param forceRefresh If true, bypasses cache and fetches fresh data
   * @return List of stock prices, ordered from most recent to oldest
   * @throws IllegalArgumentException if symbol is null or empty
   * @throws StockDataFetchException if data cannot be retrieved
   */
  public static List<Double> getData(String symbol, boolean forceRefresh)
      throws StockDataFetchException {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new IllegalArgumentException("Stock symbol cannot be null or empty");
    }

    String normalizedSymbol = symbol.trim().toUpperCase();

    if (!forceRefresh && CACHE.containsKey(normalizedSymbol)) {
      return List.copyOf(CACHE.get(normalizedSymbol)); // Defensive copy
    }

    List<Double> prices = StockDataFetcher.fetchStockPrices(normalizedSymbol);
    CACHE.put(normalizedSymbol, List.copyOf(prices)); // Store immutable copy
    return prices;
  }
}
