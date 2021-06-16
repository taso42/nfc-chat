package com.lyrisoft.chat.server.remote;

import com.lyrisoft.chat.Translator;

/*
 * Formatting tools.  Currently only contains one method, for formatting
 * milliseconds.
 */
public class Formatter {
    public static final int MILLIS_IN_SECOND = 1000;
    public static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    public static final int MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;

    /**
     * Convert milliseconds to a human-readable string that
     * contains days, hours, minutes, seconds, millis.
     * @param millis a length of time in milliseconds
     * @return a pretty-printable string
     */
    public static String millisToString(long millis) {
        long days;
        long hours;
        long minutes;
        long seconds;

        days = millis / MILLIS_IN_DAY;
        millis %= MILLIS_IN_DAY;

        hours = millis / MILLIS_IN_HOUR;
        millis %= MILLIS_IN_HOUR;

        minutes = millis / MILLIS_IN_MINUTE;
        millis %= MILLIS_IN_MINUTE;

        seconds = millis / MILLIS_IN_SECOND;
        millis %= MILLIS_IN_SECOND;

        StringBuffer sb = new StringBuffer();
        boolean comma = false;
        if (days > 0) {
            sb.append(days + " " + Translator.getMessage("days"));
            comma = true;
        }

        if (hours > 0) {
            if (comma) {
                sb.append(", ");
            }
            sb.append(hours + " " + Translator.getMessage("hours"));
            comma = true;
        }

        if (minutes > 0) {
            if (comma) {
                sb.append(", ");
            }
            sb.append(minutes + " " + Translator.getMessage("minutes"));
            comma = true;
        }

        if (comma) {
            sb.append(", ");
        }
        sb.append(seconds);
        sb.append("." + millis + " " + Translator.getMessage("seconds"));

        return sb.toString();
    }
}
