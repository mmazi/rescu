package si.mazi.rescu;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Rafał Krupiński
 */
public interface NonceGenerator {
  long nextNonce();
}
