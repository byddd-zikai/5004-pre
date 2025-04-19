import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Collections;

public class StockPredictionApp extends JFrame {
  private JTextField stockSymbolField;
  private JButton predictButton;
  private JTextArea resultArea;
  private JSplitPane splitPane;
  private ChartPanel chartPanel;

  private JList<String> recentDataList;
  private DefaultListModel<String> recentDataModel;

  public StockPredictionApp() {
    setTitle("StockPredictionApp");
    setSize(800, 700);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    stockSymbolField = new JTextField("AAPL", 10);
    predictButton     = new JButton("Predict");
    JPanel inputPanel = new JPanel();
    inputPanel.add(new JLabel("STOCK SYMBOL:"));
    inputPanel.add(stockSymbolField);
    inputPanel.add(predictButton);


    resultArea = new JTextArea();
    resultArea.setEditable(false);

    chartPanel = new ChartPanel(List.of());

    splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        new JScrollPane(resultArea),
        chartPanel
    );
    splitPane.setDividerLocation(350);
    splitPane.setOneTouchExpandable(true);

    recentDataModel = new DefaultListModel<>();
    recentDataList  = new JList<>(recentDataModel);
    JScrollPane recentScroll = new JScrollPane(recentDataList);
    recentScroll.setBorder(
        BorderFactory.createTitledBorder("Recent Prices (newest first)")
    );


    setLayout(new BorderLayout(5,5));
    add(inputPanel, BorderLayout.NORTH);
    add(splitPane,   BorderLayout.CENTER);
    add(recentScroll, BorderLayout.SOUTH);


    predictButton.addActionListener(e -> {
      String symbol = stockSymbolField.getText().trim();
      if (symbol.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "Please enter Stock Symbol",
            "Error", JOptionPane.ERROR_MESSAGE
        );
        return;
      }
      predictButton.setEnabled(false);
      predictButton.setText("Predicting...");

      new Thread(() -> {
        try {
          List<Double> prices    = StockDataCache.getData(symbol, false);
          List<Double> movingAvg = StockPredictor.predictMovingAverage(prices, 5);
          List<Double> regres    = StockPredictor.predictLinearRegression(prices);

          SwingUtilities.invokeLater(() -> {

            displayResults(symbol, prices, movingAvg, regres.get(1));


            ChartPanel newChart = new ChartPanel(prices);
            splitPane.setRightComponent(newChart);


            recentDataModel.clear();
            List<Double> reversed = prices.size() <= 10
                ? prices
                : prices.subList(0, 10);
            Collections.reverse(reversed);
            for (int i = 0; i < reversed.size(); i++) {
              recentDataModel.addElement(
                  String.format("Day %d: %.2f", i+1, reversed.get(i))
              );
            }

            revalidate();
            repaint();
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

  private void displayResults(String symbol,
      List<Double> prices,
      List<Double> movingAvg,
      double regressionPrediction) {
    StringBuilder sb = new StringBuilder();
    sb.append("=== ").append(symbol).append(" Predict result ===\n\n");

    sb.append("Ending price for 5 days:\n");
    for (int i = 0; i < Math.min(5, prices.size()); i++) {
      sb.append(String.format("Day %d: %.2f\n", i + 1, prices.get(i)));
    }

    sb.append("\nAverage move for 5 days:\n");
    for (int i = 0; i < movingAvg.size(); i++) {
      sb.append(String.format("Day %d: %.2f\n", i + 1, movingAvg.get(i)));
    }

    sb.append(String.format(
        "\nLinear regression predicts next day's price: %.2f",
        regressionPrediction
    ));

    resultArea.setText(sb.toString());
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      new StockPredictionApp().setVisible(true);
    });
  }
}


