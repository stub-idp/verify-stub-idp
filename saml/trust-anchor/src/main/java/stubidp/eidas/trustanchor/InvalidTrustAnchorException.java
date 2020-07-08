package stubidp.eidas.trustanchor;

public class InvalidTrustAnchorException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public InvalidTrustAnchorException(String message) {
    super(message);
  }

  public InvalidTrustAnchorException(String message, Throwable cause) {
    super(message, cause);
  }
}