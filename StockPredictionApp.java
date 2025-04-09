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
 * JavaFX application for stock price prediction visualization.
 * Implements the view layer of the application, separating UI concerns
 * from business logic and data access.
 */
public final class StockPredictionApp extends Application {
  private LineChart<Number, Number> chart;
  private TextField stockSymbolInput;
  private Button predictBtn;
  private ComboBox<String> methodComboBox;
  private ProgressIndicator progressIndicator;

  @Override
  public void start(Stage stage) {
    initializeUIComponents();
    setupLayout(stage);
  }

  private void initializeUIComponents() {
    stockSymbolInput = new TextField("AAPL");
    predictBtn = new Button("Predict");
    methodComboBox = new ComboBox<>();
    progressIndicator = new ProgressIndicator();
    progressIndicator.setVisible(false);

    methodComboBox.getItems().addAll("Moving Average", "Linear Regression");
    methodComboBox.setValue("Moving Average");

    NumberAxis xAxis = new NumberAxis("Days", 0, 30, 1);
    NumberAxis yAxis = new NumberAxis("Price", 0, 500, 10);
    chart = new LineChart<>(xAxis, yAxis);
    chart.setTitle("Stock Price Prediction");
    chart.setAnimated(false); // Better performance

    setupButtonAction();
  }

  private void setupButtonAction() {
    predictBtn.setOnAction(e -> {
      String symbol = stockSymbolInput.getText().trim();
      if (symbol.isEmpty()) {
        showAlert("Error", "Please enter a stock symbol (e.g., AAPL)");
        return;
      }

      setUILoadingState(true);

      new Thread(() -> {
        try {
          List<Double> prices = StockDataCache.getData(symbol, false);
          List<Double> predicted = getPrediction(methodComboBox.getValue(), prices);

          Platform.runLater(() -> {
            updateChart(prices, predicted);
            setUILoadingState(false);
          });
        } catch (Exception ex) {
          Platform.runLater(() -> {
            showAlert("Prediction Error",
                "Failed to get prediction: " + ex.getMessage());
            setUILoadingState(false);
          });
        }
      }).start();
    });
  }

  private List<Double> getPrediction(String method, List<Double> prices) {
    if ("Moving Average".equals(method)) {
      return StockPredictor.predictMovingAverage(prices, 5);
    } else {
      double futurePrice = StockPredictor.predictLinearRegression(prices);
      return List.of(prices.get(0), futurePrice); // Current and predicted price
    }
  }

  private void setUILoadingState(boolean loading) {
    predictBtn.setDisable(loading);
    progressIndicator.setVisible(loading);
    predictBtn.setText(loading ? "Predicting..." : "Predict");
  }

  private void setupLayout(Stage stage) {
    VBox root = new VBox(10,
        new Label("Stock Symbol (e.g., AAPL/MSFT):"),
        stockSymbolInput,
        new Label("Prediction Method:"),
        methodComboBox,
        new HBox(10, predictBtn, progressIndicator),
        chart
    );
    root.setPadding(new javafx.geometry.Insets(10));

    stage.setScene(new Scene(root, 800, 600));
    stage.setTitle("Java Stock Prediction System");
    stage.show();
  }

  private void updateChart(List<Double> prices, List<Double> predicted) {
    chart.getData().clear();

    XYChart.Series<Number, Number> historySeries = new XYChart.Series<>();
    historySeries.setName("Historical Data");
    for (int i = 0; i < prices.size(); i++) {
      historySeries.getData().add(new XYChart.Data<>(i, prices.get(i)));
    }

    XYChart.Series<Number, Number> predictedSeries = new XYChart.Series<>();
    predictedSeries.setName("Predicted Data");
    int startIndex = prices.size() - predicted.size();
    for (int i = 0; i < predicted.size(); i++) {
      predictedSeries.getData().add(new XYChart.Data<>(startIndex + i, predicted.get(i)));
    }

    chart.getData().addAll(historySeries, predictedSeries);
  }

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
