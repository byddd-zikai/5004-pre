import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class StockPredictionApp extends JFrame {
  private final JTextField stockSymbolField;
  private final JButton predictButton;
  private final JTextArea resultArea;
  private final JSplitPane splitPane;
  private ChartPanel chartPanel;  // note non-final so we can replace it
  private final DefaultListModel<PriceEntry> recentDataModel;

  public StockPredictionApp() {
    super("StockPredictionApp");
    setSize(800, 700);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    // input panel
    stockSymbolField = new JTextField("AAPL", 10);
    predictButton    = new JButton("Predict");
    JPanel inputPanel = new JPanel();
    inputPanel.add(new JLabel("STOCK SYMBOL:"));
    inputPanel.add(stockSymbolField);
    inputPanel.add(predictButton);

    // text area
    resultArea = new JTextArea();
    resultArea.setEditable(false);
    JScrollPane textScroll = new JScrollPane(resultArea);

    // initial (empty) chart panel
    chartPanel = new ChartPanel(List.<PriceEntry>of());

    // split pane: left=text, right=chart
    splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        textScroll,
        chartPanel
    );
    splitPane.setDividerLocation(350);
    splitPane.setOneTouchExpandable(true);

    // recent prices list
    recentDataModel = new DefaultListModel<>();
    JList<PriceEntry> recentList = new JList<>(recentDataModel);
    JScrollPane listScroll = new JScrollPane(recentList);
    listScroll.setBorder(BorderFactory.createTitledBorder(
        "Recent Prices (old→new, Date → Price)"
    ));

    // layout
    setLayout(new BorderLayout(5,5));
    add(inputPanel, BorderLayout.NORTH);
    add(splitPane,   BorderLayout.CENTER);
    add(listScroll,  BorderLayout.SOUTH);

    // button listener
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
          // fetch entries (with dates + prices)
          List<PriceEntry> entries = StockDataFetcher.fetchPriceEntries(symbol);
          // extract just prices for predictor
          List<Double> prices = entries.stream()
              .map(PriceEntry::getPrice)
              .collect(Collectors.toList());
          List<Double> movingAvg = StockPredictor.predictMovingAverage(prices, 5);
          List<Double> regres    = StockPredictor.predictLinearRegression(prices);

          SwingUtilities.invokeLater(() -> {
            // update text results
            displayResults(symbol, prices, movingAvg, regres.get(1));
            // update chartPanel with entries
            chartPanel = new ChartPanel(entries);
            splitPane.setRightComponent(chartPanel);
            // update recent list
            recentDataModel.clear();
            for (PriceEntry pe : entries) {
              recentDataModel.addElement(pe);
            }
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

    sb.append("Ending price for last 5 days:\n");
    int priceCount = prices.size();
    int startP     = Math.max(0, priceCount - 5);
    for (int i = startP; i < priceCount; i++) {
      sb.append(String.format("Day %d: %.2f\n",
          i - startP + 1,
          prices.get(i)));
    }

    sb.append("\nAverage move for last 5 days:\n");
    int maCount = movingAvg.size();
    int startM  = Math.max(0, maCount - 5);
    for (int i = startM; i < maCount; i++) {
      sb.append(String.format("Day %d: %.2f\n",
          i - startM + 1,
          movingAvg.get(i)));
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
