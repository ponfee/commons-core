package code.ponfee.commons.jce.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Key interface
 * @author Ponfee
 */
public interface Key {

    Key readKey(InputStream in) throws IOException;

    void writeKey(OutputStream out) throws IOException;

    Key getPublic();

    boolean isPublic();
}
