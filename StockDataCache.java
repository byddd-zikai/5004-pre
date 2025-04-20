import java.util.List;
import java.util.stream.Collectors;

public final class StockDataCache {
  private StockDataCache() {}

  public static List<Double> getData(String symbol, boolean useCache)
      throws StockDataFetchException {
    List<PriceEntry> entries = StockDataFetcher.fetchPriceEntries(symbol);
    return entries.stream()
        .map(PriceEntry::getPrice)
        .collect(Collectors.toList());
  }
}
