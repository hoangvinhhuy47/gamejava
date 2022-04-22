package libs.util;

import java.time.ZonedDateTime;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class Helper {
    public static int toInteger(Object o) {
        try {
            if (o instanceof Number) {
                return ((Number) o).intValue();
            }

            return Integer.parseInt(o.toString());
        } catch (Exception ex) {
            return 0;
        }
    }

    public static int toInt(Object o) {
        return toInteger(o);
    }

    public static long toLong(Object o) {
        try {
            if (o instanceof Number) {
                return ((Number) o).longValue();
            }

            return Long.parseLong(o.toString());
        } catch (Exception ex) {
            return 0;
        }
    }

    public static float toFloat(Object o) {
        try {
            if (o instanceof Number) {
                return ((Number) o).floatValue();
            }

            return Float.parseFloat(o.toString());
        } catch (Exception ex) {
            return 0f;
        }
    }

    public static double toDouble(Object o) {
        try {
            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            }

            return Double.parseDouble(o.toString());
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public static boolean toBoolean(Object o) {
        try {
            if (o instanceof Boolean) {
                return (Boolean) o;
            } else if (o instanceof Number) {
                return ((Number) o).intValue() != 0;
            }

            return Boolean.parseBoolean(o.toString());
        } catch (Exception ex) {
            return false;
        }
    }
    public static int TOTAL_SECOND_ONE_DAY = 86400;
    public static int getDayCurrent() {
        long second = ZonedDateTime.now().toEpochSecond();
        int day = (int)(second / (TOTAL_SECOND_ONE_DAY));
        return day;
    }

    public static long getTimeStamp() {
        long second = ZonedDateTime.now().toEpochSecond();
        return second;
    }
}
