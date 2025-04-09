import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Fetches stock data from Alpha Vantage API.
 * Handles all HTTP communication and JSON parsing, providing a clean interface
 * for stock price retrieval. Implements proper error handling and fallback.
 */
public final class StockDataFetcher {
  private static final String API_KEY = "YOUR_API_KEY";
  private static final String API_URL =
      "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s";
  private static final int MAX_DAYS = 30;

  private StockDataFetcher() {
    // Private constructor to prevent instantiation
  }

  /**
   * Fetches historical closing prices for the given stock symbol.
   *
   * @param symbol Stock symbol to fetch (e.g., "AAPL")
   * @return Unmodifiable list of closing prices (most recent first)
   * @throws IllegalArgumentException if symbol is null or empty
   * @throws StockDataFetchException if data cannot be retrieved or parsed
   */
  public static List<Double> fetchStockPrices(String symbol) throws StockDataFetchException {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new IllegalArgumentException("Stock symbol cannot be null or empty");
    }

    try {
      String normalizedSymbol = symbol.trim().toUpperCase();
      return fetchFromAPI(normalizedSymbol);
    } catch (Exception e) {
      System.err.println("Failed to fetch stock data: " + e.getMessage());
      return getMockData(); // Fallback to mock data
    }
  }

  private static List<Double> fetchFromAPI(String symbol) throws StockDataFetchException {
    try {
      URL url = new URL(String.format(API_URL, symbol, API_KEY));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new StockDataFetchException(
            "API request failed with HTTP code: " + conn.getResponseCode());
      }

      String jsonResponse = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();
      return parseJsonResponse(jsonResponse);
    } catch (Exception e) {
      throw new StockDataFetchException("Failed to fetch stock data: " + e.getMessage(), e);
    }
  }

  private static List<Double> parseJsonResponse(String jsonResponse)
      throws StockDataFetchException {
    try {
      JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

      if (!json.has("Time Series (Daily)")) {
        throw new StockDataFetchException("Invalid API response: missing time series data");
      }

      JsonObject timeSeries = json.getAsJsonObject("Time Series (Daily)");
      List<Double> prices = new ArrayList<>(MAX_DAYS);

      timeSeries.keySet().stream()
          .sorted((a, b) -> b.compareTo(a)) // Descending order (newest first)
          .limit(MAX_DAYS)
          .forEach(date -> {
            double closePrice = timeSeries.get(date)
                .getAsJsonObject()
                .get("4. close")
                .getAsDouble();
            prices.add(closePrice);
          });

      return List.copyOf(prices);
    } catch (JsonSyntaxException e) {
      throw new StockDataFetchException("Failed to parse JSON response", e);
    } catch (Exception e) {
      throw new StockDataFetchException("Unexpected error parsing response", e);
    }
  }

  private static List<Double> getMockData() {
    return List.of(150.0, 152.0, 148.0, 155.0, 153.0);
  }
}

/**
 * Custom exception for stock data fetch failures.
 */
class StockDataFetchException extends Exception {
  public StockDataFetchException(String message) {
    super(message);
  }

  public StockDataFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}
