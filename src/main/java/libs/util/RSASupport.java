package libs.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class RSASupport {
    private static String myPrivateKey = "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAHsdF1SmuGCXKEG4WTZl8aUgc2dpzYN3OA7i0wfbtcmpy+Cq+4RHHiPbUWrOAQL/09Q4itXjFW4rvFvub2raeXVl89gfF+sRklNe4DGIdq0DQcg201fEWXpfCL9kh7+BqgW2Nj2x2P1ZqDqMvLDZpCIV4LY5PX5ALFdFpxEacpbbNRy2AgS6mavhcBagSv1GtHh1/J5YCEH33jQYfFkkB7idN+THSI+Sf2lgRo67o6IrcnUzvk3PpyptP94StXUOSxwAP0Te/OTJfyrDHHsgCwd/wqa9acqzUMXva8C2QXxNXGC87zUwJ565j4o1ZMeVK2NixVg2YdPbhG9aGu4qzdMCAwEAAQKCAQABzTAtNe9Wwn99TrRHfoBluS7XIY43UURJu3n5EHb9h3WLSRU7H0tKnQf49yC+qX2xCk7FyHfkz9EGy1toBeBnHmHOFM5lyzEH9Uh0kRmjeHu68Upwa1gf2UooetEdtUkGol7y0ippsqkqiiqsr9KeCRHruLS0pz2+3S8PqgJpQLCEHvvF9vurRBP63oFmd+OWukdvwix41FEBcTQTH72EzZstPiyAIDN9I+y0NkSupqhaiG6GQteweh3g6UGtcyHeHhz5wW59yb6gqmaLmsGkSN2TIGNNILO8B0QC71Tg3KbZbeNY1lQS6catPaNsUSgu0NpZ5AU/bcToJnG7mfQxAoGBAOtBgcD+7CNj2j5DwVeJ/q37skH2h6+unaLIdB++Sp0FYCp5jb/2tZ7Jf8T5kP9pZEaJfgwmSWBQJUqHRI8j8x/JRir9PoqsV03UTUkAHsc3i/evnSEKINM+qX6YgeN/YMcgZYortNLuF95tO8yqZAUQl0mFO/Xj154xOViTYPQJAoGBAIX4KvBrT359fAz/2t/4aNgkLJg1qmDZnYvV1IpulfmjMD6TtO45G/qxpuM/fBZ+5M0av9MSEitFpQbzMuXLauam0c/6GqQUnMr9BriEiM6c1JkMfd4aiB3EZ0iqYcwvPDWp2sqMo2VxZ9/S8nn2EC6bIJgwT0XpVku0BhiQy4H7AoGASqd+An2i99QPBKVBrILY8+MkfTQ7eK+jzG1qFEyxpsR28OOj7N/gYKQ2zI4ZykNqk8u72SUpMcwdGVp1kirpUxPvFWNJvn3UMpmFcP39xmBFJXNwHHT3V7HR3Z+N0GCymF4aes9F0Y3Gu0nUqS0FEmpiZ+1EytHjsYqTVR1nrekCgYA7v04HIxS06OnmMkU/Fd5MZvWsfGKcBvGIZ7j+dAtW5jyh6+YJSFb+GzS+rJV+wuDvyDo8vBZ2Tf0neFb+vRXUABoakwRGoRq8PNQ+fn01CCSYjUWlRFfoTqtxcOtk0Lwdgr8dDlPg7g7Rd5bEZcUZw/1pYkI4f7QI1yTQ1+Kq+QKBgA1Y2ygwD5oTwF6w91frMaG9371/RGYFDCNRUJlGxVoT21QKXXDUzZatrD0m7GC6AEgwHMOpbTNpzLydm4eEYAWNrz0XsTuA2kGU5WnizY5iKOjXDwrXx2c9avLoaWatA9Ybp5lBDxLhY6SwBKQGXyneK2LzzT1QYu4YdMzKIZSv";
    private static String myPublicKey = "MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQB7HRdUprhglyhBuFk2ZfGl\n" +
            "IHNnac2DdzgO4tMH27XJqcvgqvuERx4j21FqzgEC/9PUOIrV4xVuK7xb7m9q2nl1\n" +
            "ZfPYHxfrEZJTXuAxiHatA0HINtNXxFl6Xwi/ZIe/gaoFtjY9sdj9Wag6jLyw2aQi\n" +
            "FeC2OT1+QCxXRacRGnKW2zUctgIEupmr4XAWoEr9RrR4dfyeWAhB9940GHxZJAe4\n" +
            "nTfkx0iPkn9pYEaOu6OiK3J1M75Nz6cqbT/eErV1DkscAD9E3vzkyX8qwxx7IAsH\n" +
            "f8KmvWnKs1DF72vAtkF8TVxgvO81MCeeuY+KNWTHlStjYsVYNmHT24RvWhruKs3T\n" +
            "AgMBAAE=";

    private static String theyPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv2QmIRNmOEW0Uz1DTnQM\n" +
            "vL9dlWbJpL2AUBx9IASDnGvcq51PygZG6MwVJahOmUPba2yL5rPAkfOfl8mvch68\n" +
            "nrXjtrp7ww8TDcKfx+e2MPhSWXk9IRWxW5CPDiLYv3gkPZwsRHIYUAY6Lo9nfRAX\n" +
            "+0MkiplUe8O9aUr/cwF5s0nVGwkch9bZpZkcw4G+FHnjT+thM3FiIXKrxZ197YLm\n" +
            "LXmVrJr+6g202kjmOhEPqE4bNDIb6BR2+eUFLmNeG2gbAt2YhgXVzbr0Ldv+h7qt\n" +
            "y/pvSbrcpULNcwLqiy+7SE6KJMTAb749TQ1VDDTjaDTwasygeaN99RGNeM84QkRh\n" +
            "CwIDAQAB";

    public static String Encrpytion(String data) {
        String result = "";
        try {
            //byte[] publicByte = Base64.getDecoder().decode(theyPublicKey);
            byte[] publicByte = Base64.decodeBase64(theyPublicKey);

            // Tạo public key
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicByte);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory.generatePublic(spec);

            // Mã hoá dữ liệu
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, pubKey);

            byte encryptOut[] = c.doFinal(data.getBytes());
            result = MyBase64.encodeBytes(encryptOut);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String Decryption(String data) {
        String result = "";
        try {
            myPrivateKey = myPrivateKey.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");

            myPrivateKey = myPrivateKey.replace("\n-----END RSA PRIVATE KEY-----\n", "");

            //byte[] privateByte = Base64.getDecoder().decode(myPrivateKey);

            byte[] privateByte2 =  DatatypeConverter.parseBase64Binary(myPrivateKey);

            // Tạo private key
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateByte2);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = factory.generatePrivate(spec);

            // Giải mã dữ liệu
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, priKey);
            //byte decryptOut[] = c.doFinal(Base64.getDecoder().decode(data));//MyBase64.decode(data)
            byte decryptOut[] = c.doFinal(Base64.decodeBase64(data));
            result = new String(decryptOut);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Encrypts the text with the public key (RSA)
     *
     * @param rawText Text to be encrypted
     * @param publicKey
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static String encrypt(String rawText, PublicKey publicKey) throws IOException, GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.encodeBase64String(cipher.doFinal(rawText.getBytes("UTF-8")));//;//Base64.getEncoder().encodeToString(cipher.doFinal(rawText.getBytes("UTF-8")));
    }

    /**
     * Decrypts the text with the private key (RSA)
     *
     * @param cipherText Text to be decrypted
     * @param privateKey
     * @return Decrypted text (Base64 encoded)
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static String decrypt(String cipherText, PrivateKey privateKey) throws IOException, GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.decodeBase64(cipherText)), "UTF-8");//;//String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), "UTF-8");
    }

    // AES
    public static String generateString(int length)
    {
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWYZ";
        Random rng = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    public static String encryptAES(String input, String key){
        byte[] crypted = null;
        try{
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(input.getBytes());
        }catch(Exception e){
            System.out.println(e.toString());
        }
        //return java.util.Base64.getEncoder().encodeToString(crypted);
        return Base64.encodeBase64String(crypted);
    }

    public static String decryptAES(String input, String key){
        byte[] output = null;
        try{
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skey);
            //output = cipher.doFinal(java.util.Base64.getDecoder().decode(input));
            output = cipher.doFinal(Base64.decodeBase64(input));
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return new String(output);
    }
}
