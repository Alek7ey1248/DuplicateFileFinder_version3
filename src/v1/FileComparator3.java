package v1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;

public class FileComparator3 {

    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold();
    private static final int BLOCK_SIZE = getBlockSize();

    // Определяем порог для больших файлов на основе доступной памяти
    private static long getLargeFileThreshold() {
        long availableMemory = Runtime.getRuntime().maxMemory();
        return availableMemory / 4; // Уменьшаем порог до 1/4 доступной памяти
    }

    // Определяем размер блока на основе количества доступных процессоров
    private static int getBlockSize() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return 4096 * availableProcessors; // Уменьшаем размер блока
    }

    // Основной метод для сравнения файлов
    public static boolean areFilesEqual(Path file1, Path file2) throws IOException {
        long size1 = Files.size(file1);

        if (size1 == 0) {
            return true; // Оба файла пусты
        }

        if (size1 > LARGE_FILE_THRESHOLD) {
            try {
                return compareLargeFiles(file1, file2);
            } catch (FileSystemException e) {
                System.err.println("Cannot open file: " + e.getFile());
                return false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return compareFilesByteByByte(file1, file2);
        } catch (FileSystemException e) {
            System.err.println("Cannot open file: " + e.getFile());
            return false;
        }
    }

    // Метод для побайтного сравнения файлов
    private static boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            long size = channel1.size();
            long blockSize = BLOCK_SIZE;

            for (long position = 0; position < size; position += blockSize) {
                long remaining = size - position;
                long bytesToRead = Math.min(blockSize, remaining);

                ByteBuffer buffer1 = ByteBuffer.allocateDirect((int) bytesToRead);
                ByteBuffer buffer2 = ByteBuffer.allocateDirect((int) bytesToRead);

                channel1.read(buffer1, position);
                channel2.read(buffer2, position);

                buffer1.flip();
                buffer2.flip();

                for (int i = 0; i < bytesToRead; i++) {
                    if (buffer1.get() != buffer2.get()) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    // Метод для сравнения больших файлов
    private static boolean compareLargeFiles(Path file1, Path file2) throws IOException, InterruptedException {
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            long size = channel1.size();
            long blockSize = BLOCK_SIZE;
            long numBlocks = (size + blockSize - 1) / blockSize;

            int availableProcessors = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(availableProcessors);
            CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);

            for (long blockIndex = 0; blockIndex < numBlocks; blockIndex++) {
                final long index = blockIndex;
                completionService.submit(() -> {
                    ByteBuffer buffer1 = ByteBuffer.allocateDirect((int) blockSize);
                    ByteBuffer buffer2 = ByteBuffer.allocateDirect((int) blockSize);

                    channel1.read(buffer1, index * blockSize);
                    channel2.read(buffer2, index * blockSize);

                    buffer1.flip();
                    buffer2.flip();

                    return buffer1.equals(buffer2);
                });
            }

            for (long i = 0; i < numBlocks; i++) {
                if (!completionService.take().get()) {
                    executor.shutdown();
                    return false;
                }
            }

            executor.shutdown();
            return true;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try {
            Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4");
            Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4");
            System.out.println(areFilesEqual(file1, file2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Comparison time: " + duration + " ms");
    }
}
