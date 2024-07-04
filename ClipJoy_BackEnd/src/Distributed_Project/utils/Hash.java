package Distributed_Project.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Hash {

    public static BigInteger getHashBigInt(String data) {
        String sha1 = null;
        BigInteger bi = null;
        try {
            MessageDigest msgDigest = MessageDigest.getInstance("SHA-1");
            msgDigest.reset();
            msgDigest.update(data.getBytes("UTF-8"), 0, data.length());
            sha1 = byteToHex(msgDigest.digest());
            bi = new BigInteger(sha1, 16);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            System.out.println("Error at SHA-1");
        }
        return bi;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
