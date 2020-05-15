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
import java.util.regex.Pattern;

/**
 * The file size human readable utility class, 
 * provide mutual conversions from human readable size to byte size<p>
 * 
 * The similar function in stackoverflow, linked:
 *  https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java?r=SearchResults<p>
 * 
 * Apache also provide similar function 
 *  {@link org.apache.commons.io.FileUtils#byteCountToDisplaySize(long)}<p>
 * 
 * spring-core 5.x: org.springframework.util.unit.DataSize
 * 
 * @author Ponfee
 */
public enum HumanReadables {

    SI    (1000, "B", "KB",  "MB",  "GB",  "TB",  "PB",  "EB" /*, "ZB",  "YB" */), // 

    BINARY(1024, "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"/*, "ZiB", "YiB"*/), // 

    ;

    private static final String  FORMAT  = "#,##0.##";
    private static final Pattern PATTERN = Pattern.compile(".*[0-9]+.*");

    private final int      base;
    private final String[] units;
    private final long[]   sizes;

    HumanReadables(int base, String... units) {
        this.base  = base;
        this.units = units;
        this.sizes = new long[this.units.length];

        this.sizes[0] = 1L;
        for (int i = 1; i < this.sizes.length; i++) {
            this.sizes[i] = this.sizes[i - 1] * base; // Maths.pow(base, i);
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
            return "0 " + this.units[0];
        }

        String sign = "";
        if (size < 0) {
            sign = "-";
            size = size == Long.MIN_VALUE ? Long.MAX_VALUE : -size;
        }

        /*int unit = (int) Maths.log(size, this.base);
        return sign + new DecimalFormat(FORMAT).format(size / Math.pow(this.base, unit)) + " " + this.units[unit];*/

        int unit = find(size);
        return new StringBuilder(13) // 13 max length like as "-1,023.45 GiB"
            .append(sign)
            .append(new DecimalFormat(FORMAT).format(size / (double) this.sizes[unit]))
            .append(" ")
            .append(this.units[unit])
            .toString();
    }

    public long parse(String size) {
        return parse(size, false);
    }

    /**
     * Parse the readable byte count, allowed suffix units: "1", "1 B", "1 MB", "1 MiB", "1 M"
     * 
     * @param size   the size
     * @param strict the strict, if BINARY then verify whether contains "i"
     * @return a long value bytes count
     */
    public strictfp long parse(String size, boolean strict) {
        if (size == null || size.isEmpty()) {
            return 0L;
        }
        if (!PATTERN.matcher(size).matches()) {
            throw new IllegalArgumentException("Invalid format [" + size + "]");
        }

        String value = size = size.trim();
        long factor = this.sizes[0];
        int sign = 1;
        switch (value.charAt(0)) {
            case '+': value = value.substring(1);            break;
            case '-': value = value.substring(1); sign = -1; break;
            default : /*          Nothing to do          */  break;
        }

        int end = 0, lastPos = value.length() - 1;
        // last character isn't a digit
        char c = value.charAt(lastPos - end);
        if (c == 'i') {
            // last pos cannot end with "i"
            throw new IllegalArgumentException("Invalid format [" + size + "], cannot end with \"i\".");
        }

        if (c == 'B') {
            end++;
            c = value.charAt(lastPos - end);

            boolean flag = isBlank(c);
            while (isBlank(c) && end < lastPos) {
                end++;
                c = value.charAt(lastPos - end);
            }
            // if "B" head has space char, then the first head non space char must be a digit
            if (flag && !Character.isDigit(c)) {
                throw new IllegalArgumentException("Invalid format [" + size + "]: \"" + c + "\".");
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
                c = value.charAt(lastPos - end);
            } else {
                if (this == BINARY && strict) {
                    // if strict, then BINARY must contains "i"
                    throw new IllegalArgumentException("Invalid BINARY format [" + size + "], miss character \"i\".");
                }
            }

            switch (c) {
                case 'K': factor = this.sizes[1]; break;
                case 'M': factor = this.sizes[2]; break;
                case 'G': factor = this.sizes[3]; break;
                case 'T': factor = this.sizes[4]; break;
                case 'P': factor = this.sizes[5]; break;
                case 'E': factor = this.sizes[6]; break;
                /*
                case 'Z': factor = this.bytes[7]; break;
                case 'Y': factor = this.bytes[8]; break;
                */
                default: throw new IllegalArgumentException("Invalid format [" + size + "]: \"" + c + "\".");
            }

            do {
                end++;
                c = value.charAt(lastPos - end);
            } while (isBlank(c) && end < lastPos);
        }

        value = value.substring(0, value.length() - end);
        try {
            return sign * (long) (factor * new DecimalFormat(FORMAT).parse(value).doubleValue());
        } catch (NumberFormatException | ParseException e) {
            throw new IllegalArgumentException("Failed to parse [" + size + "]: \"" + value + "\".");
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

    // --------------------------------------------------------------private methods
    private int find(long bytes) {
        int n = this.sizes.length;
        for (int i = 1; i < n; i++) {
            if (bytes < this.sizes[i]) {
                return i - 1;
            }
        }
        return n - 1;
    }

    private boolean isBlank(char c) {
        return c == ' ' || c == '\t';
    }

}
