package v1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class FileComparator {

    // Порог для больших файлов (30% от доступной памяти)
    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold();
    // Размер блока для поблочного чтения (8 KB * количество процессоров)
    private static final int BLOCK_SIZE = getBlockSize();

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

        // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
        if (size1 == 0) {
            return true;
        }

        // Используем ускоренный метод для больших файлов
        if (size1 > LARGE_FILE_THRESHOLD) {
            try {
            return compareLargeFiles(file1, file2);
            } catch (FileSystemException e) {
                // Логируем и пропускаем файлы, которые не удается открыть - это на случай если нет прав доступа или типа того
                System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
                return false;
            }
        }

        // Используем побайтное сравнение для всех файлов
        try {
            return compareFilesByteByByte(file1, file2);
        } catch (FileSystemException e) {
            // Логируем и пропускаем файлы, которые не удается открыть  - это на случай если нет прав доступа или типа того
            System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
            return false;
        }
    }

    // Метод для побайтного сравнения содержимого двух файлов
//    private boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
//        // Открываем каналы для чтения файлов
//        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
//             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {
//
//            // Получаем размер файлов
//            long size = channel1.size();
//
//            // Определяем размер блока для чтения
//            long blockSize = BLOCK_SIZE;
//            // Вычисляем количество блоков, необходимых для чтения всего файла
//            long numBlocks = (size + blockSize - 1) / blockSize;
//
//            // Параллельно проверяем каждый блок
//            return IntStream.range(0, (int) numBlocks).parallel().allMatch(i -> {
//                try {
//                    // Создаем буферы для чтения блоков из обоих файлов
//                    ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
//                    ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);
//
//                    // Читаем блоки из файлов в буферы
//                    channel1.read(buffer1, i * blockSize);
//                    channel2.read(buffer2, i * blockSize);
//
//                    // Переводим буферы в режим чтения
//                    buffer1.flip();
//                    buffer2.flip();
//
//                    // Сравниваем содержимое буферов
//                    return buffer1.equals(buffer2);
//                } catch (IOException e) {
//                    // В случае ошибки выводим стек ошибки и возвращаем false
//                    e.printStackTrace();
//                    return false;
//                }
//            });
//        }
//    }

    // Метод для побайтного сравнения содержимого двух файлов
    // лучше чем предыдущий метод по скорости
    private boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            // Получаем размер файлов
            long size = channel1.size();

            // Определяем размер блока для чтения
            long blockSize = BLOCK_SIZE;

            // Сравниваем файлы блок за блоком
            for (long position = 0; position < size; position += blockSize) {
                long remaining = size - position;
                long bytesToRead = Math.min(blockSize, remaining);

                // Создаем буферы для чтения блоков из обоих файлов
                ByteBuffer buffer1 = ByteBuffer.allocate((int) bytesToRead);
                ByteBuffer buffer2 = ByteBuffer.allocate((int) bytesToRead);

                // Читаем блоки из файлов в буферы
                channel1.read(buffer1, position);
                channel2.read(buffer2, position);

                // Переводим буферы в режим чтения
                buffer1.flip();
                buffer2.flip();

                // Сравниваем содержимое буферов
                for (int i = 0; i < bytesToRead; i++) {
                    if (buffer1.get() != buffer2.get()) {
                        return false; // Возвращаем false при несовпадении
                    }
                }
            }

            // Файлы идентичны
            return true;
        }
    }



    // Ускоренный метод для сравнения больших файлов
    private boolean compareLargeFiles(Path file1, Path file2) throws IOException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            // Получаем размер файла
            long size = channel1.size();

            // Увеличиваем размер блока для больших файлов
            long blockSize = BLOCK_SIZE * 2L;
            // Вычисляем количество блоков, необходимых для чтения всего файла
            long numBlocks = (size + blockSize - 1) / blockSize;

            // Создаем пул потоков с количеством потоков, равным количеству доступных процессоров
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            // Массив для хранения результатов выполнения задач
            Future<Boolean>[] futures = new Future[(int) numBlocks];

            // Для каждого блока создаем задачу для сравнения блоков файлов
            for (int i = 0; i < numBlocks; i++) {
                final int blockIndex = i;
                futures[i] = executor.submit(() -> {
                    try {
                        // Создаем буферы для чтения блоков из обоих файлов
                        ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
                        ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);

                        // Читаем блоки из файлов в буферы
                        channel1.read(buffer1, blockIndex * blockSize);
                        channel2.read(buffer2, blockIndex * blockSize);

                        // Переводим буферы в режим чтения
                        buffer1.flip();
                        buffer2.flip();

                        // Сравниваем содержимое буферов
                        return buffer1.equals(buffer2);
                    } catch (IOException e) {
                        // В случае ошибки выводим стек ошибки и возвращаем false
                        e.printStackTrace();
                        return false;
                    }
                });
            }

            // Проверяем результаты выполнения всех задач
            for (Future<Boolean> future : futures) {
                try {
                    // Если хотя бы одна задача вернула false, файлы не равны
                    if (!future.get()) {
                        executor.shutdown();
                        return false;
                    }
                } catch (Exception e) {
                    // В случае ошибки выводим стек ошибки и возвращаем false
                    e.printStackTrace();
                    executor.shutdown();
                    return false;
                }
            }

            // Завершаем работу пула потоков
            executor.shutdown();
            // Если все задачи вернули true, файлы равны
            return true;
        }
    }

    public static void main(String[] args) {
        FileComparator fileComparator = new FileComparator();

        Path file1 = Path.of("/home/alek7ey/Рабочий стол/filmsTestDuplicateFileFinder/filmCopy/videoplayback (копия).mp4");;
        Path file2 = Path.of("/home/alek7ey/Рабочий стол/filmsTestDuplicateFileFinder/videoplayback.mp4");

        long startTime = System.currentTimeMillis();

        try {
            System.out.println(fileComparator.compareLargeFiles(file1, file2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = (long) ((endTime - startTime) / 1000.0);
        System.out.println("Время выполнения areFilesEqual на сравнении файлов " + file1.getFileName() + " и " + file2.getFileName() + " --- " + duration + " секунд       " + (long) duration * 1000.0 + " милисекунд");

        System.out.println(" Блоки для чтения файлов: " + BLOCK_SIZE);
    }
}
