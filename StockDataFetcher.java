import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 从 Alpha Vantage API 获取股票数据
 */
public class StockDataFetcher {
  private static final String API_KEY = "YOUR_API_KEY"; // 替换为你的 Alpha Vantage API Key
  private static final String API_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s";

  /**
   * 获取股票历史收盘价
   * @param symbol 股票代码（如 "AAPL"）
   * @return 最近30天的收盘价列表（从最新到最旧）
   */
  public static List<Double> fetchStockPrices(String symbol) {
    List<Double> prices = new ArrayList<>();
    try {
      // 1. 构建请求 URL
      URL url = new URL(String.format(API_URL, symbol, API_KEY));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");

      // 2. 检查响应状态
      if (conn.getResponseCode() != 200) {
        System.err.println("API 请求失败: HTTP " + conn.getResponseCode());
        return getMockData(); // 返回模拟数据
      }

      // 3. 读取 JSON 响应
      String jsonResponse = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();
      JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

      // 4. 解析数据（Alpha Vantage 的 JSON 结构）
      if (!json.has("Time Series (Daily)")) {
        System.err.println("无效的 API 响应: " + json);
        return getMockData();
      }

      JsonObject timeSeries = json.getAsJsonObject("Time Series (Daily)");
      timeSeries.keySet().stream()
          .sorted((a, b) -> b.compareTo(a)) // 按日期降序（最新在前）
          .limit(30) // 仅取最近30天
          .forEach(date -> {
            double closePrice = timeSeries.get(date).getAsJsonObject().get("4. close").getAsDouble();
            prices.add(closePrice);
          });

    } catch (Exception e) {
      e.printStackTrace();
      return getMockData();
    }
    return prices;
  }

  /** 模拟数据（当 API 失败时使用） */
  private static List<Double> getMockData() {
    return List.of(150.0, 152.0, 148.0, 155.0, 153.0);
  }
}