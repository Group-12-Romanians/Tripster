package tripster.tripster.dataLayer.exceptions;

public class TooManyUsersWithOneIdException extends RuntimeException {
  public TooManyUsersWithOneIdException(String s) {
    super(s);
  }
}
