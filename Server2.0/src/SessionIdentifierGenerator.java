import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionIdentifierGenerator {
  private SecureRandom random = new SecureRandom();

  public String nextSessionId(int n) {
    return new BigInteger(128, random).toString(n);
  }
}
