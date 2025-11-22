package cse.oop2.hotelflow.Server.file;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public class BackupUtils {

    private static final Path BACKUP_DIR = Paths.get("backup");
    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static void ensureDir() throws IOException {
        if (!Files.exists(BACKUP_DIR)) {
            Files.createDirectories(BACKUP_DIR);
        }
    }

    /** target 파일이 존재하면 backup 디렉터리에 timestamp 붙여서 복사 */
    public static void backup(Path target) throws IOException {
        if (!Files.exists(target)) {
            return; // 원본이 없으면 백업할 것도 없음
        }
        ensureDir();
        String baseName = target.getFileName().toString();
        String ts = LocalDateTime.now().format(TS_FORMAT);
        Path dest = BACKUP_DIR.resolve(baseName + "." + ts + ".bak");
        Files.copy(target, dest, StandardCopyOption.COPY_ATTRIBUTES);
    }

    /** backup 디렉터리 안에서 해당 target에 대한 최신 백업을 찾아서 복구 */
    public static void restoreLatest(Path target) throws IOException {
        ensureDir();
        String baseName = target.getFileName().toString();

        try (Stream<Path> stream = Files.list(BACKUP_DIR)) {
            Path latest = stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(baseName + ".");
                    })
                    .max(Comparator.comparing(p -> p.getFileName().toString()))
                    .orElseThrow(() ->
                            new IOException("해당 파일의 백업을 찾을 수 없습니다: " + baseName));

            Files.copy(latest, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
