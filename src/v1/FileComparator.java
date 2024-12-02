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
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("availableProcessors = " + availableProcessors);
        return 8192 * availableProcessors; // 8 KB * количество процессоров
    }

    // Основной метод для сравнения файлов
    public static boolean areFilesEqual(Path file1, Path file2) throws IOException {

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
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
    private static boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            // Получаем размер файлов
            long size = channel1.size();

            // Определяем размер блока для чтения
            long blockSize = BLOCK_SIZE;

            // Сравниваем файлы блок за блоком
            for (long position = 0; position < size; position += blockSize) {
                // Вычисляем количество байт, которые нужно прочитать
                long remaining = size - position;
                // Определяем количество байт, которые нужно прочитать в текущем блоке
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


    // Метод для сравнения больших файлов
    private static boolean compareLargeFiles(Path file1, Path file2) throws IOException, InterruptedException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            //System.out.println("LARGE_FILE_THRESHOLD = " + LARGE_FILE_THRESHOLD);
            // Получаем размер файла
            long size = channel1.size();
            //System.out.println("size = " + size);

            // Увеличиваем размер блока для больших файлов
            long blockSize = BLOCK_SIZE * 2L;
            //System.out.println("blockSize = " + blockSize);
            // Вычисляем количество блоков, необходимых для чтения всего файла
            long numBlocks = (size + blockSize - 1) / blockSize; // Округляем вверх - Этот код гарантирует, что все байты файла будут проверены, даже если размер файла не кратен размеру блока.
            //System.out.println("numBlocks = " + numBlocks);

            // Количество доступных процессоров
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            //System.out.println("availableProcessors = " + availableProcessors);

            // Массив для хранения результатов выполнения задач
            boolean[] results = new boolean[availableProcessors];
            Thread[] threads = new Thread[availableProcessors];

            // Переменная для отслеживания текущего блока
            long currentBlock = 0;

            // Сравниваем файлы блок за блоком
            while (currentBlock < numBlocks) {
                // Создаем потоки для проверки блоков файлов
                for (int i = 0; i < availableProcessors && currentBlock < numBlocks; i++, currentBlock++) {
                    final long blockIndex = currentBlock;
                    final int threadIndex = i;
                    threads[threadIndex] = new Thread(() -> {
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
                            results[threadIndex] = buffer1.equals(buffer2);
                        } catch (IOException e) {
                            // В случае ошибки выводим стек ошибки и устанавливаем результат в false
                            e.printStackTrace();
                            results[threadIndex] = false;
                        }
                    });
                    threads[threadIndex].start();
                }

                // Ждем завершения всех потоков
                for (Thread thread : threads) {
                    if (thread != null) {
                        thread.join();
                    }
                }

                // Проверяем результаты выполнения всех задач
                for (boolean result : results) {
                    if (!result) {
                        return false;
                    }
                }
            }

            // Если все задачи вернули true, файлы равны
            return true;
        }
    }



    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        try {
            //Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4");
            Path file1 = Path.of("/home/alek7ey/.cache/JetBrains/IdeaIC2024.3/caches/content.dat");
            Path file2 = Path.of("/home/alek7ey/.cache/JetBrains/IdeaIC2024.2/caches/content.dat");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4");

//            Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат");
//            Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/largeFile.txt");
            System.out.println(areFilesEqual(file1, file2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения сравнения файлов --- " + duration + " ms       ");
    }
}