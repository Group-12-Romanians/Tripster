package tripster.tripster.dataLayer.exceptions;

public class AlreadyRunningTripException extends RuntimeException {
  public AlreadyRunningTripException(String s) {
    super(s);
  }
}
