import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;
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

        // Проверяем, существуют ли файлы
        if (!Files.exists(file1) || !Files.exists(file2)) {
            System.err.println("Один из файлов " + file1 + " или " + file2 + " не существует.");
            return false;
        }

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

        // Используем ускоренный метод для больших файлов
        if (size1 > LARGE_FILE_THRESHOLD) {
            return compareLargeFiles(file1, file2);
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

    // Ускоренный метод для сравнения больших файлов
    private boolean compareLargeFiles(Path file1, Path file2) throws IOException {
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            long size = channel1.size();
            if (size != channel2.size()) {
                return false;
            }

            long blockSize = BLOCK_SIZE * 2; // Увеличиваем размер блока для больших файлов
            long numBlocks = (size + blockSize - 1) / blockSize;

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            Future<Boolean>[] futures = new Future[(int) numBlocks];

            for (int i = 0; i < numBlocks; i++) {
                final int blockIndex = i;
                futures[i] = executor.submit(() -> {
                    try {
                        ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
                        ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);

                        channel1.read(buffer1, blockIndex * blockSize);
                        channel2.read(buffer2, blockIndex * blockSize);

                        buffer1.flip();
                        buffer2.flip();

                        return buffer1.equals(buffer2);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                });
            }

            for (Future<Boolean> future : futures) {
                try {
                    if (!future.get()) {
                        executor.shutdown();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    executor.shutdown();
                    return false;
                }
            }

            executor.shutdown();
            return true;
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



//public class FileComparator {
//
//    private static final long SMALL_FILE_THRESHOLD = getSmallFileThreshold();
//    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold();
//    private static final int BLOCK_SIZE = getBlockSize();
//
//    private static long getSmallFileThreshold() {
//        long availableMemory = Runtime.getRuntime().maxMemory();
//        return availableMemory / 10;
//    }
//
//    private static long getLargeFileThreshold() {
//        long availableMemory = Runtime.getRuntime().maxMemory();
//        return availableMemory / 3;
//    }
//
//    private static int getBlockSize() {
//        int availableProcessors = Runtime.getRuntime().availableProcessors();
//        return 8192 * availableProcessors;
//    }
//
//    public boolean areFilesEqual(Path file1, Path file2) throws IOException, ExecutionException, InterruptedException {
//
//        if (!Files.exists(file1) || !Files.exists(file2)) {
//            System.err.println("Один из файлов " + file1 + " или " + file2 + " не существует.");
//            return false;
//        }
//
//        long size1 = Files.size(file1);
//        long size2 = Files.size(file2);
//
//        if (size1 != size2) {
//            return false;
//        }
//
//        if (size1 == 0) {
//            return true;
//        }
//
//        if (size1 > LARGE_FILE_THRESHOLD) {
//            return compareLargeFiles(file1, file2);
//        }
//
//        return compareFilesByteByByte(file1, file2);
//    }
//
//    private boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
//        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
//             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {
//
//            long size = channel1.size();
//            if (size != channel2.size()) {
//                return false;
//            }
//
//            long blockSize = BLOCK_SIZE;
//            long numBlocks = (size + blockSize - 1) / blockSize;
//
//            return IntStream.range(0, (int) numBlocks).parallel().allMatch(i -> {
//                try {
//                    ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
//                    ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);
//
//                    channel1.read(buffer1, i * blockSize);
//                    channel2.read(buffer2, i * blockSize);
//
//                    buffer1.flip();
//                    buffer2.flip();
//
//                    return buffer1.equals(buffer2);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            });
//        }
//    }
//
//    private boolean compareLargeFiles(Path file1, Path file2) throws IOException {
//        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
//             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {
//
//            long size = channel1.size();
//            if (size != channel2.size()) {
//                return false;
//            }
//
//            long blockSize = BLOCK_SIZE; // Reduce block size to avoid excessive memory usage
//            long numBlocks = (size + blockSize - 1) / blockSize;
//
//            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//            Future<Boolean>[] futures = new Future[(int) numBlocks];
//
//            for (int i = 0; i < numBlocks; i++) {
//                final int blockIndex = i;
//                futures[i] = executor.submit(() -> {
//                    try {
//                        MappedByteBuffer buffer1 = channel1.map(FileChannel.MapMode.READ_ONLY, blockIndex * blockSize, Math.min(blockSize, size - blockIndex * blockSize));
//                        MappedByteBuffer buffer2 = channel2.map(FileChannel.MapMode.READ_ONLY, blockIndex * blockSize, Math.min(blockSize, size - blockIndex * blockSize));
//
//                        return buffer1.equals(buffer2);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        return false;
//                    }
//                });
//            }
//
//            for (Future<Boolean> future : futures) {
//                try {
//                    if (!future.get()) {
//                        executor.shutdown();
//                        return false;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    executor.shutdown();
//                    return false;
//                }
//            }
//
//            executor.shutdown();
//            return true;
//        }
//    }
//
//    public static void main(String[] args) {
//        FileComparator fileComparator = new FileComparator();
//        try {
//            Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)");
//            Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат");
//            System.out.println(fileComparator.areFilesEqual(file1, file2));
//        } catch (IOException | ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}