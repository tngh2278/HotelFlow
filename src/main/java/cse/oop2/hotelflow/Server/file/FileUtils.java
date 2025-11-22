package cse.oop2.hotelflow.Server.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import cse.oop2.hotelflow.Server.file.BackupUtils;



public class FileUtils {
    public static void atomicWrite(Path target, String content) throws IOException{
        Path temp = Paths.get(target.toString() + ".tmp");
        Files.writeString(temp,content);
        Files.move(temp, target,
        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE);
        
    }
    public static synchronized void appendWithLock(Path target, String line) throws IOException {
    Path lock = Paths.get(target.toString() + ".lock");
    try {
        // 이미 다른 쓰레드/프로세스가 쓰는 중이면 에러 던짐
        if (Files.exists(lock)) {
            throw new IOException("파일이 잠겼습니다." + target);
        }
        Files.createFile(lock);
        //백업
        BackupUtils.backup(target);

        StringBuilder sb = new StringBuilder();
        // 기존 파일 내용이 있으면 읽어오기
        if (Files.exists(target)) {
            sb.append(Files.readString(target));
        }
        // 새 한 줄 추가
        sb.append(line);

        // atomicWrite로 안전하게 덮어쓰기
        atomicWrite(target, sb.toString());
    } finally {
        Files.deleteIfExists(lock);
    }
}
    public static synchronized void writeWithLock(Path target, String content) throws IOException {
        Path lock = Paths.get(target.toString() + ".lock");
        try {
            // 이미 잠금 파일이 존재하면 다른 작업이 진행 중인 것으로 간주
            if (Files.exists(lock)) {
                throw new IOException("파일이 잠겼습니다: " + target);
            }

            // 잠금 파일 생성
            Files.createFile(lock);

            // 자동 백업
            try {
                BackupUtils.backup(target);
                atomicWrite(target, content);
            } catch (Exception e) {
                // 백업 실패는 로깅만 하고 진행
                System.err.println("[경고] 백업 실패: " + e.getMessage());
            }

            // 실제 쓰기
            atomicWrite(target, content);

        } finally {
            // 잠금 해제 (lock 파일 삭제)
            Files.deleteIfExists(lock);
        }
    }
}

