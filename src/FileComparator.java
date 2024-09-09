import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.IntStream;

public class FileComparator {

    // Порог для маленьких файлов (10% от доступной памяти)
    private static final long SMALL_FILE_THRESHOLD = getSmallFileThreshold();
    // Порог для больших файлов (30% от доступной памяти)
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

        // Используем побайтное сравнение для всех файлов
        return compareFilesByteByByte(file1, file2);
    }

    // Метод для побайтного сравнения содержимого двух файлов
    private boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            long size = channel1.size();
            if (size != channel2.size()) {
                return false;
            }

            long blockSize = BLOCK_SIZE;
            long numBlocks = (size + blockSize - 1) / blockSize;

            return IntStream.range(0, (int) numBlocks).parallel().allMatch(i -> {
                try {
                    ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
                    ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);

                    channel1.read(buffer1, i * blockSize);
                    channel2.read(buffer2, i * blockSize);

                    buffer1.flip();
                    buffer2.flip();

                    return buffer1.equals(buffer2);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            });
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