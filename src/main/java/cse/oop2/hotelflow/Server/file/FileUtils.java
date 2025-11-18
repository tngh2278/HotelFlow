package cse.oop2.hotelflow.Server.file;

import java.io.IOException;
import java.nio.file.*;



public class FileUtils {
    public static void atomicWrite(Path target, String content) throws IOException{
        Path temp = Paths.get(target.toString() + ".tmp");
        Files.writeString(temp,content);
        Files.move(temp, target,
        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE);
        
    }
    public static synchronized void writeWithLock ( Path target, String content)throws IOException {
        Path lock = Paths.get(target.toString() + ".lock");
        try{
            if(Files.exists(lock)){
                throw new IOException("파일이 잠겼습니다." + target);
            }
            Files.createFile(lock);
            atomicWrite(target, content);
        } finally {
            Files.deleteIfExists(lock);
        }
        
    }
}
