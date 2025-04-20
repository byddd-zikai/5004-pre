import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.math3.stat.regression.SimpleRegression;


public class ChartPanel extends JPanel {
  private final List<PriceEntry> entries;
  private final double slope;
  private final double intercept;
  private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

  public ChartPanel(List<PriceEntry> entries) {
    this.entries = entries;
    SimpleRegression reg = new SimpleRegression(true);
    for (int i = 0; i < entries.size(); i++) {
      reg.addData(i, entries.get(i).getPrice());
    }
    this.slope = reg.getSlope();
    this.intercept = reg.getIntercept();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (entries.isEmpty()) return;

    Graphics2D g2 = (Graphics2D) g;
    int w = getWidth();
    int h = getHeight();
    int margin = 60;
    int bottomMargin = 90;
    int chartW = w - 2 * margin;
    int chartH = h - margin - bottomMargin;
    int n = entries.size();

    double maxP = entries.stream()
        .mapToDouble(PriceEntry::getPrice)
        .max().orElse(1);
    double minP = entries.stream()
        .mapToDouble(PriceEntry::getPrice)
        .min().orElse(0);


    g2.setColor(Color.BLACK);
    g2.drawLine(margin, margin, margin, margin + chartH);
    g2.drawLine(margin, margin + chartH, margin + chartW, margin + chartH);

    FontMetrics fm = g2.getFontMetrics();


    int maxLabels = 10;
    int skip = Math.max(1, n / maxLabels);


    for (int i = 0; i < n; i += skip) {
      int x = margin + (int) ((double) i / (n - 1) * chartW);
      g2.drawLine(x, margin + chartH, x, margin + chartH + 5);
      String text = entries.get(i).getDate().format(fmt);
      int tw = fm.stringWidth(text);
      g2.drawString(text, x - tw / 2, margin + chartH + fm.getAscent() + 20);
    }

    if ((n - 1) % skip != 0) {
      int i = n - 1;
      int x = margin + chartW;
      g2.drawLine(x, margin + chartH, x, margin + chartH + 5);
      String text = entries.get(i).getDate().format(fmt);
      int tw = fm.stringWidth(text);
      g2.drawString(text, x - tw / 2, margin + chartH + fm.getAscent() + 20);
    }


    g2.setColor(Color.BLUE);
    for (int i = 0; i < n; i++) {
      double p = entries.get(i).getPrice();
      int x = margin + (int) ((double) i / (n - 1) * chartW);
      int y = margin + chartH - (int) ((p - minP) / (maxP - minP) * chartH);
      g2.fillOval(x - 4, y - 4, 8, 8);
    }

    double y0 = intercept + slope * 0;
    double yN = intercept + slope * (n - 1);
    int x1 = margin;
    int y1 = margin + chartH - (int) ((y0 - minP) / (maxP - minP) * chartH);
    int x2 = margin + chartW;
    int y2 = margin + chartH - (int) ((yN - minP) / (maxP - minP) * chartH);
    g2.setStroke(new BasicStroke(2f));
    g2.setColor(Color.RED);
    g2.draw(new Line2D.Double(x1, y1, x2, y2));
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(600, 400);
  }
}

