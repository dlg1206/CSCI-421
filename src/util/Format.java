package util;

public class Format {
    public static String centerString (int width, String s) {
        return String.format("%-" + width  + "s", String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
    }


    public static String rightAlignString (int width, String s) {
        return String.format("%" + width  + "." + width + "s", s);
    }
}
