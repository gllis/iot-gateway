package com.gllis.gateway.server.util;


/**
 * 16进制工具类
 *
 * @author glli
 * @date 2023/8/15
 */
public class HexUtil {
    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * byte数组转16进制字符串
     *
     * @param src
     * @return
     */
    public static String convertByteToHex(byte[] src) {
        return convertByteToHex(src, 0, src.length);
    }

    /**
     * byte数组转16进制字符串，指定的区间
     *
     * @param src
     * @param start 起始索引
     * @param len   起始位后多少位,长度
     * @return
     */
    public static String convertByteToHex(byte[] src, int start, int len) {
        char[] hex = new char[2];
        StringBuffer strBuffer = new StringBuffer(len * 2);
        int abyte;
        for (int i = start; i < start + len; i++) {
            abyte = src[i] < 0 ? 256 + src[i] : src[i];
            hex[0] = HEX[abyte / 16];
            hex[1] = HEX[abyte % 16];
            strBuffer.append(hex);
        }
        return strBuffer.toString();
    }

    public static String convertByteToHexSpace(byte[] src) {
        char[] hex = new char[2];
        int start = 0, len = src.length;
        StringBuffer strBuffer = new StringBuffer(len * 2);
        int abyte;
        for (int i = start; i < start + len; i++) {
            abyte = src[i] < 0 ? 256 + src[i] : src[i];
            hex[0] = HEX[abyte / 16];
            hex[1] = HEX[abyte % 16];
            strBuffer.append(hex).append(' ');
        }
        return strBuffer.toString().trim();
    }

    private static byte uniteBytes(String src0, String src1) {
        byte b0 = Byte.decode("0x" + src0).byteValue();
        b0 = (byte) (b0 << 4);
        byte b1 = Byte.decode("0x" + src1).byteValue();
        byte ret = (byte) (b0 | b1);
        return ret;
    }

    /**
     * 16进制字符串转byte数组
     *
     * @param src
     * @return
     */
    public static byte[] convertHexToByte4(String src) {
        int m = 0, n = 0;
        while (src.length() < 4) {
            src = "0" + src;
        }
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
        }
        return ret;
    }

    public static byte[] convertHexToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * 16进制转int数组
     *
     * @param hex
     * @return
     */
    public static int[] convertHexToInt(String hex) {
        int len = (hex.length() / 2);
        int[] result = new int[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (int) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        int b = (int) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 字符串转16进制
     *
     * @param s
     * @return
     */
    public static String convertHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str.toUpperCase();
    }

    public static short convertUnsigned(byte b) {
        return (short) (b >= 0 ? b : 256 + b);
    }

    /**
     * ASCII码字符串转数字字符串
     *
     * @param content ASCII字符串
     * @return 字符串
     */
    public static String convertAsciiToString(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    /**
     * ASCII码字符串转数字字符串 去掉NULL值
     *
     * @param content
     * @return
     */
    public static String convertAsciiToStringFilterNull(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            if ("00".equals(c)) {
                continue;
            }
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    /**
     * 十六进制字符串装十进制
     *
     * @param hex 十六进制字符串
     * @return 十进制数值
     */
    public static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    /**
     * 16进制转字符串
     *
     * @param hex
     * @return
     */
    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            // convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }

        return sb.toString();
    }

    /**
     * int转4位二进制，不足补0
     *
     * @param str
     * @return
     */
    public static String toBinary(String str) {
        String binaryString = "0000" + Integer.toBinaryString(Integer.parseInt(str));
        int len = binaryString.length();
        return binaryString.substring(len - 4, len);
    }

    public static String StringToA(String content) {
        String result = "";
        int max = content.length();
        for (int i = 0; i < max; i++) {
            char c = content.charAt(i);
            int b = (int) c;
            result = result + Integer.toHexString(b);

        }
        return result;
    }

}
