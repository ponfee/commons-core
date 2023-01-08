/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io.charset;


import cn.ponfee.commons.io.CharsetDetector;
import cn.ponfee.commons.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Byte array charset detector
 *
 * @author Ponfee
 */
public class BytesDetector {

    private static final Logger LOG = LoggerFactory.getLogger(BytesDetector.class);

    public static Charset detect(InputStream input, int length) throws IOException {
        String charset = detect(Files.readByteArray(input, length));
        return charset == null ? CharsetDetector.DEFAULT_CHARSET : Charset.forName(charset);
    }

    public static String detect(byte[] rawtext) {
        int[] scores = new int[Encoding.TOTAL_TYPES];
        // Assign Scores
        scores[Encoding.GB2312]        = gb2312_probability(rawtext)     ;
        scores[Encoding.GBK]           = gbk_probability(rawtext)        ;
        scores[Encoding.GB18030]       = gb18030_probability(rawtext)    ;
        scores[Encoding.HZ]            = hz_probability(rawtext)         ;
        scores[Encoding.BIG5]          = big5_probability(rawtext)       ;
        scores[Encoding.CNS11643]      = euc_tw_probability(rawtext)     ;
        scores[Encoding.UTF8]          = utf8_probability(rawtext)       ;
        scores[Encoding.UTF8T]         = 0                               ;
        scores[Encoding.UTF8S]         = 0                               ;
        scores[Encoding.UNICODE]       = utf16_probability(rawtext)      ;
        scores[Encoding.UNICODET]      = 0                               ;
        scores[Encoding.UNICODES]      = 0                               ;
        scores[Encoding.ISO2022CN]     = iso_2022_cn_probability(rawtext);
        scores[Encoding.ISO2022CN_CNS] = 0                               ;
        scores[Encoding.ISO2022CN_GB]  = 0                               ;
        scores[Encoding.EUC_KR]        = euc_kr_probability(rawtext)     ;
        scores[Encoding.CP949]         = cp949_probability(rawtext)      ;
        scores[Encoding.ISO2022KR]     = iso_2022_kr_probability(rawtext);
        scores[Encoding.JOHAB]         = 0                               ;
        scores[Encoding.SJIS]          = sjis_probability(rawtext)       ;
        scores[Encoding.EUC_JP]        = euc_jp_probability(rawtext)     ;
        scores[Encoding.ISO2022JP]     = iso_2022_jp_probability(rawtext);
        scores[Encoding.ASCII]         = ascii_probability(rawtext)      ;

        // Tabulate Scores
        int maxScore = 0, encodingGuess = -1;
        for (int i = 0; i < scores.length; i++) {
            LOG.debug("Encoding {} score {}", Encoding.JAVA_CHARSET[i], scores[i]);
            if (scores[i] > maxScore) {
                encodingGuess = i;
                maxScore = scores[i];
            }
        }
        // Not guessed if nothing scored above 50
        return maxScore > 50 ? Encoding.JAVA_CHARSET[encodingGuess] : null;
    }

