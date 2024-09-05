import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileComparator {

    // Порог для маленьких файлов (10% от доступной памяти)
    private static final long SMALL_FILE_THRESHOLD = getSmallFileThreshold();
    // Порог для больших файло�� (30% от доступной памяти)
    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold();
    // Размер блока для поблочного чтения (8 KB * количество процессоров)
    private static final int BLOCK_SIZE = getBlockSize();

    // Метод для получения порога для маленьких файлов
    private static long getSmallFileThreshold() {
        long availableMemory = Runtime.getRuntime().maxMemory();
        return availableMemory / 10; // 10% от доступной памяти
    }

    // Метод для получения порога для больших файлов
    private static long getLargeFileThreshold() {
        long availableMemory = Runtime.getRuntime().maxMemory();
        return availableMemory / 3; // 30% от доступной памяти
    }

    // Метод для получения размера блока для поблочного чтения
    private static int getBlockSize() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return 8192 * availableProcessors; // 8 KB * количество процессоров
    }

    // Основной метод для сравнения файлов
    public boolean areFilesEqual(Path file1, Path file2) throws IOException {
        // Получаем размеры файлов
        long size1 = Files.size(file1);
        long size2 = Files.size(file2);

        // Если размеры файлов различаются, файлы не равны
        if (size1 != size2) {
            return false;
        }

        // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
        if (size1 == 0) {
            return true;
        }

        // Если файлы маленькие, используем побайтное сравнение
        if (size1 <= SMALL_FILE_THRESHOLD) {
            return compareFilesByteByByte(file1, file2);
        }
        // Если файлы большие, используем поблочное чтение и сравнение
        else if (size1 <= LARGE_FILE_THRESHOLD) {
            return compareFilesByBlocks(file1, file2);
        }
        // Если файлы очень большие, используем хеширование
        else {
            return compareFilesUsingHash(file1, file2);
        }
    }

    // Метод для побайтного сравнения содержимого двух файлов
    private boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
        try (InputStream is1 = Files.newInputStream(file1);
             InputStream is2 = Files.newInputStream(file2)) {

            int byte1, byte2;
            // Читаем и сравниваем байты до конца файла
            while ((byte1 = is1.read()) != -1 && (byte2 = is2.read()) != -1) {
                if (byte1 != byte2) {
                    return false;
                }
            }
            return true;
        }
    }

    // Метод для поблочного чтения и сравнения содержимого двух файлов
    private boolean compareFilesByBlocks(Path file1, Path file2) throws IOException {
        try (InputStream is1 = Files.newInputStream(file1);
             InputStream is2 = Files.newInputStream(file2)) {

            byte[] buffer1 = new byte[BLOCK_SIZE];
            byte[] buffer2 = new byte[BLOCK_SIZE];

            int bytesRead1, bytesRead2;
            // Читаем и сравниваем блоки до конца файла
            while ((bytesRead1 = is1.read(buffer1)) != -1 && (bytesRead2 = is2.read(buffer2)) != -1) {
                if (bytesRead1 != bytesRead2 || !java.util.Arrays.equals(buffer1, buffer2)) {
                    return false;
                }
            }
            return true;
        }
    }

    // Метод для сравнения файлов с использованием хеширования
    private boolean compareFilesUsingHash(Path file1, Path file2) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // Вычисляем хеши файлов в параллельных потоках
            Future<String> hash1 = executor.submit(() -> computeHash(file1));
            Future<String> hash2 = executor.submit(() -> computeHash(file2));

            // Сравниваем хеши
            return hash1.get().equals(hash2.get());
        } catch (Exception e) {
            throw new IOException("Error computing file hashes", e);
        } finally {
            executor.shutdown();
        }
    }

    // Метод для вычисления хеша файла
    public String computeHash(Path file) throws IOException {
        if (Files.isDirectory(file)) {
            throw new IOException("Это каталог: " + file);
        }

        try (InputStream is = Files.newInputStream(file)) {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;
            // Читаем файл и обновляем хеш
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hash = digest.digest();
            // Возвраща��м хеш в виде строки Base64
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 algorithm not found", e);
        }
    }



    public static void main(String[] args) {
        FileComparator fileComparator = new FileComparator();
        try {
            Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)");
            Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат");
            System.out.println(fileComparator.areFilesEqual(file1, file2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}