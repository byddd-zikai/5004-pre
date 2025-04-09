import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StockPredictionApp extends JFrame {
  private JTextField stockSymbolField;
  private JButton predictButton;
  private JTextArea resultArea;

  public StockPredictionApp() {
    setTitle("StockPridictionApp");
    setSize(500, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    stockSymbolField = new JTextField("AAPL", 10);
    predictButton = new JButton("Predict");
    resultArea = new JTextArea();
    resultArea.setEditable(false);

    JPanel inputPanel = new JPanel();
    inputPanel.add(new JLabel("STOCK SYMBOL:"));
    inputPanel.add(stockSymbolField);
    inputPanel.add(predictButton);

    JScrollPane scrollPane = new JScrollPane(resultArea);

    setLayout(new BorderLayout());
    add(inputPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    predictButton.addActionListener(e -> {
      String symbol = stockSymbolField.getText().trim();
      if (symbol.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter Storck Symble", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      predictButton.setEnabled(false);
      predictButton.setText("Predicting...");

      new Thread(() -> {
        try {
          List<Double> prices = StockDataCache.getData(symbol, false);
          List<Double> movingAvg = StockPredictor.predictMovingAverage(prices, 5);
          double regressionPrediction = StockPredictor.predictLinearRegression(prices).get(1);

          SwingUtilities.invokeLater(() -> {
            displayResults(symbol, prices, movingAvg, regressionPrediction);
            predictButton.setEnabled(true);
            predictButton.setText("Predict");
          });
        } catch (Exception ex) {
          SwingUtilities.invokeLater(() -> {
            resultArea.setText("Error: " + ex.getMessage());
            predictButton.setEnabled(true);
            predictButton.setText("Predict");
          });
        }
      }).start();
    });
  }

  private void displayResults(String symbol, List<Double> prices,
      List<Double> movingAvg, double regressionPrediction) {
    StringBuilder sb = new StringBuilder();
    sb.append("=== ").append(symbol).append(" Predict result ===\n\n");

    sb.append("Ending price for 5 days:\n");
    for (int i = 0; i < Math.min(5, prices.size()); i++) {
      sb.append(String.format("The %d day: %.2f\n", i+1, prices.get(i)));
    }

    sb.append("\nAverage move for 5 days:\n");
    for (int i = 0; i < movingAvg.size(); i++) {
      sb.append(String.format("The %d day: %.2f\n", i+1, movingAvg.get(i)));
    }

    sb.append(String.format("\nLinear regression predicts the next day's price: %.2f", regressionPrediction));

    resultArea.setText(sb.toString());
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      StockPredictionApp app = new StockPredictionApp();
      app.setVisible(true);
    });
  }
}
