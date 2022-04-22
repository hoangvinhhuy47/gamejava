package libs.lang;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class I18n {
    private static String language = "vi";
    private static String country = "VN";

    private static Locale locale;
    private static ResourceBundle messages;

    public final static void setLocale(Locale locale) {
        I18n.locale = locale;
        //messages = ResourceBundle.getBundle("MessagesBundle", locale);
        try {
            InputStream stream = ClassLoader.getSystemResourceAsStream("MessagesBundle.properties");
            messages = new PropertyResourceBundle(new InputStreamReader(stream, "utf-8"));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public final static void setLocale(String language, String country) {
        setLocale(new Locale(language, country));
    }

    public final static String getString(String s) {
        if(locale == null) {
            setLocale(language, country);
        }
        return messages.getString(s);
    }

    public final static String getString(Language lang) {
        if(locale == null) {
            setLocale(language, country);
        }
        return messages.getString(lang.name());
    }
}
