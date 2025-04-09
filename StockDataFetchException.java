public class StockDataFetchException extends Exception {
  public StockDataFetchException(String message) {
    super(message);
  }

  public StockDataFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}