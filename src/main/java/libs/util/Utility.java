package libs.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import games.core.constant.Version;
import org.apache.commons.net.util.SubnetUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class Utility {
    public static String md5(String src) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] data = src.getBytes(Charset.forName("UTF-8"));
            md5.update(data);
            byte[] outData = md5.digest();
            md5.reset();
            StringBuilder hexString = new StringBuilder();
            for (byte anOutData : outData) {
                String passChar = Integer.toHexString(0xFF & anOutData);
                if (passChar.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(passChar);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final long SecondPerDay = 24 * 60 * 60;
    public static long getTimeSecondWhenEndDay(long currentDay) {
        return (currentDay + 1) * SecondPerDay;
    }

    public static long getTotalDayCurrent() {
        return getTotalDay(System.currentTimeMillis());
    }

    public static long getUnixTimeWhenEndDay(long currentDay) {
        return (currentDay + 1) * MilisecondPerDay;
    }

    public static final long MilisecondPerDay = 86400000;

    public static long getTotalDay(long timeInMilis) {
        return (long) (timeInMilis / MilisecondPerDay);
        //return (long) (timeInMilis / 60000);
    }

    public static long getTotalDayBySecond(long seconds) {
        return seconds / SecondPerDay;
    }

    public static String md5(byte[] src) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(src);
            byte[] outData = md5.digest();
            md5.reset();
            StringBuilder hexString = new StringBuilder();
            for (byte anOutData : outData) {
                String passChar = Integer.toHexString(0xFF & anOutData);
                if (passChar.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(passChar);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean authenticate(String attemptedPassword, String encryptedPassword, String salt) {
        return encryptedPassword.equals(getEncryptedPassword(attemptedPassword, salt));
    }

    public static String getEncryptedPassword(String password, String salt) {
        return XXTEA.encryptToBase64String(password, salt);
    }

    public static String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return MyBase64.encodeBytes(salt);
    }

    /*
     * String dateTime = "30/7/2013 08:00:00";
     */
    public static String formatDateTime(ZonedDateTime datetime) {
        if (datetime == null)
            return null;
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(datetime);
    }

    /*
     * String dateTime = "30-7-2013";
     */
    public static String formatDateTimeForKey(ZonedDateTime datetime) {
        if (datetime == null)
            return null;

        return DateTimeFormatter.ofPattern("dd-MM-yyyy").format(datetime);
    }

    /*
     * String dateTime = "30/7/2013 08:00:00";
     */
    public static ZonedDateTime convertDateTime(String dateTime) {
        if (dateTime == null)
            return null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return (ZonedDateTime)dtf.parse(dateTime);
    }

    /*
     * String dateTime = "30/7/2013";
     */
    public static String formatDate(ZonedDateTime datetime) {
        if (datetime == null)
            return null;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormatter.format(datetime);
    }

    /*
     * String dateTime = "30/7/2013 08:00:00";
     */
    public static Date convertDate(String dateTime) {
        if (dateTime == null)
            return null;
        try {
            SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return dateParser.parse(dateTime);
        } catch (Exception ignored) {
        }
        return null;
    }

    /*
     * IMPORTANT : Some special characters need to be escaped while providing them as delimiters like "." and "|".
     */
    public static boolean checkVersion(String clientV, String newestV) {
        String delimiter = "\\.";
        String[] clientVSplit = clientV.split(delimiter);
        String[] newestVVSplit = newestV.split(delimiter);
        for (int i = 0; i < clientVSplit.length; i++) {
            String c = clientVSplit[i];
            String n = newestVVSplit[i];
            if (c.compareTo(n) < 0) {
                return true;
            } else if (c.compareTo(n) > 0) {
                return false;
            }
        }
        return false;
    }

    public static boolean checkTypeOfList(Object object, Class<?> clazz) {
        if (object instanceof List<?>) {
            List<?> arrayList = (List<?>) object;
            for (Object anArrayList : arrayList) {
                if (anArrayList != null && anArrayList.getClass() != clazz) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static void sendMail(String from, final String password, String to, String subject, String content) {
        // final String username = "bingotavietnam@gmail.com";
//		final String username = from.substring(0, from.indexOf("@"));
//		// using SSL
//		Properties props = new Properties();
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.socketFactory.port", "465");
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.port", "465");
//		Session channel = Session.getDefaultInstance(props, new Authenticator() {
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(username, password);
//			}
//		});
//		try {
//
//			Message message = new MimeMessage(channel);
//			message.setFrom(new InternetAddress(from));
//			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
//			message.setSubject(subject);
//			message.setText(content);
//			Transport.send(message);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }

    private static boolean initilized = false;

    private static class ObjectMapperWrapper {
        private static final ObjectMapper INSTANCE = new ObjectMapper();
    }

    public static ObjectMapper getObjectMapper() {
        if (!initilized) {
            initilized = true;
            ObjectMapperWrapper.INSTANCE.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            ObjectMapperWrapper.INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectMapperWrapper.INSTANCE.setPropertyNamingStrategy(new PropertyNamingStrategy());
        }
        return ObjectMapperWrapper.INSTANCE;
    }

    public static ObjectMapper getYmlObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SubnetUtils.SubnetInfo.class, new SubnetDeserializer());
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.registerModule(module);

        return mapper;
    }

    public static <T> T loadConfig(String file, Class<T> clazz) throws IOException {
        File configFile = new File(file);
        return Utility.getYmlObjectMapper().readValue(configFile, clazz);
    }

    public static boolean isWhitelisted(final List<SubnetUtils.SubnetInfo> whitelist, String ip) {
        if (whitelist == null || whitelist.size() <= 0)
            return true;

        for (SubnetUtils.SubnetInfo subnet : whitelist) {
            if (subnet.isInRange(ip)) {
                return true;
            }
        }
        return false;
    }

//    private static Snowflake snowflake;
//
//    public synchronized static long getNextId() {
//        if (snowflake == null)
//            snowflake = new Snowflake(XConfig.getServerSettings().serverId);
//        return (snowflake.next()/1000);
//    }


    public static List<Integer> intArrayToList(int[] arr) {
        List<Integer> result = new ArrayList<>(arr.length);
        for (int i = 0; i < arr.length; i++)
        {
            result.add(arr[i]);
        }
        return result;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || "".equals(s);
    }

    public static List<Integer> parseIntArray(String s) {
        List<Integer> result = new ArrayList<>();
        if (!Utility.isNullOrEmpty(s)) {
            for (String s1 : s.split("-")) {
                result.add(Integer.valueOf(s1));
            }
        }
        return result;
    }

    public static List<Integer> parseIntArray(String s, String splitter) {
        List<Integer> result = new ArrayList<>();
        if (!Utility.isNullOrEmpty(s)) {
            for (String s1 : s.split(splitter)) {
                result.add(Integer.valueOf(s1));
            }
        }
        return result;
    }

    public static boolean testVersion(String config, Version version) {
        if(Utility.isNullOrEmpty(config))
            return true;
        if(version == null) {
            return false;
        }
        String[] parts = config.split("-");
        int mode = Integer.parseInt(parts[0]);
        List<Integer> includes = Utility.parseIntArray(parts[1], ",");
        List<Integer> excludes = Utility.parseIntArray(parts[2], ",");

        switch (mode) {
            case 0:
                return true;
            case 1:
                return includes.contains(version.getId());
            case 2:
                return !excludes.contains(version.getId());
            case 3:
                return (includes.contains(version.getId()) && !excludes.contains(version.getId()));
        }
        return false;
    }
}
