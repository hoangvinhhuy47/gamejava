package libs.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class UserUtil {
    public static List<String> restrictPassword;
    public static List<String>	restrictNickName;
    public static List<String>	restrictUserName;

    static {
        restrictPassword = new ArrayList<>();
        restrictNickName = new ArrayList<>();
        restrictUserName = new ArrayList<>();
    }

    public static void setRestrictPassword(List<String> input) {
        restrictPassword = input;
    }

    public static void setRestrictNickName(List<String> input) {
        restrictNickName = input;
    }

    public static void setRestrictUserName(List<String> input) {
        restrictUserName = input;
    }

    public static boolean checkUserName(String username) {
        int length = username.length();
        if (length < 6 || length > 32) {
            return false;
        }
        String lowerUsername = username.toLowerCase();
        if (isFormat(lowerUsername))
            return false;
        for (int i = 1; i < length; i++) {
            char character = username.charAt(i);
            if (!isLetter(character) && !isDigit(character)) {
                return false;
            }
        }
        for (String restrictString : restrictUserName) {
            if (lowerUsername.contains(restrictString))
                return false;
        }
        return true;
    }

    public static boolean checkNickName(String nickname) {
        int length = nickname.length();
        if (length < 6 || length > 32) {
            return false;
        }
        String lowerUsername = nickname.toLowerCase();
        if (isFormat(lowerUsername))
            return false;
        for (String restrictString : restrictNickName) {
            if (lowerUsername.contains(restrictString))
                return false;
        }
        return true;
    }

    public static boolean checkPassword(String password) {
        int length = password.length();
        if (length < 6) {
            return false;
        }
        // for (int i = 1; i < length; i++) {
        // char character = password.charAt(i);
        // if (!isLetter(character) && !isDigit(character) && (character != '_')) {
        // return false;
        // }
        // }
        String lowerPassword = password.toLowerCase();
        for (String restrictString : restrictPassword) {
            if (lowerPassword.compareTo(restrictString) == 0)
                return false;
        }
        return true;
    }

    private static boolean isFormat(String format) {
        if (format.contains("/"))
            return true;
        return format.contains("\\") || format.contains("[-]");
    }

    private static boolean isDigit(char character) {
        return '0' <= character && character <= '9';
    }

    private static boolean isLetter(char c) {
        return ('a' <= c && c <= 'z');
    }
}
