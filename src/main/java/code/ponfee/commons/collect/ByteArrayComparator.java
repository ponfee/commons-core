package code.ponfee.commons.collect;

/**
 * The utility for compare two byte array
 * 
 * For compare
 * 
 * @author Ponfee
 */
public final class ByteArrayComparator {

    public static int compareTo(final byte[] left, final byte[] right) {
        return compareTo(left, 0, left.length, right, 0, right.length);
    }

    public static int compareTo(byte[] buffer1, int offset1, int length1,
                                byte[] buffer2, int offset2, int length2) {
        // Short circuit equal case
        if (buffer1 == buffer2 &&
            offset1 == offset2 &&
            length1 == length2) {
            return 0;
        }
        // Bring WritableComparator code local
        int end1 = offset1 + length1;
        int end2 = offset2 + length2;
        for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
            int a = (buffer1[i] & 0xff);
            int b = (buffer2[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return length1 - length2;
    }

}
