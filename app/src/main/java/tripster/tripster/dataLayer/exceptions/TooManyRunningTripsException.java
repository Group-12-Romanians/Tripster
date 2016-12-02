package tripster.tripster.dataLayer.exceptions;

/**
 * Created by dragos on 11/25/16.
 */
public class TooManyRunningTripsException extends RuntimeException {
  public TooManyRunningTripsException(String s) {
    super(s);
  }
}
