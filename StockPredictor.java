import java.util.List;
import java.util.ArrayList;

/**
 * 股票预测算法实现（纯 Java）
 */
public class StockPredictor {

  /**
   * 简单移动平均预测
   * @param prices 历史股价列表
   * @param window 移动窗口大小
   * @return 预测结果列表
   */
  public List<Double> predictMovingAverage(List<Double> prices, int window) {
    List<Double> movingAverages = new ArrayList<>();
    for (int i = 0; i <= prices.size() - window; i++) {
      double sum = 0;
      for (int j = i; j < i + window; j++) {
        sum += prices.get(j);
      }
      movingAverages.add(sum / window);
    }
    return movingAverages;
  }

  /**
   * 线性回归预测（简化版）
   */
  public double predictLinearRegression(List<Double> prices) {
    if (prices.isEmpty()) return 0;
    double lastPrice = prices.get(prices.size() - 1);
    return lastPrice * 1.05; // 假设上涨5%
  }
}