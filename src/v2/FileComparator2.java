package v2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class FileComparator2 {

    // Порог для больших файлов (30% от доступной памяти)
    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold();

    // Размер блока для поблочного чтения (8 KB * количество процессоров)
    private static final int BLOCK_SIZE = getBlockSize();


    // Метод для получения порога для больших файлов
    private static long getLargeFileThreshold() {
        //  Получаем максимальное количество памяти, доступное для JVM
        long availableMemory = Runtime.getRuntime().maxMemory();
        return availableMemory / 3; // возвращаем 30% от доступной памяти
    }

    // Метод для получения размера блока для поблочного чтения
    private static int getBlockSize() {
        // количество доступных процессоров (ядер) на машине, на которой выполняется JVM
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return 8192 * availableProcessors; // Возвращает размер блока для поблочного чтения, который равен 8 KB умноженному на количество процессоров. Это значение используется для определения размера буфера при чтении файлов блоками.
    }

    // Основной метод для сравнения файлов
    // Метод для побайтного сравнения содержимого двух  файлов
    public static boolean areFilesEqual(Path file1, Path file2) throws IOException {

        // ----------------------------------------------------
        // для больших файлов используем ускоренный метод
        if (Files.size(file1) > LARGE_FILE_THRESHOLD) {
            try {
                return areLargeFilesEqual(file1, file2);
            } catch (FileSystemException e) {
                // Логируем и пропускаем файлы, которые не удается открыть - это на случай если нет прав доступа или типа того
                System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
                return false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // ----------------------------------------------------

        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            // Получаем размеры файлов
            long size = channel1.size();

            // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
            if (size == 0) {
                return true;
            }

            // Сравниваем файлы поблочно
            long position = 0;
            long blockSize = BLOCK_SIZE; // Размер блока для чтения

            while (position < size) {
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

                position += bytesToRead;
            }

            // Файлы идентичны
            return true;
        }
    }



    // Ускоренный метод для сравнения больших файлов
    private static boolean areLargeFilesEqual(Path file1, Path file2) throws IOException, InterruptedException {
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
                        executor.awaitTermination(60, TimeUnit.SECONDS);
                        return false;
                    }
                } catch (Exception e) {
                    // В случае ошибки выводим стек ошибки и возвращаем false
                    e.printStackTrace();
                    executor.shutdown();
                    executor.awaitTermination(60, TimeUnit.SECONDS);
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

        System.out.println(" Размер блока для поблочного чтения (8 KB * количество процессоров): " + BLOCK_SIZE);
        //System.out.println(" Порог для маленьких файлов (10% от доступной памяти): " + SMALL_FILE_THRESHOLD);
        //System.out.println(" Порог для больших файлов (30% от доступной памяти): " + LARGE_FILE_THRESHOLD);


        //Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4");
        Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/BiglargeFile.txt");
        //Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .zip");
        Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4");


        long startTime = System.currentTimeMillis();

        try {
            System.out.println(areFilesEqual(file1, file2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = (long) ((endTime - startTime) / 1000.0);
        System.out.println("Время выполнения areFilesEqual на сравнении файлов " + file1.getFileName() + " и " + file2.getFileName() + " --- " + duration + " секунд       " + (long) duration * 1000.0 + " милисекунд");

    }
}
