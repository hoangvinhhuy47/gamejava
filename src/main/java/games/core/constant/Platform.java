package games.core.constant;

/**
 * Created by tuanhoang on 4/13/17.
 */
public enum Platform {
    UNKNOWN (0),
    IOS (1),
    ANDROID (2),
    WINDOW_PHONE (3),
    WEB (4);

    int value;
    Platform(int val) {
        this.value = val;
    }
    public int getValue() {
        return this.value;
    }

    public static Platform valueOf(int p) {
        return values()[p];
    }

}
