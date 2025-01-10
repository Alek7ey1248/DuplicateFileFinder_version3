package V11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileComparator {

    //private static ExecutorService executor;
    //private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold();   // Порог для больших файлов
    private static final int BLOCK_SIZE = getBlockSize();       // Размер блока для поблочного чтения для больших файлов

    // Метод для получения порога для больших файлов
    private static long getLargeFileThreshold() {
        long maxMemory = Runtime.getRuntime().maxMemory();  // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors();    // Количество доступных процессоров
        return (int) (maxMemory / (availableProcessors * 2));
    }

    // Метод для получения размера блока для поблочного чтения
    private static int getBlockSize() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Получаем доступную память
        int availableProcessors = Runtime.getRuntime().availableProcessors();   // Получаем количество доступных процессоров
        int bufferSize = (int) (maxMemory / (availableProcessors * 8192));   // Вычисляем размер буфера для чтения
        int minBufferSize = 1024 * availableProcessors / 2 ;    // Минимальный размер буфера
        return Math.max(bufferSize, minBufferSize);
    }

    // Основной метод для сравнения файлов
    public static boolean areFilesEqual(Path file1, Path file2) throws IOException {
        //System.out.println("======================================");
        //return false;

        long size = Files.size(file1);  // Получаем размеры файлов

        if (size == 0) {   // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
            return true;
        }

        if (size < LARGE_FILE_THRESHOLD) {
            // Используем побайтное сравнение для небольших файлов
            try {
                return compareFiles(file1, file2);
            } catch (FileSystemException e) {
                // Логируем и пропускаем файлы, которые не удается открыть  - это на случай если нет прав доступа или типа того
                System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
                return false;
            }
        }

         //Используем ускоренный метод для больших файлов
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



    /* Основной метод для побайтного сравнения содержимого двух файлов
    * @param file1 - путь к первому файлу
    * @param file2 - путь ко второму файлу
    */
    public static boolean compareFiles(Path file1, Path file2) throws IOException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {
            long size = Files.size(file1); // Получаем размер файлов
            // Сравниваем содержимое файлов
            return compareFileContents(channel1, channel2, size);
        }
    }

    /* Метод для сравнения больших файлов
     * @param file1 - путь к первому файлу
     * @param file2 - путь ко второму файлу
     */
    private static boolean compareLargeFiles(Path file1, Path file2) throws IOException, InterruptedException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {
            long size = Files.size(file1); // Получаем размер файлов
            // Сравниваем содержимое файлов
            return compareLargeFileContents(channel1, channel2, size);
        }
    }


    /* Вспомогательный метод для сравнения содержимого каналов двух файлов */
    private static boolean compareFileContents(FileChannel channel1, FileChannel channel2, long size) throws IOException {
        long position = 0;

        while (position < size) { // Пока не достигнем конца файла
            long remaining = size - position;
            long bytesToRead = Math.min(8192, remaining);

            // Читаем блоки данных из каналов
            ByteBuffer buffer1 = readFileBlock(channel1, position, bytesToRead);
            ByteBuffer buffer2 = readFileBlock(channel2, position, bytesToRead);

            if (!compareBuffers(buffer1, buffer2, bytesToRead)) {
                return false; // Возвращаем false при несовпадении
            }

            position += bytesToRead; // Переходим к следующему блоку
        }

        return true; // Файлы идентичны
    }


    /* Вспомогательный метод для сравнения содержимого каналов двух больших файлов */
    private static boolean compareLargeFileContents(FileChannel channel1, FileChannel channel2, long size) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Boolean>> futures = new ArrayList<>();
        long position = 0;

        try {
            while (position < size) { // Пока не достигнем конца файла
                long remaining = size - position;
                long bytesToRead = Math.min(BLOCK_SIZE, remaining);
                // Создаем задачу для сравнения содержимого блока
                TaskCompareFileContents task = new TaskCompareFileContents(channel1, channel2, position, bytesToRead);
                try {
                    Future<Boolean> future = executor.submit(task);
                    futures.add(future);
                } catch (RejectedExecutionException e) {
                    //System.out.println("Задача была отклонена: " + e.getMessage());
                    return false; // Завершаем, если задача была отклонена
                }
                position += bytesToRead; // Переходим к следующему блоку
            }

            // Ждем завершения всех задач и проверяем результаты
            for (Future<Boolean> future : futures) {
                try {
                    if (!future.get()) {
                        return false; // Возвращаем false при несовпадении
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
                    System.out.println("Поток был прерван: " + e.getMessage());
                    return false;
                } catch (ExecutionException e) {
                    System.out.println("Ошибка при выполнении задачи: " + e.getCause());
                    return false; // Возвращаем false в случае ошибки выполнения
                }
            }
        } finally {
            executor.shutdown(); // Завершаем работу пула потоков
//            try {
//                //System.out.println("Ожидаем завершения потоков...");
//                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                    executor.shutdownNow(); // Принудительно завершаем, если не завершился
//                }
//            } catch (InterruptedException e) {
//                executor.shutdownNow(); // Принудительно завершаем в случае прерывания
//                Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
//            }
        }
        return true; // Возвращаем true, если все блоки совпадают
    }



    /* Вспомогательный метод для чтения блока данных из канала */
    static ByteBuffer readFileBlock(FileChannel channel, long position, long bytesToRead) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((int) bytesToRead); // Создаем буфер для чтения
        channel.read(buffer, position);    // Читаем данные из канала
        buffer.flip(); // Переводим буфер в режим чтения
        return buffer;
    }

    /* Вспомогательный метод для сравнения содержимого двух буферов */
    static boolean compareBuffers(ByteBuffer buffer1, ByteBuffer buffer2, long bytesToRead) {
        for (int i = 0; i < bytesToRead; i++) {
            if (buffer1.get(i) != buffer2.get(i)) {
                return false; // Возвращаем false при несовпадении
            }
        }
        return true; // Буферы идентичны
    }




    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        try {
            //Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (середина изменена).mp4");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/BiglargeFile.txt");


            //Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат.zip");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат1.zip");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (пустой).zip");  // Разные файлы

            Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (копия)");
            Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия)");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена)");

            //Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат");


            System.out.println(areFilesEqual(file1, file2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(" Размер блока - " + BLOCK_SIZE);
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения сравнения файлов --- " + duration + " ms       ");
    }
}

// Задача потокам для сравнения содержимого каждого блока
class TaskCompareFileContents implements Callable {
    private final FileChannel channel1;
    private final FileChannel channel2;
    private final long bytesToRead;  // Количество байт для чтения
    private final long position; // Позиция в файле, с которой начинается чтение блока

    public TaskCompareFileContents(FileChannel channel1, FileChannel channel2, long position, long bytesToRead) {
        this.channel1 = channel1;
        this.channel2 = channel2;
        this.position = position;
        this.bytesToRead = bytesToRead;
    }

    @Override
    public Boolean call() {
        try {
            //System.out.println("Запущен поток: " + Thread.currentThread());


            ByteBuffer buffer1 = FileComparator.readFileBlock(channel1, position, bytesToRead);
            ByteBuffer buffer2 = FileComparator.readFileBlock(channel2, position, bytesToRead);
            if (!FileComparator.compareBuffers(buffer1, buffer2, bytesToRead)) {
                //throw new FileSystemException("Files are not equal");
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}