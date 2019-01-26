package test.jce.ecc2;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 * Utilities for converting various strings into byte arrays they encode for given bases
 */
public class BaseConvert {

    /**
     * Convert a string to a byte array it encodes
     *
     * @param string A string representing an array of bytes
     * @param radix  The base the string is encoded with
     * @return The byte array the string represents
     * @throws UnsupportedBaseException Thrown if an unsupported base is handed as an argument
     */
    public static byte[] baseEncodedStringToByteArray(
            String string,
            int radix) throws UnsupportedBaseException {
        switch (radix) {
            case 16:
                return parseHexBinary(string);
            case 64:
                return parseBase64Binary(string);
            default:
                throw new UnsupportedBaseException("Unknown base given when trying to parse an encoded string to a byte array");
        }
    }

    /**
     * Convert a byte array into a encoded string
     *
     * @param bytes The bytes to encode as a string
     * @param radix The base of the encoding
     * @return An encoded string
     * @throws UnsupportedBaseException Thrown if an unsupported base is handed as an argument
     */
    public static String byteArrayToBaseEncodedString(
            byte[] bytes,
            int radix) throws UnsupportedBaseException {
        switch (radix) {
            case 16:
                return printHexBinary(bytes).toLowerCase();
            case 64:
                return printBase64Binary(bytes);
            default:
                throw new UnsupportedBaseException("Unknown base given when trying to write an string encoding a byte array");
        }
    }
}
