package tripster.tripster.dataLayer.exceptions;

/**
 * Created by dragos on 11/25/16.
 */
public class AlreadyRunningTripException extends RuntimeException {
  public AlreadyRunningTripException(String s) {
    super(s);
  }
}
