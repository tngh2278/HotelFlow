package cse.oop2.hotelflow.Server.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HotelLogger {

    private static final Path LOG_DIR = Paths.get("logs");
    private static final Path AUDIT_LOG = LOG_DIR.resolve("audit.log");
    private static final Path ERROR_LOG = LOG_DIR.resolve("error.log");

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void ensureDir() throws IOException {
        if (!Files.exists(LOG_DIR)) {
            Files.createDirectories(LOG_DIR);
        }
    }

    public static synchronized void audit(String actor, String action, String detail) {
        try {
            ensureDir();
            String ts = LocalDateTime.now().format(TS_FORMAT);
            String line = String.format(
                    "%s | %s | %s | %s%n",
                    ts,
                    actor == null ? "-" : actor,
                    action,
                    detail == null ? "" : detail);
            Files.writeString(
                    AUDIT_LOG,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            // 로깅 중 에러는 콘솔만 찍고 무시
            e.printStackTrace();
        }
    }

    public static synchronized void error(String location, String message, Throwable t) {
        try {
            ensureDir();
            String ts = LocalDateTime.now().format(TS_FORMAT);

            StringWriter sw = new StringWriter();
            if (t != null) {
                t.printStackTrace(new PrintWriter(sw));
            }

            String line = String.format(
                    "%s | %s | %s%n%s%n",
                    ts,
                    location,
                    message,
                    (t == null ? "" : sw.toString()));

            Files.writeString(
                    ERROR_LOG,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
