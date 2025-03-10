package kr.co.metabuild.kotris.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteUtil {

    private static final Logger logger = LoggerFactory.getLogger(ByteUtil.class);

    private ByteUtil() {
        // should never be called!
    }


    // Boolean Conversions

    /**
     * This function converts a boolean to its corresponding byte array format.
     *
     * @param b The boolean to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] booleanToBytes(boolean b) {
        byte[] buffer = new byte[1];
        booleanToBytes(b, buffer);
        return buffer;
    }


    /**
     * This function converts a boolean into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param b      The boolean to be converted.
     * @param buffer The byte array.
     * @return The number of bytes inserted.
     */
    public static int booleanToBytes(boolean b, byte[] buffer) {
        return booleanToBytes(b, buffer, 0);
    }


    /**
     * This function converts a boolean into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param b      The boolean to be converted.
     * @param buffer The byte array.
     * @param index  The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int booleanToBytes(boolean b, byte[] buffer, int index) {
        int length = 1;
        buffer[index] = (byte) (b ? 0xFF : 0x00);
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding boolean value.
     *
     * @param buffer The byte array containing the boolean.
     * @return The corresponding boolean value.
     */
    public static boolean bytesToBoolean(byte[] buffer) {
        return bytesToBoolean(buffer, 0);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding boolean value.
     *
     * @param buffer The byte array containing the boolean.
     * @param index  The index for the first byte in the byte array.
     * @return The corresponding boolean value.
     */
    public static boolean bytesToBoolean(byte[] buffer, int index) {
        return buffer[index] != 0;
    }


    // Short Conversions

    /**
     * This function converts a short to its corresponding byte array format.
     *
     * @param s The short to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] shortToBytes(short s) {
        byte[] buffer = new byte[2];
        shortToBytes(s, buffer, 0);
        return buffer;
    }


    /**
     * This function converts a short into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param s      The short to be converted.
     * @param buffer The byte array.
     * @return The number of bytes inserted.
     */
    public static int shortToBytes(short s, byte[] buffer) {
        return shortToBytes(s, buffer, 0);
    }


    /**
     * This function converts a short into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param s      The short to be converted.
     * @param buffer The byte array.
     * @param index  The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int shortToBytes(short s, byte[] buffer, int index) {
        int length = 2;
        for (int i = 0; i < length; i++) {
            buffer[index + length - i - 1] = (byte) (s >> (i * 8));
        }
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding short value.
     *
     * @param buffer The byte array containing the short.
     * @return The corresponding short value.
     */
    public static short bytesToShort(byte[] buffer) {
        return bytesToShort(buffer, 0);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding short value.
     *
     * @param buffer The byte array containing the short.
     * @param index  The index for the first byte in the byte array.
     * @return The corresponding short value.
     */
    public static short bytesToShort(byte[] buffer, int index) {
        int length = 2;
        short integer = 0;
        for (int i = 0; i < length; i++) {
            integer |= ((buffer[index + length - i - 1] & 0xFF) << (i * 8));
        }
        return integer;
    }


    // Integer Conversions

    /**
     * This function converts an integer to its corresponding byte array format.
     *
     * @param i The integer to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] intToBytes(int i) {
        byte[] buffer = new byte[4];
        intToBytes(i, buffer, 0);
        return buffer;
    }


    /**
     * This function converts an integer into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param i      The integer to be converted.
     * @param buffer The byte array.
     * @return The number of bytes inserted.
     */
    public static int intToBytes(int i, byte[] buffer) {
        return intToBytes(i, buffer, 0);
    }


    /**
     * This function converts an integer into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param i      The integer to be converted.
     * @param buffer The byte array.
     * @param index  The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int intToBytes(int i, byte[] buffer, int index) {
        int length = buffer.length - index;
        if (length > 4) length = 4;
        for (int j = 0; j < length; j++) {
            buffer[index + length - j - 1] = (byte) (i >> (j * 8));
        }
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding integer value.
     *
     * @param buffer The byte array containing the integer.
     * @return The corresponding integer value.
     */
    public static int bytesToInt(byte[] buffer) {
        return bytesToInt(buffer, 0);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding integer value.
     *
     * @param buffer The byte array containing the integer.
     * @param index  The index for the first byte in the byte array.
     * @return The corresponding integer value.
     */
    public static int bytesToInt(byte[] buffer, int index) {
        int length = buffer.length - index;
        if (length > 4) length = 4;
        int integer = 0;
        for (int i = 0; i < length; i++) {
            integer |= ((buffer[index + length - i - 1] & 0xFF) << (i * 8));
        }
        return integer;
    }


    // Long Integer Conversions

    /**
     * This function converts a long to its corresponding byte array format.
     *
     * @param l The long to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] longToBytes(long l) {
        byte[] buffer = new byte[8];
        longToBytes(l, buffer, 0);
        return buffer;
    }


    /**
     * This function converts a long into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param l      The long to be converted.
     * @param buffer The byte array.
     * @return The number of bytes inserted.
     */
    public static int longToBytes(long l, byte[] buffer) {
        return longToBytes(l, buffer, 0);
    }


    /**
     * This function converts a long into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param l      The long to be converted.
     * @param buffer The byte array.
     * @param index  The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int longToBytes(long l, byte[] buffer, int index) {
        int length = buffer.length - index;
        if (length > 8) length = 8;
        for (int i = 0; i < length; i++) {
            buffer[index + length - i - 1] = (byte) (l >> (i * 8));
        }
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding long value.
     *
     * @param buffer The byte array containing the long.
     * @return The corresponding long value.
     */
    public static long bytesToLong(byte[] buffer) {
        return bytesToLong(buffer, 0);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding long value.
     *
     * @param buffer The byte array containing the long.
     * @param index  The index for the first byte in the byte array.
     * @return The corresponding long value.
     */
    public static long bytesToLong(byte[] buffer, int index) {
        int length = buffer.length - index;
        if (length > 8) length = 8;
        long l = 0;
        for (int i = 0; i < length; i++) {
            l |= ((buffer[index + length - i - 1] & 0xFFL) << (i * 8));
        }
        return l;
    }


    // Big Integer Conversions

    /**
     * This function converts a big integer to its corresponding byte array format.
     *
     * @param integer The big integer to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] bigIntegerToBytes(BigInteger integer) {
        return integer.toByteArray();
    }


    /**
     * This function converts a big integer into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param integer The big integer to be converted.
     * @param buffer  The byte array.
     * @return The number of bytes inserted.
     */
    public static int bigIntegerToBytes(BigInteger integer, byte[] buffer) {
        return bigIntegerToBytes(integer, buffer, 0);
    }


    /**
     * This function converts a big integer into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param integer The big integer to be converted.
     * @param buffer  The byte array.
     * @param index   The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int bigIntegerToBytes(BigInteger integer, byte[] buffer, int index) {
        int length = 4 + (integer.bitLength() + 8) / 8;
        System.arraycopy(intToBytes(length), 0, buffer, index, 4);  // copy in the length
        index += 4;
        System.arraycopy(integer.toByteArray(), 0, buffer, index, length - 4);  // copy in the big integer
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding big integer value.
     *
     * @param buffer The byte array containing the big integer.
     * @return The corresponding big integer value.
     */
    public static BigInteger bytesToBigInteger(byte[] buffer) {
        return new BigInteger(buffer);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding big integer value.
     *
     * @param buffer The byte array containing the big integer.
     * @param index  The index for the first byte in the byte array.
     * @return The corresponding big integer value.
     */
    public static BigInteger bytesToBigInteger(byte[] buffer, int index) {
        int length = bytesToInt(buffer, index);  // pull out the length of the big integer
        index += 4;
        byte[] bytes = new byte[length];
        System.arraycopy(buffer, index, bytes, 0, length);  // pull out the bytes for the big integer
        return new BigInteger(bytes);
    }


    // Double Precision Floating Point Conversions

    /**
     * This function converts a double to its corresponding byte array format.
     *
     * @param d The double to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] doubleToBytes(double d) {
        byte[] buffer = new byte[8];
        doubleToBytes(d, buffer, 0);
        return buffer;
    }


    /**
     * This function converts a double into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param d      The double to be converted.
     * @param buffer The byte array.
     * @return The number of bytes inserted.
     */
    public static int doubleToBytes(double d, byte[] buffer) {
        return doubleToBytes(d, buffer, 0);
    }


    /**
     * This function converts a double into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param s      The double to be converted.
     * @param buffer The byte array.
     * @param index  The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int doubleToBytes(double s, byte[] buffer, int index) {
        long bits = Double.doubleToRawLongBits(s);
        int length = longToBytes(bits, buffer, index);
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding double value.
     *
     * @param buffer The byte array containing the double.
     * @return The corresponding double value.
     */
    public static double bytesToDouble(byte[] buffer) {
        return bytesToDouble(buffer, 0);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding double value.
     *
     * @param buffer The byte array containing the double.
     * @param index  The index for the first byte in the byte array.
     * @return The corresponding double value.
     */
    public static double bytesToDouble(byte[] buffer, int index) {
        double real;
        long bits = bytesToLong(buffer, index);
        real = Double.longBitsToDouble(bits);
        return real;
    }


    // Big Floating Point Conversions

    /**
     * This function converts a big decimal to its corresponding byte array format.
     *
     * @param decimal The big decimal to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] bigDecimalToBytes(BigDecimal decimal) {
        String string = decimal.toString();
        return stringToBytes(string);
    }


    /**
     * This function converts a big decimal into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param decimal The big decimal to be converted.
     * @param buffer  The byte array.
     * @return The number of bytes inserted.
     */
    public static int bigDecimalToBytes(BigDecimal decimal, byte[] buffer) {
        return bigDecimalToBytes(decimal, buffer, 0);
    }


    /**
     * This function converts a big decimal into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param decimal The big decimal to be converted.
     * @param buffer  The byte array.
     * @param index   The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int bigDecimalToBytes(BigDecimal decimal, byte[] buffer, int index) {
        BigInteger intVal = decimal.unscaledValue();
        int length = 12 + (intVal.bitLength() + 8) / 8;

        int scale = decimal.scale();
        System.arraycopy(intToBytes(scale), 0, buffer, index, 4);  // copy in the scale
        index += 4;

        int precision = decimal.precision();
        System.arraycopy(intToBytes(precision), 0, buffer, index, 4);  // copy in the scale
        index += 4;

        System.arraycopy(bigIntegerToBytes(intVal), 0, buffer, index, length - 8);     // copy in the big integer
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding big decimal value.
     *
     * @param buffer The byte array containing the big decimal.
     * @return The corresponding big decimal value.
     */
    public static BigDecimal bytesToBigDecimal(byte[] buffer) {
        String string = bytesToString(buffer);
        return new BigDecimal(string);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding big decimal value.
     *
     * @param buffer The byte array containing the big decimal.
     * @param index  The index for the first byte in the byte array.
     * @return The corresponding big decimal value.
     */
    public static BigDecimal bytesToBigDecimal(byte[] buffer, int index) {
        int scale = bytesToInt(buffer, index);
        index += 4;
        int precision = bytesToInt(buffer, index);
        index += 4;
        BigInteger intVal = bytesToBigInteger(buffer, index);
        return new BigDecimal(intVal, scale, new MathContext(precision));
    }


    // String Conversions

    /**
     * This function converts a string to its corresponding byte array format.
     *
     * @param string The string to be converted.
     * @return The corresponding byte array.
     */
    public static byte[] stringToBytes(String string) {
        return string.getBytes();
    }


    /**
     * This function converts a string into its corresponding byte format and inserts
     * it into the specified buffer.
     *
     * @param string The string to be converted.
     * @param buffer The byte array.
     * @return The number of bytes inserted.
     */
    public static int stringToBytes(String string, byte[] buffer) {
        return stringToBytes(string, buffer, 0);
    }


    /**
     * This function converts a string into its corresponding byte format and inserts
     * it into the specified buffer at the specified index.
     *
     * @param string The string to be converted.
     * @param buffer The byte array.
     * @param index  The index in the array to begin inserting bytes.
     * @return The number of bytes inserted.
     */
    public static int stringToBytes(String string, byte[] buffer, int index) {
        byte[] bytes = string.getBytes();
        int length = bytes.length;
        System.arraycopy(bytes, 0, buffer, index, length);
        return length;
    }


    /**
     * This function converts the bytes in a byte array to its corresponding string value.
     *
     * @param buffer The byte array containing the string.
     * @return The corresponding string value.
     */
    public static String bytesToString(byte[] buffer) {
        return bytesToString(buffer, 0, buffer.length);
    }


    /**
     * This function converts the bytes in a byte array at the specified index to its
     * corresponding string value.
     *
     * @param buffer The byte array containing the string.
     * @param index  The index for the first byte in the byte array.
     * @param length The number of bytes that make up the string.
     * @return The corresponding string value.
     */
    public static String bytesToString(byte[] buffer, int index, int length) {
        return new String(buffer, index, length);
    }


    // Miscellaneous

    /**
     * This function converts a byte into its unsigned integer form.
     *
     * @param b The byte to be converted.
     * @return The unsigned version of the byte as an integer.
     */
    public static int byteToUnsigned(byte b) {
        int unsigned = b & 0xFF;
        return unsigned;
    }


    /**
     * This function calculates a hash code for the specified byte array.
     *
     * @param bytes The byte array.
     * @return The hash code.
     */
    public static int hashCode(byte[] bytes) {
        int hash = Arrays.hashCode(bytes);
        return hash;
    }


    /**
     * This function creates a copy of a byte array.
     *
     * @param bytes The byte array.
     * @return A copy of the byte array.
     */
    public static byte[] copy(byte[] bytes) {
        return Arrays.copyOf(bytes, bytes.length);
    }


    /**
     * This function compares two byte arrays to see if they are equal.
     *
     * @param first  The first byte array.
     * @param second The second byte array.
     * @return Whether or not, the two byte arrays are equal.
     */
    public static boolean equals(byte[] first, byte[] second) {
        return Arrays.equals(first, second);
    }


    /**
     * This function compares two byte arrays for canonical ordering.
     *
     * @param first  The first byte array.
     * @param second The second byte array.
     * @return The sign Num result of the comparison.
     */
    public static int compare(byte[] first, byte[] second) {
        // choose the shorter array length
        int firstLength = first.length;
        int secondLength = second.length;
        int shorterLength = Math.min(firstLength, secondLength);

        // compare matching bytes
        for (int i = 0; i < shorterLength; i++) {
            int result = Byte.compare(first[i], second[i]);
            if (result != 0) return Integer.signum(result);
        }

        // compare lengths if bytes match
        if (firstLength < secondLength) return -1;
        if (firstLength > secondLength) return 1;

        // must be same length and equal
        return 0;
    }


    public static byte[] leftPad(byte[] data, int size, byte pad) {
        if (size <= data.length) {
            return data;
        }

        byte[] newData = new byte[size];
        for (int i = 0; i < size; i++) {
            newData[i] = pad;
        }
        for (int i = 0; i < data.length; i++) {
            newData[size - i - 1] = data[data.length - i - 1];
        }

        return newData;
    }

    public static String leftAlignedPaddedString(String input, int length) {
        if (length <= 0) {
            return null;
        }

        StringBuilder output = new StringBuilder();
        char space = ' ';

        if (input != null) {
            if (input.length() < length) {
                output.append(input);
                for (int i = input.length(); i < length; i++) {
                    output.append(space);
                }
            } else {
                output.append(input.substring(0, length));
            }
        } else {
            for (int i = 0; i < length; i++) {
                output.append(space);
            }
        }

        return output.toString();
    }

    public static String leftPadSpace(final String input, final int size) {
        int pads = size - input.length();
        if (pads <= 0)
            return input;
        StringBuilder out = new StringBuilder();
        for (int i = pads; i > 0; i--)
            out.append(' ');
        return out.append(input).toString();
    }

    private static String leftPadZero(final String numStr, final int numDigits) {
        StringBuffer buff = new StringBuffer(numStr);
        int delta = numDigits - numStr.length();
        for (int i = 0; i < delta; i++) {
            buff.insert(0, '0');
        }
        return buff.toString();
    }


    public static byte[] setString(String data, int size) {
        byte pad = 0x20;
        int len = data.getBytes().length;
        byte[] newData = new byte[size];
        if (size <= len) {
            System.arraycopy(data.getBytes(), 0, newData, 0, size);
            return newData;
        }
        System.arraycopy(data.getBytes(), 0, newData, 0, len);
        for (int i = len; i < size; i++) {
            newData[i] = pad;
        }

        return newData;
    }

    public static byte[] setNumber(String data, int size) {
        byte pad = 0x30;
        int len = data.getBytes().length;
        byte[] newData = new byte[size];
        if (size <= len) {
            System.arraycopy(data.getBytes(), 0, newData, 0, size);
            return newData;
        }
        int offset = size - len;
        for (int i = 0; i < offset; i++) {
            newData[i] = pad;
        }
        System.arraycopy(data.getBytes(), 0, newData, offset, len);

        return newData;
    }

    /**
     * 주어진 ByteBuf에서 지정된 offset과 length에 해당하는 영역을 UTF-8 문자열로 변환하여 반환합니다.
     *
     * @param bb     변환할 ByteBuf (null이 아니어야 합니다)
     * @param offset 시작 인덱스(0부터 시작하는 바이트 위치)
     * @param length 읽어올 바이트의 수
     * @return 해당 영역을 UTF-8로 디코딩한 문자열
     * @throws IllegalArgumentException     bb가 null인 경우
     * @throws IndexOutOfBoundsException    offset 또는 length가 올바르지 않은 경우
     */
    public static String getSubString(ByteBuf bb, int offset, int length) {
        if (bb == null) {
            throw new IllegalArgumentException("ByteBuf cannot be null");
        }

        // offset과 length가 bb 용량 범위를 벗어나지 않는지 (또는 원하는 읽기 범위) 확인
        if (offset < 0 || length < 0 || offset + length > bb.capacity()) {
            throw new IndexOutOfBoundsException("Invalid offset or length");
        }

        // toString(offset, length, Charset) : 지정 위치부터 지정 길이만큼의 데이터를 해당 인코딩 방식으로 디코딩하여 문자열로 변환.
        return bb.toString(offset, length, Charset.forName("EUC-KR"));
    }


    /**
     * 주어진 offset부터 length만큼의 바이트를 지정된 Charset을 이용해 문자열로 변환합니다.
     *
     * @param buf     데이터가 담긴 ByteBuf
     * @param offset  읽기 시작 인덱스
     * @param length  읽을 바이트 수 (문자열의 바이트 길이)
     * @param charset 문자열 인코딩 방식
     * @return 추출한 문자열
     * @throws IndexOutOfBoundsException 만약 buf의 크기가 offset+length보다 작다면
     */
    public static String extractString(ByteBuf buf, int offset, int length, Charset charset) {
        if (buf.capacity() < offset + length) {
            throw new IndexOutOfBoundsException("Buffer capacity is less than offset + length.");
        }
        return buf.toString(offset, length, charset);
    }


    /**
     * 기본 Charset (UTF-8)을 사용하여 주어진 offset부터 length만큼의 바이트를 문자열로 변환합니다.
     *
     * @param buf    데이터가 담긴 ByteBuf
     * @param offset 읽기 시작 인덱스
     * @param length 읽을 바이트 수 (문자열의 바이트 길이)
     * @return 추출한 문자열
     */
    public static String extractString(ByteBuf buf, int offset, int length) {
        return extractString(buf, offset, length, StandardCharsets.UTF_8);
    }


    /**
     * 주어진 offset에서 4바이트를 읽어 int 값을 추출합니다.
     *
     * @param buf     데이터가 담긴 ByteBuf
     * @param offset  읽기 시작 인덱스
     * @param length 읽을 길이(반드시 4여야 함)
     * @return 추출한 int 값
     * @throws IllegalArgumentException length가 4가 아니면 발생
     * @throws IndexOutOfBoundsException 만약 buf의 크기가 offset+4보다 작다면
     */
    public static int extractInt(ByteBuf buf, int offset, int length) {
        if (length != 4) {
            throw new IllegalArgumentException("Int extraction requires length of 4 bytes, but got " + length);
        }
        if (buf.capacity() < offset + 4) {
            throw new IndexOutOfBoundsException("Not enough bytes in the buffer for int extraction.");
        }
        return buf.getInt(offset);
    }


    /**
     * 주어진 offset에서 8바이트를 읽어 long 값을 추출합니다.
     *
     * @param buf     데이터가 담긴 ByteBuf
     * @param offset  읽기 시작 인덱스
     * @param length 읽을 길이(반드시 8이어야 함)
     * @return 추출한 long 값
     * @throws IllegalArgumentException length가 8이 아니면 발생
     * @throws IndexOutOfBoundsException 만약 buf의 크기가 offset+8보다 작다면
     */
    public static long extractLong(ByteBuf buf, int offset, int length) {
        if (length != 8) {
            throw new IllegalArgumentException("Long extraction requires length of 8 bytes, but got " + length);
        }
        if (buf.capacity() < offset + 8) {
            throw new IndexOutOfBoundsException("Not enough bytes in the buffer for long extraction.");
        }
        return buf.getLong(offset);
    }


    /**
     * 주어진 offset부터 length만큼의 바이트를 읽어 byte 배열로 반환합니다.
     *
     * @param buf    데이터가 담긴 ByteBuf
     * @param offset 읽기 시작 인덱스
     * @param length 읽을 바이트 수
     * @return 추출한 byte 배열
     * @throws IndexOutOfBoundsException 만약 buf의 크기가 offset+length보다 작다면
     */
    public static byte[] extractBytes(ByteBuf buf, int offset, int length) {
        if (buf.capacity() < offset + length) {
            throw new IndexOutOfBoundsException("Not enough bytes in the buffer for byte array extraction.");
        }
        byte[] bytes = new byte[length];
        buf.getBytes(offset, bytes);
        return bytes;
    }

}
