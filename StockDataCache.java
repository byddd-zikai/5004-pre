import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;


public final class StockDataCache {
  private static final Map<String, List<Double>> CACHE = new HashMap<>();

  private StockDataCache() {
  }


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
