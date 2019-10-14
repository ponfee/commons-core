package code.ponfee.commons.base;

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