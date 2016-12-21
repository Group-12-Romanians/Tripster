package tripster.tripster.dataLayer.exceptions;

public class TooManyRunningTripsException extends RuntimeException {
  public TooManyRunningTripsException(String s) {
    super(s);
  }
}
