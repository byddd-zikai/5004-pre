import java.time.LocalDate;

/**
 * 将 LocalDate 与对应的收盘价捆绑，toString() 输出“YYYY‑MM‑DD → price”。
 */
public final class PriceEntry {
  private final LocalDate date;
  private final double price;

  public PriceEntry(LocalDate date, double price) {
    this.date  = date;
    this.price = price;
  }

  public LocalDate getDate() {
    return date;
  }

  public double getPrice() {
    return price;
  }

  @Override
  public String toString() {
    // 例如: "2025-04-18 → 123.45"
    return String.format("%s → %.2f", date, price);
  }
}
