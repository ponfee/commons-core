/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.io;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.math.Maths;

/**
 * Human readable utility class
 * 
 * The similar function in stackoverflow, linked:
 *  https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java?r=SearchResults
 * 
 * Apache also provide similar function
 * @see org.apache.commons.io.FileUtils#byteCountToDisplaySize(long)
 * 
 * @author Ponfee
 */
public enum HumanReadables {

    SI    (1000, "B", "KB",  "MB",  "GB",  "TB",  "PB",  "EB" /*, "ZB",  "YB" */), // 

    BINARY(1024, "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"/*, "ZiB", "YiB"*/), // 
    ;

    private static final String FORMAT = "#,##0.##";

    private final int      base;
    private final String[] units;
    private final long[]   sizes;

    HumanReadables(int base, String... units) {
        this.base  = base;
        this.units = units;
        this.sizes = new long[this.units.length];
        long size = 1;
        for (int i = 0; i < this.sizes.length; i++) {
            this.sizes[i] = (size = size * this.base);
        }
    }

    /**
     * Returns a string of bytes count human readable size
     * 
     * @param size the size
     * @return human readable size
     */
    public strictfp String human(long size) {
        if (size == 0) {
            return "0" + this.units[0];
        }

        String signed = "";
        if (size < 0) {
            signed = "-";
            size = size == Long.MIN_VALUE ? Long.MAX_VALUE : -size;
        }

        int digit = (int) Maths.log(size, this.base);
        return new StringBuilder(13).append(signed)
            .append(new DecimalFormat(FORMAT).format(size / Math.pow(this.base, digit)))
            .append(" ").append(this.units[digit]).toString();
    }

    public strictfp long parse(String size) {
        return parse(size, false);
    }

    /**
     * Parse the readable byte count, allowed suffix units: "1", "1B", "1MB", "1MiB", "1M"
     * 
     * @param size   the size
     * @param strict the strict, if BINARY then verify whether contains "i"
     * @return a long value bytes count
     */
    public strictfp long parse(String size, boolean strict) {
        if (StringUtils.isBlank(size)) {
            return 0L;
        }
        if (!size.matches(".*[0-9]+.*")) {
            throw new IllegalArgumentException("Invalid format [" + size + "]");
        }

        String str = size = size.trim();
        long factor = 1L;
        switch (str.charAt(0)) {
            case '+': str = str.substring(1);               break;
            case '-': str = str.substring(1); factor = -1L; break;
        }

        int end = 0, lastPos = str.length() - 1;
        // last character isn't a digit
        char c = str.charAt(lastPos - end);
        if (c == 'i') {
            // the last pos cannot end with "i"
            throw new IllegalArgumentException("Invalid format [" + size + "], cannot end with \"i\".");
        }

        if (c == 'B') {
            end++;
            c = str.charAt(lastPos - end);

            if (isBlank(c)) {
                while (isBlank(c) && end < lastPos) {
                    end++;
                    c = str.charAt(lastPos - end);
                }
                // if "B" prefix has space char, then the first prefix non space char must be a digit
                if (!Character.isDigit(c)) {
                    throw new IllegalArgumentException("Invalid format [" + size + "]: \"" + c + "\".");
                }
            }
        }

        if (!Character.isDigit(c)) {
            // if not a digit character, then assume is a unit character
            if (c == 'i') {
                if (this == SI) {
                    // SI cannot contains "i"
                    throw new IllegalArgumentException("Invalid SI format [" + size + "], cannot contains \"i\".");
                }
                end++;
                c = str.charAt(lastPos - end);
            } else {
                if (this == BINARY && strict) {
                    // if strict, then BINARY must contains "i"
                    throw new IllegalArgumentException("Invalid BINARY format [" + size + "], miss character \"i\".");
                }
            }

            switch (c) {
                case 'K': factor *= this.sizes[0]; break;
                case 'M': factor *= this.sizes[1]; break;
                case 'G': factor *= this.sizes[2]; break;
                case 'T': factor *= this.sizes[3]; break;
                case 'P': factor *= this.sizes[4]; break;
                case 'E': factor *= this.sizes[5]; break;
                /*
                case 'Z': factor *= this.bytes[6]; break;
                case 'Y': factor *= this.bytes[7]; break;
                */
                default: throw new IllegalArgumentException("Invalid format [" + size + "]: \"" + c + "\".");
            }

            do {
                end++;
                c = str.charAt(lastPos - end);
            } while (isBlank(c) && end < lastPos);
        }

        str = str.substring(0, str.length() - end);
        try {
            return (long) (factor * new DecimalFormat(FORMAT).parse(str).doubleValue());
        } catch (NumberFormatException | ParseException e) {
            throw new IllegalArgumentException("Failed to parse [" + size + "]: \"" + str + "\".");
        }
    }

    public int base() {
        return this.base;
    }

    public String[] units() {
        return Arrays.copyOf(this.units, this.units.length);
    }

    public long[] sizes() {
        return Arrays.copyOf(this.sizes, this.sizes.length);
    }

    private static boolean isBlank(char c) {
        return c == ' ' || c == '\t';
    }

}
