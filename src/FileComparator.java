
// FileComparator.java
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileComparator {
    // Метод для побайтного сравнения содержимого двух файлов
    public boolean areFilesEqual(Path file1, Path file2) throws IOException {
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        }

        try (InputStream is1 = Files.newInputStream(file1);
             InputStream is2 = Files.newInputStream(file2)) {

            int byte1, byte2;
            while ((byte1 = is1.read()) != -1 && (byte2 = is2.read()) != -1) {
                if (byte1 != byte2) {
                    return false;
                }
            }
            return true;
        }
    }
}