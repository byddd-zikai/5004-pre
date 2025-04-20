import java.time.LocalDate;


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
