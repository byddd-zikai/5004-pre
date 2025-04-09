import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存股票数据，减少 API 调用次数
 */
public class StockDataCache {
  private static final Map<String, List<Double>> cache = new HashMap<>();

  public static List<Double> getData(String symbol, boolean forceRefresh) {
    if (!forceRefresh && cache.containsKey(symbol)) {
      System.out.println("使用缓存数据: " + symbol);
      return cache.get(symbol);
    }
    List<Double> prices = StockDataFetcher.fetchStockPrices(symbol);
    cache.put(symbol, prices);
    return prices;
  }
}