import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;


public final class StockDataFetcher {
  private static final String YAHOO_FINANCE_JSON_API =
      "https://query1.finance.yahoo.com/v7/finance/chart/%s?period1=1695497364&period2=1727119764&interval=1d&events=history&includeAdjustedClose=true";

  private static final int MAX_DAYS = 30;

  private StockDataFetcher() {
  }


  public static List<Double> fetchStockPrices(String symbol) throws StockDataFetchException {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new IllegalArgumentException("Stock symbol cannot be null or empty");
    }

    try {
      String normalizedSymbol = symbol.trim().toUpperCase();
      String jsonData = fetchJSONFromYahooFinance(normalizedSymbol);
      return parseJSONData(jsonData);
    } catch (StockDataFetchException e) {
      throw e;
    } catch (Exception e) {
      System.err.println("Failed to fetch stock data from Yahoo Finance: " + e.getMessage());
      return getMockData();
    }
  }

  private static String fetchJSONFromYahooFinance(String symbol) throws StockDataFetchException {
    try {
      URL url = new URL(String.format(YAHOO_FINANCE_JSON_API, symbol));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // 模拟浏览器请求

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new StockDataFetchException(
            "Yahoo Finance API request failed with HTTP code: " + conn.getResponseCode());
      }

      try (Scanner scanner = new Scanner(conn.getInputStream())) {
        scanner.useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
      }
    } catch (Exception e) {
      throw new StockDataFetchException("Failed to fetch JSON data from Yahoo Finance: " + e.getMessage(), e);
    }
  }


  private static List<Double> parseJSONData(String jsonData) throws StockDataFetchException {
    try {
      JSONObject jsonObject = new JSONObject(jsonData);
      JSONObject chart = jsonObject.getJSONObject("chart");

      // 检查返回的错误信息（若存在）
      if (!chart.isNull("error")) {
        JSONObject error = chart.getJSONObject("error");
        String errorMessage = error.optString("description", "Unknown error");
        throw new StockDataFetchException("Yahoo Finance API error: " + errorMessage);
      }

      JSONArray result = chart.getJSONArray("result");
      if (result.length() == 0) {
        throw new StockDataFetchException("No result found in JSON data");
      }
      JSONObject firstResult = result.getJSONObject(0);
      JSONObject indicators = firstResult.getJSONObject("indicators");
      JSONArray quoteArray = indicators.getJSONArray("quote");

      if (quoteArray.length() == 0) {
        throw new StockDataFetchException("No quote data found in JSON data");
      }

      JSONObject quote = quoteArray.getJSONObject(0);
      JSONArray closeArray = quote.getJSONArray("close");

      List<Double> prices = new ArrayList<>();
      for (int i = 0; i < closeArray.length(); i++) {
        if (!closeArray.isNull(i)) {
          prices.add(closeArray.getDouble(i));
        }
      }

      if (prices.isEmpty()) {
        throw new StockDataFetchException("No valid closing prices found in JSON data");
      }

      int startIndex = Math.max(0, prices.size() - MAX_DAYS);
      List<Double> recentPrices = new ArrayList<>(prices.subList(startIndex, prices.size()));
      Collections.reverse(recentPrices);
      return List.copyOf(recentPrices);
    } catch (Exception e) {
      throw new StockDataFetchException("Failed to parse JSON data: " + e.getMessage(), e);
    }
  }

  private static List<Double> getMockData() {
    return List.of(150.0, 152.0, 148.0, 155.0, 153.0);
  }
}

