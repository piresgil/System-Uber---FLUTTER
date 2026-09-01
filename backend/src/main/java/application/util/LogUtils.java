package application.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtils {
    private static final Logger logger = Logger.getLogger(LogUtils.class.getName());

    public static void logError(String mensagem, Exception e) {
        logger.log(Level.SEVERE, mensagem, e);
    }

    public static void logInfo(String mensagem) {
        logger.log(Level.INFO, mensagem);
    }

}