    /*
     * Function: gb2312_probability Argument: pointer to byte array Returns : number from 0 to 100 representing
     * probability text in array uses GB-2312 encoding
     */
    private static int gb2312_probability(byte[] rawtext) {
        int dbchars = 1, gbchars = 1;
        long gbfreq = 0, totalfreq = 1;
        // Stage 1: Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            // System.err.println(rawtext[i]);
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xF7 && (byte) 0xA1 <= rawtext[i + 1]
                    && rawtext[i + 1] <= (byte) 0xFE) {
                    gbchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0xA1;
                    column = rawtext[i + 1] + 256 - 0xA1;
                    if (GB_FREQ[row][column] != 0) {
                        gbfreq += GB_FREQ[row][column];
                    } else if (15 <= row && row < 55) {
                        // In GB high-freq character range
                        gbfreq += 200;
                    }
                }
                i++;
            }
        }
        float rangeval = 50 * ((float) gbchars / (float) dbchars);
        float freqval = 50 * ((float) gbfreq / (float) totalfreq);
        return (int) (rangeval + freqval);
    }

    /*
     * Function: gbk_probability Argument: pointer to byte array Returns : number from 0 to 100 representing
     * probability text in array uses GBK encoding
     */
    private static int gbk_probability(byte[] rawtext) {
        int dbchars = 1, gbchars = 1;
        long gbfreq = 0, totalfreq = 1;
        // Stage 1: Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            // System.err.println(rawtext[i]);
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xF7 && // Original GB range
                    (byte) 0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE) {
                    gbchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0xA1;
                    column = rawtext[i + 1] + 256 - 0xA1;
                    // System.out.println("original row " + row + " column " +
                    // column);
                    if (GB_FREQ[row][column] != 0) {
                        gbfreq += GB_FREQ[row][column];
                    } else if (15 <= row && row < 55) {
                        gbfreq += 200;
                    }
                } else if ((byte) 0x81 <= rawtext[i] && rawtext[i] <= (byte) 0xFE && // Extended GB range
                    (((byte) 0x80 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE)
                        || ((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E))) {
                    gbchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0x81;
                    if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
                        column = rawtext[i + 1] - 0x40;
                    } else {
                        column = rawtext[i + 1] + 256 - 0x40;
                    }
                    // System.out.println("extended row " + row + " column " +
                    // column + " rawtext[i] " + rawtext[i]);
                    if (GBK_FREQ[row][column] != 0) {
                        gbfreq += GBK_FREQ[row][column];
                    }
                }
                i++;
            }
        }
        float rangeval = 50 * ((float) gbchars / (float) dbchars);
        float freqval = 50 * ((float) gbfreq / (float) totalfreq);
        // For regular GB files, this would give the same score, so I handicap
        // it slightly
        return (int) (rangeval + freqval) - 1;
    }

    /*
     * Function: gb18030_probability Argument: pointer to byte array Returns : number from 0 to 100 representing
     * probability text in array uses GBK encoding
     */
    private static int gb18030_probability(byte[] rawtext) {
        int dbchars = 1, gbchars = 1;
        long gbfreq = 0, totalfreq = 1;
        // Stage 1: Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            // System.err.println(rawtext[i]);
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xF7 && // Original GB range
                    i + 1 < rawtext.length && (byte) 0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE) {
                    gbchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0xA1;
                    column = rawtext[i + 1] + 256 - 0xA1;
                    // System.out.println("original row " + row + " column " +
                    // column);
                    if (GB_FREQ[row][column] != 0) {
                        gbfreq += GB_FREQ[row][column];
                    } else if (15 <= row && row < 55) {
                        gbfreq += 200;
                    }
                } else if ((byte) 0x81 <= rawtext[i] && rawtext[i] <= (byte) 0xFE && // Extended GB range
                    i + 1 < rawtext.length && (((byte) 0x80 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE)
                    || ((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E))) {
                    gbchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0x81;
                    if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
                        column = rawtext[i + 1] - 0x40;
                    } else {
                        column = rawtext[i + 1] + 256 - 0x40;
                    }
                    // System.out.println("extended row " + row + " column " +
                    // column + " rawtext[i] " + rawtext[i]);
                    if (GBK_FREQ[row][column] != 0) {
                        gbfreq += GBK_FREQ[row][column];
                    }
                } else if ((byte) 0x81 <= rawtext[i] && rawtext[i] <= (byte) 0xFE && // Extended GB range
                    i + 3 < rawtext.length && (byte) 0x30 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x39
                    && (byte) 0x81 <= rawtext[i + 2] && rawtext[i + 2] <= (byte) 0xFE && (byte) 0x30 <= rawtext[i + 3]
                    && rawtext[i + 3] <= (byte) 0x39) {
                    gbchars++;
                    /*
                     * totalfreq += 500; row = rawtext[i] + 256 - 0x81; if (0x40 <= rawtext[i+1] && rawtext[i+1] <=
                     * 0x7E) { column = rawtext[i+1] - 0x40; } else { column = rawtext[i+1] + 256 - 0x40; }
                     * //System.out.println("extended row " + row + " column " + column + " rawtext[i] " +
                     * rawtext[i]); if (GBKFreq[row][column] != 0) { gbfreq += GBKFreq[row][column]; }
                     */
                }
                i++;
            }
        }
        float rangeval = 50 * ((float) gbchars / (float) dbchars);
        float freqval = 50 * ((float) gbfreq / (float) totalfreq);
        // For regular GB files, this would give the same score, so I handicap
        // it slightly
        return (int) (rangeval + freqval) - 1;
    }

    /*
     * Function: hz_probability Argument: byte array Returns : number from 0 to 100 representing probability text in
     * array uses HZ encoding
     */
    private static int hz_probability(byte[] rawtext) {
        long hzfreq = 0, totalfreq = 1;
        int hzstart = 0;
        for (int i = 0, row, column, n = rawtext.length - 1; i < rawtext.length; i++) {
            if (rawtext[i] == '~') {
                if (rawtext[i + 1] == '{') {
                    hzstart++;
                    i += 2;
                    while (i < n) {
                        if (rawtext[i] == 0x0A || rawtext[i] == 0x0D) {
                            break;
                        } else if (rawtext[i] == '~' && rawtext[i + 1] == '}') {
                            i++;
                            break;
                        } else if ((0x21 <= rawtext[i] && rawtext[i] <= 0x77) && (0x21 <= rawtext[i + 1] && rawtext[i + 1] <= 0x77)) {
                            row = rawtext[i] - 0x21;
                            column = rawtext[i + 1] - 0x21;
                            totalfreq += 500;
                            if (GB_FREQ[row][column] != 0) {
                                hzfreq += GB_FREQ[row][column];
                            } else if (15 <= row && row < 55) {
                                hzfreq += 200;
                            }
                        } else if (between(rawtext[i]) && between(rawtext[i + 1])) {
                            row = rawtext[i] + 256 - 0xA1;
                            column = rawtext[i + 1] + 256 - 0xA1;
                            totalfreq += 500;
                            if (GB_FREQ[row][column] != 0) {
                                hzfreq += GB_FREQ[row][column];
                            } else if (15 <= row && row < 55) {
                                hzfreq += 200;
                            }
                        }
                        i += 2;
                    }
                } else if (rawtext[i + 1] == '}') {
                    i++;
                } else if (rawtext[i + 1] == '~') {
                    i++;
                }
            }
        }

        float rangeval;
        if (hzstart > 4) {
            rangeval = 50;
        } else if (hzstart > 1) {
            rangeval = 41;
        } else if (hzstart > 0) { // Only 39 in case the sequence happened to
            // occur
            rangeval = 39; // in otherwise non-Hz text
        } else {
            rangeval = 0;
        }
        float freqval = 50 * ((float) hzfreq / (float) totalfreq);
        return (int) (rangeval + freqval);
    }

    private static boolean between(byte b) {
        return (byte) 0xA1 <= b && (byte) 0xF7 >= b;
    }

    /**
     * Function: big5_probability Argument: byte array Returns : number from 0 to 100 representing probability text
     * in array uses Big5 encoding
     */
    private static int big5_probability(byte[] rawtext) {
        int dbchars = 1, bfchars = 1;
        long bffreq = 0, totalfreq = 1;
        // Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xF9
                    && (((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E)
                    || ((byte) 0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE))) {
                    bfchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0xA1;
                    if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
                        column = rawtext[i + 1] - 0x40;
                    } else {
                        column = rawtext[i + 1] + 256 - 0x61;
                    }
                    if (BIG5_FREQ[row][column] != 0) {
                        bffreq += BIG5_FREQ[row][column];
                    } else if (3 <= row && row <= 37) {
                        bffreq += 200;
                    }
                }
                i++;
            }
        }
        float rangeval = 50 * ((float) bfchars / (float) dbchars);
        float freqval = 50 * ((float) bffreq / (float) totalfreq);
        return (int) (rangeval + freqval);
    }

    /*
     * Function: euc_tw_probability Argument: byte array Returns : number from 0 to 100 representing probability
     * text in array uses EUC-TW (CNS 11643) encoding
     */
    private static int euc_tw_probability(byte[] rawtext) {
        int dbchars = 1, cnschars = 1;
        long cnsfreq = 0, totalfreq = 1;
        // Check to see if characters fit into acceptable ranges
        // and have expected frequency of use
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            if (rawtext[i] >= 0) { // in ASCII range
                // asciichars++;
            } else { // high bit set
                dbchars++;
                if (i + 3 < rawtext.length && (byte) 0x8E == rawtext[i] && (byte) 0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xB0
                    && (byte) 0xA1 <= rawtext[i + 2] && rawtext[i + 2] <= (byte) 0xFE && (byte) 0xA1 <= rawtext[i + 3]
                    && rawtext[i + 3] <= (byte) 0xFE) { // Planes 1 - 16
                    cnschars++;
                    // System.out.println("plane 2 or above CNS char");
                    // These are all less frequent chars so just ignore freq
                    i += 3;
                } else if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xFE && // Plane 1
                    (byte) 0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE) {
                    cnschars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0xA1;
                    column = rawtext[i + 1] + 256 - 0xA1;
                    if (EUC_TW_FREQ[row][column] != 0) {
                        cnsfreq += EUC_TW_FREQ[row][column];
                    } else if (35 <= row && row <= 92) {
                        cnsfreq += 150;
                    }
                    i++;
                }
            }
        }
        float rangeval = 50 * ((float) cnschars / (float) dbchars);
        float freqval = 50 * ((float) cnsfreq / (float) totalfreq);
        return (int) (rangeval + freqval);
    }

    /*
     * Function: iso_2022_cn_probability Argument: byte array Returns : number from 0 to 100 representing
     * probability text in array uses ISO 2022-CN encoding WORKS FOR BASIC CASES, BUT STILL NEEDS MORE WORK
     */
    private static int iso_2022_cn_probability(byte[] rawtext) {
        int dbchars = 1, isochars = 1;
        long isofreq = 0, totalfreq = 1;
        // Check to see if characters fit into acceptable ranges
        // and have expected frequency of use
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            if (rawtext[i] == (byte) 0x1B && i + 3 < rawtext.length) { // Escape
                // char ESC
                if (rawtext[i + 1] == (byte) 0x24 && rawtext[i + 2] == 0x29 && rawtext[i + 3] == (byte) 0x41) { // GB
                    // Escape
                    // $
                    // )
                    // A
                    i += 4;
                    while (rawtext[i] != (byte) 0x1B) {
                        dbchars++;
                        if ((0x21 <= rawtext[i] && rawtext[i] <= 0x77) && (0x21 <= rawtext[i + 1] && rawtext[i + 1] <= 0x77)) {
                            isochars++;
                            row = rawtext[i] - 0x21;
                            column = rawtext[i + 1] - 0x21;
                            totalfreq += 500;
                            if (GB_FREQ[row][column] != 0) {
                                isofreq += GB_FREQ[row][column];
                            } else if (15 <= row && row < 55) {
                                isofreq += 200;
                            }
                            i++;
                        }
                        i++;
                    }
                } else if (i + 3 < rawtext.length && rawtext[i + 1] == (byte) 0x24 && rawtext[i + 2] == (byte) 0x29
                    && rawtext[i + 3] == (byte) 0x47) {
                    // CNS Escape $ ) G
                    i += 4;
                    while (rawtext[i] != (byte) 0x1B) {
                        dbchars++;
                        if ((byte) 0x21 <= rawtext[i] && rawtext[i] <= (byte) 0x7E && (byte) 0x21 <= rawtext[i + 1]
                            && rawtext[i + 1] <= (byte) 0x7E) {
                            isochars++;
                            totalfreq += 500;
                            row = rawtext[i] - 0x21;
                            column = rawtext[i + 1] - 0x21;
                            if (EUC_TW_FREQ[row][column] != 0) {
                                isofreq += EUC_TW_FREQ[row][column];
                            } else if (35 <= row && row <= 92) {
                                isofreq += 150;
                            }
                            i++;
                        }
                        i++;
                    }
                }
                if (rawtext[i] == (byte) 0x1B && i + 2 < rawtext.length && rawtext[i + 1] == (byte) 0x28 && rawtext[i + 2] == (byte) 0x42) { // ASCII:
                    // ESC
                    // ( B
                    i += 2;
                }
            }
        }
        float rangeval = 50 * ((float) isochars / (float) dbchars);
        float freqval = 50 * ((float) isofreq / (float) totalfreq);
        // System.out.println("isochars dbchars isofreq totalfreq " + isochars +
        // " " + dbchars + " " + isofreq + " " + totalfreq + "
        // " + rangeval + " " + freqval);
        return (int) (rangeval + freqval);
        // return 0;
    }

    /*
     * Function: utf8_probability Argument: byte array Returns : number from 0 to 100 representing probability text
     * in array uses UTF-8 encoding of Unicode
     */
    private static int utf8_probability(byte[] rawtext) {
        int goodbytes = 0, asciibytes = 0;
        // Maybe also use UTF8 Byte Order Mark: EF BB BF
        // Check to see if characters fit into acceptable ranges
        for (int i = 0; i < rawtext.length; i++) {
            if ((rawtext[i] & (byte) 0x7F) == rawtext[i]) { // One byte
                asciibytes++;
                // Ignore ASCII, can throw off count
            } else if (-64 <= rawtext[i] && rawtext[i] <= -33 && // Two bytes
                i + 1 < rawtext.length && -128 <= rawtext[i + 1] && rawtext[i + 1] <= -65) {
                goodbytes += 2;
                i++;
            } else if (-32 <= rawtext[i] && rawtext[i] <= -17 && // Three bytes
                i + 2 < rawtext.length && -128 <= rawtext[i + 1] && rawtext[i + 1] <= -65 && -128 <= rawtext[i + 2]
                && rawtext[i + 2] <= -65) {
                goodbytes += 3;
                i += 2;
            }
        }
        if (asciibytes == rawtext.length) {
            return 0;
        }

        int score = (int) (100 * ((float) goodbytes / (float) (rawtext.length - asciibytes)));
        // System.out.println("rawtextlen " + rawtextlen + " goodbytes " +
        // goodbytes + " asciibytes " + asciibytes + " score " +
        // score);
        // If not above 98, reduce to zero to prevent coincidental matches
        // Allows for some (few) bad formed sequences
        if (score > 98) {
            return score;
        } else if (score > 95 && goodbytes > 30) {
            return score;
        } else {
            return 0;
        }
    }

    /*
     * Function: utf16_probability Argument: byte array Returns : number from 0 to 100 representing probability text
     * in array uses UTF-16 encoding of Unicode, guess based on BOM // NOT VERY GENERAL, NEEDS MUCH MORE WORK
     */
    private static int utf16_probability(byte[] rawtext) {
        // int score = 0;
        // int i, rawtextlen = 0;
        // int goodbytes = 0, asciibytes = 0;
        if (rawtext.length > 1 && ((byte) 0xFE == rawtext[0] && (byte) 0xFF == rawtext[1]) || // Big-endian
            ((byte) 0xFF == rawtext[0] && (byte) 0xFE == rawtext[1])) { // Little-endian
            return 100;
        }
        return 0;
        /*
         * // Check to see if characters fit into acceptable ranges rawtextlen = rawtext.length; for (i = 0; i <
         * rawtextlen; i++) { if ((rawtext[i] & (byte)0x7F) == rawtext[i]) { // One byte goodbytes += 1;
         * asciibytes++; } else if ((rawtext[i] & (byte)0xDF) == rawtext[i]) { // Two bytes if (i+1 < rawtextlen &&
         * (rawtext[i+1] & (byte)0xBF) == rawtext[i+1]) { goodbytes += 2; i++; } } else if ((rawtext[i] &
         * (byte)0xEF) == rawtext[i]) { // Three bytes if (i+2 < rawtextlen && (rawtext[i+1] & (byte)0xBF) ==
         * rawtext[i+1] && (rawtext[i+2] & (byte)0xBF) == rawtext[i+2]) { goodbytes += 3; i+=2; } } }
         *
         * score = (int)(100 * ((float)goodbytes/(float)rawtext.length)); // An all ASCII file is also a good UTF8
         * file, but I'd rather it // get identified as ASCII. Can delete following 3 lines otherwise if (goodbytes
         * == asciibytes) { score = 0; } // If not above 90, reduce to zero to prevent coincidental matches if
         * (score > 90) { return score; } else { return 0; }
         */
    }

    /*
     * Function: ascii_probability Argument: byte array Returns : number from 0 to 100 representing probability text
     * in array uses all ASCII Description: Sees if array has any characters not in ASCII range, if so, score is
     * reduced
     */
    private static int ascii_probability(byte[] rawtext) {
        int score = 75;
        for (int i = 0; i < rawtext.length; i++) {
            if (rawtext[i] < 0) {
                score = score - 5;
            } else if (rawtext[i] == (byte) 0x1B) { // ESC (used by ISO 2022)
                score = score - 5;
            }
            if (score <= 0) {
                return 0;
            }
        }
        return score;
    }

    /*
     * Function: euc_kr__probability Argument: pointer to byte array Returns : number from 0 to 100 representing
     * probability text in array uses EUC-KR encoding
     */
    private static int euc_kr_probability(byte[] rawtext) {
        int dbchars = 1, krchars = 1;
        long krfreq = 0, totalfreq = 1;
        // Stage 1: Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            // System.err.println(rawtext[i]);
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xFE && (byte) 0xA1 <= rawtext[i + 1]
                    && rawtext[i + 1] <= (byte) 0xFE) {
                    krchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0xA1;
                    column = rawtext[i + 1] + 256 - 0xA1;
                    if (KR_FREQ[row][column] != 0) {
                        krfreq += KR_FREQ[row][column];
                    } else if (15 <= row && row < 55) {
                        krfreq += 0;
                    }
                }
                i++;
            }
        }
        float rangeval = 50 * ((float) krchars / (float) dbchars);
        float freqval = 50 * ((float) krfreq / (float) totalfreq);
        return (int) (rangeval + freqval);
    }

    /*
     * Function: cp949__probability Argument: pointer to byte array Returns : number from 0 to 100 representing
     * probability text in array uses Cp949 encoding
     */
    private static int cp949_probability(byte[] rawtext) {
        int dbchars = 1, krchars = 1;
        long krfreq = 0, totalfreq = 1;
        // Stage 1: Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            // System.err.println(rawtext[i]);
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if ((byte) 0x81 <= rawtext[i] && rawtext[i] <= (byte) 0xFE
                    && ((byte) 0x41 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x5A
                    || (byte) 0x61 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7A
                    || (byte) 0x81 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE)) {
                    krchars++;
                    totalfreq += 500;
                    if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xFE && (byte) 0xA1 <= rawtext[i + 1]
                        && rawtext[i + 1] <= (byte) 0xFE) {
                        row = rawtext[i] + 256 - 0xA1;
                        column = rawtext[i + 1] + 256 - 0xA1;
                        if (KR_FREQ[row][column] != 0) {
                            krfreq += KR_FREQ[row][column];
                        }
                    }
                }
                i++;
            }
        }
        float rangeval = 50 * ((float) krchars / (float) dbchars);
        float freqval = 50 * ((float) krfreq / (float) totalfreq);
        return (int) (rangeval + freqval);
    }

    private static int iso_2022_kr_probability(byte[] rawtext) {
        for (int i = 0; i < rawtext.length; i++) {
            if (i + 3 < rawtext.length && rawtext[i] == 0x1b && (char) rawtext[i + 1] == '$' && (char) rawtext[i + 2] == ')'
                && (char) rawtext[i + 3] == 'C') {
                return 100;
            }
        }
        return 0;
    }

    /*
     * Function: euc_jp_probability Argument: pointer to byte array Returns : number from 0 to 100 representing
     * probability text in array uses EUC-JP encoding
     */
    private static int euc_jp_probability(byte[] rawtext) {
        int dbchars = 1, jpchars = 1;
        long jpfreq = 0, totalfreq = 1;
        // Stage 1: Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, n = rawtext.length - 1; i < n; i++) {
            // System.err.println(rawtext[i]);
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xFE && (byte) 0xA1 <= rawtext[i + 1]
                    && rawtext[i + 1] <= (byte) 0xFE) {
                    jpchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256 - 0xA1;
                    column = rawtext[i + 1] + 256 - 0xA1;
                    if (JP_FREQ[row][column] != 0) {
                        jpfreq += JP_FREQ[row][column];
                    } else if (15 <= row && row < 55) {
                        jpfreq += 0;
                    }
                }
                i++;
            }
        }
        float rangeval = 50 * ((float) jpchars / (float) dbchars);
        float freqval = 50 * ((float) jpfreq / (float) totalfreq);
        return (int) (rangeval + freqval);
    }

    private static int iso_2022_jp_probability(byte[] rawtext) {
        for (int i = 0; i < rawtext.length; i++) {
            if (i + 2 < rawtext.length && rawtext[i] == 0x1b && (char) rawtext[i + 1] == '$' && (char) rawtext[i + 2] == 'B') {
                return 100;
            }
        }
        return 0;
    }

    /*
     * Function: sjis_probability Argument: pointer to byte array Returns : number from 0 to 100 representing
     * probability text in array uses Shift-JIS encoding
     */
    private static int sjis_probability(byte[] rawtext) {
        int dbchars = 1, jpchars = 1;
        long jpfreq = 0, totalfreq = 1;
        // Stage 1: Check to see if characters fit into acceptable ranges
        for (int i = 0, row, column, adjust, n = rawtext.length - 1; i < n; i++) {
            // System.err.println(rawtext[i]);
            if (rawtext[i] >= 0) {
                // asciichars++;
            } else {
                dbchars++;
                if (i + 1 < rawtext.length
                    && (((byte) 0x81 <= rawtext[i] && rawtext[i] <= (byte) 0x9F)
                    || ((byte) 0xE0 <= rawtext[i] && rawtext[i] <= (byte) 0xEF))
                    && (((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E)
                    || ((byte) 0x80 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFC))) {
                    jpchars++;
                    totalfreq += 500;
                    row = rawtext[i] + 256;
                    column = rawtext[i + 1] + 256;
                    if (column < 0x9f) {
                        adjust = 1;
                        if (column > 0x7f) {
                            column -= 0x20;
                        } else {
                            column -= 0x19;
                        }
                    } else {
                        adjust = 0;
                        column -= 0x7e;
                    }
                    if (row < 0xa0) {
                        row = ((row - 0x70) << 1) - adjust;
                    } else {
                        row = ((row - 0xb0) << 1) - adjust;
                    }
                    row -= 0x20;
                    column = 0x20;
                    // System.out.println("original row " + row + " column " +
                    // column);
                    if (row < JP_FREQ.length && column < JP_FREQ[row].length && JP_FREQ[row][column] != 0) {
                        jpfreq += JP_FREQ[row][column];
                    }
                    i++;
                } else if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xDF) {
                    // half-width katakana, convert to full-width
                }
            }
        }
        float rangeval = 50 * ((float) jpchars / (float) dbchars);
        float freqval = 50 * ((float) jpfreq / (float) totalfreq);
        // For regular GB files, this would give the same score, so I handicap
        // it slightly
        return (int) (rangeval + freqval) - 1;
    }

    // ------------------------------------------------------------------------------------private static fields

    private static final int[][] GB_FREQ     = new int[ 94][ 94];
    private static final int[][] GBK_FREQ    = new int[126][191];
    private static final int[][] BIG5_FREQ   = new int[ 94][158];
    private static final int[][] BIG5P_FREQ  = new int[126][191];
    private static final int[][] EUC_TW_FREQ = new int[ 94][ 94];
    private static final int[][] KR_FREQ     = new int[ 94][ 94];
    private static final int[][] JP_FREQ     = new int[ 94][ 94];
    static {

        // ------------------------------------------------------------------------------------GB_FREQ

        GB_FREQ[20][35] = 599; GB_FREQ[49][26] = 598; GB_FREQ[41][38] = 597; GB_FREQ[17][26] = 596; GB_FREQ[32][42] = 595;
        GB_FREQ[39][42] = 594; GB_FREQ[45][49] = 593; GB_FREQ[51][57] = 592; GB_FREQ[50][47] = 591; GB_FREQ[42][90] = 590;
        GB_FREQ[52][65] = 589; GB_FREQ[53][47] = 588; GB_FREQ[19][82] = 587; GB_FREQ[31][19] = 586; GB_FREQ[40][46] = 585;
        GB_FREQ[24][89] = 584; GB_FREQ[23][85] = 583; GB_FREQ[20][28] = 582; GB_FREQ[42][20] = 581; GB_FREQ[34][38] = 580;
        GB_FREQ[45][9]  = 579; GB_FREQ[54][50] = 578; GB_FREQ[25][44] = 577; GB_FREQ[35][66] = 576; GB_FREQ[20][55] = 575;
        GB_FREQ[18][85] = 574; GB_FREQ[20][31] = 573; GB_FREQ[49][17] = 572; GB_FREQ[41][16] = 571; GB_FREQ[35][73] = 570;
        GB_FREQ[20][34] = 569; GB_FREQ[29][44] = 568; GB_FREQ[35][38] = 567; GB_FREQ[49][9]  = 566; GB_FREQ[46][33] = 565;
        GB_FREQ[49][51] = 564; GB_FREQ[40][89] = 563; GB_FREQ[26][64] = 562; GB_FREQ[54][51] = 561; GB_FREQ[54][36] = 560;
        GB_FREQ[39][4]  = 559; GB_FREQ[53][13] = 558; GB_FREQ[24][92] = 557; GB_FREQ[27][49] = 556; GB_FREQ[48][6]  = 555;
        GB_FREQ[21][51] = 554; GB_FREQ[30][40] = 553; GB_FREQ[42][92] = 552; GB_FREQ[31][78] = 551; GB_FREQ[25][82] = 550;
        GB_FREQ[47][0]  = 549; GB_FREQ[34][19] = 548; GB_FREQ[47][35] = 547; GB_FREQ[21][63] = 546; GB_FREQ[43][75] = 545;
        GB_FREQ[21][87] = 544; GB_FREQ[35][59] = 543; GB_FREQ[25][34] = 542; GB_FREQ[21][27] = 541; GB_FREQ[39][26] = 540;
        GB_FREQ[34][26] = 539; GB_FREQ[39][52] = 538; GB_FREQ[50][57] = 537; GB_FREQ[37][79] = 536; GB_FREQ[26][24] = 535;
        GB_FREQ[22][1]  = 534; GB_FREQ[18][40] = 533; GB_FREQ[41][33] = 532; GB_FREQ[53][26] = 531; GB_FREQ[54][86] = 530;
        GB_FREQ[20][16] = 529; GB_FREQ[46][74] = 528; GB_FREQ[30][19] = 527; GB_FREQ[45][35] = 526; GB_FREQ[45][61] = 525;
        GB_FREQ[30][9]  = 524; GB_FREQ[41][53] = 523; GB_FREQ[41][13] = 522; GB_FREQ[50][34] = 521; GB_FREQ[53][86] = 520;
        GB_FREQ[47][47] = 519; GB_FREQ[22][28] = 518; GB_FREQ[50][53] = 517; GB_FREQ[39][70] = 516; GB_FREQ[38][15] = 515;
        GB_FREQ[42][88] = 514; GB_FREQ[16][29] = 513; GB_FREQ[27][90] = 512; GB_FREQ[29][12] = 511; GB_FREQ[44][22] = 510;
        GB_FREQ[34][69] = 509; GB_FREQ[24][10] = 508; GB_FREQ[44][11] = 507; GB_FREQ[39][92] = 506; GB_FREQ[49][48] = 505;
        GB_FREQ[31][46] = 504; GB_FREQ[19][50] = 503; GB_FREQ[21][14] = 502; GB_FREQ[32][28] = 501; GB_FREQ[18][3]  = 500;
        GB_FREQ[53][9]  = 499; GB_FREQ[34][80] = 498; GB_FREQ[48][88] = 497; GB_FREQ[46][53] = 496; GB_FREQ[22][53] = 495;
        GB_FREQ[28][10] = 494; GB_FREQ[44][65] = 493; GB_FREQ[20][10] = 492; GB_FREQ[40][76] = 491; GB_FREQ[47][8]  = 490;
        GB_FREQ[50][74] = 489; GB_FREQ[23][62] = 488; GB_FREQ[49][65] = 487; GB_FREQ[28][87] = 486; GB_FREQ[15][48] = 485;
        GB_FREQ[22][7]  = 484; GB_FREQ[19][42] = 483; GB_FREQ[41][20] = 482; GB_FREQ[26][55] = 481; GB_FREQ[21][93] = 480;
        GB_FREQ[31][76] = 479; GB_FREQ[34][31] = 478; GB_FREQ[20][66] = 477; GB_FREQ[51][33] = 476; GB_FREQ[34][86] = 475;
        GB_FREQ[37][67] = 474; GB_FREQ[53][53] = 473; GB_FREQ[40][88] = 472; GB_FREQ[39][10] = 471; GB_FREQ[24][3]  = 470;
        GB_FREQ[27][25] = 469; GB_FREQ[26][15] = 468; GB_FREQ[21][88] = 467; GB_FREQ[52][62] = 466; GB_FREQ[46][81] = 465;
        GB_FREQ[38][72] = 464; GB_FREQ[17][30] = 463; GB_FREQ[52][92] = 462; GB_FREQ[34][90] = 461; GB_FREQ[21][7]  = 460;
        GB_FREQ[36][13] = 459; GB_FREQ[45][41] = 458; GB_FREQ[32][5]  = 457; GB_FREQ[26][89] = 456; GB_FREQ[23][87] = 455;
        GB_FREQ[20][39] = 454; GB_FREQ[27][23] = 453; GB_FREQ[25][59] = 452; GB_FREQ[49][20] = 451; GB_FREQ[54][77] = 450;
        GB_FREQ[27][67] = 449; GB_FREQ[47][33] = 448; GB_FREQ[41][17] = 447; GB_FREQ[19][81] = 446; GB_FREQ[16][66] = 445;
        GB_FREQ[45][26] = 444; GB_FREQ[49][81] = 443; GB_FREQ[53][55] = 442; GB_FREQ[16][26] = 441; GB_FREQ[54][62] = 440;
        GB_FREQ[20][70] = 439; GB_FREQ[42][35] = 438; GB_FREQ[20][57] = 437; GB_FREQ[34][36] = 436; GB_FREQ[46][63] = 435;
        GB_FREQ[19][45] = 434; GB_FREQ[21][10] = 433; GB_FREQ[52][93] = 432; GB_FREQ[25][2]  = 431; GB_FREQ[30][57] = 430;
        GB_FREQ[41][24] = 429; GB_FREQ[28][43] = 428; GB_FREQ[45][86] = 427; GB_FREQ[51][56] = 426; GB_FREQ[37][28] = 425;
        GB_FREQ[52][69] = 424; GB_FREQ[43][92] = 423; GB_FREQ[41][31] = 422; GB_FREQ[37][87] = 421; GB_FREQ[47][36] = 420;
        GB_FREQ[16][16] = 419; GB_FREQ[40][56] = 418; GB_FREQ[24][55] = 417; GB_FREQ[17][1]  = 416; GB_FREQ[35][57] = 415;
        GB_FREQ[27][50] = 414; GB_FREQ[26][14] = 413; GB_FREQ[50][40] = 412; GB_FREQ[39][19] = 411; GB_FREQ[19][89] = 410;
        GB_FREQ[29][91] = 409; GB_FREQ[17][89] = 408; GB_FREQ[39][74] = 407; GB_FREQ[46][39] = 406; GB_FREQ[40][28] = 405;
        GB_FREQ[45][68] = 404; GB_FREQ[43][10] = 403; GB_FREQ[42][13] = 402; GB_FREQ[44][81] = 401; GB_FREQ[41][47] = 400;
        GB_FREQ[48][58] = 399; GB_FREQ[43][68] = 398; GB_FREQ[16][79] = 397; GB_FREQ[19][5]  = 396; GB_FREQ[54][59] = 395;
        GB_FREQ[17][36] = 394; GB_FREQ[18][0]  = 393; GB_FREQ[41][5]  = 392; GB_FREQ[41][72] = 391; GB_FREQ[16][39] = 390;
        GB_FREQ[54][0]  = 389; GB_FREQ[51][16] = 388; GB_FREQ[29][36] = 387; GB_FREQ[47][5]  = 386; GB_FREQ[47][51] = 385;
        GB_FREQ[44][7]  = 384; GB_FREQ[35][30] = 383; GB_FREQ[26][9]  = 382; GB_FREQ[16][7]  = 381; GB_FREQ[32][1]  = 380;
        GB_FREQ[33][76] = 379; GB_FREQ[34][91] = 378; GB_FREQ[52][36] = 377; GB_FREQ[26][77] = 376; GB_FREQ[35][48] = 375;
        GB_FREQ[40][80] = 374; GB_FREQ[41][92] = 373; GB_FREQ[27][93] = 372; GB_FREQ[15][17] = 371; GB_FREQ[16][76] = 370;
        GB_FREQ[51][12] = 369; GB_FREQ[18][20] = 368; GB_FREQ[15][54] = 367; GB_FREQ[50][5]  = 366; GB_FREQ[33][22] = 365;
        GB_FREQ[37][57] = 364; GB_FREQ[28][47] = 363; GB_FREQ[42][31] = 362; GB_FREQ[18][2]  = 361; GB_FREQ[43][64] = 360;
        GB_FREQ[23][47] = 359; GB_FREQ[28][79] = 358; GB_FREQ[25][45] = 357; GB_FREQ[23][91] = 356; GB_FREQ[22][19] = 355;
        GB_FREQ[25][46] = 354; GB_FREQ[22][36] = 353; GB_FREQ[54][85] = 352; GB_FREQ[46][20] = 351; GB_FREQ[27][37] = 350;
        GB_FREQ[26][81] = 349; GB_FREQ[42][29] = 348; GB_FREQ[31][90] = 347; GB_FREQ[41][59] = 346; GB_FREQ[24][65] = 345;
        GB_FREQ[44][84] = 344; GB_FREQ[24][90] = 343; GB_FREQ[38][54] = 342; GB_FREQ[28][70] = 341; GB_FREQ[27][15] = 340;
        GB_FREQ[28][80] = 339; GB_FREQ[29][8]  = 338; GB_FREQ[45][80] = 337; GB_FREQ[53][37] = 336; GB_FREQ[28][65] = 335;
        GB_FREQ[23][86] = 334; GB_FREQ[39][45] = 333; GB_FREQ[53][32] = 332; GB_FREQ[38][68] = 331; GB_FREQ[45][78] = 330;
        GB_FREQ[43][7]  = 329; GB_FREQ[46][82] = 328; GB_FREQ[27][38] = 327; GB_FREQ[16][62] = 326; GB_FREQ[24][17] = 325;
        GB_FREQ[22][70] = 324; GB_FREQ[52][28] = 323; GB_FREQ[23][40] = 322; GB_FREQ[28][50] = 321; GB_FREQ[42][91] = 320;
        GB_FREQ[47][76] = 319; GB_FREQ[15][42] = 318; GB_FREQ[43][55] = 317; GB_FREQ[29][84] = 316; GB_FREQ[44][90] = 315;
        GB_FREQ[53][16] = 314; GB_FREQ[22][93] = 313; GB_FREQ[34][10] = 312; GB_FREQ[32][53] = 311; GB_FREQ[43][65] = 310;
        GB_FREQ[28][7]  = 309; GB_FREQ[35][46] = 308; GB_FREQ[21][39] = 307; GB_FREQ[44][18] = 306; GB_FREQ[40][10] = 305;
        GB_FREQ[54][53] = 304; GB_FREQ[38][74] = 303; GB_FREQ[28][26] = 302; GB_FREQ[15][13] = 301; GB_FREQ[39][34] = 300;
        GB_FREQ[39][46] = 299; GB_FREQ[42][66] = 298; GB_FREQ[33][58] = 297; GB_FREQ[15][56] = 296; GB_FREQ[18][51] = 295;
        GB_FREQ[49][68] = 294; GB_FREQ[30][37] = 293; GB_FREQ[51][84] = 292; GB_FREQ[51][9]  = 291; GB_FREQ[40][70] = 290;
        GB_FREQ[41][84] = 289; GB_FREQ[28][64] = 288; GB_FREQ[32][88] = 287; GB_FREQ[24][5]  = 286; GB_FREQ[53][23] = 285;
        GB_FREQ[42][27] = 284; GB_FREQ[22][38] = 283; GB_FREQ[32][86] = 282; GB_FREQ[34][30] = 281; GB_FREQ[38][63] = 280;
        GB_FREQ[24][59] = 279; GB_FREQ[22][81] = 278; GB_FREQ[32][11] = 277; GB_FREQ[51][21] = 276; GB_FREQ[54][41] = 275;
        GB_FREQ[21][50] = 274; GB_FREQ[23][89] = 273; GB_FREQ[19][87] = 272; GB_FREQ[26][7]  = 271; GB_FREQ[30][75] = 270;
        GB_FREQ[43][84] = 269; GB_FREQ[51][25] = 268; GB_FREQ[16][67] = 267; GB_FREQ[32][9]  = 266; GB_FREQ[48][51] = 265;
        GB_FREQ[39][7]  = 264; GB_FREQ[44][88] = 263; GB_FREQ[52][24] = 262; GB_FREQ[23][34] = 261; GB_FREQ[32][75] = 260;
        GB_FREQ[19][10] = 259; GB_FREQ[28][91] = 258; GB_FREQ[32][83] = 257; GB_FREQ[25][75] = 256; GB_FREQ[53][45] = 255;
        GB_FREQ[29][85] = 254; GB_FREQ[53][59] = 253; GB_FREQ[16][2]  = 252; GB_FREQ[19][78] = 251; GB_FREQ[15][75] = 250;
        GB_FREQ[51][42] = 249; GB_FREQ[45][67] = 248; GB_FREQ[15][74] = 247; GB_FREQ[25][81] = 246; GB_FREQ[37][62] = 245;
        GB_FREQ[16][55] = 244; GB_FREQ[18][38] = 243; GB_FREQ[23][23] = 242; GB_FREQ[38][30] = 241; GB_FREQ[17][28] = 240;
        GB_FREQ[44][73] = 239; GB_FREQ[23][78] = 238; GB_FREQ[40][77] = 237; GB_FREQ[38][87] = 236; GB_FREQ[27][19] = 235;
        GB_FREQ[38][82] = 234; GB_FREQ[37][22] = 233; GB_FREQ[41][30] = 232; GB_FREQ[54][9]  = 231; GB_FREQ[32][30] = 230;
        GB_FREQ[30][52] = 229; GB_FREQ[40][84] = 228; GB_FREQ[53][57] = 227; GB_FREQ[27][27] = 226; GB_FREQ[38][64] = 225;
        GB_FREQ[18][43] = 224; GB_FREQ[23][69] = 223; GB_FREQ[28][12] = 222; GB_FREQ[50][78] = 221; GB_FREQ[50][1]  = 220;
        GB_FREQ[26][88] = 219; GB_FREQ[36][40] = 218; GB_FREQ[33][89] = 217; GB_FREQ[41][28] = 216; GB_FREQ[31][77] = 215;
        GB_FREQ[46][1]  = 214; GB_FREQ[47][19] = 213; GB_FREQ[35][55] = 212; GB_FREQ[41][21] = 211; GB_FREQ[27][10] = 210;
        GB_FREQ[32][77] = 209; GB_FREQ[26][37] = 208; GB_FREQ[20][33] = 207; GB_FREQ[41][52] = 206; GB_FREQ[32][18] = 205;
        GB_FREQ[38][13] = 204; GB_FREQ[20][18] = 203; GB_FREQ[20][24] = 202; GB_FREQ[45][19] = 201; GB_FREQ[18][53] = 200;
        /*
        GBFreq[39][0]  = 199; GBFreq[40][71] = 198; GBFreq[41][27] = 197; GBFreq[15][69] = 196; GBFreq[42][10] = 195;
        GBFreq[31][89] = 194; GBFreq[51][28] = 193; GBFreq[41][22] = 192; GBFreq[40][43] = 191; GBFreq[38][6]  = 190;
        GBFreq[37][11] = 189; GBFreq[39][60] = 188; GBFreq[48][47] = 187; GBFreq[46][80] = 186; GBFreq[52][49] = 185;
        GBFreq[50][48] = 184; GBFreq[25][1]  = 183; GBFreq[52][29] = 182; GBFreq[24][66] = 181; GBFreq[23][35] = 180;
        GBFreq[49][72] = 179; GBFreq[47][45] = 178; GBFreq[45][14] = 177; GBFreq[51][70] = 176; GBFreq[22][30] = 175;
        GBFreq[49][83] = 174; GBFreq[26][79] = 173; GBFreq[27][41] = 172; GBFreq[51][81] = 171; GBFreq[41][54] = 170;
        GBFreq[20][4]  = 169; GBFreq[29][60] = 168; GBFreq[20][27] = 167; GBFreq[50][15] = 166; GBFreq[41][6]  = 165;
        GBFreq[35][34] = 164; GBFreq[44][87] = 163; GBFreq[46][66] = 162; GBFreq[42][37] = 161; GBFreq[42][24] = 160;
        GBFreq[54][7]  = 159; GBFreq[41][14] = 158; GBFreq[39][83] = 157; GBFreq[16][87] = 156; GBFreq[20][59] = 155;
        GBFreq[42][12] = 154; GBFreq[47][2]  = 153; GBFreq[21][32] = 152; GBFreq[53][29] = 151; GBFreq[22][40] = 150;
        GBFreq[24][58] = 149; GBFreq[52][88] = 148; GBFreq[29][30] = 147; GBFreq[15][91] = 146; GBFreq[54][72] = 145;
        GBFreq[51][75] = 144; GBFreq[33][67] = 143; GBFreq[41][50] = 142; GBFreq[27][34] = 141; GBFreq[46][17] = 140;
        GBFreq[31][74] = 139; GBFreq[42][67] = 138; GBFreq[54][87] = 137; GBFreq[27][14] = 136; GBFreq[16][63] = 135;
        GBFreq[16][5]  = 134; GBFreq[43][23] = 133; GBFreq[23][13] = 132; GBFreq[31][12] = 131; GBFreq[25][57] = 130;
        GBFreq[38][49] = 129; GBFreq[42][69] = 128; GBFreq[23][80] = 127; GBFreq[29][0]  = 126; GBFreq[28][2]  = 125;
        GBFreq[28][17] = 124; GBFreq[17][27] = 123; GBFreq[40][16] = 122; GBFreq[45][1]  = 121; GBFreq[36][33] = 120;
        GBFreq[35][23] = 119; GBFreq[20][86] = 118; GBFreq[29][53] = 117; GBFreq[23][88] = 116; GBFreq[51][87] = 115;
        GBFreq[54][27] = 114; GBFreq[44][36] = 113; GBFreq[21][45] = 112; GBFreq[53][52] = 111; GBFreq[31][53] = 110;
        GBFreq[38][47] = 109; GBFreq[27][21] = 108; GBFreq[30][42] = 107; GBFreq[29][10] = 106; GBFreq[35][35] = 105;
        GBFreq[24][56] = 104; GBFreq[41][29] = 103; GBFreq[18][68] = 102; GBFreq[29][24] = 101; GBFreq[25][84] = 100;
        GBFreq[35][47] =  99; GBFreq[29][56] =  98; GBFreq[30][44] =  97; GBFreq[53][3]  =  96; GBFreq[30][63] =  95;
        GBFreq[52][52] =  94; GBFreq[54][1]  =  93; GBFreq[22][48] =  92; GBFreq[54][66] =  91; GBFreq[21][90] =  90;
        GBFreq[52][47] =  89; GBFreq[39][25] =  88; GBFreq[39][39] =  87; GBFreq[44][37] =  86; GBFreq[44][76] =  85;
        GBFreq[46][75] =  84; GBFreq[18][37] =  83; GBFreq[47][42] =  82; GBFreq[19][92] =  81; GBFreq[51][27] =  80;
        GBFreq[48][83] =  79; GBFreq[23][70] =  78; GBFreq[29][9]  =  77; GBFreq[33][79] =  76; GBFreq[52][90] =  75;
        GBFreq[53][6]  =  74; GBFreq[24][36] =  73; GBFreq[25][25] =  72; GBFreq[44][26] =  71; GBFreq[25][36] =  70;
        GBFreq[29][87] =  69; GBFreq[48][0]  =  68; GBFreq[15][40] =  67; GBFreq[17][45] =  66; GBFreq[30][14] =  65;
        GBFreq[48][38] =  64; GBFreq[23][19] =  63; GBFreq[40][42] =  62; GBFreq[31][63] =  61; GBFreq[16][23] =  60;
        GBFreq[26][21] =  59; GBFreq[32][76] =  58; GBFreq[23][58] =  57; GBFreq[41][37] =  56; GBFreq[30][43] =  55;
        GBFreq[47][38] =  54; GBFreq[21][46] =  53; GBFreq[18][33] =  52; GBFreq[52][37] =  51; GBFreq[36][8]  =  50;
        GBFreq[49][24] =  49; GBFreq[15][66] =  48; GBFreq[35][77] =  47; GBFreq[27][58] =  46; GBFreq[35][51] =  45;
        GBFreq[24][69] =  44; GBFreq[20][54] =  43; GBFreq[24][41] =  42; GBFreq[41][0]  =  41; GBFreq[33][71] =  40;
        GBFreq[23][52] =  39; GBFreq[29][67] =  38; GBFreq[46][51] =  37; GBFreq[46][90] =  36; GBFreq[49][33] =  35;
        GBFreq[33][28] =  34; GBFreq[37][86] =  33; GBFreq[39][22] =  32; GBFreq[37][37] =  31; GBFreq[29][62] =  30;
        GBFreq[29][50] =  29; GBFreq[36][89] =  28; GBFreq[42][44] =  27; GBFreq[51][82] =  26; GBFreq[28][83] =  25;
        GBFreq[15][78] =  24; GBFreq[46][62] =  23; GBFreq[19][69] =  22; GBFreq[51][23] =  21; GBFreq[37][69] =  20;
        GBFreq[25][5]  =  19; GBFreq[51][85] =  18; GBFreq[48][77] =  17; GBFreq[32][46] =  16; GBFreq[53][60] =  15;
        GBFreq[28][57] =  14; GBFreq[54][82] =  13; GBFreq[54][15] =  12; GBFreq[49][54] =  11; GBFreq[53][87] =  10;
        GBFreq[27][16] =   9; GBFreq[29][34] =   8; GBFreq[20][44] =   7; GBFreq[42][73] =   6; GBFreq[47][71] =   5;
        GBFreq[29][37] =   4; GBFreq[25][50] =   3; GBFreq[18][84] =   2; GBFreq[50][45] =   1; GBFreq[48][46] =   0;
        GBFreq[43][89] =  -1; GBFreq[54][68] =  -2;
        */

        // ------------------------------------------------------------------------------------BIG5_FREQ

        BIG5_FREQ[9][89]    = 600; BIG5_FREQ[11][15]   = 599; BIG5_FREQ[3][66]    = 598; BIG5_FREQ[6][121]   = 597; BIG5_FREQ[3][0]     = 596;
        BIG5_FREQ[5][82]    = 595; BIG5_FREQ[3][42]    = 594; BIG5_FREQ[5][34]    = 593; BIG5_FREQ[3][8]     = 592; BIG5_FREQ[3][6]     = 591;
        BIG5_FREQ[3][67]    = 590; BIG5_FREQ[7][139]   = 589; BIG5_FREQ[23][137]  = 588; BIG5_FREQ[12][46]   = 587; BIG5_FREQ[4][8]     = 586;
        BIG5_FREQ[4][41]    = 585; BIG5_FREQ[18][47]   = 584; BIG5_FREQ[12][114]  = 583; BIG5_FREQ[6][1]     = 582; BIG5_FREQ[22][60]   = 581;
        BIG5_FREQ[5][46]    = 580; BIG5_FREQ[11][79]   = 579; BIG5_FREQ[3][23]    = 578; BIG5_FREQ[7][114]   = 577; BIG5_FREQ[29][102]  = 576;
        BIG5_FREQ[19][14]   = 575; BIG5_FREQ[4][133]   = 574; BIG5_FREQ[3][29]    = 573; BIG5_FREQ[4][109]   = 572; BIG5_FREQ[14][127]  = 571;
        BIG5_FREQ[5][48]    = 570; BIG5_FREQ[13][104]  = 569; BIG5_FREQ[3][132]   = 568; BIG5_FREQ[26][64]   = 567; BIG5_FREQ[7][19]    = 566;
        BIG5_FREQ[4][12]    = 565; BIG5_FREQ[11][124]  = 564; BIG5_FREQ[7][89]    = 563; BIG5_FREQ[15][124]  = 562; BIG5_FREQ[4][108]   = 561;
        BIG5_FREQ[19][66]   = 560; BIG5_FREQ[3][21]    = 559; BIG5_FREQ[24][12]   = 558; BIG5_FREQ[28][111]  = 557; BIG5_FREQ[12][107]  = 556;
        BIG5_FREQ[3][112]   = 555; BIG5_FREQ[8][113]   = 554; BIG5_FREQ[5][40]    = 553; BIG5_FREQ[26][145]  = 552; BIG5_FREQ[3][48]    = 551;
        BIG5_FREQ[3][70]    = 550; BIG5_FREQ[22][17]   = 549; BIG5_FREQ[16][47]   = 548; BIG5_FREQ[3][53]    = 547; BIG5_FREQ[4][24]    = 546;
        BIG5_FREQ[32][120]  = 545; BIG5_FREQ[24][49]   = 544; BIG5_FREQ[24][142]  = 543; BIG5_FREQ[18][66]   = 542; BIG5_FREQ[29][150]  = 541;
        BIG5_FREQ[5][122]   = 540; BIG5_FREQ[5][114]   = 539; BIG5_FREQ[3][44]    = 538; BIG5_FREQ[10][128]  = 537; BIG5_FREQ[15][20]   = 536;
        BIG5_FREQ[13][33]   = 535; BIG5_FREQ[14][87]   = 534; BIG5_FREQ[3][126]   = 533; BIG5_FREQ[4][53]    = 532; BIG5_FREQ[4][40]    = 531;
        BIG5_FREQ[9][93]    = 530; BIG5_FREQ[15][137]  = 529; BIG5_FREQ[10][123]  = 528; BIG5_FREQ[4][56]    = 527; BIG5_FREQ[5][71]    = 526;
        BIG5_FREQ[10][8]    = 525; BIG5_FREQ[5][16]    = 524; BIG5_FREQ[5][146]   = 523; BIG5_FREQ[18][88]   = 522; BIG5_FREQ[24][4]    = 521;
        BIG5_FREQ[20][47]   = 520; BIG5_FREQ[5][33]    = 519; BIG5_FREQ[9][43]    = 518; BIG5_FREQ[20][12]   = 517; BIG5_FREQ[20][13]   = 516;
        BIG5_FREQ[5][156]   = 515; BIG5_FREQ[22][140]  = 514; BIG5_FREQ[8][146]   = 513; BIG5_FREQ[21][123]  = 512; BIG5_FREQ[4][90]    = 511;
        BIG5_FREQ[5][62]    = 510; BIG5_FREQ[17][59]   = 509; BIG5_FREQ[10][37]   = 508; BIG5_FREQ[18][107]  = 507; BIG5_FREQ[14][53]   = 506;
        BIG5_FREQ[22][51]   = 505; BIG5_FREQ[8][13]    = 504; BIG5_FREQ[5][29]    = 503; BIG5_FREQ[9][7]     = 502; BIG5_FREQ[22][14]   = 501;
        BIG5_FREQ[8][55]    = 500; BIG5_FREQ[33][9]    = 499; BIG5_FREQ[16][64]   = 498; BIG5_FREQ[7][131]   = 497; BIG5_FREQ[34][4]    = 496;
        BIG5_FREQ[7][101]   = 495; BIG5_FREQ[11][139]  = 494; BIG5_FREQ[3][135]   = 493; BIG5_FREQ[7][102]   = 492; BIG5_FREQ[17][13]   = 491;
        BIG5_FREQ[3][20]    = 490; BIG5_FREQ[27][106]  = 489; BIG5_FREQ[5][88]    = 488; BIG5_FREQ[6][33]    = 487; BIG5_FREQ[5][139]   = 486;
        BIG5_FREQ[6][0]     = 485; BIG5_FREQ[17][58]   = 484; BIG5_FREQ[5][133]   = 483; BIG5_FREQ[9][107]   = 482; BIG5_FREQ[23][39]   = 481;
        BIG5_FREQ[5][23]    = 480; BIG5_FREQ[3][79]    = 479; BIG5_FREQ[32][97]   = 478; BIG5_FREQ[3][136]   = 477; BIG5_FREQ[4][94]    = 476;
        BIG5_FREQ[21][61]   = 475; BIG5_FREQ[23][123]  = 474; BIG5_FREQ[26][16]   = 473; BIG5_FREQ[24][137]  = 472; BIG5_FREQ[22][18]   = 471;
        BIG5_FREQ[5][1]     = 470; BIG5_FREQ[20][119]  = 469; BIG5_FREQ[3][7]     = 468; BIG5_FREQ[10][79]   = 467; BIG5_FREQ[15][105]  = 466;
        BIG5_FREQ[3][144]   = 465; BIG5_FREQ[12][80]   = 464; BIG5_FREQ[15][73]   = 463; BIG5_FREQ[3][19]    = 462; BIG5_FREQ[8][109]   = 461;
        BIG5_FREQ[3][15]    = 460; BIG5_FREQ[31][82]   = 459; BIG5_FREQ[3][43]    = 458; BIG5_FREQ[25][119]  = 457; BIG5_FREQ[16][111]  = 456;
        BIG5_FREQ[7][77]    = 455; BIG5_FREQ[3][95]    = 454; BIG5_FREQ[24][82]   = 453; BIG5_FREQ[7][52]    = 452; BIG5_FREQ[9][151]   = 451;
        BIG5_FREQ[3][129]   = 450; BIG5_FREQ[5][87]    = 449; BIG5_FREQ[3][55]    = 448; BIG5_FREQ[8][153]   = 447; BIG5_FREQ[4][83]    = 446;
        BIG5_FREQ[3][114]   = 445; BIG5_FREQ[23][147]  = 444; BIG5_FREQ[15][31]   = 443; BIG5_FREQ[3][54]    = 442; BIG5_FREQ[11][122]  = 441;
        BIG5_FREQ[4][4]     = 440; BIG5_FREQ[34][149]  = 439; BIG5_FREQ[3][17]    = 438; BIG5_FREQ[21][64]   = 437; BIG5_FREQ[26][144]  = 436;
        BIG5_FREQ[4][62]    = 435; BIG5_FREQ[8][15]    = 434; BIG5_FREQ[35][80]   = 433; BIG5_FREQ[7][110]   = 432; BIG5_FREQ[23][114]  = 431;
        BIG5_FREQ[3][108]   = 430; BIG5_FREQ[3][62]    = 429; BIG5_FREQ[21][41]   = 428; BIG5_FREQ[15][99]   = 427; BIG5_FREQ[5][47]    = 426;
        BIG5_FREQ[4][96]    = 425; BIG5_FREQ[20][122]  = 424; BIG5_FREQ[5][21]    = 423; BIG5_FREQ[4][157]   = 422; BIG5_FREQ[16][14]   = 421;
        BIG5_FREQ[3][117]   = 420; BIG5_FREQ[7][129]   = 419; BIG5_FREQ[4][27]    = 418; BIG5_FREQ[5][30]    = 417; BIG5_FREQ[22][16]   = 416;
        BIG5_FREQ[5][64]    = 415; BIG5_FREQ[17][99]   = 414; BIG5_FREQ[17][57]   = 413; BIG5_FREQ[8][105]   = 412; BIG5_FREQ[5][112]   = 411;
        BIG5_FREQ[20][59]   = 410; BIG5_FREQ[6][129]   = 409; BIG5_FREQ[18][17]   = 408; BIG5_FREQ[3][92]    = 407; BIG5_FREQ[28][118]  = 406;
        BIG5_FREQ[3][109]   = 405; BIG5_FREQ[31][51]   = 404; BIG5_FREQ[13][116]  = 403; BIG5_FREQ[6][15]    = 402; BIG5_FREQ[36][136]  = 401;
        BIG5_FREQ[12][74]   = 400; BIG5_FREQ[20][88]   = 399; BIG5_FREQ[36][68]   = 398; BIG5_FREQ[3][147]   = 397; BIG5_FREQ[15][84]   = 396;
        BIG5_FREQ[16][32]   = 395; BIG5_FREQ[16][58]   = 394; BIG5_FREQ[7][66]    = 393; BIG5_FREQ[23][107]  = 392; BIG5_FREQ[9][6]     = 391;
        BIG5_FREQ[12][86]   = 390; BIG5_FREQ[23][112]  = 389; BIG5_FREQ[37][23]   = 388; BIG5_FREQ[3][138]   = 387; BIG5_FREQ[20][68]   = 386;
        BIG5_FREQ[15][116]  = 385; BIG5_FREQ[18][64]   = 384; BIG5_FREQ[12][139]  = 383; BIG5_FREQ[11][155]  = 382; BIG5_FREQ[4][156]   = 381;
        BIG5_FREQ[12][84]   = 380; BIG5_FREQ[18][49]   = 379; BIG5_FREQ[25][125]  = 378; BIG5_FREQ[25][147]  = 377; BIG5_FREQ[15][110]  = 376;
        BIG5_FREQ[19][96]   = 375; BIG5_FREQ[30][152]  = 374; BIG5_FREQ[6][31]    = 373; BIG5_FREQ[27][117]  = 372; BIG5_FREQ[3][10]    = 371;
        BIG5_FREQ[6][131]   = 370; BIG5_FREQ[13][112]  = 369; BIG5_FREQ[36][156]  = 368; BIG5_FREQ[4][60]    = 367; BIG5_FREQ[15][121]  = 366;
        BIG5_FREQ[4][112]   = 365; BIG5_FREQ[30][142]  = 364; BIG5_FREQ[23][154]  = 363; BIG5_FREQ[27][101]  = 362; BIG5_FREQ[9][140]   = 361;
        BIG5_FREQ[3][89]    = 360; BIG5_FREQ[18][148]  = 359; BIG5_FREQ[4][69]    = 358; BIG5_FREQ[16][49]   = 357; BIG5_FREQ[6][117]   = 356;
        BIG5_FREQ[36][55]   = 355; BIG5_FREQ[5][123]   = 354; BIG5_FREQ[4][126]   = 353; BIG5_FREQ[4][119]   = 352; BIG5_FREQ[9][95]    = 351;
        BIG5_FREQ[5][24]    = 350; BIG5_FREQ[16][133]  = 349; BIG5_FREQ[10][134]  = 348; BIG5_FREQ[26][59]   = 347; BIG5_FREQ[6][41]    = 346;
        BIG5_FREQ[6][146]   = 345; BIG5_FREQ[19][24]   = 344; BIG5_FREQ[5][113]   = 343; BIG5_FREQ[10][118]  = 342; BIG5_FREQ[34][151]  = 341;
        BIG5_FREQ[9][72]    = 340; BIG5_FREQ[31][25]   = 339; BIG5_FREQ[18][126]  = 338; BIG5_FREQ[18][28]   = 337; BIG5_FREQ[4][153]   = 336;
        BIG5_FREQ[3][84]    = 335; BIG5_FREQ[21][18]   = 334; BIG5_FREQ[25][129]  = 333; BIG5_FREQ[6][107]   = 332; BIG5_FREQ[12][25]   = 331;
        BIG5_FREQ[17][109]  = 330; BIG5_FREQ[7][76]    = 329; BIG5_FREQ[15][15]   = 328; BIG5_FREQ[4][14]    = 327; BIG5_FREQ[23][88]   = 326;
        BIG5_FREQ[18][2]    = 325; BIG5_FREQ[6][88]    = 324; BIG5_FREQ[16][84]   = 323; BIG5_FREQ[12][48]   = 322; BIG5_FREQ[7][68]    = 321;
        BIG5_FREQ[5][50]    = 320; BIG5_FREQ[13][54]   = 319; BIG5_FREQ[7][98]    = 318; BIG5_FREQ[11][6]    = 317; BIG5_FREQ[9][80]    = 316;
        BIG5_FREQ[16][41]   = 315; BIG5_FREQ[7][43]    = 314; BIG5_FREQ[28][117]  = 313; BIG5_FREQ[3][51]    = 312; BIG5_FREQ[7][3]     = 311;
        BIG5_FREQ[20][81]   = 310; BIG5_FREQ[4][2]     = 309; BIG5_FREQ[11][16]   = 308; BIG5_FREQ[10][4]    = 307; BIG5_FREQ[10][119]  = 306;
        BIG5_FREQ[6][142]   = 305; BIG5_FREQ[18][51]   = 304; BIG5_FREQ[8][144]   = 303; BIG5_FREQ[10][65]   = 302; BIG5_FREQ[11][64]   = 301;
        BIG5_FREQ[11][130]  = 300; BIG5_FREQ[9][92]    = 299; BIG5_FREQ[18][29]   = 298; BIG5_FREQ[18][78]   = 297; BIG5_FREQ[18][151]  = 296;
        BIG5_FREQ[33][127]  = 295; BIG5_FREQ[35][113]  = 294; BIG5_FREQ[10][155]  = 293; BIG5_FREQ[3][76]    = 292; BIG5_FREQ[36][123]  = 291;
        BIG5_FREQ[13][143]  = 290; BIG5_FREQ[5][135]   = 289; BIG5_FREQ[23][116]  = 288; BIG5_FREQ[6][101]   = 287; BIG5_FREQ[14][74]   = 286;
        BIG5_FREQ[7][153]   = 285; BIG5_FREQ[3][101]   = 284; BIG5_FREQ[9][74]    = 283; BIG5_FREQ[3][156]   = 282; BIG5_FREQ[4][147]   = 281;
        BIG5_FREQ[9][12]    = 280; BIG5_FREQ[18][133]  = 279; BIG5_FREQ[4][0]     = 278; BIG5_FREQ[7][155]   = 277; BIG5_FREQ[9][144]   = 276;
        BIG5_FREQ[23][49]   = 275; BIG5_FREQ[5][89]    = 274; BIG5_FREQ[10][11]   = 273; BIG5_FREQ[3][110]   = 272; BIG5_FREQ[3][40]    = 271;
        BIG5_FREQ[29][115]  = 270; BIG5_FREQ[9][100]   = 269; BIG5_FREQ[21][67]   = 268; BIG5_FREQ[23][145]  = 267; BIG5_FREQ[10][47]   = 266;
        BIG5_FREQ[4][31]    = 265; BIG5_FREQ[4][81]    = 264; BIG5_FREQ[22][62]   = 263; BIG5_FREQ[4][28]    = 262; BIG5_FREQ[27][39]   = 261;
        BIG5_FREQ[27][54]   = 260; BIG5_FREQ[32][46]   = 259; BIG5_FREQ[4][76]    = 258; BIG5_FREQ[26][15]   = 257; BIG5_FREQ[12][154]  = 256;
        BIG5_FREQ[9][150]   = 255; BIG5_FREQ[15][17]   = 254; BIG5_FREQ[5][129]   = 253; BIG5_FREQ[10][40]   = 252; BIG5_FREQ[13][37]   = 251;
        BIG5_FREQ[31][104]  = 250; BIG5_FREQ[3][152]   = 249; BIG5_FREQ[5][22]    = 248; BIG5_FREQ[8][48]    = 247; BIG5_FREQ[4][74]    = 246;
        BIG5_FREQ[6][17]    = 245; BIG5_FREQ[30][82]   = 244; BIG5_FREQ[4][116]   = 243; BIG5_FREQ[16][42]   = 242; BIG5_FREQ[5][55]    = 241;
        BIG5_FREQ[4][64]    = 240; BIG5_FREQ[14][19]   = 239; BIG5_FREQ[35][82]   = 238; BIG5_FREQ[30][139]  = 237; BIG5_FREQ[26][152]  = 236;
        BIG5_FREQ[32][32]   = 235; BIG5_FREQ[21][102]  = 234; BIG5_FREQ[10][131]  = 233; BIG5_FREQ[9][128]   = 232; BIG5_FREQ[3][87]    = 231;
        BIG5_FREQ[4][51]    = 230; BIG5_FREQ[10][15]   = 229; BIG5_FREQ[4][150]   = 228; BIG5_FREQ[7][4]     = 227; BIG5_FREQ[7][51]    = 226;
        BIG5_FREQ[7][157]   = 225; BIG5_FREQ[4][146]   = 224; BIG5_FREQ[4][91]    = 223; BIG5_FREQ[7][13]    = 222; BIG5_FREQ[17][116]  = 221;
        BIG5_FREQ[23][21]   = 220; BIG5_FREQ[5][106]   = 219; BIG5_FREQ[14][100]  = 218; BIG5_FREQ[10][152]  = 217; BIG5_FREQ[14][89]   = 216;
        BIG5_FREQ[6][138]   = 215; BIG5_FREQ[12][157]  = 214; BIG5_FREQ[10][102]  = 213; BIG5_FREQ[19][94]   = 212; BIG5_FREQ[7][74]    = 211;
        BIG5_FREQ[18][128]  = 210; BIG5_FREQ[27][111]  = 209; BIG5_FREQ[11][57]   = 208; BIG5_FREQ[3][131]   = 207; BIG5_FREQ[30][23]   = 206;
        BIG5_FREQ[30][126]  = 205; BIG5_FREQ[4][36]    = 204; BIG5_FREQ[26][124]  = 203; BIG5_FREQ[4][19]    = 202; BIG5_FREQ[9][152]   = 201;
        BIG5P_FREQ[41][122] = 600; BIG5P_FREQ[35][0]   = 599; BIG5P_FREQ[43][15]  = 598; BIG5P_FREQ[35][99]  = 597; BIG5P_FREQ[35][6]   = 596;
        BIG5P_FREQ[35][8]   = 595; BIG5P_FREQ[38][154] = 594; BIG5P_FREQ[37][34]  = 593; BIG5P_FREQ[37][115] = 592; BIG5P_FREQ[36][12]  = 591;
        BIG5P_FREQ[18][77]  = 590; BIG5P_FREQ[35][100] = 589; BIG5P_FREQ[35][42]  = 588; BIG5P_FREQ[120][75] = 587; BIG5P_FREQ[35][23]  = 586;
        BIG5P_FREQ[13][72]  = 585; BIG5P_FREQ[0][67]   = 584; BIG5P_FREQ[39][172] = 583; BIG5P_FREQ[22][182] = 582; BIG5P_FREQ[15][186] = 581;
        BIG5P_FREQ[15][165] = 580; BIG5P_FREQ[35][44]  = 579; BIG5P_FREQ[40][13]  = 578; BIG5P_FREQ[38][1]   = 577; BIG5P_FREQ[37][33]  = 576;
        BIG5P_FREQ[36][24]  = 575; BIG5P_FREQ[56][4]   = 574; BIG5P_FREQ[35][29]  = 573; BIG5P_FREQ[9][96]   = 572; BIG5P_FREQ[37][62]  = 571;
        BIG5P_FREQ[48][47]  = 570; BIG5P_FREQ[51][14]  = 569; BIG5P_FREQ[39][122] = 568; BIG5P_FREQ[44][46]  = 567; BIG5P_FREQ[35][21]  = 566;
        BIG5P_FREQ[36][8]   = 565; BIG5P_FREQ[36][141] = 564; BIG5P_FREQ[3][81]   = 563; BIG5P_FREQ[37][155] = 562; BIG5P_FREQ[42][84]  = 561;
        BIG5P_FREQ[36][40]  = 560; BIG5P_FREQ[35][103] = 559; BIG5P_FREQ[11][84]  = 558; BIG5P_FREQ[45][33]  = 557; BIG5P_FREQ[121][79] = 556;
        BIG5P_FREQ[2][77]   = 555; BIG5P_FREQ[36][41]  = 554; BIG5P_FREQ[37][47]  = 553; BIG5P_FREQ[39][125] = 552; BIG5P_FREQ[37][26]  = 551;
        BIG5P_FREQ[35][48]  = 550; BIG5P_FREQ[35][28]  = 549; BIG5P_FREQ[35][159] = 548; BIG5P_FREQ[37][40]  = 547; BIG5P_FREQ[35][145] = 546;
        BIG5P_FREQ[37][147] = 545; BIG5P_FREQ[46][160] = 544; BIG5P_FREQ[37][46]  = 543; BIG5P_FREQ[50][99]  = 542; BIG5P_FREQ[52][13]  = 541;
        BIG5P_FREQ[10][82]  = 540; BIG5P_FREQ[35][169] = 539; BIG5P_FREQ[35][31]  = 538; BIG5P_FREQ[47][31]  = 537; BIG5P_FREQ[18][79]  = 536;
        BIG5P_FREQ[16][113] = 535; BIG5P_FREQ[37][104] = 534; BIG5P_FREQ[39][134] = 533; BIG5P_FREQ[36][53]  = 532; BIG5P_FREQ[38][0]   = 531;
        BIG5P_FREQ[4][86]   = 530; BIG5P_FREQ[54][17]  = 529; BIG5P_FREQ[43][157] = 528; BIG5P_FREQ[35][165] = 527; BIG5P_FREQ[69][147] = 526;
        BIG5P_FREQ[117][95] = 525; BIG5P_FREQ[35][162] = 524; BIG5P_FREQ[35][17]  = 523; BIG5P_FREQ[36][142] = 522; BIG5P_FREQ[36][4]   = 521;
        BIG5P_FREQ[37][166] = 520; BIG5P_FREQ[35][168] = 519; BIG5P_FREQ[35][19]  = 518; BIG5P_FREQ[37][48]  = 517; BIG5P_FREQ[42][37]  = 516;
        BIG5P_FREQ[40][146] = 515; BIG5P_FREQ[36][123] = 514; BIG5P_FREQ[22][41]  = 513; BIG5P_FREQ[20][119] = 512; BIG5P_FREQ[2][74]   = 511;
        BIG5P_FREQ[44][113] = 510; BIG5P_FREQ[35][125] = 509; BIG5P_FREQ[37][16]  = 508; BIG5P_FREQ[35][20]  = 507; BIG5P_FREQ[35][55]  = 506;
        BIG5P_FREQ[37][145] = 505; BIG5P_FREQ[0][88]   = 504; BIG5P_FREQ[3][94]   = 503; BIG5P_FREQ[6][65]   = 502; BIG5P_FREQ[26][15]  = 501;
        BIG5P_FREQ[41][126] = 500; BIG5P_FREQ[36][129] = 499; BIG5P_FREQ[31][75]  = 498; BIG5P_FREQ[19][61]  = 497; BIG5P_FREQ[35][128] = 496;
        BIG5P_FREQ[29][79]  = 495; BIG5P_FREQ[36][62]  = 494; BIG5P_FREQ[37][189] = 493; BIG5P_FREQ[39][109] = 492; BIG5P_FREQ[39][135] = 491;
        BIG5P_FREQ[72][15]  = 490; BIG5P_FREQ[47][106] = 489; BIG5P_FREQ[54][14]  = 488; BIG5P_FREQ[24][52]  = 487; BIG5P_FREQ[38][162] = 486;
        BIG5P_FREQ[41][43]  = 485; BIG5P_FREQ[37][121] = 484; BIG5P_FREQ[14][66]  = 483; BIG5P_FREQ[37][30]  = 482; BIG5P_FREQ[35][7]   = 481;
        BIG5P_FREQ[49][58]  = 480; BIG5P_FREQ[43][188] = 479; BIG5P_FREQ[24][66]  = 478; BIG5P_FREQ[35][171] = 477; BIG5P_FREQ[40][186] = 476;
        BIG5P_FREQ[39][164] = 475; BIG5P_FREQ[78][186] = 474; BIG5P_FREQ[8][72]   = 473; BIG5P_FREQ[36][190] = 472; BIG5P_FREQ[35][53]  = 471;
        BIG5P_FREQ[35][54]  = 470; BIG5P_FREQ[22][159] = 469; BIG5P_FREQ[35][9]   = 468; BIG5P_FREQ[41][140] = 467; BIG5P_FREQ[37][22]  = 466;
        BIG5P_FREQ[48][97]  = 465; BIG5P_FREQ[50][97]  = 464; BIG5P_FREQ[36][127] = 463; BIG5P_FREQ[37][23]  = 462; BIG5P_FREQ[40][55]  = 461;
        BIG5P_FREQ[35][43]  = 460; BIG5P_FREQ[26][22]  = 459; BIG5P_FREQ[35][15]  = 458; BIG5P_FREQ[72][179] = 457; BIG5P_FREQ[20][129] = 456;
        BIG5P_FREQ[52][101] = 455; BIG5P_FREQ[35][12]  = 454; BIG5P_FREQ[42][156] = 453; BIG5P_FREQ[15][157] = 452; BIG5P_FREQ[50][140] = 451;
        BIG5P_FREQ[26][28]  = 450; BIG5P_FREQ[54][51]  = 449; BIG5P_FREQ[35][112] = 448; BIG5P_FREQ[36][116] = 447; BIG5P_FREQ[42][11]  = 446;
        BIG5P_FREQ[37][172] = 445; BIG5P_FREQ[37][29]  = 444; BIG5P_FREQ[44][107] = 443; BIG5P_FREQ[50][17]  = 442; BIG5P_FREQ[39][107] = 441;
        BIG5P_FREQ[19][109] = 440; BIG5P_FREQ[36][60]  = 439; BIG5P_FREQ[49][132] = 438; BIG5P_FREQ[26][16]  = 437; BIG5P_FREQ[43][155] = 436;
        BIG5P_FREQ[37][120] = 435; BIG5P_FREQ[15][159] = 434; BIG5P_FREQ[43][6]   = 433; BIG5P_FREQ[45][188] = 432; BIG5P_FREQ[35][38]  = 431;
        BIG5P_FREQ[39][143] = 430; BIG5P_FREQ[48][144] = 429; BIG5P_FREQ[37][168] = 428; BIG5P_FREQ[37][1]   = 427; BIG5P_FREQ[36][109] = 426;
        BIG5P_FREQ[46][53]  = 425; BIG5P_FREQ[38][54]  = 424; BIG5P_FREQ[36][0]   = 423; BIG5P_FREQ[72][33]  = 422; BIG5P_FREQ[42][8]   = 421;
        BIG5P_FREQ[36][31]  = 420; BIG5P_FREQ[35][150] = 419; BIG5P_FREQ[118][93] = 418; BIG5P_FREQ[37][61]  = 417; BIG5P_FREQ[0][85]   = 416;
        BIG5P_FREQ[36][27]  = 415; BIG5P_FREQ[35][134] = 414; BIG5P_FREQ[36][145] = 413; BIG5P_FREQ[6][96]   = 412; BIG5P_FREQ[36][14]  = 411;
        BIG5P_FREQ[16][36]  = 410; BIG5P_FREQ[15][175] = 409; BIG5P_FREQ[35][10]  = 408; BIG5P_FREQ[36][189] = 407; BIG5P_FREQ[35][51]  = 406;
        BIG5P_FREQ[35][109] = 405; BIG5P_FREQ[35][147] = 404; BIG5P_FREQ[35][180] = 403; BIG5P_FREQ[72][5]   = 402; BIG5P_FREQ[36][107] = 401;
        BIG5P_FREQ[49][116] = 400; BIG5P_FREQ[73][30]  = 399; BIG5P_FREQ[6][90]   = 398; BIG5P_FREQ[2][70]   = 397; BIG5P_FREQ[17][141] = 396;
        BIG5P_FREQ[35][62]  = 395; BIG5P_FREQ[16][180] = 394; BIG5P_FREQ[4][91]   = 393; BIG5P_FREQ[15][171] = 392; BIG5P_FREQ[35][177] = 391;
        BIG5P_FREQ[37][173] = 390; BIG5P_FREQ[16][121] = 389; BIG5P_FREQ[35][5]   = 388; BIG5P_FREQ[46][122] = 387; BIG5P_FREQ[40][138] = 386;
        BIG5P_FREQ[50][49]  = 385; BIG5P_FREQ[36][152] = 384; BIG5P_FREQ[13][43]  = 383; BIG5P_FREQ[9][88]   = 382; BIG5P_FREQ[36][159] = 381;
        BIG5P_FREQ[27][62]  = 380; BIG5P_FREQ[40][18]  = 379; BIG5P_FREQ[17][129] = 378; BIG5P_FREQ[43][97]  = 377; BIG5P_FREQ[13][131] = 376;
        BIG5P_FREQ[46][107] = 375; BIG5P_FREQ[60][64]  = 374; BIG5P_FREQ[36][179] = 373; BIG5P_FREQ[37][55]  = 372; BIG5P_FREQ[41][173] = 371;
        BIG5P_FREQ[44][172] = 370; BIG5P_FREQ[23][187] = 369; BIG5P_FREQ[36][149] = 368; BIG5P_FREQ[17][125] = 367; BIG5P_FREQ[55][180] = 366;
        BIG5P_FREQ[51][129] = 365; BIG5P_FREQ[36][51]  = 364; BIG5P_FREQ[37][122] = 363; BIG5P_FREQ[48][32]  = 362; BIG5P_FREQ[51][99]  = 361;
        BIG5P_FREQ[54][16]  = 360; BIG5P_FREQ[41][183] = 359; BIG5P_FREQ[37][179] = 358; BIG5P_FREQ[38][179] = 357; BIG5P_FREQ[35][143] = 356;
        BIG5P_FREQ[37][24]  = 355; BIG5P_FREQ[40][177] = 354; BIG5P_FREQ[47][117] = 353; BIG5P_FREQ[39][52]  = 352; BIG5P_FREQ[22][99]  = 351;
        BIG5P_FREQ[40][142] = 350; BIG5P_FREQ[36][49]  = 349; BIG5P_FREQ[38][17]  = 348; BIG5P_FREQ[39][188] = 347; BIG5P_FREQ[36][186] = 346;
        BIG5P_FREQ[35][189] = 345; BIG5P_FREQ[41][7]   = 344; BIG5P_FREQ[18][91]  = 343; BIG5P_FREQ[43][137] = 342; BIG5P_FREQ[35][142] = 341;
        BIG5P_FREQ[35][117] = 340; BIG5P_FREQ[39][138] = 339; BIG5P_FREQ[16][59]  = 338; BIG5P_FREQ[39][174] = 337; BIG5P_FREQ[55][145] = 336;
        BIG5P_FREQ[37][21]  = 335; BIG5P_FREQ[36][180] = 334; BIG5P_FREQ[37][156] = 333; BIG5P_FREQ[49][13]  = 332; BIG5P_FREQ[41][107] = 331;
        BIG5P_FREQ[36][56]  = 330; BIG5P_FREQ[53][8]   = 329; BIG5P_FREQ[22][114] = 328; BIG5P_FREQ[5][95]   = 327; BIG5P_FREQ[37][0]   = 326;
        BIG5P_FREQ[26][183] = 325; BIG5P_FREQ[22][66]  = 324; BIG5P_FREQ[35][58]  = 323; BIG5P_FREQ[48][117] = 322; BIG5P_FREQ[36][102] = 321;
        BIG5P_FREQ[22][122] = 320; BIG5P_FREQ[35][11]  = 319; BIG5P_FREQ[46][19]  = 318; BIG5P_FREQ[22][49]  = 317; BIG5P_FREQ[48][166] = 316;
        BIG5P_FREQ[41][125] = 315; BIG5P_FREQ[41][1]   = 314; BIG5P_FREQ[35][178] = 313; BIG5P_FREQ[41][12]  = 312; BIG5P_FREQ[26][167] = 311;
        BIG5P_FREQ[42][152] = 310; BIG5P_FREQ[42][46]  = 309; BIG5P_FREQ[42][151] = 308; BIG5P_FREQ[20][135] = 307; BIG5P_FREQ[37][162] = 306;
        BIG5P_FREQ[37][50]  = 305; BIG5P_FREQ[22][185] = 304; BIG5P_FREQ[36][166] = 303; BIG5P_FREQ[19][40]  = 302; BIG5P_FREQ[22][107] = 301;
        BIG5P_FREQ[22][102] = 300; BIG5P_FREQ[57][162] = 299; BIG5P_FREQ[22][124] = 298; BIG5P_FREQ[37][138] = 297; BIG5P_FREQ[37][25]  = 296;
        BIG5P_FREQ[0][69]   = 295; BIG5P_FREQ[43][172] = 294; BIG5P_FREQ[42][167] = 293; BIG5P_FREQ[35][120] = 292; BIG5P_FREQ[41][128] = 291;
        BIG5P_FREQ[2][88]   = 290; BIG5P_FREQ[20][123] = 289; BIG5P_FREQ[35][123] = 288; BIG5P_FREQ[36][28]  = 287; BIG5P_FREQ[42][188] = 286;
        BIG5P_FREQ[42][164] = 285; BIG5P_FREQ[42][4]   = 284; BIG5P_FREQ[43][57]  = 283; BIG5P_FREQ[39][3]   = 282; BIG5P_FREQ[42][3]   = 281;
        BIG5P_FREQ[57][158] = 280; BIG5P_FREQ[35][146] = 279; BIG5P_FREQ[24][54]  = 278; BIG5P_FREQ[13][110] = 277; BIG5P_FREQ[23][132] = 276;
        BIG5P_FREQ[26][102] = 275; BIG5P_FREQ[55][178] = 274; BIG5P_FREQ[17][117] = 273; BIG5P_FREQ[41][161] = 272; BIG5P_FREQ[38][150] = 271;
        BIG5P_FREQ[10][71]  = 270; BIG5P_FREQ[47][60]  = 269; BIG5P_FREQ[16][114] = 268; BIG5P_FREQ[21][47]  = 267; BIG5P_FREQ[39][101] = 266;
        BIG5P_FREQ[18][45]  = 265; BIG5P_FREQ[40][121] = 264; BIG5P_FREQ[45][41]  = 263; BIG5P_FREQ[22][167] = 262; BIG5P_FREQ[26][149] = 261;
        BIG5P_FREQ[15][189] = 260; BIG5P_FREQ[41][177] = 259; BIG5P_FREQ[46][36]  = 258; BIG5P_FREQ[20][40]  = 257; BIG5P_FREQ[41][54]  = 256;
        BIG5P_FREQ[3][87]   = 255; BIG5P_FREQ[40][16]  = 254; BIG5P_FREQ[42][15]  = 253; BIG5P_FREQ[11][83]  = 252; BIG5P_FREQ[0][94]   = 251;
        BIG5P_FREQ[122][81] = 250; BIG5P_FREQ[41][26]  = 249; BIG5P_FREQ[36][34]  = 248; BIG5P_FREQ[44][148] = 247; BIG5P_FREQ[35][3]   = 246;
        BIG5P_FREQ[36][114] = 245; BIG5P_FREQ[42][112] = 244; BIG5P_FREQ[35][183] = 243; BIG5P_FREQ[49][73]  = 242; BIG5P_FREQ[39][2]   = 241;
        BIG5P_FREQ[38][121] = 240; BIG5P_FREQ[44][114] = 239; BIG5P_FREQ[49][32]  = 238; BIG5P_FREQ[1][65]   = 237; BIG5P_FREQ[38][25]  = 236;
        BIG5P_FREQ[39][4]   = 235; BIG5P_FREQ[42][62]  = 234; BIG5P_FREQ[35][40]  = 233; BIG5P_FREQ[24][2]   = 232; BIG5P_FREQ[53][49]  = 231;
        BIG5P_FREQ[41][133] = 230; BIG5P_FREQ[43][134] = 229; BIG5P_FREQ[3][83]   = 228; BIG5P_FREQ[38][158] = 227; BIG5P_FREQ[24][17]  = 226;
        BIG5P_FREQ[52][59]  = 225; BIG5P_FREQ[38][41]  = 224; BIG5P_FREQ[37][127] = 223; BIG5P_FREQ[22][175] = 222; BIG5P_FREQ[44][30]  = 221;
        BIG5P_FREQ[47][178] = 220; BIG5P_FREQ[43][99]  = 219; BIG5P_FREQ[19][4]   = 218; BIG5P_FREQ[37][97]  = 217; BIG5P_FREQ[38][181] = 216;
        BIG5P_FREQ[45][103] = 215; BIG5P_FREQ[1][86]   = 214; BIG5P_FREQ[40][15]  = 213; BIG5P_FREQ[22][136] = 212; BIG5P_FREQ[75][165] = 211;
        BIG5P_FREQ[36][15]  = 210; BIG5P_FREQ[46][80]  = 209; BIG5P_FREQ[59][55]  = 208; BIG5P_FREQ[37][108] = 207; BIG5P_FREQ[21][109] = 206;
        BIG5P_FREQ[24][165] = 205; BIG5P_FREQ[79][158] = 204; BIG5P_FREQ[44][139] = 203; BIG5P_FREQ[36][124] = 202; BIG5P_FREQ[42][185] = 201;
        BIG5P_FREQ[39][186] = 200; BIG5P_FREQ[22][128] = 199; BIG5P_FREQ[40][44]  = 198; BIG5P_FREQ[41][105] = 197; BIG5P_FREQ[1][70]   = 196;
        BIG5P_FREQ[1][68]   = 195; BIG5P_FREQ[53][22]  = 194; BIG5P_FREQ[36][54]  = 193; BIG5P_FREQ[47][147] = 192; BIG5P_FREQ[35][36]  = 191;
        BIG5P_FREQ[35][185] = 190; BIG5P_FREQ[45][37]  = 189; BIG5P_FREQ[43][163] = 188; BIG5P_FREQ[56][115] = 187; BIG5P_FREQ[38][164] = 186;
        BIG5P_FREQ[35][141] = 185; BIG5P_FREQ[42][132] = 184; BIG5P_FREQ[46][120] = 183; BIG5P_FREQ[69][142] = 182; BIG5P_FREQ[38][175] = 181;
        BIG5P_FREQ[22][112] = 180; BIG5P_FREQ[38][142] = 179; BIG5P_FREQ[40][37]  = 178; BIG5P_FREQ[37][109] = 177; BIG5P_FREQ[40][144] = 176;
        BIG5P_FREQ[44][117] = 175; BIG5P_FREQ[35][181] = 174; BIG5P_FREQ[26][105] = 173; BIG5P_FREQ[16][48]  = 172; BIG5P_FREQ[44][122] = 171;
        BIG5P_FREQ[12][86]  = 170; BIG5P_FREQ[84][53]  = 169; BIG5P_FREQ[17][44]  = 168; BIG5P_FREQ[59][54]  = 167; BIG5P_FREQ[36][98]  = 166;
        BIG5P_FREQ[45][115] = 165; BIG5P_FREQ[73][9]   = 164; BIG5P_FREQ[44][123] = 163; BIG5P_FREQ[37][188] = 162; BIG5P_FREQ[51][117] = 161;
        BIG5P_FREQ[15][156] = 160; BIG5P_FREQ[36][155] = 159; BIG5P_FREQ[44][25]  = 158; BIG5P_FREQ[38][12]  = 157; BIG5P_FREQ[38][140] = 156;
        BIG5P_FREQ[23][4]   = 155; BIG5P_FREQ[45][149] = 154; BIG5P_FREQ[22][189] = 153; BIG5P_FREQ[38][147] = 152; BIG5P_FREQ[27][5]   = 151;
        BIG5P_FREQ[22][42]  = 150; BIG5P_FREQ[3][68]   = 149; BIG5P_FREQ[39][51]  = 148; BIG5P_FREQ[36][29]  = 147; BIG5P_FREQ[20][108] = 146;
        BIG5P_FREQ[50][57]  = 145; BIG5P_FREQ[55][104] = 144; BIG5P_FREQ[22][46]  = 143; BIG5P_FREQ[18][164] = 142; BIG5P_FREQ[50][159] = 141;
        BIG5P_FREQ[85][131] = 140; BIG5P_FREQ[26][79]  = 139; BIG5P_FREQ[38][100] = 138; BIG5P_FREQ[53][112] = 137; BIG5P_FREQ[20][190] = 136;
        BIG5P_FREQ[14][69]  = 135; BIG5P_FREQ[23][11]  = 134; BIG5P_FREQ[40][114] = 133; BIG5P_FREQ[40][148] = 132; BIG5P_FREQ[53][130] = 131;
        BIG5P_FREQ[36][2]   = 130; BIG5P_FREQ[66][82]  = 129; BIG5P_FREQ[45][166] = 128; BIG5P_FREQ[4][88]   = 127; BIG5P_FREQ[16][57]  = 126;
        BIG5P_FREQ[22][116] = 125; BIG5P_FREQ[36][108] = 124; BIG5P_FREQ[13][48]  = 123; BIG5P_FREQ[54][12]  = 122; BIG5P_FREQ[40][136] = 121;
        BIG5P_FREQ[36][128] = 120; BIG5P_FREQ[23][6]   = 119; BIG5P_FREQ[38][125] = 118; BIG5P_FREQ[45][154] = 117; BIG5P_FREQ[51][127] = 116;
        BIG5P_FREQ[44][163] = 115; BIG5P_FREQ[16][173] = 114; BIG5P_FREQ[43][49]  = 113; BIG5P_FREQ[20][112] = 112; BIG5P_FREQ[15][168] = 111;
        BIG5P_FREQ[35][129] = 110; BIG5P_FREQ[20][45]  = 109; BIG5P_FREQ[38][10]  = 108; BIG5P_FREQ[57][171] = 107; BIG5P_FREQ[44][190] = 106;
        BIG5P_FREQ[40][56]  = 105; BIG5P_FREQ[36][156] = 104; BIG5P_FREQ[3][88]   = 103; BIG5P_FREQ[50][122] = 102; BIG5P_FREQ[36][7]   = 101;
        BIG5P_FREQ[39][43]  = 100; BIG5P_FREQ[15][166] =  99; BIG5P_FREQ[42][136] =  98; BIG5P_FREQ[22][131] =  97; BIG5P_FREQ[44][23]  =  96;
        BIG5P_FREQ[54][147] =  95; BIG5P_FREQ[41][32]  =  94; BIG5P_FREQ[23][121] =  93; BIG5P_FREQ[39][108] =  92; BIG5P_FREQ[2][78]   =  91;
        BIG5P_FREQ[40][155] =  90; BIG5P_FREQ[55][51]  =  89; BIG5P_FREQ[19][34]  =  88; BIG5P_FREQ[48][128] =  87; BIG5P_FREQ[48][159] =  86;
        BIG5P_FREQ[20][70]  =  85; BIG5P_FREQ[34][71]  =  84; BIG5P_FREQ[16][31]  =  83; BIG5P_FREQ[42][157] =  82; BIG5P_FREQ[20][44]  =  81;
        BIG5P_FREQ[11][92]  =  80; BIG5P_FREQ[44][180] =  79; BIG5P_FREQ[84][33]  =  78; BIG5P_FREQ[16][116] =  77; BIG5P_FREQ[61][163] =  76;
        BIG5P_FREQ[35][164] =  75; BIG5P_FREQ[36][42]  =  74; BIG5P_FREQ[13][40]  =  73; BIG5P_FREQ[43][176] =  72; BIG5P_FREQ[2][66]   =  71;
        BIG5P_FREQ[20][133] =  70; BIG5P_FREQ[36][65]  =  69; BIG5P_FREQ[38][33]  =  68; BIG5P_FREQ[12][91]  =  67; BIG5P_FREQ[36][26]  =  66;
        BIG5P_FREQ[15][174] =  65; BIG5P_FREQ[77][32]  =  64; BIG5P_FREQ[16][1]   =  63; BIG5P_FREQ[25][86]  =  62; BIG5P_FREQ[17][13]  =  61;
        BIG5P_FREQ[5][75]   =  60; BIG5P_FREQ[36][52]  =  59; BIG5P_FREQ[51][164] =  58; BIG5P_FREQ[12][85]  =  57; BIG5P_FREQ[39][168] =  56;
        BIG5P_FREQ[43][16]  =  55; BIG5P_FREQ[40][69]  =  54; BIG5P_FREQ[26][108] =  53; BIG5P_FREQ[51][56]  =  52; BIG5P_FREQ[16][37]  =  51;
        BIG5P_FREQ[40][29]  =  50; BIG5P_FREQ[46][171] =  49; BIG5P_FREQ[40][128] =  48; BIG5P_FREQ[72][114] =  47; BIG5P_FREQ[21][103] =  46;
        BIG5P_FREQ[22][44]  =  45; BIG5P_FREQ[40][115] =  44; BIG5P_FREQ[43][7]   =  43; BIG5P_FREQ[43][153] =  42; BIG5P_FREQ[17][20]  =  41;
        BIG5P_FREQ[16][49]  =  40; BIG5P_FREQ[36][57]  =  39; BIG5P_FREQ[18][38]  =  38; BIG5P_FREQ[45][184] =  37; BIG5P_FREQ[37][167] =  36;
        BIG5P_FREQ[26][106] =  35; BIG5P_FREQ[61][121] =  34; BIG5P_FREQ[89][140] =  33; BIG5P_FREQ[46][61]  =  32; BIG5P_FREQ[39][163] =  31;
        BIG5P_FREQ[40][62]  =  30; BIG5P_FREQ[38][165] =  29; BIG5P_FREQ[47][37]  =  28; BIG5P_FREQ[18][155] =  27; BIG5P_FREQ[20][33]  =  26;
        BIG5P_FREQ[29][90]  =  25; BIG5P_FREQ[20][103] =  24; BIG5P_FREQ[37][51]  =  23; BIG5P_FREQ[57][0]   =  22; BIG5P_FREQ[40][31]  =  21;
        BIG5P_FREQ[45][32]  =  20; BIG5P_FREQ[59][23]  =  19; BIG5P_FREQ[18][47]  =  18; BIG5P_FREQ[45][134] =  17; BIG5P_FREQ[37][59]  =  16;
        BIG5P_FREQ[21][128] =  15; BIG5P_FREQ[36][106] =  14; BIG5P_FREQ[31][39]  =  13; BIG5P_FREQ[40][182] =  12; BIG5P_FREQ[52][155] =  11;
        BIG5P_FREQ[42][166] =  10; BIG5P_FREQ[35][27]  =   9; BIG5P_FREQ[38][3]   =   8; BIG5P_FREQ[13][44]  =   7; BIG5P_FREQ[58][157] =   6;
        BIG5P_FREQ[47][51]  =   5; BIG5P_FREQ[41][37]  =   4; BIG5P_FREQ[41][172] =   3; BIG5P_FREQ[51][165] =   2; BIG5P_FREQ[15][161] =   1;
        BIG5P_FREQ[24][181] =   0;
        /*
        Big5Freq[5][0]     = 200; Big5Freq[26][57]   = 199; Big5Freq[13][155]  = 198; Big5Freq[3][38]    = 197; Big5Freq[9][155]   = 196;
        Big5Freq[28][53]   = 195; Big5Freq[15][71]   = 194; Big5Freq[21][95]   = 193; Big5Freq[15][112]  = 192; Big5Freq[14][138]  = 191;
        Big5Freq[8][18]    = 190; Big5Freq[20][151]  = 189; Big5Freq[37][27]   = 188; Big5Freq[32][48]   = 187; Big5Freq[23][66]   = 186;
        Big5Freq[9][2]     = 185; Big5Freq[13][133]  = 184; Big5Freq[7][127]   = 183; Big5Freq[3][11]    = 182; Big5Freq[12][118]  = 181;
        Big5Freq[13][101]  = 180; Big5Freq[30][153]  = 179; Big5Freq[4][65]    = 178; Big5Freq[5][25]    = 177; Big5Freq[5][140]   = 176;
        Big5Freq[6][25]    = 175; Big5Freq[4][52]    = 174; Big5Freq[30][156]  = 173; Big5Freq[16][13]   = 172; Big5Freq[21][8]    = 171;
        Big5Freq[19][74]   = 170; Big5Freq[15][145]  = 169; Big5Freq[9][15]    = 168; Big5Freq[13][82]   = 167; Big5Freq[26][86]   = 166;
        Big5Freq[18][52]   = 165; Big5Freq[6][109]   = 164; Big5Freq[10][99]   = 163; Big5Freq[18][101]  = 162; Big5Freq[25][49]   = 161;
        Big5Freq[31][79]   = 160; Big5Freq[28][20]   = 159; Big5Freq[12][115]  = 158; Big5Freq[15][66]   = 157; Big5Freq[11][104]  = 156;
        Big5Freq[23][106]  = 155; Big5Freq[34][157]  = 154; Big5Freq[32][94]   = 153; Big5Freq[29][88]   = 152; Big5Freq[10][46]   = 151;
        Big5Freq[13][118]  = 150; Big5Freq[20][37]   = 149; Big5Freq[12][30]   = 148; Big5Freq[21][4]    = 147; Big5Freq[16][33]   = 146;
        Big5Freq[13][52]   = 145; Big5Freq[4][7]     = 144; Big5Freq[21][49]   = 143; Big5Freq[3][27]    = 142; Big5Freq[16][91]   = 141;
        Big5Freq[5][155]   = 140; Big5Freq[29][130]  = 139; Big5Freq[3][125]   = 138; Big5Freq[14][26]   = 137; Big5Freq[15][39]   = 136;
        Big5Freq[24][110]  = 135; Big5Freq[7][141]   = 134; Big5Freq[21][15]   = 133; Big5Freq[32][104]  = 132; Big5Freq[8][31]    = 131;
        Big5Freq[34][112]  = 130; Big5Freq[10][75]   = 129; Big5Freq[21][23]   = 128; Big5Freq[34][131]  = 127; Big5Freq[12][3]    = 126;
        Big5Freq[10][62]   = 125; Big5Freq[9][120]   = 124; Big5Freq[32][149]  = 123; Big5Freq[8][44]    = 122; Big5Freq[24][2]    = 121;
        Big5Freq[6][148]   = 120; Big5Freq[15][103]  = 119; Big5Freq[36][54]   = 118; Big5Freq[36][134]  = 117; Big5Freq[11][7]    = 116;
        Big5Freq[3][90]    = 115; Big5Freq[36][73]   = 114; Big5Freq[8][102]   = 113; Big5Freq[12][87]   = 112; Big5Freq[25][64]   = 111;
        Big5Freq[9][1]     = 110; Big5Freq[24][121]  = 109; Big5Freq[5][75]    = 108; Big5Freq[17][83]   = 107; Big5Freq[18][57]   = 106;
        Big5Freq[8][95]    = 105; Big5Freq[14][36]   = 104; Big5Freq[28][113]  = 103; Big5Freq[12][56]   = 102; Big5Freq[14][61]   = 101;
        Big5Freq[25][138]  = 100; Big5Freq[4][34]    =  99; Big5Freq[11][152]  =  98; Big5Freq[35][0]    =  97; Big5Freq[4][15]    =  96;
        Big5Freq[8][82]    =  95; Big5Freq[20][73]   =  94; Big5Freq[25][52]   =  93; Big5Freq[24][6]    =  92; Big5Freq[21][78]   =  91;
        Big5Freq[17][32]   =  90; Big5Freq[17][91]   =  89; Big5Freq[5][76]    =  88; Big5Freq[15][60]   =  87; Big5Freq[15][150]  =  86;
        Big5Freq[5][80]    =  85; Big5Freq[15][81]   =  84; Big5Freq[28][108]  =  83; Big5Freq[18][14]   =  82; Big5Freq[19][109]  =  81;
        Big5Freq[28][133]  =  80; Big5Freq[21][97]   =  79; Big5Freq[5][105]   =  78; Big5Freq[18][114]  =  77; Big5Freq[16][95]   =  76;
        Big5Freq[5][51]    =  75; Big5Freq[3][148]   =  74; Big5Freq[22][102]  =  73; Big5Freq[4][123]   =  72; Big5Freq[8][88]    =  71;
        Big5Freq[25][111]  =  70; Big5Freq[8][149]   =  69; Big5Freq[9][48]    =  68; Big5Freq[16][126]  =  67; Big5Freq[33][150]  =  66;
        Big5Freq[9][54]    =  65; Big5Freq[29][104]  =  64; Big5Freq[3][3]     =  63; Big5Freq[11][49]   =  62; Big5Freq[24][109]  =  61;
        Big5Freq[28][116]  =  60; Big5Freq[34][113]  =  59; Big5Freq[5][3]     =  58; Big5Freq[21][106]  =  57; Big5Freq[4][98]    =  56;
        Big5Freq[12][135]  =  55; Big5Freq[16][101]  =  54; Big5Freq[12][147]  =  53; Big5Freq[27][55]   =  52; Big5Freq[3][5]     =  51;
        Big5Freq[11][101]  =  50; Big5Freq[16][157]  =  49; Big5Freq[22][114]  =  48; Big5Freq[18][46]   =  47; Big5Freq[4][29]    =  46;
        Big5Freq[8][103]   =  45; Big5Freq[16][151]  =  44; Big5Freq[8][29]    =  43; Big5Freq[15][114]  =  42; Big5Freq[22][70]   =  41;
        Big5Freq[13][121]  =  40; Big5Freq[7][112]   =  39; Big5Freq[20][83]   =  38; Big5Freq[3][36]    =  37; Big5Freq[10][103]  =  36;
        Big5Freq[3][96]    =  35; Big5Freq[21][79]   =  34; Big5Freq[25][120]  =  33; Big5Freq[29][121]  =  32; Big5Freq[23][71]   =  31;
        Big5Freq[21][22]   =  30; Big5Freq[18][89]   =  29; Big5Freq[25][104]  =  28; Big5Freq[10][124]  =  27; Big5Freq[26][4]    =  26;
        Big5Freq[21][136]  =  25; Big5Freq[6][112]   =  24; Big5Freq[12][103]  =  23; Big5Freq[17][66]   =  22; Big5Freq[13][151]  =  21;
        Big5Freq[33][152]  =  20; Big5Freq[11][148]  =  19; Big5Freq[13][57]   =  18; Big5Freq[13][41]   =  17; Big5Freq[7][60]    =  16;
        Big5Freq[21][29]   =  15; Big5Freq[9][157]   =  14; Big5Freq[24][95]   =  13; Big5Freq[15][148]  =  12; Big5Freq[15][122]  =  11;
        Big5Freq[6][125]   =  10; Big5Freq[11][25]   =   9; Big5Freq[20][55]   =   8; Big5Freq[19][84]   =   7; Big5Freq[21][82]   =   6;
        Big5Freq[24][3]    =   5; Big5Freq[13][70]   =   4; Big5Freq[6][21]    =   3; Big5Freq[21][86]   =   2; Big5Freq[12][23]   =   1;
        Big5Freq[3][85]    =   0;
        */

        // ------------------------------------------------------------------------------------EUC_TW_FREQ

        EUC_TW_FREQ[48][49] = 599; EUC_TW_FREQ[35][65] = 598; EUC_TW_FREQ[41][27] = 597; EUC_TW_FREQ[35][0]  = 596; EUC_TW_FREQ[39][19] = 595;
        EUC_TW_FREQ[35][42] = 594; EUC_TW_FREQ[38][66] = 593; EUC_TW_FREQ[35][8]  = 592; EUC_TW_FREQ[35][6]  = 591; EUC_TW_FREQ[35][66] = 590;
        EUC_TW_FREQ[43][14] = 589; EUC_TW_FREQ[69][80] = 588; EUC_TW_FREQ[50][48] = 587; EUC_TW_FREQ[36][71] = 586; EUC_TW_FREQ[37][10] = 585;
        EUC_TW_FREQ[60][52] = 584; EUC_TW_FREQ[51][21] = 583; EUC_TW_FREQ[40][2]  = 582; EUC_TW_FREQ[67][35] = 581; EUC_TW_FREQ[38][78] = 580;
        EUC_TW_FREQ[49][18] = 579; EUC_TW_FREQ[35][23] = 578; EUC_TW_FREQ[42][83] = 577; EUC_TW_FREQ[79][47] = 576; EUC_TW_FREQ[61][82] = 575;
        EUC_TW_FREQ[38][7]  = 574; EUC_TW_FREQ[35][29] = 573; EUC_TW_FREQ[37][77] = 572; EUC_TW_FREQ[54][67] = 571; EUC_TW_FREQ[38][80] = 570;
        EUC_TW_FREQ[52][74] = 569; EUC_TW_FREQ[36][37] = 568; EUC_TW_FREQ[74][8]  = 567; EUC_TW_FREQ[41][83] = 566; EUC_TW_FREQ[36][75] = 565;
        EUC_TW_FREQ[49][63] = 564; EUC_TW_FREQ[42][58] = 563; EUC_TW_FREQ[56][33] = 562; EUC_TW_FREQ[37][76] = 561; EUC_TW_FREQ[62][39] = 560;
        EUC_TW_FREQ[35][21] = 559; EUC_TW_FREQ[70][19] = 558; EUC_TW_FREQ[77][88] = 557; EUC_TW_FREQ[51][14] = 556; EUC_TW_FREQ[36][17] = 555;
        EUC_TW_FREQ[44][51] = 554; EUC_TW_FREQ[38][72] = 553; EUC_TW_FREQ[74][90] = 552; EUC_TW_FREQ[35][48] = 551; EUC_TW_FREQ[35][69] = 550;
        EUC_TW_FREQ[66][86] = 549; EUC_TW_FREQ[57][20] = 548; EUC_TW_FREQ[35][53] = 547; EUC_TW_FREQ[36][87] = 546; EUC_TW_FREQ[84][67] = 545;
        EUC_TW_FREQ[70][56] = 544; EUC_TW_FREQ[71][54] = 543; EUC_TW_FREQ[60][70] = 542; EUC_TW_FREQ[80][1]  = 541; EUC_TW_FREQ[39][59] = 540;
        EUC_TW_FREQ[39][51] = 539; EUC_TW_FREQ[35][44] = 538; EUC_TW_FREQ[48][4]  = 537; EUC_TW_FREQ[55][24] = 536; EUC_TW_FREQ[52][4]  = 535;
        EUC_TW_FREQ[54][26] = 534; EUC_TW_FREQ[36][31] = 533; EUC_TW_FREQ[37][22] = 532; EUC_TW_FREQ[37][9]  = 531; EUC_TW_FREQ[46][0]  = 530;
        EUC_TW_FREQ[56][46] = 529; EUC_TW_FREQ[47][93] = 528; EUC_TW_FREQ[37][25] = 527; EUC_TW_FREQ[39][8]  = 526; EUC_TW_FREQ[46][73] = 525;
        EUC_TW_FREQ[38][48] = 524; EUC_TW_FREQ[39][83] = 523; EUC_TW_FREQ[60][92] = 522; EUC_TW_FREQ[70][11] = 521; EUC_TW_FREQ[63][84] = 520;
        EUC_TW_FREQ[38][65] = 519; EUC_TW_FREQ[45][45] = 518; EUC_TW_FREQ[63][49] = 517; EUC_TW_FREQ[63][50] = 516; EUC_TW_FREQ[39][93] = 515;
        EUC_TW_FREQ[68][20] = 514; EUC_TW_FREQ[44][84] = 513; EUC_TW_FREQ[66][34] = 512; EUC_TW_FREQ[37][58] = 511; EUC_TW_FREQ[39][0]  = 510;
        EUC_TW_FREQ[59][1]  = 509; EUC_TW_FREQ[47][8]  = 508; EUC_TW_FREQ[61][17] = 507; EUC_TW_FREQ[53][87] = 506; EUC_TW_FREQ[67][26] = 505;
        EUC_TW_FREQ[43][46] = 504; EUC_TW_FREQ[38][61] = 503; EUC_TW_FREQ[45][9]  = 502; EUC_TW_FREQ[66][83] = 501; EUC_TW_FREQ[43][88] = 500;
        EUC_TW_FREQ[85][20] = 499; EUC_TW_FREQ[57][36] = 498; EUC_TW_FREQ[43][6]  = 497; EUC_TW_FREQ[86][77] = 496; EUC_TW_FREQ[42][70] = 495;
        EUC_TW_FREQ[49][78] = 494; EUC_TW_FREQ[36][40] = 493; EUC_TW_FREQ[42][71] = 492; EUC_TW_FREQ[58][49] = 491; EUC_TW_FREQ[35][20] = 490;
        EUC_TW_FREQ[76][20] = 489; EUC_TW_FREQ[39][25] = 488; EUC_TW_FREQ[40][34] = 487; EUC_TW_FREQ[39][76] = 486; EUC_TW_FREQ[40][1]  = 485;
        EUC_TW_FREQ[59][0]  = 484; EUC_TW_FREQ[39][70] = 483; EUC_TW_FREQ[46][14] = 482; EUC_TW_FREQ[68][77] = 481; EUC_TW_FREQ[38][55] = 480;
        EUC_TW_FREQ[35][78] = 479; EUC_TW_FREQ[84][44] = 478; EUC_TW_FREQ[36][41] = 477; EUC_TW_FREQ[37][62] = 476; EUC_TW_FREQ[65][67] = 475;
        EUC_TW_FREQ[69][66] = 474; EUC_TW_FREQ[73][55] = 473; EUC_TW_FREQ[71][49] = 472; EUC_TW_FREQ[66][87] = 471; EUC_TW_FREQ[38][33] = 470;
        EUC_TW_FREQ[64][61] = 469; EUC_TW_FREQ[35][7]  = 468; EUC_TW_FREQ[47][49] = 467; EUC_TW_FREQ[56][14] = 466; EUC_TW_FREQ[36][49] = 465;
        EUC_TW_FREQ[50][81] = 464; EUC_TW_FREQ[55][76] = 463; EUC_TW_FREQ[35][19] = 462; EUC_TW_FREQ[44][47] = 461; EUC_TW_FREQ[35][15] = 460;
        EUC_TW_FREQ[82][59] = 459; EUC_TW_FREQ[35][43] = 458; EUC_TW_FREQ[73][0]  = 457; EUC_TW_FREQ[57][83] = 456; EUC_TW_FREQ[42][46] = 455;
        EUC_TW_FREQ[36][0]  = 454; EUC_TW_FREQ[70][88] = 453; EUC_TW_FREQ[42][22] = 452; EUC_TW_FREQ[46][58] = 451; EUC_TW_FREQ[36][34] = 450;
        EUC_TW_FREQ[39][24] = 449; EUC_TW_FREQ[35][55] = 448; EUC_TW_FREQ[44][91] = 447; EUC_TW_FREQ[37][51] = 446; EUC_TW_FREQ[36][19] = 445;
        EUC_TW_FREQ[69][90] = 444; EUC_TW_FREQ[55][35] = 443; EUC_TW_FREQ[35][54] = 442; EUC_TW_FREQ[49][61] = 441; EUC_TW_FREQ[36][67] = 440;
        EUC_TW_FREQ[88][34] = 439; EUC_TW_FREQ[35][17] = 438; EUC_TW_FREQ[65][69] = 437; EUC_TW_FREQ[74][89] = 436; EUC_TW_FREQ[37][31] = 435;
        EUC_TW_FREQ[43][48] = 434; EUC_TW_FREQ[89][27] = 433; EUC_TW_FREQ[42][79] = 432; EUC_TW_FREQ[69][57] = 431; EUC_TW_FREQ[36][13] = 430;
        EUC_TW_FREQ[35][62] = 429; EUC_TW_FREQ[65][47] = 428; EUC_TW_FREQ[56][8]  = 427; EUC_TW_FREQ[38][79] = 426; EUC_TW_FREQ[37][64] = 425;
        EUC_TW_FREQ[64][64] = 424; EUC_TW_FREQ[38][53] = 423; EUC_TW_FREQ[38][31] = 422; EUC_TW_FREQ[56][81] = 421; EUC_TW_FREQ[36][22] = 420;
        EUC_TW_FREQ[43][4]  = 419; EUC_TW_FREQ[36][90] = 418; EUC_TW_FREQ[38][62] = 417; EUC_TW_FREQ[66][85] = 416; EUC_TW_FREQ[39][1]  = 415;
        EUC_TW_FREQ[59][40] = 414; EUC_TW_FREQ[58][93] = 413; EUC_TW_FREQ[44][43] = 412; EUC_TW_FREQ[39][49] = 411; EUC_TW_FREQ[64][2]  = 410;
        EUC_TW_FREQ[41][35] = 409; EUC_TW_FREQ[60][22] = 408; EUC_TW_FREQ[35][91] = 407; EUC_TW_FREQ[78][1]  = 406; EUC_TW_FREQ[36][14] = 405;
        EUC_TW_FREQ[82][29] = 404; EUC_TW_FREQ[52][86] = 403; EUC_TW_FREQ[40][16] = 402; EUC_TW_FREQ[91][52] = 401; EUC_TW_FREQ[50][75] = 400;
        EUC_TW_FREQ[64][30] = 399; EUC_TW_FREQ[90][78] = 398; EUC_TW_FREQ[36][52] = 397; EUC_TW_FREQ[55][87] = 396; EUC_TW_FREQ[57][5]  = 395;
        EUC_TW_FREQ[57][31] = 394; EUC_TW_FREQ[42][35] = 393; EUC_TW_FREQ[69][50] = 392; EUC_TW_FREQ[45][8]  = 391; EUC_TW_FREQ[50][87] = 390;
        EUC_TW_FREQ[69][55] = 389; EUC_TW_FREQ[92][3]  = 388; EUC_TW_FREQ[36][43] = 387; EUC_TW_FREQ[64][10] = 386; EUC_TW_FREQ[56][25] = 385;
        EUC_TW_FREQ[60][68] = 384; EUC_TW_FREQ[51][46] = 383; EUC_TW_FREQ[50][0]  = 382; EUC_TW_FREQ[38][30] = 381; EUC_TW_FREQ[50][85] = 380;
        EUC_TW_FREQ[60][54] = 379; EUC_TW_FREQ[73][6]  = 378; EUC_TW_FREQ[73][28] = 377; EUC_TW_FREQ[56][19] = 376; EUC_TW_FREQ[62][69] = 375;
        EUC_TW_FREQ[81][66] = 374; EUC_TW_FREQ[40][32] = 373; EUC_TW_FREQ[76][31] = 372; EUC_TW_FREQ[35][10] = 371; EUC_TW_FREQ[41][37] = 370;
        EUC_TW_FREQ[52][82] = 369; EUC_TW_FREQ[91][72] = 368; EUC_TW_FREQ[37][29] = 367; EUC_TW_FREQ[56][30] = 366; EUC_TW_FREQ[37][80] = 365;
        EUC_TW_FREQ[81][56] = 364; EUC_TW_FREQ[70][3]  = 363; EUC_TW_FREQ[76][15] = 362; EUC_TW_FREQ[46][47] = 361; EUC_TW_FREQ[35][88] = 360;
        EUC_TW_FREQ[61][58] = 359; EUC_TW_FREQ[37][37] = 358; EUC_TW_FREQ[57][22] = 357; EUC_TW_FREQ[41][23] = 356; EUC_TW_FREQ[90][66] = 355;
        EUC_TW_FREQ[39][60] = 354; EUC_TW_FREQ[38][0]  = 353; EUC_TW_FREQ[37][87] = 352; EUC_TW_FREQ[46][2]  = 351; EUC_TW_FREQ[38][56] = 350;
        EUC_TW_FREQ[58][11] = 349; EUC_TW_FREQ[48][10] = 348; EUC_TW_FREQ[74][4]  = 347; EUC_TW_FREQ[40][42] = 346; EUC_TW_FREQ[41][52] = 345;
        EUC_TW_FREQ[61][92] = 344; EUC_TW_FREQ[39][50] = 343; EUC_TW_FREQ[47][88] = 342; EUC_TW_FREQ[88][36] = 341; EUC_TW_FREQ[45][73] = 340;
        EUC_TW_FREQ[82][3]  = 339; EUC_TW_FREQ[61][36] = 338; EUC_TW_FREQ[60][33] = 337; EUC_TW_FREQ[38][27] = 336; EUC_TW_FREQ[35][83] = 335;
        EUC_TW_FREQ[65][24] = 334; EUC_TW_FREQ[73][10] = 333; EUC_TW_FREQ[41][13] = 332; EUC_TW_FREQ[50][27] = 331; EUC_TW_FREQ[59][50] = 330;
        EUC_TW_FREQ[42][45] = 329; EUC_TW_FREQ[55][19] = 328; EUC_TW_FREQ[36][77] = 327; EUC_TW_FREQ[69][31] = 326; EUC_TW_FREQ[60][7]  = 325;
        EUC_TW_FREQ[40][88] = 324; EUC_TW_FREQ[57][56] = 323; EUC_TW_FREQ[50][50] = 322; EUC_TW_FREQ[42][37] = 321; EUC_TW_FREQ[38][82] = 320;
        EUC_TW_FREQ[52][25] = 319; EUC_TW_FREQ[42][67] = 318; EUC_TW_FREQ[48][40] = 317; EUC_TW_FREQ[45][81] = 316; EUC_TW_FREQ[57][14] = 315;
        EUC_TW_FREQ[42][13] = 314; EUC_TW_FREQ[78][0]  = 313; EUC_TW_FREQ[35][51] = 312; EUC_TW_FREQ[41][67] = 311; EUC_TW_FREQ[64][23] = 310;
        EUC_TW_FREQ[36][65] = 309; EUC_TW_FREQ[48][50] = 308; EUC_TW_FREQ[46][69] = 307; EUC_TW_FREQ[47][89] = 306; EUC_TW_FREQ[41][48] = 305;
        EUC_TW_FREQ[60][56] = 304; EUC_TW_FREQ[44][82] = 303; EUC_TW_FREQ[47][35] = 302; EUC_TW_FREQ[49][3]  = 301; EUC_TW_FREQ[49][69] = 300;
        EUC_TW_FREQ[45][93] = 299; EUC_TW_FREQ[60][34] = 298; EUC_TW_FREQ[60][82] = 297; EUC_TW_FREQ[61][61] = 296; EUC_TW_FREQ[86][42] = 295;
        EUC_TW_FREQ[89][60] = 294; EUC_TW_FREQ[48][31] = 293; EUC_TW_FREQ[35][75] = 292; EUC_TW_FREQ[91][39] = 291; EUC_TW_FREQ[53][19] = 290;
        EUC_TW_FREQ[39][72] = 289; EUC_TW_FREQ[69][59] = 288; EUC_TW_FREQ[41][7]  = 287; EUC_TW_FREQ[54][13] = 286; EUC_TW_FREQ[43][28] = 285;
        EUC_TW_FREQ[36][6]  = 284; EUC_TW_FREQ[45][75] = 283; EUC_TW_FREQ[36][61] = 282; EUC_TW_FREQ[38][21] = 281; EUC_TW_FREQ[45][14] = 280;
        EUC_TW_FREQ[61][43] = 279; EUC_TW_FREQ[36][63] = 278; EUC_TW_FREQ[43][30] = 277; EUC_TW_FREQ[46][51] = 276; EUC_TW_FREQ[68][87] = 275;
        EUC_TW_FREQ[39][26] = 274; EUC_TW_FREQ[46][76] = 273; EUC_TW_FREQ[36][15] = 272; EUC_TW_FREQ[35][40] = 271; EUC_TW_FREQ[79][60] = 270;
        EUC_TW_FREQ[46][7]  = 269; EUC_TW_FREQ[65][72] = 268; EUC_TW_FREQ[69][88] = 267; EUC_TW_FREQ[47][18] = 266; EUC_TW_FREQ[37][0]  = 265;
        EUC_TW_FREQ[37][49] = 264; EUC_TW_FREQ[67][37] = 263; EUC_TW_FREQ[36][91] = 262; EUC_TW_FREQ[75][48] = 261; EUC_TW_FREQ[75][63] = 260;
        EUC_TW_FREQ[83][87] = 259; EUC_TW_FREQ[37][44] = 258; EUC_TW_FREQ[73][54] = 257; EUC_TW_FREQ[51][61] = 256; EUC_TW_FREQ[46][57] = 255;
        EUC_TW_FREQ[55][21] = 254; EUC_TW_FREQ[39][66] = 253; EUC_TW_FREQ[47][11] = 252; EUC_TW_FREQ[52][8]  = 251; EUC_TW_FREQ[82][81] = 250;
        EUC_TW_FREQ[36][57] = 249; EUC_TW_FREQ[38][54] = 248; EUC_TW_FREQ[43][81] = 247; EUC_TW_FREQ[37][42] = 246; EUC_TW_FREQ[40][18] = 245;
        EUC_TW_FREQ[80][90] = 244; EUC_TW_FREQ[37][84] = 243; EUC_TW_FREQ[57][15] = 242; EUC_TW_FREQ[38][87] = 241; EUC_TW_FREQ[37][32] = 240;
        EUC_TW_FREQ[53][53] = 239; EUC_TW_FREQ[89][29] = 238; EUC_TW_FREQ[81][53] = 237; EUC_TW_FREQ[75][3]  = 236; EUC_TW_FREQ[83][73] = 235;
        EUC_TW_FREQ[66][13] = 234; EUC_TW_FREQ[48][7]  = 233; EUC_TW_FREQ[46][35] = 232; EUC_TW_FREQ[35][86] = 231; EUC_TW_FREQ[37][20] = 230;
        EUC_TW_FREQ[46][80] = 229; EUC_TW_FREQ[38][24] = 228; EUC_TW_FREQ[41][68] = 227; EUC_TW_FREQ[42][21] = 226; EUC_TW_FREQ[43][32] = 225;
        EUC_TW_FREQ[38][20] = 224; EUC_TW_FREQ[37][59] = 223; EUC_TW_FREQ[41][77] = 222; EUC_TW_FREQ[59][57] = 221; EUC_TW_FREQ[68][59] = 220;
        EUC_TW_FREQ[39][43] = 219; EUC_TW_FREQ[54][39] = 218; EUC_TW_FREQ[48][28] = 217; EUC_TW_FREQ[54][28] = 216; EUC_TW_FREQ[41][44] = 215;
        EUC_TW_FREQ[51][64] = 214; EUC_TW_FREQ[47][72] = 213; EUC_TW_FREQ[62][67] = 212; EUC_TW_FREQ[42][43] = 211; EUC_TW_FREQ[61][38] = 210;
        EUC_TW_FREQ[76][25] = 209; EUC_TW_FREQ[48][91] = 208; EUC_TW_FREQ[36][36] = 207; EUC_TW_FREQ[80][32] = 206; EUC_TW_FREQ[81][40] = 205;
        EUC_TW_FREQ[37][5]  = 204; EUC_TW_FREQ[74][69] = 203; EUC_TW_FREQ[36][82] = 202; EUC_TW_FREQ[46][59] = 201;
        /*
        EUC_TWFreq[38][32] = 200; EUC_TWFreq[74][2]  = 199; EUC_TWFreq[53][31] = 198; EUC_TWFreq[35][38] = 197; EUC_TWFreq[46][62] = 196;
        EUC_TWFreq[77][31] = 195; EUC_TWFreq[55][74] = 194; EUC_TWFreq[66][6]  = 193; EUC_TWFreq[56][21] = 192; EUC_TWFreq[54][78] = 191;
        EUC_TWFreq[43][51] = 190; EUC_TWFreq[64][93] = 189; EUC_TWFreq[92][7]  = 188; EUC_TWFreq[83][89] = 187; EUC_TWFreq[69][9]  = 186;
        EUC_TWFreq[45][4]  = 185; EUC_TWFreq[53][9]  = 184; EUC_TWFreq[43][2]  = 183; EUC_TWFreq[35][11] = 182; EUC_TWFreq[51][25] = 181;
        EUC_TWFreq[52][71] = 180; EUC_TWFreq[81][67] = 179; EUC_TWFreq[37][33] = 178; EUC_TWFreq[38][57] = 177; EUC_TWFreq[39][77] = 176;
        EUC_TWFreq[40][26] = 175; EUC_TWFreq[37][21] = 174; EUC_TWFreq[81][70] = 173; EUC_TWFreq[56][80] = 172; EUC_TWFreq[65][14] = 171;
        EUC_TWFreq[62][47] = 170; EUC_TWFreq[56][54] = 169; EUC_TWFreq[45][17] = 168; EUC_TWFreq[52][52] = 167; EUC_TWFreq[74][30] = 166;
        EUC_TWFreq[60][57] = 165; EUC_TWFreq[41][15] = 164; EUC_TWFreq[47][69] = 163; EUC_TWFreq[61][11] = 162; EUC_TWFreq[72][25] = 161;
        EUC_TWFreq[82][56] = 160; EUC_TWFreq[76][92] = 159; EUC_TWFreq[51][22] = 158; EUC_TWFreq[55][69] = 157; EUC_TWFreq[49][43] = 156;
        EUC_TWFreq[69][49] = 155; EUC_TWFreq[88][42] = 154; EUC_TWFreq[84][41] = 153; EUC_TWFreq[79][33] = 152; EUC_TWFreq[47][17] = 151;
        EUC_TWFreq[52][88] = 150; EUC_TWFreq[63][74] = 149; EUC_TWFreq[50][32] = 148; EUC_TWFreq[65][10] = 147; EUC_TWFreq[57][6]  = 146;
        EUC_TWFreq[52][23] = 145; EUC_TWFreq[36][70] = 144; EUC_TWFreq[65][55] = 143; EUC_TWFreq[35][27] = 142; EUC_TWFreq[57][63] = 141;
        EUC_TWFreq[39][92] = 140; EUC_TWFreq[79][75] = 139; EUC_TWFreq[36][30] = 138; EUC_TWFreq[53][60] = 137; EUC_TWFreq[55][43] = 136;
        EUC_TWFreq[71][22] = 135; EUC_TWFreq[43][16] = 134; EUC_TWFreq[65][21] = 133; EUC_TWFreq[84][51] = 132; EUC_TWFreq[43][64] = 131;
        EUC_TWFreq[87][91] = 130; EUC_TWFreq[47][45] = 129; EUC_TWFreq[65][29] = 128; EUC_TWFreq[88][16] = 127; EUC_TWFreq[50][5]  = 126;
        EUC_TWFreq[47][33] = 125; EUC_TWFreq[46][27] = 124; EUC_TWFreq[85][2]  = 123; EUC_TWFreq[43][77] = 122; EUC_TWFreq[70][9]  = 121;
        EUC_TWFreq[41][54] = 120; EUC_TWFreq[56][12] = 119; EUC_TWFreq[90][65] = 118; EUC_TWFreq[91][50] = 117; EUC_TWFreq[48][41] = 116;
        EUC_TWFreq[35][89] = 115; EUC_TWFreq[90][83] = 114; EUC_TWFreq[44][40] = 113; EUC_TWFreq[50][88] = 112; EUC_TWFreq[72][39] = 111;
        EUC_TWFreq[45][3]  = 110; EUC_TWFreq[71][33] = 109; EUC_TWFreq[39][12] = 108; EUC_TWFreq[59][24] = 107; EUC_TWFreq[60][62] = 106;
        EUC_TWFreq[44][33] = 105; EUC_TWFreq[53][70] = 104; EUC_TWFreq[77][90] = 103; EUC_TWFreq[50][58] = 102; EUC_TWFreq[54][1]  = 101;
        EUC_TWFreq[73][19] = 100; EUC_TWFreq[37][3]  =  99; EUC_TWFreq[49][91] =  98; EUC_TWFreq[88][43] =  97; EUC_TWFreq[36][78] =  96;
        EUC_TWFreq[44][20] =  95; EUC_TWFreq[64][15] =  94; EUC_TWFreq[72][28] =  93; EUC_TWFreq[70][13] =  92; EUC_TWFreq[65][83] =  91;
        EUC_TWFreq[58][68] =  90; EUC_TWFreq[59][32] =  89; EUC_TWFreq[39][13] =  88; EUC_TWFreq[55][64] =  87; EUC_TWFreq[56][59] =  86;
        EUC_TWFreq[39][17] =  85; EUC_TWFreq[55][84] =  84; EUC_TWFreq[77][85] =  83; EUC_TWFreq[60][19] =  82; EUC_TWFreq[62][82] =  81;
        EUC_TWFreq[78][16] =  80; EUC_TWFreq[66][8]  =  79; EUC_TWFreq[39][42] =  78; EUC_TWFreq[61][24] =  77; EUC_TWFreq[57][67] =  76;
        EUC_TWFreq[38][83] =  75; EUC_TWFreq[36][53] =  74; EUC_TWFreq[67][76] =  73; EUC_TWFreq[37][91] =  72; EUC_TWFreq[44][26] =  71;
        EUC_TWFreq[72][86] =  70; EUC_TWFreq[44][87] =  69; EUC_TWFreq[45][50] =  68; EUC_TWFreq[58][4]  =  67; EUC_TWFreq[86][65] =  66;
        EUC_TWFreq[45][56] =  65; EUC_TWFreq[79][49] =  64; EUC_TWFreq[35][3]  =  63; EUC_TWFreq[48][83] =  62; EUC_TWFreq[71][21] =  61;
        EUC_TWFreq[77][93] =  60; EUC_TWFreq[87][92] =  59; EUC_TWFreq[38][35] =  58; EUC_TWFreq[66][17] =  57; EUC_TWFreq[37][66] =  56;
        EUC_TWFreq[51][42] =  55; EUC_TWFreq[57][73] =  54; EUC_TWFreq[51][54] =  53; EUC_TWFreq[75][64] =  52; EUC_TWFreq[35][5]  =  51;
        EUC_TWFreq[49][40] =  50; EUC_TWFreq[58][35] =  49; EUC_TWFreq[67][88] =  48; EUC_TWFreq[60][51] =  47; EUC_TWFreq[36][92] =  46;
        EUC_TWFreq[44][41] =  45; EUC_TWFreq[58][29] =  44; EUC_TWFreq[43][62] =  43; EUC_TWFreq[56][23] =  42; EUC_TWFreq[67][44] =  41;
        EUC_TWFreq[52][91] =  40; EUC_TWFreq[42][81] =  39; EUC_TWFreq[64][25] =  38; EUC_TWFreq[35][36] =  37; EUC_TWFreq[47][73] =  36;
        EUC_TWFreq[36][1]  =  35; EUC_TWFreq[65][84] =  34; EUC_TWFreq[73][1]  =  33; EUC_TWFreq[79][66] =  32; EUC_TWFreq[69][14] =  31;
        EUC_TWFreq[65][28] =  30; EUC_TWFreq[60][93] =  29; EUC_TWFreq[72][79] =  28; EUC_TWFreq[48][0]  =  27; EUC_TWFreq[73][43] =  26;
        EUC_TWFreq[66][47] =  25; EUC_TWFreq[41][18] =  24; EUC_TWFreq[51][10] =  23; EUC_TWFreq[59][7]  =  22; EUC_TWFreq[53][27] =  21;
        EUC_TWFreq[86][67] =  20; EUC_TWFreq[49][87] =  19; EUC_TWFreq[52][28] =  18; EUC_TWFreq[52][12] =  17; EUC_TWFreq[42][30] =  16;
        EUC_TWFreq[65][35] =  15; EUC_TWFreq[46][64] =  14; EUC_TWFreq[71][7]  =  13; EUC_TWFreq[56][57] =  12; EUC_TWFreq[56][31] =  11;
        EUC_TWFreq[41][31] =  10; EUC_TWFreq[48][59] =   9; EUC_TWFreq[63][92] =   8; EUC_TWFreq[62][57] =   7; EUC_TWFreq[65][87] =   6;
        EUC_TWFreq[70][10] =   5; EUC_TWFreq[52][40] =   4; EUC_TWFreq[40][22] =   3; EUC_TWFreq[65][91] =   2; EUC_TWFreq[50][25] =   1;
        EUC_TWFreq[35][84] =   0; EUC_TWFreq[45][90] = 600;
        */

        // ------------------------------------------------------------------------------------GBK_FREQ

        GBK_FREQ[52][132]  = 600; GBK_FREQ[73][135]  = 599; GBK_FREQ[49][123]  = 598; GBK_FREQ[77][146]  = 597; GBK_FREQ[81][123]  = 596;
        GBK_FREQ[82][144]  = 595; GBK_FREQ[51][179]  = 594; GBK_FREQ[83][154]  = 593; GBK_FREQ[71][139]  = 592; GBK_FREQ[64][139]  = 591;
        GBK_FREQ[85][144]  = 590; GBK_FREQ[52][125]  = 589; GBK_FREQ[88][25]   = 588; GBK_FREQ[81][106]  = 587; GBK_FREQ[81][148]  = 586;
        GBK_FREQ[62][137]  = 585; GBK_FREQ[94][0]    = 584; GBK_FREQ[1][64]    = 583; GBK_FREQ[67][163]  = 582; GBK_FREQ[20][190]  = 581;
        GBK_FREQ[57][131]  = 580; GBK_FREQ[29][169]  = 579; GBK_FREQ[72][143]  = 578; GBK_FREQ[0][173]   = 577; GBK_FREQ[11][23]   = 576;
        GBK_FREQ[61][141]  = 575; GBK_FREQ[60][123]  = 574; GBK_FREQ[81][114]  = 573; GBK_FREQ[82][131]  = 572; GBK_FREQ[67][156]  = 571;
        GBK_FREQ[71][167]  = 570; GBK_FREQ[20][50]   = 569; GBK_FREQ[77][132]  = 568; GBK_FREQ[84][38]   = 567; GBK_FREQ[26][29]   = 566;
        GBK_FREQ[74][187]  = 565; GBK_FREQ[62][116]  = 564; GBK_FREQ[67][135]  = 563; GBK_FREQ[5][86]    = 562; GBK_FREQ[72][186]  = 561;
        GBK_FREQ[75][161]  = 560; GBK_FREQ[78][130]  = 559; GBK_FREQ[94][30]   = 558; GBK_FREQ[84][72]   = 557; GBK_FREQ[1][67]    = 556;
        GBK_FREQ[75][172]  = 555; GBK_FREQ[74][185]  = 554; GBK_FREQ[53][160]  = 553; GBK_FREQ[123][14]  = 552; GBK_FREQ[79][97]   = 551;
        GBK_FREQ[85][110]  = 550; GBK_FREQ[78][171]  = 549; GBK_FREQ[52][131]  = 548; GBK_FREQ[56][100]  = 547; GBK_FREQ[50][182]  = 546;
        GBK_FREQ[94][64]   = 545; GBK_FREQ[106][74]  = 544; GBK_FREQ[11][102]  = 543; GBK_FREQ[53][124]  = 542; GBK_FREQ[24][3]    = 541;
        GBK_FREQ[86][148]  = 540; GBK_FREQ[53][184]  = 539; GBK_FREQ[86][147]  = 538; GBK_FREQ[96][161]  = 537; GBK_FREQ[82][77]   = 536;
        GBK_FREQ[59][146]  = 535; GBK_FREQ[84][126]  = 534; GBK_FREQ[79][132]  = 533; GBK_FREQ[85][123]  = 532; GBK_FREQ[71][101]  = 531;
        GBK_FREQ[85][106]  = 530; GBK_FREQ[6][184]   = 529; GBK_FREQ[57][156]  = 528; GBK_FREQ[75][104]  = 527; GBK_FREQ[50][137]  = 526;
        GBK_FREQ[79][133]  = 525; GBK_FREQ[76][108]  = 524; GBK_FREQ[57][142]  = 523; GBK_FREQ[84][130]  = 522; GBK_FREQ[52][128]  = 521;
        GBK_FREQ[47][44]   = 520; GBK_FREQ[52][152]  = 519; GBK_FREQ[54][104]  = 518; GBK_FREQ[30][47]   = 517; GBK_FREQ[71][123]  = 516;
        GBK_FREQ[52][107]  = 515; GBK_FREQ[45][84]   = 514; GBK_FREQ[107][118] = 513; GBK_FREQ[5][161]   = 512; GBK_FREQ[48][126]  = 511;
        GBK_FREQ[67][170]  = 510; GBK_FREQ[43][6]    = 509; GBK_FREQ[70][112]  = 508; GBK_FREQ[86][174]  = 507; GBK_FREQ[84][166]  = 506;
        GBK_FREQ[79][130]  = 505; GBK_FREQ[57][141]  = 504; GBK_FREQ[81][178]  = 503; GBK_FREQ[56][187]  = 502; GBK_FREQ[81][162]  = 501;
        GBK_FREQ[53][104]  = 500; GBK_FREQ[123][35]  = 499; GBK_FREQ[70][169]  = 498; GBK_FREQ[69][164]  = 497; GBK_FREQ[109][61]  = 496;
        GBK_FREQ[73][130]  = 495; GBK_FREQ[62][134]  = 494; GBK_FREQ[54][125]  = 493; GBK_FREQ[79][105]  = 492; GBK_FREQ[70][165]  = 491;
        GBK_FREQ[71][189]  = 490; GBK_FREQ[23][147]  = 489; GBK_FREQ[51][139]  = 488; GBK_FREQ[47][137]  = 487; GBK_FREQ[77][123]  = 486;
        GBK_FREQ[86][183]  = 485; GBK_FREQ[63][173]  = 484; GBK_FREQ[79][144]  = 483; GBK_FREQ[84][159]  = 482; GBK_FREQ[60][91]   = 481;
        GBK_FREQ[66][187]  = 480; GBK_FREQ[73][114]  = 479; GBK_FREQ[85][56]   = 478; GBK_FREQ[71][149]  = 477; GBK_FREQ[84][189]  = 476;
        GBK_FREQ[104][31]  = 475; GBK_FREQ[83][82]   = 474; GBK_FREQ[68][35]   = 473; GBK_FREQ[11][77]   = 472; GBK_FREQ[15][155]  = 471;
        GBK_FREQ[83][153]  = 470; GBK_FREQ[71][1]    = 469; GBK_FREQ[53][190]  = 468; GBK_FREQ[50][135]  = 467; GBK_FREQ[3][147]   = 466;
        GBK_FREQ[48][136]  = 465; GBK_FREQ[66][166]  = 464; GBK_FREQ[55][159]  = 463; GBK_FREQ[82][150]  = 462; GBK_FREQ[58][178]  = 461;
        GBK_FREQ[64][102]  = 460; GBK_FREQ[16][106]  = 459; GBK_FREQ[68][110]  = 458; GBK_FREQ[54][14]   = 457; GBK_FREQ[60][140]  = 456;
        GBK_FREQ[91][71]   = 455; GBK_FREQ[54][150]  = 454; GBK_FREQ[78][177]  = 453; GBK_FREQ[78][117]  = 452; GBK_FREQ[104][12]  = 451;
        GBK_FREQ[73][150]  = 450; GBK_FREQ[51][142]  = 449; GBK_FREQ[81][145]  = 448; GBK_FREQ[66][183]  = 447; GBK_FREQ[51][178]  = 446;
        GBK_FREQ[75][107]  = 445; GBK_FREQ[65][119]  = 444; GBK_FREQ[69][176]  = 443; GBK_FREQ[59][122]  = 442; GBK_FREQ[78][160]  = 441;
        GBK_FREQ[85][183]  = 440; GBK_FREQ[105][16]  = 439; GBK_FREQ[73][110]  = 438; GBK_FREQ[104][39]  = 437; GBK_FREQ[119][16]  = 436;
        GBK_FREQ[76][162]  = 435; GBK_FREQ[67][152]  = 434; GBK_FREQ[82][24]   = 433; GBK_FREQ[73][121]  = 432; GBK_FREQ[83][83]   = 431;
        GBK_FREQ[82][145]  = 430; GBK_FREQ[49][133]  = 429; GBK_FREQ[94][13]   = 428; GBK_FREQ[58][139]  = 427; GBK_FREQ[74][189]  = 426;
        GBK_FREQ[66][177]  = 425; GBK_FREQ[85][184]  = 424; GBK_FREQ[55][183]  = 423; GBK_FREQ[71][107]  = 422; GBK_FREQ[11][98]   = 421;
        GBK_FREQ[72][153]  = 420; GBK_FREQ[2][137]   = 419; GBK_FREQ[59][147]  = 418; GBK_FREQ[58][152]  = 417; GBK_FREQ[55][144]  = 416;
        GBK_FREQ[73][125]  = 415; GBK_FREQ[52][154]  = 414; GBK_FREQ[70][178]  = 413; GBK_FREQ[79][148]  = 412; GBK_FREQ[63][143]  = 411;
        GBK_FREQ[50][140]  = 410; GBK_FREQ[47][145]  = 409; GBK_FREQ[48][123]  = 408; GBK_FREQ[56][107]  = 407; GBK_FREQ[84][83]   = 406;
        GBK_FREQ[59][112]  = 405; GBK_FREQ[124][72]  = 404; GBK_FREQ[79][99]   = 403; GBK_FREQ[3][37]    = 402; GBK_FREQ[114][55]  = 401;
        GBK_FREQ[85][152]  = 400; GBK_FREQ[60][47]   = 399; GBK_FREQ[65][96]   = 398; GBK_FREQ[74][110]  = 397; GBK_FREQ[86][182]  = 396;
        GBK_FREQ[50][99]   = 395; GBK_FREQ[67][186]  = 394; GBK_FREQ[81][74]   = 393; GBK_FREQ[80][37]   = 392; GBK_FREQ[21][60]   = 391;
        GBK_FREQ[110][12]  = 390; GBK_FREQ[60][162]  = 389; GBK_FREQ[29][115]  = 388; GBK_FREQ[83][130]  = 387; GBK_FREQ[52][136]  = 386;
        GBK_FREQ[63][114]  = 385; GBK_FREQ[49][127]  = 384; GBK_FREQ[83][109]  = 383; GBK_FREQ[66][128]  = 382; GBK_FREQ[78][136]  = 381;
        GBK_FREQ[81][180]  = 380; GBK_FREQ[76][104]  = 379; GBK_FREQ[56][156]  = 378; GBK_FREQ[61][23]   = 377; GBK_FREQ[4][30]    = 376;
        GBK_FREQ[69][154]  = 375; GBK_FREQ[100][37]  = 374; GBK_FREQ[54][177]  = 373; GBK_FREQ[23][119]  = 372; GBK_FREQ[71][171]  = 371;
        GBK_FREQ[84][146]  = 370; GBK_FREQ[20][184]  = 369; GBK_FREQ[86][76]   = 368; GBK_FREQ[74][132]  = 367; GBK_FREQ[47][97]   = 366;
        GBK_FREQ[82][137]  = 365; GBK_FREQ[94][56]   = 364; GBK_FREQ[92][30]   = 363; GBK_FREQ[19][117]  = 362; GBK_FREQ[48][173]  = 361;
        GBK_FREQ[2][136]   = 360; GBK_FREQ[7][182]   = 359; GBK_FREQ[74][188]  = 358; GBK_FREQ[14][132]  = 357; GBK_FREQ[62][172]  = 356;
        GBK_FREQ[25][39]   = 355; GBK_FREQ[85][129]  = 354; GBK_FREQ[64][98]   = 353; GBK_FREQ[67][127]  = 352; GBK_FREQ[72][167]  = 351;
        GBK_FREQ[57][143]  = 350; GBK_FREQ[76][187]  = 349; GBK_FREQ[83][181]  = 348; GBK_FREQ[84][10]   = 347; GBK_FREQ[55][166]  = 346;
        GBK_FREQ[55][188]  = 345; GBK_FREQ[13][151]  = 344; GBK_FREQ[62][124]  = 343; GBK_FREQ[53][136]  = 342; GBK_FREQ[106][57]  = 341;
        GBK_FREQ[47][166]  = 340; GBK_FREQ[109][30]  = 339; GBK_FREQ[78][114]  = 338; GBK_FREQ[83][19]   = 337; GBK_FREQ[56][162]  = 336;
        GBK_FREQ[60][177]  = 335; GBK_FREQ[88][9]    = 334; GBK_FREQ[74][163]  = 333; GBK_FREQ[52][156]  = 332; GBK_FREQ[71][180]  = 331;
        GBK_FREQ[60][57]   = 330; GBK_FREQ[72][173]  = 329; GBK_FREQ[82][91]   = 328; GBK_FREQ[51][186]  = 327; GBK_FREQ[75][86]   = 326;
        GBK_FREQ[75][78]   = 325; GBK_FREQ[76][170]  = 324; GBK_FREQ[60][147]  = 323; GBK_FREQ[82][75]   = 322; GBK_FREQ[80][148]  = 321;
        GBK_FREQ[86][150]  = 320; GBK_FREQ[13][95]   = 319; GBK_FREQ[0][11]    = 318; GBK_FREQ[84][190]  = 317; GBK_FREQ[76][166]  = 316;
        GBK_FREQ[14][72]   = 315; GBK_FREQ[67][144]  = 314; GBK_FREQ[84][44]   = 313; GBK_FREQ[72][125]  = 312; GBK_FREQ[66][127]  = 311;
        GBK_FREQ[60][25]   = 310; GBK_FREQ[70][146]  = 309; GBK_FREQ[79][135]  = 308; GBK_FREQ[54][135]  = 307; GBK_FREQ[60][104]  = 306;
        GBK_FREQ[55][132]  = 305; GBK_FREQ[94][2]    = 304; GBK_FREQ[54][133]  = 303; GBK_FREQ[56][190]  = 302; GBK_FREQ[58][174]  = 301;
        GBK_FREQ[80][144]  = 300; GBK_FREQ[85][113]  = 299;
        /*
        GBKFreq[83][15]  = 298; GBKFreq[105][80] = 297; GBKFreq[7][179]  = 296; GBKFreq[93][4]   = 295; GBKFreq[123][40] = 294;
        GBKFreq[85][120] = 293; GBKFreq[77][165] = 292; GBKFreq[86][67]  = 291; GBKFreq[25][162] = 290; GBKFreq[77][183] = 289;
        GBKFreq[83][71]  = 288; GBKFreq[78][99]  = 287; GBKFreq[72][177] = 286; GBKFreq[71][97]  = 285; GBKFreq[58][111] = 284;
        GBKFreq[77][175] = 283; GBKFreq[76][181] = 282; GBKFreq[71][142] = 281; GBKFreq[64][150] = 280; GBKFreq[5][142]  = 279;
        GBKFreq[73][128] = 278; GBKFreq[73][156] = 277; GBKFreq[60][188] = 276; GBKFreq[64][56]  = 275; GBKFreq[74][128] = 274;
        GBKFreq[48][163] = 273; GBKFreq[54][116] = 272; GBKFreq[73][127] = 271; GBKFreq[16][176] = 270; GBKFreq[62][149] = 269;
        GBKFreq[105][96] = 268; GBKFreq[55][186] = 267; GBKFreq[4][51]   = 266; GBKFreq[48][113] = 265; GBKFreq[48][152] = 264;
        GBKFreq[23][9]   = 263; GBKFreq[56][102] = 262; GBKFreq[11][81]  = 261; GBKFreq[82][112] = 260; GBKFreq[65][85]  = 259;
        GBKFreq[69][125] = 258; GBKFreq[68][31]  = 257; GBKFreq[5][20]   = 256; GBKFreq[60][176] = 255; GBKFreq[82][81]  = 254;
        GBKFreq[72][107] = 253; GBKFreq[3][52]   = 252; GBKFreq[71][157] = 251; GBKFreq[24][46]  = 250; GBKFreq[69][108] = 249;
        GBKFreq[78][178] = 248; GBKFreq[9][69]   = 247; GBKFreq[73][144] = 246; GBKFreq[63][187] = 245; GBKFreq[68][36]  = 244;
        GBKFreq[47][151] = 243; GBKFreq[14][74]  = 242; GBKFreq[47][114] = 241; GBKFreq[80][171] = 240; GBKFreq[75][152] = 239;
        GBKFreq[86][40]  = 238; GBKFreq[93][43]  = 237; GBKFreq[2][50]   = 236; GBKFreq[62][66]  = 235; GBKFreq[1][183]  = 234;
        GBKFreq[74][124] = 233; GBKFreq[58][104] = 232; GBKFreq[83][106] = 231; GBKFreq[60][144] = 230; GBKFreq[48][99]  = 229;
        GBKFreq[54][157] = 228; GBKFreq[70][179] = 227; GBKFreq[61][127] = 226; GBKFreq[57][135] = 225; GBKFreq[59][190] = 224;
        GBKFreq[77][116] = 223; GBKFreq[26][17]  = 222; GBKFreq[60][13]  = 221; GBKFreq[71][38]  = 220; GBKFreq[85][177] = 219;
        GBKFreq[59][73]  = 218; GBKFreq[50][150] = 217; GBKFreq[79][102] = 216; GBKFreq[76][118] = 215; GBKFreq[67][132] = 214;
        GBKFreq[73][146] = 213; GBKFreq[83][184] = 212; GBKFreq[86][159] = 211; GBKFreq[95][120] = 210; GBKFreq[23][139] = 209;
        GBKFreq[64][183] = 208; GBKFreq[85][103] = 207; GBKFreq[41][90]  = 206; GBKFreq[87][72]  = 205; GBKFreq[62][104] = 204;
        GBKFreq[79][168] = 203; GBKFreq[79][150] = 202; GBKFreq[104][20] = 201; GBKFreq[56][114] = 200; GBKFreq[84][26]  = 199;
        GBKFreq[57][99]  = 198; GBKFreq[62][154] = 197; GBKFreq[47][98]  = 196; GBKFreq[61][64]  = 195; GBKFreq[112][18] = 194;
        GBKFreq[123][19] = 193; GBKFreq[4][98]   = 192; GBKFreq[47][163] = 191; GBKFreq[66][188] = 190; GBKFreq[81][85]  = 189;
        GBKFreq[82][30]  = 188; GBKFreq[65][83]  = 187; GBKFreq[67][24]  = 186; GBKFreq[68][179] = 185; GBKFreq[55][177] = 184;
        GBKFreq[2][122]  = 183; GBKFreq[47][139] = 182; GBKFreq[79][158] = 181; GBKFreq[64][143] = 180; GBKFreq[100][24] = 179;
        GBKFreq[73][103] = 178; GBKFreq[50][148] = 177; GBKFreq[86][97]  = 176; GBKFreq[59][116] = 175; GBKFreq[64][173] = 174;
        GBKFreq[99][91]  = 173; GBKFreq[11][99]  = 172; GBKFreq[78][179] = 171; GBKFreq[18][17]  = 170; GBKFreq[58][185] = 169;
        GBKFreq[47][165] = 168; GBKFreq[67][131] = 167; GBKFreq[94][40]  = 166; GBKFreq[74][153] = 165; GBKFreq[79][142] = 164;
        GBKFreq[57][98]  = 163; GBKFreq[1][164]  = 162; GBKFreq[55][168] = 161; GBKFreq[13][141] = 160; GBKFreq[51][31]  = 159;
        GBKFreq[57][178] = 158; GBKFreq[50][189] = 157; GBKFreq[60][167] = 156; GBKFreq[80][34]  = 155; GBKFreq[109][80] = 154;
        GBKFreq[85][54]  = 153; GBKFreq[69][183] = 152; GBKFreq[67][143] = 151; GBKFreq[47][120] = 150; GBKFreq[45][75]  = 149;
        GBKFreq[82][98]  = 148; GBKFreq[83][22]  = 147; GBKFreq[13][103] = 146; GBKFreq[49][174] = 145; GBKFreq[57][181] = 144;
        GBKFreq[64][127] = 143; GBKFreq[61][131] = 142; GBKFreq[52][180] = 141; GBKFreq[74][134] = 140; GBKFreq[84][187] = 139;
        GBKFreq[81][189] = 138; GBKFreq[47][160] = 137; GBKFreq[66][148] = 136; GBKFreq[7][4]    = 135; GBKFreq[85][134] = 134;
        GBKFreq[88][13]  = 133; GBKFreq[88][80]  = 132; GBKFreq[69][166] = 131; GBKFreq[86][18]  = 130; GBKFreq[79][141] = 129;
        GBKFreq[50][108] = 128; GBKFreq[94][69]  = 127; GBKFreq[81][110] = 126; GBKFreq[69][119] = 125; GBKFreq[72][161] = 124;
        GBKFreq[106][45] = 123; GBKFreq[73][124] = 122; GBKFreq[94][28]  = 121; GBKFreq[63][174] = 120; GBKFreq[3][149]  = 119;
        GBKFreq[24][160] = 118; GBKFreq[113][94] = 117; GBKFreq[56][138] = 116; GBKFreq[64][185] = 115; GBKFreq[86][56]  = 114;
        GBKFreq[56][150] = 113; GBKFreq[110][55] = 112; GBKFreq[28][13]  = 111; GBKFreq[54][190] = 110; GBKFreq[8][180]  = 109;
        GBKFreq[73][149] = 108; GBKFreq[80][155] = 107; GBKFreq[83][172] = 106; GBKFreq[67][174] = 105; GBKFreq[64][180] = 104;
        GBKFreq[84][46]  = 103; GBKFreq[91][74]  = 102; GBKFreq[69][134] = 101; GBKFreq[61][107] = 100; GBKFreq[47][171] =  99;
        GBKFreq[59][51]  =  98; GBKFreq[109][74] =  97; GBKFreq[64][174] =  96; GBKFreq[52][151] =  95; GBKFreq[51][176] =  94;
        GBKFreq[80][157] =  93; GBKFreq[94][31]  =  92; GBKFreq[79][155] =  91; GBKFreq[72][174] =  90; GBKFreq[69][113] =  89;
        GBKFreq[83][167] =  88; GBKFreq[83][122] =  87; GBKFreq[8][178]  =  86; GBKFreq[70][186] =  85; GBKFreq[59][153] =  84;
        GBKFreq[84][68]  =  83; GBKFreq[79][39]  =  82; GBKFreq[47][180] =  81; GBKFreq[88][53]  =  80; GBKFreq[57][154] =  79;
        GBKFreq[47][153] =  78; GBKFreq[3][153]  =  77; GBKFreq[76][134] =  76; GBKFreq[51][166] =  75; GBKFreq[58][176] =  74;
        GBKFreq[27][138] =  73; GBKFreq[73][126] =  72; GBKFreq[76][185] =  71; GBKFreq[52][186] =  70; GBKFreq[81][151] =  69;
        GBKFreq[26][50]  =  68; GBKFreq[76][173] =  67; GBKFreq[106][56] =  66; GBKFreq[85][142] =  65; GBKFreq[11][103] =  64;
        GBKFreq[69][159] =  63; GBKFreq[53][142] =  62; GBKFreq[7][6]    =  61; GBKFreq[84][59]  =  60; GBKFreq[86][3]   =  59;
        GBKFreq[64][144] =  58; GBKFreq[1][187]  =  57; GBKFreq[82][128] =  56; GBKFreq[3][66]   =  55; GBKFreq[68][133] =  54;
        GBKFreq[55][167] =  53; GBKFreq[52][130] =  52; GBKFreq[61][133] =  51; GBKFreq[72][181] =  50; GBKFreq[25][98]  =  49;
        GBKFreq[84][149] =  48; GBKFreq[91][91]  =  47; GBKFreq[47][188] =  46; GBKFreq[68][130] =  45; GBKFreq[22][44]  =  44;
        GBKFreq[81][121] =  43; GBKFreq[72][140] =  42; GBKFreq[55][133] =  41; GBKFreq[55][185] =  40; GBKFreq[56][105] =  39;
        GBKFreq[60][30]  =  38; GBKFreq[70][103] =  37; GBKFreq[62][141] =  36; GBKFreq[70][144] =  35; GBKFreq[59][111] =  34;
        GBKFreq[54][17]  =  33; GBKFreq[18][190] =  32; GBKFreq[65][164] =  31; GBKFreq[83][125] =  30; GBKFreq[61][121] =  29;
        GBKFreq[48][13]  =  28; GBKFreq[51][189] =  27; GBKFreq[65][68]  =  26; GBKFreq[7][0]    =  25; GBKFreq[76][188] =  24;
        GBKFreq[85][117] =  23; GBKFreq[45][33]  =  22; GBKFreq[78][187] =  21; GBKFreq[106][48] =  20; GBKFreq[59][52]  =  19;
        GBKFreq[86][185] =  18; GBKFreq[84][121] =  17; GBKFreq[82][189] =  16; GBKFreq[68][156] =  15; GBKFreq[55][125] =  14;
        GBKFreq[65][175] =  13; GBKFreq[7][140]  =  12; GBKFreq[50][106] =  11; GBKFreq[59][124] =  10; GBKFreq[67][115] =   9;
        GBKFreq[82][114] =   8; GBKFreq[74][121] =   7; GBKFreq[106][69] =   6; GBKFreq[94][27]  =   5; GBKFreq[78][98]  =   4;
        GBKFreq[85][186] =   3; GBKFreq[108][90] =   2; GBKFreq[62][160] =   1; GBKFreq[60][169] =   0;
        */

        // ------------------------------------------------------------------------------------KR_FREQ

        KR_FREQ[31][43] = 600; KR_FREQ[19][56] = 599; KR_FREQ[38][46] = 598; KR_FREQ[3][3]   = 597; KR_FREQ[29][77] = 596;
        KR_FREQ[19][33] = 595; KR_FREQ[30][0]  = 594; KR_FREQ[29][89] = 593; KR_FREQ[31][26] = 592; KR_FREQ[31][38] = 591;
        KR_FREQ[32][85] = 590; KR_FREQ[15][0]  = 589; KR_FREQ[16][54] = 588; KR_FREQ[15][76] = 587; KR_FREQ[31][25] = 586;
        KR_FREQ[23][13] = 585; KR_FREQ[28][34] = 584; KR_FREQ[18][9]  = 583; KR_FREQ[29][37] = 582; KR_FREQ[22][45] = 581;
        KR_FREQ[19][46] = 580; KR_FREQ[16][65] = 579; KR_FREQ[23][5]  = 578; KR_FREQ[26][70] = 577; KR_FREQ[31][53] = 576;
        KR_FREQ[27][12] = 575; KR_FREQ[30][67] = 574; KR_FREQ[31][57] = 573; KR_FREQ[20][20] = 572; KR_FREQ[30][31] = 571;
        KR_FREQ[20][72] = 570; KR_FREQ[15][51] = 569; KR_FREQ[3][8]   = 568; KR_FREQ[32][53] = 567; KR_FREQ[27][85] = 566;
        KR_FREQ[25][23] = 565; KR_FREQ[15][44] = 564; KR_FREQ[32][3]  = 563; KR_FREQ[31][68] = 562; KR_FREQ[30][24] = 561;
        KR_FREQ[29][49] = 560; KR_FREQ[27][49] = 559; KR_FREQ[23][23] = 558; KR_FREQ[31][91] = 557; KR_FREQ[31][46] = 556;
        KR_FREQ[19][74] = 555; KR_FREQ[27][27] = 554; KR_FREQ[3][17]  = 553; KR_FREQ[20][38] = 552; KR_FREQ[21][82] = 551;
        KR_FREQ[28][25] = 550; KR_FREQ[32][5]  = 549; KR_FREQ[31][23] = 548; KR_FREQ[25][45] = 547; KR_FREQ[32][87] = 546;
        KR_FREQ[18][26] = 545; KR_FREQ[24][10] = 544; KR_FREQ[26][82] = 543; KR_FREQ[15][89] = 542; KR_FREQ[28][36] = 541;
        KR_FREQ[28][31] = 540; KR_FREQ[16][23] = 539; KR_FREQ[16][77] = 538; KR_FREQ[19][84] = 537; KR_FREQ[23][72] = 536;
        KR_FREQ[38][48] = 535; KR_FREQ[23][2]  = 534; KR_FREQ[30][20] = 533; KR_FREQ[38][47] = 532; KR_FREQ[39][12] = 531;
        KR_FREQ[23][21] = 530; KR_FREQ[18][17] = 529; KR_FREQ[30][87] = 528; KR_FREQ[29][62] = 527; KR_FREQ[29][87] = 526;
        KR_FREQ[34][53] = 525; KR_FREQ[32][29] = 524; KR_FREQ[35][0]  = 523; KR_FREQ[24][43] = 522; KR_FREQ[36][44] = 521;
        KR_FREQ[20][30] = 520; KR_FREQ[39][86] = 519; KR_FREQ[22][14] = 518; KR_FREQ[29][39] = 517; KR_FREQ[28][38] = 516;
        KR_FREQ[23][79] = 515; KR_FREQ[24][56] = 514; KR_FREQ[29][63] = 513; KR_FREQ[31][45] = 512; KR_FREQ[23][26] = 511;
        KR_FREQ[15][87] = 510; KR_FREQ[30][74] = 509; KR_FREQ[24][69] = 508; KR_FREQ[20][4]  = 507; KR_FREQ[27][50] = 506;
        KR_FREQ[30][75] = 505; KR_FREQ[24][13] = 504; KR_FREQ[30][8]  = 503; KR_FREQ[31][6]  = 502; KR_FREQ[25][80] = 501;
        KR_FREQ[36][8]  = 500; KR_FREQ[15][18] = 499; KR_FREQ[39][23] = 498; KR_FREQ[16][24] = 497; KR_FREQ[31][89] = 496;
        KR_FREQ[15][71] = 495; KR_FREQ[15][57] = 494; KR_FREQ[30][11] = 493; KR_FREQ[15][36] = 492; KR_FREQ[16][60] = 491;
        KR_FREQ[24][45] = 490; KR_FREQ[37][35] = 489; KR_FREQ[24][87] = 488; KR_FREQ[20][45] = 487; KR_FREQ[31][90] = 486;
        KR_FREQ[32][21] = 485; KR_FREQ[19][70] = 484; KR_FREQ[24][15] = 483; KR_FREQ[26][92] = 482; KR_FREQ[37][13] = 481;
        KR_FREQ[39][2]  = 480; KR_FREQ[23][70] = 479; KR_FREQ[27][25] = 478; KR_FREQ[15][69] = 477; KR_FREQ[19][61] = 476;
        KR_FREQ[31][58] = 475; KR_FREQ[24][57] = 474; KR_FREQ[36][74] = 473; KR_FREQ[21][6]  = 472; KR_FREQ[30][44] = 471;
        KR_FREQ[15][91] = 470; KR_FREQ[27][16] = 469; KR_FREQ[29][42] = 468; KR_FREQ[33][86] = 467; KR_FREQ[29][41] = 466;
        KR_FREQ[20][68] = 465; KR_FREQ[25][47] = 464; KR_FREQ[22][0]  = 463; KR_FREQ[18][14] = 462; KR_FREQ[31][28] = 461;
        KR_FREQ[15][2]  = 460; KR_FREQ[23][76] = 459; KR_FREQ[38][32] = 458; KR_FREQ[29][82] = 457; KR_FREQ[21][86] = 456;
        KR_FREQ[24][62] = 455; KR_FREQ[31][64] = 454; KR_FREQ[38][26] = 453; KR_FREQ[32][86] = 452; KR_FREQ[22][32] = 451;
        KR_FREQ[19][59] = 450; KR_FREQ[34][18] = 449; KR_FREQ[18][54] = 448; KR_FREQ[38][63] = 447; KR_FREQ[36][23] = 446;
        KR_FREQ[35][35] = 445; KR_FREQ[32][62] = 444; KR_FREQ[28][35] = 443; KR_FREQ[27][13] = 442; KR_FREQ[31][59] = 441;
        KR_FREQ[29][29] = 440; KR_FREQ[15][64] = 439; KR_FREQ[26][84] = 438; KR_FREQ[21][90] = 437; KR_FREQ[20][24] = 436;
        KR_FREQ[16][18] = 435; KR_FREQ[22][23] = 434; KR_FREQ[31][14] = 433; KR_FREQ[15][1]  = 432; KR_FREQ[18][63] = 431;
        KR_FREQ[19][10] = 430; KR_FREQ[25][49] = 429; KR_FREQ[36][57] = 428; KR_FREQ[20][22] = 427; KR_FREQ[15][15] = 426;
        KR_FREQ[31][51] = 425; KR_FREQ[24][60] = 424; KR_FREQ[31][70] = 423; KR_FREQ[15][7]  = 422; KR_FREQ[28][40] = 421;
        KR_FREQ[18][41] = 420; KR_FREQ[15][38] = 419; KR_FREQ[32][0]  = 418; KR_FREQ[19][51] = 417; KR_FREQ[34][62] = 416;
        KR_FREQ[16][27] = 415; KR_FREQ[20][70] = 414; KR_FREQ[22][33] = 413; KR_FREQ[26][73] = 412; KR_FREQ[20][79] = 411;
        KR_FREQ[23][6]  = 410; KR_FREQ[24][85] = 409; KR_FREQ[38][51] = 408; KR_FREQ[29][88] = 407; KR_FREQ[38][55] = 406;
        KR_FREQ[32][32] = 405; KR_FREQ[27][18] = 404; KR_FREQ[23][87] = 403; KR_FREQ[35][6]  = 402; KR_FREQ[34][27] = 401;
        KR_FREQ[39][35] = 400; KR_FREQ[30][88] = 399; KR_FREQ[32][92] = 398; KR_FREQ[32][49] = 397; KR_FREQ[24][61] = 396;
        KR_FREQ[18][74] = 395; KR_FREQ[23][77] = 394; KR_FREQ[23][50] = 393; KR_FREQ[23][32] = 392; KR_FREQ[23][36] = 391;
        KR_FREQ[38][38] = 390; KR_FREQ[29][86] = 389; KR_FREQ[36][15] = 388; KR_FREQ[31][50] = 387; KR_FREQ[15][86] = 386;
        KR_FREQ[39][13] = 385; KR_FREQ[34][26] = 384; KR_FREQ[19][34] = 383; KR_FREQ[16][3]  = 382; KR_FREQ[26][93] = 381;
        KR_FREQ[19][67] = 380; KR_FREQ[24][72] = 379; KR_FREQ[29][17] = 378; KR_FREQ[23][24] = 377; KR_FREQ[25][19] = 376;
        KR_FREQ[18][65] = 375; KR_FREQ[30][78] = 374; KR_FREQ[27][52] = 373; KR_FREQ[22][18] = 372; KR_FREQ[16][38] = 371;
        KR_FREQ[21][26] = 370; KR_FREQ[34][20] = 369; KR_FREQ[15][42] = 368; KR_FREQ[16][71] = 367; KR_FREQ[17][17] = 366;
        KR_FREQ[24][71] = 365; KR_FREQ[18][84] = 364; KR_FREQ[15][40] = 363; KR_FREQ[31][62] = 362; KR_FREQ[15][8]  = 361;
        KR_FREQ[16][69] = 360; KR_FREQ[29][79] = 359; KR_FREQ[38][91] = 358; KR_FREQ[31][92] = 357; KR_FREQ[20][77] = 356;
        KR_FREQ[3][16]  = 355; KR_FREQ[27][87] = 354; KR_FREQ[16][25] = 353; KR_FREQ[36][33] = 352; KR_FREQ[37][76] = 351;
        KR_FREQ[30][12] = 350; KR_FREQ[26][75] = 349; KR_FREQ[25][14] = 348; KR_FREQ[32][26] = 347; KR_FREQ[23][22] = 346;
        KR_FREQ[20][90] = 345; KR_FREQ[19][8]  = 344; KR_FREQ[38][41] = 343; KR_FREQ[34][2]  = 342; KR_FREQ[39][4]  = 341;
        KR_FREQ[27][89] = 340; KR_FREQ[28][41] = 339; KR_FREQ[28][44] = 338; KR_FREQ[24][92] = 337; KR_FREQ[34][65] = 336;
        KR_FREQ[39][14] = 335; KR_FREQ[21][38] = 334; KR_FREQ[19][31] = 333; KR_FREQ[37][39] = 332; KR_FREQ[33][41] = 331;
        KR_FREQ[38][4]  = 330; KR_FREQ[23][80] = 329; KR_FREQ[25][24] = 328; KR_FREQ[37][17] = 327; KR_FREQ[22][16] = 326;
        KR_FREQ[22][46] = 325; KR_FREQ[33][91] = 324; KR_FREQ[24][89] = 323; KR_FREQ[30][52] = 322; KR_FREQ[29][38] = 321;
        KR_FREQ[38][85] = 320; KR_FREQ[15][12] = 319; KR_FREQ[27][58] = 318; KR_FREQ[29][52] = 317; KR_FREQ[37][38] = 316;
        KR_FREQ[34][41] = 315; KR_FREQ[31][65] = 314; KR_FREQ[29][53] = 313; KR_FREQ[22][47] = 312; KR_FREQ[22][19] = 311;
        KR_FREQ[26][0]  = 310; KR_FREQ[37][86] = 309; KR_FREQ[35][4]  = 308; KR_FREQ[36][54] = 307; KR_FREQ[20][76] = 306;
        KR_FREQ[30][9]  = 305; KR_FREQ[30][33] = 304; KR_FREQ[23][17] = 303; KR_FREQ[23][33] = 302; KR_FREQ[38][52] = 301;
        KR_FREQ[15][19] = 300; KR_FREQ[28][45] = 299; KR_FREQ[29][78] = 298; KR_FREQ[23][15] = 297; KR_FREQ[33][5]  = 296;
        KR_FREQ[17][40] = 295; KR_FREQ[30][83] = 294; KR_FREQ[18][1]  = 293; KR_FREQ[30][81] = 292; KR_FREQ[19][40] = 291;
        KR_FREQ[24][47] = 290; KR_FREQ[17][56] = 289; KR_FREQ[39][80] = 288; KR_FREQ[30][46] = 287; KR_FREQ[16][61] = 286;
        KR_FREQ[26][78] = 285; KR_FREQ[26][57] = 284; KR_FREQ[20][46] = 283; KR_FREQ[25][15] = 282; KR_FREQ[25][91] = 281;
        KR_FREQ[21][83] = 280; KR_FREQ[30][77] = 279; KR_FREQ[35][30] = 278; KR_FREQ[30][34] = 277; KR_FREQ[20][69] = 276;
        KR_FREQ[35][10] = 275; KR_FREQ[29][70] = 274; KR_FREQ[22][50] = 273; KR_FREQ[18][0]  = 272; KR_FREQ[22][64] = 271;
        KR_FREQ[38][65] = 270; KR_FREQ[22][70] = 269; KR_FREQ[24][58] = 268; KR_FREQ[19][66] = 267; KR_FREQ[30][59] = 266;
        KR_FREQ[37][14] = 265; KR_FREQ[16][56] = 264; KR_FREQ[29][85] = 263; KR_FREQ[31][15] = 262; KR_FREQ[36][84] = 261;
        KR_FREQ[39][15] = 260; KR_FREQ[39][90] = 259; KR_FREQ[18][12] = 258; KR_FREQ[21][93] = 257; KR_FREQ[24][66] = 256;
        KR_FREQ[27][90] = 255; KR_FREQ[25][90] = 254; KR_FREQ[22][24] = 253; KR_FREQ[36][67] = 252; KR_FREQ[33][90] = 251;
        KR_FREQ[15][60] = 250; KR_FREQ[23][85] = 249; KR_FREQ[34][1]  = 248; KR_FREQ[39][37] = 247; KR_FREQ[21][18] = 246;
        KR_FREQ[34][4]  = 245; KR_FREQ[28][33] = 244; KR_FREQ[15][13] = 243; KR_FREQ[32][22] = 242; KR_FREQ[30][76] = 241;
        KR_FREQ[20][21] = 240; KR_FREQ[38][66] = 239; KR_FREQ[32][55] = 238; KR_FREQ[32][89] = 237; KR_FREQ[25][26] = 236;
        KR_FREQ[16][80] = 235; KR_FREQ[15][43] = 234; KR_FREQ[38][54] = 233; KR_FREQ[39][68] = 232; KR_FREQ[22][88] = 231;
        KR_FREQ[21][84] = 230; KR_FREQ[21][17] = 229; KR_FREQ[20][28] = 228; KR_FREQ[32][1]  = 227; KR_FREQ[33][87] = 226;
        KR_FREQ[38][71] = 225; KR_FREQ[37][47] = 224; KR_FREQ[18][77] = 223; KR_FREQ[37][58] = 222; KR_FREQ[34][74] = 221;
        KR_FREQ[32][54] = 220; KR_FREQ[27][33] = 219; KR_FREQ[32][93] = 218; KR_FREQ[23][51] = 217; KR_FREQ[20][57] = 216;
        KR_FREQ[22][37] = 215; KR_FREQ[39][10] = 214; KR_FREQ[39][17] = 213; KR_FREQ[33][4]  = 212; KR_FREQ[32][84] = 211;
        KR_FREQ[34][3]  = 210; KR_FREQ[28][27] = 209; KR_FREQ[15][79] = 208; KR_FREQ[34][21] = 207; KR_FREQ[34][69] = 206;
        KR_FREQ[21][62] = 205; KR_FREQ[36][24] = 204; KR_FREQ[16][89] = 203; KR_FREQ[18][48] = 202; KR_FREQ[38][15] = 201;
        KR_FREQ[36][58] = 200; KR_FREQ[21][56] = 199; KR_FREQ[34][48] = 198; KR_FREQ[21][15] = 197; KR_FREQ[39][3]  = 196;
        KR_FREQ[16][44] = 195; KR_FREQ[18][79] = 194; KR_FREQ[25][13] = 193; KR_FREQ[29][47] = 192; KR_FREQ[38][88] = 191;
        KR_FREQ[20][71] = 190; KR_FREQ[16][58] = 189; KR_FREQ[35][57] = 188; KR_FREQ[29][30] = 187; KR_FREQ[29][23] = 186;
        KR_FREQ[34][93] = 185; KR_FREQ[30][85] = 184; KR_FREQ[15][80] = 183; KR_FREQ[32][78] = 182; KR_FREQ[37][82] = 181;
        KR_FREQ[22][40] = 180; KR_FREQ[21][69] = 179; KR_FREQ[26][85] = 178; KR_FREQ[31][31] = 177; KR_FREQ[28][64] = 176;
        KR_FREQ[38][13] = 175; KR_FREQ[25][2]  = 174; KR_FREQ[22][34] = 173; KR_FREQ[28][28] = 172; KR_FREQ[24][91] = 171;
        KR_FREQ[33][74] = 170; KR_FREQ[29][40] = 169; KR_FREQ[15][77] = 168; KR_FREQ[32][80] = 167; KR_FREQ[30][41] = 166;
        KR_FREQ[23][30] = 165; KR_FREQ[24][63] = 164; KR_FREQ[30][53] = 163; KR_FREQ[39][70] = 162; KR_FREQ[23][61] = 161;
        KR_FREQ[37][27] = 160; KR_FREQ[16][55] = 159; KR_FREQ[22][74] = 158; KR_FREQ[26][50] = 157; KR_FREQ[16][10] = 156;
        KR_FREQ[34][63] = 155; KR_FREQ[35][14] = 154; KR_FREQ[17][7]  = 153; KR_FREQ[15][59] = 152; KR_FREQ[27][23] = 151;
        KR_FREQ[18][70] = 150; KR_FREQ[32][56] = 149; KR_FREQ[37][87] = 148; KR_FREQ[17][61] = 147; KR_FREQ[18][83] = 146;
        KR_FREQ[23][86] = 145; KR_FREQ[17][31] = 144; KR_FREQ[23][83] = 143; KR_FREQ[35][2]  = 142; KR_FREQ[18][64] = 141;
        KR_FREQ[27][43] = 140; KR_FREQ[32][42] = 139; KR_FREQ[25][76] = 138; KR_FREQ[19][85] = 137; KR_FREQ[37][81] = 136;
        KR_FREQ[38][83] = 135; KR_FREQ[35][7]  = 134; KR_FREQ[16][51] = 133; KR_FREQ[27][22] = 132; KR_FREQ[16][76] = 131;
        KR_FREQ[22][4]  = 130; KR_FREQ[38][84] = 129; KR_FREQ[17][83] = 128; KR_FREQ[24][46] = 127; KR_FREQ[33][15] = 126;
        KR_FREQ[20][48] = 125; KR_FREQ[17][30] = 124; KR_FREQ[30][93] = 123; KR_FREQ[28][11] = 122; KR_FREQ[28][30] = 121;
        KR_FREQ[15][62] = 120; KR_FREQ[17][87] = 119; KR_FREQ[32][81] = 118; KR_FREQ[23][37] = 117; KR_FREQ[30][22] = 116;
        KR_FREQ[32][66] = 115; KR_FREQ[33][78] = 114; KR_FREQ[21][4]  = 113; KR_FREQ[31][17] = 112; KR_FREQ[39][61] = 111;
        KR_FREQ[18][76] = 110; KR_FREQ[15][85] = 109; KR_FREQ[31][47] = 108; KR_FREQ[19][57] = 107; KR_FREQ[23][55] = 106;
        KR_FREQ[27][29] = 105; KR_FREQ[29][46] = 104; KR_FREQ[33][0]  = 103; KR_FREQ[16][83] = 102; KR_FREQ[39][78] = 101;
        KR_FREQ[32][77] = 100; KR_FREQ[36][25] =  99; KR_FREQ[34][19] =  98; KR_FREQ[38][49] =  97; KR_FREQ[19][25] =  96;
        KR_FREQ[23][53] =  95; KR_FREQ[28][43] =  94; KR_FREQ[31][44] =  93; KR_FREQ[36][34] =  92; KR_FREQ[16][34] =  91;
        KR_FREQ[35][1]  =  90; KR_FREQ[19][87] =  89; KR_FREQ[18][53] =  88; KR_FREQ[29][54] =  87; KR_FREQ[22][41] =  86;
        KR_FREQ[38][18] =  85; KR_FREQ[22][2]  =  84; KR_FREQ[20][3]  =  83; KR_FREQ[39][69] =  82; KR_FREQ[30][29] =  81;
        KR_FREQ[28][19] =  80; KR_FREQ[29][90] =  79; KR_FREQ[17][86] =  78; KR_FREQ[15][9]  =  77; KR_FREQ[39][73] =  76;
        KR_FREQ[15][37] =  75; KR_FREQ[35][40] =  74; KR_FREQ[33][77] =  73; KR_FREQ[27][86] =  72; KR_FREQ[36][79] =  71;
        KR_FREQ[23][18] =  70; KR_FREQ[34][87] =  69; KR_FREQ[39][24] =  68; KR_FREQ[26][8]  =  67; KR_FREQ[33][48] =  66;
        KR_FREQ[39][30] =  65; KR_FREQ[33][28] =  64; KR_FREQ[16][67] =  63; KR_FREQ[31][78] =  62; KR_FREQ[32][23] =  61;
        KR_FREQ[24][55] =  60; KR_FREQ[30][68] =  59; KR_FREQ[18][60] =  58; KR_FREQ[15][17] =  57; KR_FREQ[23][34] =  56;
        KR_FREQ[20][49] =  55; KR_FREQ[15][78] =  54; KR_FREQ[24][14] =  53; KR_FREQ[19][41] =  52; KR_FREQ[31][55] =  51;
        KR_FREQ[21][39] =  50; KR_FREQ[35][9]  =  49; KR_FREQ[30][15] =  48; KR_FREQ[20][52] =  47; KR_FREQ[35][71] =  46;
        KR_FREQ[20][7]  =  45; KR_FREQ[29][72] =  44; KR_FREQ[37][77] =  43; KR_FREQ[22][35] =  42; KR_FREQ[20][61] =  41;
        KR_FREQ[31][60] =  40; KR_FREQ[20][93] =  39; KR_FREQ[27][92] =  38; KR_FREQ[28][16] =  37; KR_FREQ[36][26] =  36;
        KR_FREQ[18][89] =  35; KR_FREQ[21][63] =  34; KR_FREQ[22][52] =  33; KR_FREQ[24][65] =  32; KR_FREQ[31][8]  =  31;
        KR_FREQ[31][49] =  30; KR_FREQ[33][30] =  29; KR_FREQ[37][15] =  28; KR_FREQ[18][18] =  27; KR_FREQ[25][50] =  26;
        KR_FREQ[29][20] =  25; KR_FREQ[35][48] =  24; KR_FREQ[38][75] =  23; KR_FREQ[26][83] =  22; KR_FREQ[21][87] =  21;
        KR_FREQ[27][71] =  20; KR_FREQ[32][91] =  19; KR_FREQ[25][73] =  18; KR_FREQ[16][84] =  17; KR_FREQ[25][31] =  16;
        KR_FREQ[17][90] =  15; KR_FREQ[18][40] =  14; KR_FREQ[17][77] =  13; KR_FREQ[17][35] =  12; KR_FREQ[23][52] =  11;
        KR_FREQ[23][35] =  10; KR_FREQ[16][5]  =   9; KR_FREQ[23][58] =   8; KR_FREQ[19][60] =   7; KR_FREQ[30][32] =   6;
        KR_FREQ[38][34] =   5; KR_FREQ[23][4]  =   4; KR_FREQ[23][1]  =   3; KR_FREQ[27][57] =   2; KR_FREQ[39][38] =   1;
        KR_FREQ[32][33] =   0;

        // ------------------------------------------------------------------------------------JP_FREQ

        JP_FREQ[3][74]  = 600; JP_FREQ[3][45]  = 599; JP_FREQ[3][3]   = 598; JP_FREQ[3][24]  = 597; JP_FREQ[3][30]  = 596;
        JP_FREQ[3][42]  = 595; JP_FREQ[3][46]  = 594; JP_FREQ[3][39]  = 593; JP_FREQ[3][11]  = 592; JP_FREQ[3][37]  = 591;
        JP_FREQ[3][38]  = 590; JP_FREQ[3][31]  = 589; JP_FREQ[3][41]  = 588; JP_FREQ[3][5]   = 587; JP_FREQ[3][10]  = 586;
        JP_FREQ[3][75]  = 585; JP_FREQ[3][65]  = 584; JP_FREQ[3][72]  = 583; JP_FREQ[37][91] = 582; JP_FREQ[0][27]  = 581;
        JP_FREQ[3][18]  = 580; JP_FREQ[3][22]  = 579; JP_FREQ[3][61]  = 578; JP_FREQ[3][14]  = 577; JP_FREQ[24][80] = 576;
        JP_FREQ[4][82]  = 575; JP_FREQ[17][80] = 574; JP_FREQ[30][44] = 573; JP_FREQ[3][73]  = 572; JP_FREQ[3][64]  = 571;
        JP_FREQ[38][14] = 570; JP_FREQ[33][70] = 569; JP_FREQ[3][1]   = 568; JP_FREQ[3][16]  = 567; JP_FREQ[3][35]  = 566;
        JP_FREQ[3][40]  = 565; JP_FREQ[4][74]  = 564; JP_FREQ[4][24]  = 563; JP_FREQ[42][59] = 562; JP_FREQ[3][7]   = 561;
        JP_FREQ[3][71]  = 560; JP_FREQ[3][12]  = 559; JP_FREQ[15][75] = 558; JP_FREQ[3][20]  = 557; JP_FREQ[4][39]  = 556;
        JP_FREQ[34][69] = 555; JP_FREQ[3][28]  = 554; JP_FREQ[35][24] = 553; JP_FREQ[3][82]  = 552; JP_FREQ[28][47] = 551;
        JP_FREQ[3][67]  = 550; JP_FREQ[37][16] = 549; JP_FREQ[26][93] = 548; JP_FREQ[4][1]   = 547; JP_FREQ[26][85] = 546;
        JP_FREQ[31][14] = 545; JP_FREQ[4][3]   = 544; JP_FREQ[4][72]  = 543; JP_FREQ[24][51] = 542; JP_FREQ[27][51] = 541;
        JP_FREQ[27][49] = 540; JP_FREQ[22][77] = 539; JP_FREQ[27][10] = 538; JP_FREQ[29][68] = 537; JP_FREQ[20][35] = 536;
        JP_FREQ[41][11] = 535; JP_FREQ[24][70] = 534; JP_FREQ[36][61] = 533; JP_FREQ[31][23] = 532; JP_FREQ[43][16] = 531;
        JP_FREQ[23][68] = 530; JP_FREQ[32][15] = 529; JP_FREQ[3][32]  = 528; JP_FREQ[19][53] = 527; JP_FREQ[40][83] = 526;
        JP_FREQ[4][14]  = 525; JP_FREQ[36][9]  = 524; JP_FREQ[4][73]  = 523; JP_FREQ[23][10] = 522; JP_FREQ[3][63]  = 521;
        JP_FREQ[39][14] = 520; JP_FREQ[3][78]  = 519; JP_FREQ[33][47] = 518; JP_FREQ[21][39] = 517; JP_FREQ[34][46] = 516;
        JP_FREQ[36][75] = 515; JP_FREQ[41][92] = 514; JP_FREQ[37][93] = 513; JP_FREQ[4][34]  = 512; JP_FREQ[15][86] = 511;
        JP_FREQ[46][1]  = 510; JP_FREQ[37][65] = 509; JP_FREQ[3][62]  = 508; JP_FREQ[32][73] = 507; JP_FREQ[21][65] = 506;
        JP_FREQ[29][75] = 505; JP_FREQ[26][51] = 504; JP_FREQ[3][34]  = 503; JP_FREQ[4][10]  = 502; JP_FREQ[30][22] = 501;
        JP_FREQ[35][73] = 500; JP_FREQ[17][82] = 499; JP_FREQ[45][8]  = 498; JP_FREQ[27][73] = 497; JP_FREQ[18][55] = 496;
        JP_FREQ[25][2]  = 495; JP_FREQ[3][26]  = 494; JP_FREQ[45][46] = 493; JP_FREQ[4][22]  = 492; JP_FREQ[4][40]  = 491;
        JP_FREQ[18][10] = 490; JP_FREQ[32][9]  = 489; JP_FREQ[26][49] = 488; JP_FREQ[3][47]  = 487; JP_FREQ[24][65] = 486;
        JP_FREQ[4][76]  = 485; JP_FREQ[43][67] = 484; JP_FREQ[3][9]   = 483; JP_FREQ[41][37] = 482; JP_FREQ[33][68] = 481;
        JP_FREQ[43][31] = 480; JP_FREQ[19][55] = 479; JP_FREQ[4][30]  = 478; JP_FREQ[27][33] = 477; JP_FREQ[16][62] = 476;
        JP_FREQ[36][35] = 475; JP_FREQ[37][15] = 474; JP_FREQ[27][70] = 473; JP_FREQ[22][71] = 472; JP_FREQ[33][45] = 471;
        JP_FREQ[31][78] = 470; JP_FREQ[43][59] = 469; JP_FREQ[32][19] = 468; JP_FREQ[17][28] = 467; JP_FREQ[40][28] = 466;
        JP_FREQ[20][93] = 465; JP_FREQ[18][15] = 464; JP_FREQ[4][23]  = 463; JP_FREQ[3][23]  = 462; JP_FREQ[26][64] = 461;
        JP_FREQ[44][92] = 460; JP_FREQ[17][27] = 459; JP_FREQ[3][56]  = 458; JP_FREQ[25][38] = 457; JP_FREQ[23][31] = 456;
        JP_FREQ[35][43] = 455; JP_FREQ[4][54]  = 454; JP_FREQ[35][19] = 453; JP_FREQ[22][47] = 452; JP_FREQ[42][0]  = 451;
        JP_FREQ[23][28] = 450; JP_FREQ[46][33] = 449; JP_FREQ[36][85] = 448; JP_FREQ[31][12] = 447; JP_FREQ[3][76]  = 446;
        JP_FREQ[4][75]  = 445; JP_FREQ[36][56] = 444; JP_FREQ[4][64]  = 443; JP_FREQ[25][77] = 442; JP_FREQ[15][52] = 441;
        JP_FREQ[33][73] = 440; JP_FREQ[3][55]  = 439; JP_FREQ[43][82] = 438; JP_FREQ[27][82] = 437; JP_FREQ[20][3]  = 436;
        JP_FREQ[40][51] = 435; JP_FREQ[3][17]  = 434; JP_FREQ[27][71] = 433; JP_FREQ[4][52]  = 432; JP_FREQ[44][48] = 431;
        JP_FREQ[27][2]  = 430; JP_FREQ[17][39] = 429; JP_FREQ[31][8]  = 428; JP_FREQ[44][54] = 427; JP_FREQ[43][18] = 426;
        JP_FREQ[43][77] = 425; JP_FREQ[4][61]  = 424; JP_FREQ[19][91] = 423; JP_FREQ[31][13] = 422; JP_FREQ[44][71] = 421;
        JP_FREQ[20][0]  = 420; JP_FREQ[23][87] = 419; JP_FREQ[21][14] = 418; JP_FREQ[29][13] = 417; JP_FREQ[3][58]  = 416;
        JP_FREQ[26][18] = 415; JP_FREQ[4][47]  = 414; JP_FREQ[4][18]  = 413; JP_FREQ[3][53]  = 412; JP_FREQ[26][92] = 411;
        JP_FREQ[21][7]  = 410; JP_FREQ[4][37]  = 409; JP_FREQ[4][63]  = 408; JP_FREQ[36][51] = 407; JP_FREQ[4][32]  = 406;
        JP_FREQ[28][73] = 405; JP_FREQ[4][50]  = 404; JP_FREQ[41][60] = 403; JP_FREQ[23][1]  = 402; JP_FREQ[36][92] = 401;
        JP_FREQ[15][41] = 400; JP_FREQ[21][71] = 399; JP_FREQ[41][30] = 398; JP_FREQ[32][76] = 397; JP_FREQ[17][34] = 396;
        JP_FREQ[26][15] = 395; JP_FREQ[26][25] = 394; JP_FREQ[31][77] = 393; JP_FREQ[31][3]  = 392; JP_FREQ[46][34] = 391;
        JP_FREQ[27][84] = 390; JP_FREQ[23][8]  = 389; JP_FREQ[16][0]  = 388; JP_FREQ[28][80] = 387; JP_FREQ[26][54] = 386;
        JP_FREQ[33][18] = 385; JP_FREQ[31][20] = 384; JP_FREQ[31][62] = 383; JP_FREQ[30][41] = 382; JP_FREQ[33][30] = 381;
        JP_FREQ[45][45] = 380; JP_FREQ[37][82] = 379; JP_FREQ[15][33] = 378; JP_FREQ[20][12] = 377; JP_FREQ[18][5]  = 376;
        JP_FREQ[28][86] = 375; JP_FREQ[30][19] = 374; JP_FREQ[42][43] = 373; JP_FREQ[36][31] = 372; JP_FREQ[17][93] = 371;
        JP_FREQ[4][15]  = 370; JP_FREQ[21][20] = 369; JP_FREQ[23][21] = 368; JP_FREQ[28][72] = 367; JP_FREQ[4][20]  = 366;
        JP_FREQ[26][55] = 365; JP_FREQ[21][5]  = 364; JP_FREQ[19][16] = 363; JP_FREQ[23][64] = 362; JP_FREQ[40][59] = 361;
        JP_FREQ[37][26] = 360; JP_FREQ[26][56] = 359; JP_FREQ[4][12]  = 358; JP_FREQ[33][71] = 357; JP_FREQ[32][39] = 356;
        JP_FREQ[38][40] = 355; JP_FREQ[22][74] = 354; JP_FREQ[3][25]  = 353; JP_FREQ[15][48] = 352; JP_FREQ[41][82] = 351;
        JP_FREQ[41][9]  = 350; JP_FREQ[25][48] = 349; JP_FREQ[31][71] = 348; JP_FREQ[43][29] = 347; JP_FREQ[26][80] = 346;
        JP_FREQ[4][5]   = 345; JP_FREQ[18][71] = 344; JP_FREQ[29][0]  = 343; JP_FREQ[43][43] = 342; JP_FREQ[23][81] = 341;
        JP_FREQ[4][42]  = 340; JP_FREQ[44][28] = 339; JP_FREQ[23][93] = 338; JP_FREQ[17][81] = 337; JP_FREQ[25][25] = 336;
        JP_FREQ[41][23] = 335; JP_FREQ[34][35] = 334; JP_FREQ[4][53]  = 333; JP_FREQ[28][36] = 332; JP_FREQ[4][41]  = 331;
        JP_FREQ[25][60] = 330; JP_FREQ[23][20] = 329; JP_FREQ[3][43]  = 328; JP_FREQ[24][79] = 327; JP_FREQ[29][41] = 326;
        JP_FREQ[30][83] = 325; JP_FREQ[3][50]  = 324; JP_FREQ[22][18] = 323; JP_FREQ[18][3]  = 322; JP_FREQ[39][30] = 321;
        JP_FREQ[4][28]  = 320; JP_FREQ[21][64] = 319; JP_FREQ[4][68]  = 318; JP_FREQ[17][71] = 317; JP_FREQ[27][0]  = 316;
        JP_FREQ[39][28] = 315; JP_FREQ[30][13] = 314; JP_FREQ[36][70] = 313; JP_FREQ[20][82] = 312; JP_FREQ[33][38] = 311;
        JP_FREQ[44][87] = 310; JP_FREQ[34][45] = 309; JP_FREQ[4][26]  = 308; JP_FREQ[24][44] = 307; JP_FREQ[38][67] = 306;
        JP_FREQ[38][6]  = 305; JP_FREQ[30][68] = 304; JP_FREQ[15][89] = 303; JP_FREQ[24][93] = 302; JP_FREQ[40][41] = 301;
        JP_FREQ[38][3]  = 300; JP_FREQ[28][23] = 299; JP_FREQ[26][17] = 298; JP_FREQ[4][38]  = 297; JP_FREQ[22][78] = 296;
        JP_FREQ[15][37] = 295; JP_FREQ[25][85] = 294; JP_FREQ[4][9]   = 293; JP_FREQ[4][7]   = 292; JP_FREQ[27][53] = 291;
        JP_FREQ[39][29] = 290; JP_FREQ[41][43] = 289; JP_FREQ[25][62] = 288; JP_FREQ[4][48]  = 287; JP_FREQ[28][28] = 286;
        JP_FREQ[21][40] = 285; JP_FREQ[36][73] = 284; JP_FREQ[26][39] = 283; JP_FREQ[22][54] = 282; JP_FREQ[33][5]  = 281;
        JP_FREQ[19][21] = 280; JP_FREQ[46][31] = 279; JP_FREQ[20][64] = 278; JP_FREQ[26][63] = 277; JP_FREQ[22][23] = 276;
        JP_FREQ[25][81] = 275; JP_FREQ[4][62]  = 274; JP_FREQ[37][31] = 273; JP_FREQ[40][52] = 272; JP_FREQ[29][79] = 271;
        JP_FREQ[41][48] = 270; JP_FREQ[31][57] = 269; JP_FREQ[32][92] = 268; JP_FREQ[36][36] = 267; JP_FREQ[27][7]  = 266;
        JP_FREQ[35][29] = 265; JP_FREQ[37][34] = 264; JP_FREQ[34][42] = 263; JP_FREQ[27][15] = 262; JP_FREQ[33][27] = 261;
        JP_FREQ[31][38] = 260; JP_FREQ[19][79] = 259; JP_FREQ[4][31]  = 258; JP_FREQ[4][66]  = 257; JP_FREQ[17][32] = 256;
        JP_FREQ[26][67] = 255; JP_FREQ[16][30] = 254; JP_FREQ[26][46] = 253; JP_FREQ[24][26] = 252; JP_FREQ[35][10] = 251;
        JP_FREQ[18][37] = 250; JP_FREQ[3][19]  = 249; JP_FREQ[33][69] = 248; JP_FREQ[31][9]  = 247; JP_FREQ[45][29] = 246;
        JP_FREQ[3][15]  = 245; JP_FREQ[18][54] = 244; JP_FREQ[3][44]  = 243; JP_FREQ[31][29] = 242; JP_FREQ[18][45] = 241;
        JP_FREQ[38][28] = 240; JP_FREQ[24][12] = 239; JP_FREQ[35][82] = 238; JP_FREQ[17][43] = 237; JP_FREQ[28][9]  = 236;
        JP_FREQ[23][25] = 235; JP_FREQ[44][37] = 234; JP_FREQ[23][75] = 233; JP_FREQ[23][92] = 232; JP_FREQ[0][24]  = 231;
        JP_FREQ[19][74] = 230; JP_FREQ[45][32] = 229; JP_FREQ[16][72] = 228; JP_FREQ[16][93] = 227; JP_FREQ[45][13] = 226;
        JP_FREQ[24][8]  = 225; JP_FREQ[25][47] = 224; JP_FREQ[28][26] = 223; JP_FREQ[43][81] = 222; JP_FREQ[32][71] = 221;
        JP_FREQ[18][41] = 220; JP_FREQ[26][62] = 219; JP_FREQ[41][24] = 218; JP_FREQ[40][11] = 217; JP_FREQ[43][57] = 216;
        JP_FREQ[34][53] = 215; JP_FREQ[20][32] = 214; JP_FREQ[34][43] = 213; JP_FREQ[41][91] = 212; JP_FREQ[29][57] = 211;
        JP_FREQ[15][43] = 210; JP_FREQ[22][89] = 209; JP_FREQ[33][83] = 208; JP_FREQ[43][20] = 207; JP_FREQ[25][58] = 206;
        JP_FREQ[30][30] = 205; JP_FREQ[4][56]  = 204; JP_FREQ[17][64] = 203; JP_FREQ[23][0]  = 202; JP_FREQ[44][12] = 201;
        JP_FREQ[25][37] = 200; JP_FREQ[35][13] = 199; JP_FREQ[20][30] = 198; JP_FREQ[21][84] = 197; JP_FREQ[29][14] = 196;
        JP_FREQ[30][5]  = 195; JP_FREQ[37][2]  = 194; JP_FREQ[4][78]  = 193; JP_FREQ[29][78] = 192; JP_FREQ[29][84] = 191;
        JP_FREQ[32][86] = 190; JP_FREQ[20][68] = 189; JP_FREQ[30][39] = 188; JP_FREQ[15][69] = 187; JP_FREQ[4][60]  = 186;
        JP_FREQ[20][61] = 185; JP_FREQ[41][67] = 184; JP_FREQ[16][35] = 183; JP_FREQ[36][57] = 182; JP_FREQ[39][80] = 181;
        JP_FREQ[4][59]  = 180; JP_FREQ[4][44]  = 179; JP_FREQ[40][54] = 178; JP_FREQ[30][8]  = 177; JP_FREQ[44][30] = 176;
        JP_FREQ[31][93] = 175; JP_FREQ[31][47] = 174; JP_FREQ[16][70] = 173; JP_FREQ[21][0]  = 172; JP_FREQ[17][35] = 171;
        JP_FREQ[21][67] = 170; JP_FREQ[44][18] = 169; JP_FREQ[36][29] = 168; JP_FREQ[18][67] = 167; JP_FREQ[24][28] = 166;
        JP_FREQ[36][24] = 165; JP_FREQ[23][5]  = 164; JP_FREQ[31][65] = 163; JP_FREQ[26][59] = 162; JP_FREQ[28][2]  = 161;
        JP_FREQ[39][69] = 160; JP_FREQ[42][40] = 159; JP_FREQ[37][80] = 158; JP_FREQ[15][66] = 157; JP_FREQ[34][38] = 156;
        JP_FREQ[28][48] = 155; JP_FREQ[37][77] = 154; JP_FREQ[29][34] = 153; JP_FREQ[33][12] = 152; JP_FREQ[4][65]  = 151;
        JP_FREQ[30][31] = 150; JP_FREQ[27][92] = 149; JP_FREQ[4][2]   = 148; JP_FREQ[4][51]  = 147; JP_FREQ[23][77] = 146;
        JP_FREQ[4][35]  = 145; JP_FREQ[3][13]  = 144; JP_FREQ[26][26] = 143; JP_FREQ[44][4]  = 142; JP_FREQ[39][53] = 141;
        JP_FREQ[20][11] = 140; JP_FREQ[40][33] = 139; JP_FREQ[45][7]  = 138; JP_FREQ[4][70]  = 137; JP_FREQ[3][49]  = 136;
        JP_FREQ[20][59] = 135; JP_FREQ[21][12] = 134; JP_FREQ[33][53] = 133; JP_FREQ[20][14] = 132; JP_FREQ[37][18] = 131;
        JP_FREQ[18][17] = 130; JP_FREQ[36][23] = 129; JP_FREQ[18][57] = 128; JP_FREQ[26][74] = 127; JP_FREQ[35][2]  = 126;
        JP_FREQ[38][58] = 125; JP_FREQ[34][68] = 124; JP_FREQ[29][81] = 123; JP_FREQ[20][69] = 122; JP_FREQ[39][86] = 121;
        JP_FREQ[4][16]  = 120; JP_FREQ[16][49] = 119; JP_FREQ[15][72] = 118; JP_FREQ[26][35] = 117; JP_FREQ[32][14] = 116;
        JP_FREQ[40][90] = 115; JP_FREQ[33][79] = 114; JP_FREQ[35][4]  = 113; JP_FREQ[23][33] = 112; JP_FREQ[19][19] = 111;
        JP_FREQ[31][41] = 110; JP_FREQ[44][1]  = 109; JP_FREQ[22][56] = 108; JP_FREQ[31][27] = 107; JP_FREQ[32][18] = 106;
        JP_FREQ[27][32] = 105; JP_FREQ[37][39] = 104; JP_FREQ[42][11] = 103; JP_FREQ[29][71] = 102; JP_FREQ[32][58] = 101;
        JP_FREQ[46][10] = 100; JP_FREQ[17][30] =  99; JP_FREQ[38][15] =  98; JP_FREQ[29][60] =  97; JP_FREQ[4][11]  =  96;
        JP_FREQ[38][31] =  95; JP_FREQ[40][79] =  94; JP_FREQ[28][49] =  93; JP_FREQ[28][84] =  92; JP_FREQ[26][77] =  91;
        JP_FREQ[22][32] =  90; JP_FREQ[33][17] =  89; JP_FREQ[23][18] =  88; JP_FREQ[32][64] =  87; JP_FREQ[4][6]   =  86;
        JP_FREQ[33][51] =  85; JP_FREQ[44][77] =  84; JP_FREQ[29][5]  =  83; JP_FREQ[46][25] =  82; JP_FREQ[19][58] =  81;
        JP_FREQ[4][46]  =  80; JP_FREQ[15][71] =  79; JP_FREQ[18][58] =  78; JP_FREQ[26][45] =  77; JP_FREQ[45][66] =  76;
        JP_FREQ[34][10] =  75; JP_FREQ[19][37] =  74; JP_FREQ[33][65] =  73; JP_FREQ[44][52] =  72; JP_FREQ[16][38] =  71;
        JP_FREQ[36][46] =  70; JP_FREQ[20][26] =  69; JP_FREQ[30][37] =  68; JP_FREQ[4][58]  =  67; JP_FREQ[43][2]  =  66;
        JP_FREQ[30][18] =  65; JP_FREQ[19][35] =  64; JP_FREQ[15][68] =  63; JP_FREQ[3][36]  =  62; JP_FREQ[35][40] =  61;
        JP_FREQ[36][32] =  60; JP_FREQ[37][14] =  59; JP_FREQ[17][11] =  58; JP_FREQ[19][78] =  57; JP_FREQ[37][11] =  56;
        JP_FREQ[28][63] =  55; JP_FREQ[29][61] =  54; JP_FREQ[33][3]  =  53; JP_FREQ[41][52] =  52; JP_FREQ[33][63] =  51;
        JP_FREQ[22][41] =  50; JP_FREQ[4][19]  =  49; JP_FREQ[32][41] =  48; JP_FREQ[24][4]  =  47; JP_FREQ[31][28] =  46;
        JP_FREQ[43][30] =  45; JP_FREQ[17][3]  =  44; JP_FREQ[43][70] =  43; JP_FREQ[34][19] =  42; JP_FREQ[20][77] =  41;
        JP_FREQ[18][83] =  40; JP_FREQ[17][15] =  39; JP_FREQ[23][61] =  38; JP_FREQ[40][27] =  37; JP_FREQ[16][48] =  36;
        JP_FREQ[39][78] =  35; JP_FREQ[41][53] =  34; JP_FREQ[40][91] =  33; JP_FREQ[40][72] =  32; JP_FREQ[18][52] =  31;
        JP_FREQ[35][66] =  30; JP_FREQ[39][93] =  29; JP_FREQ[19][48] =  28; JP_FREQ[26][36] =  27; JP_FREQ[27][25] =  26;
        JP_FREQ[42][71] =  25; JP_FREQ[42][85] =  24; JP_FREQ[26][48] =  23; JP_FREQ[28][15] =  22; JP_FREQ[3][66]  =  21;
        JP_FREQ[25][24] =  20; JP_FREQ[27][43] =  19; JP_FREQ[27][78] =  18; JP_FREQ[45][43] =  17; JP_FREQ[27][72] =  16;
        JP_FREQ[40][29] =  15; JP_FREQ[41][0]  =  14; JP_FREQ[19][57] =  13; JP_FREQ[15][59] =  12; JP_FREQ[29][29] =  11;
        JP_FREQ[4][25]  =  10; JP_FREQ[21][42] =   9; JP_FREQ[23][35] =   8; JP_FREQ[33][1]  =   7; JP_FREQ[4][57]  =   6;
        JP_FREQ[17][60] =   5; JP_FREQ[25][19] =   4; JP_FREQ[22][65] =   3; JP_FREQ[42][29] =   2; JP_FREQ[27][66] =   1;
        JP_FREQ[26][89] =   0;
    }

    private static class Encoding {
        private static final int TOTAL_TYPES = 23;

        // Supported Encoding Types
        private static final int GB2312        = 0;  // 1980年中国发布了第一个汉字编码标准
        private static final int GBK           = 1;  // 在GB2312的基础上添加了部分字符(如GB2312发布后才简化的汉字、部分缺少的人名/繁体字/日语/朝鲜语中的汉字)
        private static final int GB18030       = 2;  // 全称《信息技术中文编码字符集》，在GBK的基础上增加了中日韩语中的汉字和少数名族的文字及字符
        private static final int HZ            = 3;
        private static final int BIG5          = 4;
        private static final int CNS11643      = 5;
        private static final int UTF8          = 6;
        private static final int UTF8T         = 7;
        private static final int UTF8S         = 8;
        private static final int UNICODE       = 9;
        private static final int UNICODET      = 10;
        private static final int UNICODES      = 11;
        private static final int ISO2022CN     = 12;
        private static final int ISO2022CN_CNS = 13;
        private static final int ISO2022CN_GB  = 14;
        private static final int EUC_KR        = 15;
        private static final int CP949         = 16;
        private static final int ISO2022KR     = 17;
        private static final int JOHAB         = 18;
        private static final int SJIS          = 19;
        private static final int EUC_JP        = 20;
        private static final int ISO2022JP     = 21;
        private static final int ASCII         = 22;

        private static final String[] JAVA_CHARSET = new String[TOTAL_TYPES]; // Names of the encodings as understood by Java
        private static final String[] NICE_CHARSET = new String[TOTAL_TYPES]; // Names of the encodings for human viewing
        private static final String[] HTML_CHARSET = new String[TOTAL_TYPES]; // Names of charsets as used in charset parameter of HTML Meta tag
        static {
            // Assign encoding names
            JAVA_CHARSET[GB2312]        = "GB2312";
            JAVA_CHARSET[GBK]           = "GBK";
            JAVA_CHARSET[GB18030]       = "GB18030";
            JAVA_CHARSET[HZ]            = "ASCII"; // What to put here? Sun doesn't support HZ
            JAVA_CHARSET[ISO2022CN_GB]  = "ISO2022CN_GB";
            JAVA_CHARSET[BIG5]          = "Big5";
            JAVA_CHARSET[CNS11643]      = "EUC-TW";
            JAVA_CHARSET[ISO2022CN_CNS] = "ISO2022CN_CNS";
            JAVA_CHARSET[ISO2022CN]     = "ISO2022CN";
            JAVA_CHARSET[UTF8]          = "UTF-8";
            JAVA_CHARSET[UTF8T]         = "UTF-8";
            JAVA_CHARSET[UTF8S]         = "UTF-8";
            JAVA_CHARSET[UNICODE]       = "Unicode";
            JAVA_CHARSET[UNICODET]      = "Unicode";
            JAVA_CHARSET[UNICODES]      = "Unicode";
            JAVA_CHARSET[EUC_KR]        = "EUC-KR";
            JAVA_CHARSET[CP949]         = "MS949";
            JAVA_CHARSET[ISO2022KR]     = "ISO2022KR";
            JAVA_CHARSET[JOHAB]         = "Johab";
            JAVA_CHARSET[SJIS]          = "SJIS";
            JAVA_CHARSET[EUC_JP]        = "EUC_JP";
            JAVA_CHARSET[ISO2022JP]     = "ISO2022JP";
            JAVA_CHARSET[ASCII]         = "ASCII";

            // Assign encoding names
            HTML_CHARSET[GB2312]        = "GB2312";
            HTML_CHARSET[GBK]           = "GBK";
            HTML_CHARSET[GB18030]       = "GB18030";
            HTML_CHARSET[HZ]            = "HZ-GB-2312";
            HTML_CHARSET[ISO2022CN_GB]  = "ISO-2022-CN-EXT";
            HTML_CHARSET[BIG5]          = "Big5";
            HTML_CHARSET[CNS11643]      = "EUC-TW";
            HTML_CHARSET[ISO2022CN_CNS] = "ISO-2022-CN-EXT";
            HTML_CHARSET[ISO2022CN]     = "ISO-2022-CN";
            HTML_CHARSET[UTF8]          = "UTF-8";
            HTML_CHARSET[UTF8T]         = "UTF-8";
            HTML_CHARSET[UTF8S]         = "UTF-8";
            HTML_CHARSET[UNICODE]       = "UTF-16";
            HTML_CHARSET[UNICODET]      = "UTF-16";
            HTML_CHARSET[UNICODES]      = "UTF-16";
            HTML_CHARSET[EUC_KR]        = "EUC-KR";
            HTML_CHARSET[CP949]         = "x-windows-949";
            HTML_CHARSET[ISO2022KR]     = "ISO-2022-KR";
            HTML_CHARSET[JOHAB]         = "x-Johab";
            HTML_CHARSET[SJIS]          = "Shift_JIS";
            HTML_CHARSET[EUC_JP]        = "EUC-JP";
            HTML_CHARSET[ISO2022JP]     = "ISO-2022-JP";
            HTML_CHARSET[ASCII]         = "ASCII";

            // Assign Human readable names
            NICE_CHARSET[GB2312]        = "GB-2312";
            NICE_CHARSET[GBK]           = "GBK";
            NICE_CHARSET[GB18030]       = "GB18030";
            NICE_CHARSET[HZ]            = "HZ";
            NICE_CHARSET[ISO2022CN_GB]  = "ISO2022CN-GB";
            NICE_CHARSET[BIG5]          = "Big5";
            NICE_CHARSET[CNS11643]      = "CNS11643";
            NICE_CHARSET[ISO2022CN_CNS] = "ISO2022CN-CNS";
            NICE_CHARSET[ISO2022CN]     = "ISO2022 CN";
            NICE_CHARSET[UTF8]          = "UTF-8";
            NICE_CHARSET[UTF8T]         = "UTF-8 (Trad)";
            NICE_CHARSET[UTF8S]         = "UTF-8 (Simp)";
            NICE_CHARSET[UNICODE]       = "Unicode";
            NICE_CHARSET[UNICODET]      = "Unicode (Trad)";
            NICE_CHARSET[UNICODES]      = "Unicode (Simp)";
            NICE_CHARSET[EUC_KR]        = "EUC-KR";
            NICE_CHARSET[CP949]         = "CP949";
            NICE_CHARSET[ISO2022KR]     = "ISO 2022 KR";
            NICE_CHARSET[JOHAB]         = "Johab";
            NICE_CHARSET[SJIS]          = "Shift-JIS";
            NICE_CHARSET[EUC_JP]        = "EUC-JP";
            NICE_CHARSET[ISO2022JP]     = "ISO 2022 JP";
            NICE_CHARSET[ASCII]         = "ASCII";
        }
    }

}
