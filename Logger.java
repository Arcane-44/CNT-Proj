import java.text.SimpleDateFormat;
import java.util.*;

public class Logger {
    private static String get_time() {
        Date date = new Date();
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        return date_format.format(date);
    }

    public static void log() {

    }
}