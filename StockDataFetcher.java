import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.json.JSONArray;
import org.json.JSONObject;

public final class StockDataFetcher {
  private static final String API_TEMPLATE =
      "https://query1.finance.yahoo.com/v7/finance/chart/%s"
          + "?period1=%d&period2=%d&interval=1d&events=history&includeAdjustedClose=true";
  private static final int MAX_DAYS = 30;

  private StockDataFetcher() { }

  public static List<PriceEntry> fetchPriceEntries(String symbol)
      throws StockDataFetchException {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Stock symbol cannot be null or empty");
    }
    try {
      String sym   = symbol.trim().toUpperCase();
      long toSec    = Instant.now().getEpochSecond();
      long fromSec  = Instant.now()
          .minus(MAX_DAYS, ChronoUnit.DAYS)
          .getEpochSecond();
      String url    = String.format(API_TEMPLATE, sym, fromSec, toSec);

      String json   = fetchJson(url);
      return parseJsonToEntries(json);
    } catch (StockDataFetchException e) {
      throw e;
    } catch (Exception e) {
      System.err.println("Fetch failure: " + e.getMessage());
      return getMockEntries();
    }
  }

  private static String fetchJson(String urlStr) throws StockDataFetchException {
    try {
      HttpURLConnection conn = (HttpURLConnection)new URL(urlStr).openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.setRequestProperty("User-Agent", "Mozilla/5.0");
      if (conn.getResponseCode() != 200) {
        throw new StockDataFetchException("HTTP error code: " + conn.getResponseCode());
      }
      try (Scanner sc = new Scanner(conn.getInputStream())) {
        sc.useDelimiter("\\A");
        return sc.hasNext() ? sc.next() : "";
      }
    } catch (Exception e) {
      throw new StockDataFetchException("Failed to fetch JSON: " + e.getMessage(), e);
    }
  }

  private static List<PriceEntry> parseJsonToEntries(String jsonData)
      throws StockDataFetchException {
    try {
      JSONObject root    = new JSONObject(jsonData);
      JSONObject chart   = root.getJSONObject("chart");
      if (!chart.isNull("error")) {
        String desc = chart.getJSONObject("error")
            .optString("description", "Unknown error");
        throw new StockDataFetchException("API error: " + desc);
      }
      JSONObject res0    = chart.getJSONArray("result").getJSONObject(0);


      JSONArray tsArr    = res0.getJSONArray("timestamp");
      List<LocalDate> dates = new ArrayList<>(tsArr.length());
      for (int i = 0; i < tsArr.length(); i++) {
        long ts = tsArr.getLong(i);
        dates.add(Instant.ofEpochSecond(ts)
            .atZone(ZoneId.systemDefault())
            .toLocalDate());
      }

   
      JSONArray priceArr = null;
      JSONArray adjArr   = res0
          .getJSONObject("indicators")
          .optJSONArray("adjclose");
      if (adjArr != null && adjArr.length() > 0) {
        priceArr = adjArr.getJSONObject(0).getJSONArray("adjclose");
      } else {
        priceArr = res0
            .getJSONObject("indicators")
            .getJSONArray("quote")
            .getJSONObject(0)
            .getJSONArray("close");
      }


      List<PriceEntry> all = new ArrayList<>();
      for (int i = 0; i < priceArr.length(); i++) {
        if (!priceArr.isNull(i)) {
          all.add(new PriceEntry(dates.get(i), priceArr.getDouble(i)));
        }
      }
      if (all.isEmpty()) {
        throw new StockDataFetchException("No valid prices found");
      }

      int start = Math.max(0, all.size() - MAX_DAYS);
      return List.copyOf(all.subList(start, all.size()));
    } catch (StockDataFetchException e) {
      throw e;
    } catch (Exception e) {
      throw new StockDataFetchException("Parse error: " + e.getMessage(), e);
    }
  }

  private static List<PriceEntry> getMockEntries() {
    List<PriceEntry> mock = new ArrayList<>();
    mock.add(new PriceEntry(LocalDate.now().minusDays(2), 150.0));
    mock.add(new PriceEntry(LocalDate.now().minusDays(1), 152.0));
    mock.add(new PriceEntry(LocalDate.now(), 148.0));
    return mock;
  }
}


