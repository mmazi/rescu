package si.mazi.rescu;

public class Utils {
    public static String clip(String str, int startChars) {
        return str.length() <= startChars ? str : str.substring(0, startChars) + "...";
    }
}
