import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

/**
 * 股票预测系统主界面（JavaFX）
 */
public class StockPredictionApp extends Application {
  private LineChart<Number, Number> chart;
  private TextField stockSymbolInput;
  private Button predictBtn;
  private ComboBox<String> methodComboBox;

  @Override
  public void start(Stage stage) {
    // 1. 初始化控件
    stockSymbolInput = new TextField("AAPL");
    predictBtn = new Button("预测");
    methodComboBox = new ComboBox<>();
    methodComboBox.getItems().addAll("移动平均", "线性回归");
    methodComboBox.setValue("移动平均");

    NumberAxis xAxis = new NumberAxis("天数", 0, 30, 1);
    NumberAxis yAxis = new NumberAxis("价格", 0, 500, 10);
    chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle("股票价格预测");

    // 2. 预测按钮事件
    predictBtn.setOnAction(e -> {
      String symbol = stockSymbolInput.getText().trim().toUpperCase();
      if (symbol.isEmpty()) {
        showAlert("错误", "请输入股票代码（如 AAPL）");
        return;
      }

      predictBtn.setText("预测中...");
      predictBtn.setDisable(true);

      // 异步调用 API 和预测（避免阻塞 UI）
      new Thread(() -> {
        List<Double> prices = StockDataCache.getData(symbol, false);
        List<Double> predicted = null;

        // 根据选择的方法预测
        String method = methodComboBox.getValue();
        StockPredictor predictor = new StockPredictor();
        if ("移动平均".equals(method)) {
          predicted = predictor.predictMovingAverage(prices, 5);
        } else {
          double futurePrice = predictor.predictLinearRegression(prices);
          predicted = List.of(prices.get(prices.size() - 1), futurePrice);
        }

        // 更新 UI（必须在 JavaFX 主线程执行）
        Platform.runLater(() -> {
          updateChart(prices, predicted);
          predictBtn.setText("预测");
          predictBtn.setDisable(false);
        });
      }).start();
    });

    // 3. 布局
    VBox root = new VBox(10,
        new Label("股票代码（如 AAPL/MSFT）："),
        stockSymbolInput,
        new Label("预测方法："),
        methodComboBox,
        predictBtn,
        chart
    );
    root.setPadding(new javafx.geometry.Insets(10));

    // 4. 显示窗口
    stage.setScene(new Scene(root, 800, 600));
    stage.setTitle("Java 股票预测系统");
    stage.show();
  }

  /** 更新图表 */
  private void updateChart(List<Double> prices, List<Double> predicted) {
    chart.getData().clear();

    // 历史数据系列
    XYChart.Series<Number, Number> historySeries = new XYChart.Series<>();
    historySeries.setName("历史数据");
    for (int i = 0; i < prices.size(); i++) {
      historySeries.getData().add(new XYChart.Data<>(i, prices.get(i)));
    }

    // 预测数据系列
    XYChart.Series<Number, Number> predictedSeries = new XYChart.Series<>();
    predictedSeries.setName("预测数据");
    int startIndex = prices.size() - predicted.size();
    for (int i = 0; i < predicted.size(); i++) {
      predictedSeries.getData().add(new XYChart.Data<>(startIndex + i, predicted.get(i)));
    }

    chart.getData().addAll(historySeries, predictedSeries);
  }

  /** 显示错误弹窗 */
  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}