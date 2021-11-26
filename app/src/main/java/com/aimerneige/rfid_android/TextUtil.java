package com.aimerneige.rfid_android;

public class TextUtil {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * 将 byte 数组 转化为十六进制字符串
     *
     * @param byteArray byte 数组
     * @return 十六进制字符串
     */
    public static String byteArrayToHexString(final byte[] byteArray) {
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 将十六进制字符串转化为 byte 数组
     *
     * @param hexString 十六进制字符串
     * @return byte 数组
     */
    public static byte[] hexStringToByteArray(final String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
