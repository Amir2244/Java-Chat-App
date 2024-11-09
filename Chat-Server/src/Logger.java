import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public final class Logger {
    private static final AtomicReference<Logger> instance = new AtomicReference<>();
    private static final String LOG_FILE = "./chat_app_logs.txt";
    private final SimpleDateFormat dateFormat;

    private Logger() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        createLogFile();
    }

    public static Logger getInstance() {
        Logger result = instance.get();
        if (result == null) {
            synchronized (Logger.class) {
                result = instance.get();
                if (result == null) {
                    result = new Logger();
                    instance.set(result);
                }
            }
        }
        return result;
    }

    private void createLogFile() {
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException("Could not create log file");
                }
            } catch (IOException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Failed to create log file", e);
            }
        }
    }

    public void log(String level, String message) {
        if (level == null || message == null) {
            throw new IllegalArgumentException("Level and message cannot be null");
        }

        String logEntry = String.format("[%s] [%s] %s%n",
                dateFormat.format(new Date()), level, message);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to write to log file", e);
        }
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    public void warn(String message) {
        log("WARN", message);
    }
}