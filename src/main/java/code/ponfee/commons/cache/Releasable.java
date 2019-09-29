package code.ponfee.commons.cache;

import java.io.Closeable;

/**
 * Release resources
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface Releasable extends Closeable {

    @Override
    void close();

}