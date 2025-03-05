package V13;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileComparator {
    // (Мой ноут)При тестировании на скорость - До размера 512000L(500Кб) - 1048576L(10МБ) - compareFiles быстрее или равно compareLargeFiles,
    //значит В классе FileComparator - LARGE_FILE_THRESHOLD надо делить на 120 - 60. - НО ЭТО ДЛЯ 2 файлов
    //  Для моего компа подобрал - 30, но вырубается - перегружается
    // (Большой комп) - compareFiles быстрее чем compareLargeFiles до 307200(300Кб) на одинаковых ф
    //                                                                307200(300Кб) - 512000(500Кб) на разных
    // Значит LARGE_FILE_THRESHOLD(131203072) надо делить на 256 - 428
    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold()/428L; // Порог для больших файлов
    private static final int BLOCK_SIZE = getBlockSize(); // Размер блока для поблочного чтения больших файлов

    // Метод для получения порога для больших файлов
    private static long getLargeFileThreshold() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Количество доступных процессоров
        return maxMemory / (availableProcessors * 4L); // Возвращаем порог
    }

    // Метод для получения размера блока для поблочного чтения
    private static int getBlockSize() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Получаем доступную память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Получаем количество доступных процессоров
        int bufferSize = (int) (maxMemory / (availableProcessors * 8192)); // Вычисляем размер буфера
        int minBufferSize = 1024 * availableProcessors / 2; // Минимальный размер буфера
        return Math.max(bufferSize, minBufferSize); // Возвращаем максимальное значение
    }

    // Основной метод для сравнения файлов
    public static boolean areFilesEqual(File file1, File file2) throws IOException {
        long size = file1.length(); // Получаем размеры файлов
        if (size == 0) return true; // Если размер файлов равен нулю, файлы равны

        try {
            if (size < LARGE_FILE_THRESHOLD) {
                return compareFiles(file1, file2, size); // Сравнение для небольших файлов
            } else {
                return compareLargeFiles(file1, file2, size); // Сравнение для больших файлов
            }
        } catch (FileSystemException e) {
            System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
            return false; // Возвращаем false в случае ошибки доступа
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
            System.err.println("Поток был прерван при сравнении файлов: " + e.getMessage());
            return false;
        }
    }

    // Метод для побайтного сравнения содержимого двух файлов
    static boolean compareFiles(File file1, File file2, long fileSize) throws IOException {
        try (FileChannel channel1 = FileChannel.open(file1.toPath(), StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2.toPath(), StandardOpenOption.READ)) {
            return compareFileContents(channel1, channel2, fileSize); // Сравниваем содержимое
        }
    }

    // Метод для побайтного сравнения больших файлов
    static boolean compareLargeFiles(File file1, File file2, long fileSize) throws IOException, InterruptedException {
        try (FileChannel channel1 = FileChannel.open(file1.toPath(), StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2.toPath(), StandardOpenOption.READ)) {
            return compareLargeFileContents(channel1, channel2, fileSize); // Сравниваем содержимое
        }
    }

    // Вспомогательный метод для сравнения содержимого каналов двух файлов
    private static boolean compareFileContents(FileChannel channel1, FileChannel channel2, long size) throws IOException {
        long position = 0;
        while (position < size) { // Пока не достигнем конца файла
            long remaining = size - position; // Оставшееся количество байт
            long bytesToRead = Math.min(8192, remaining); // Читаем блоки данных
            ByteBuffer buffer1 = readFileBlock(channel1, position, bytesToRead); // Читаем из первого канала
            ByteBuffer buffer2 = readFileBlock(channel2, position, bytesToRead); // Читаем из второго канала

            if (!compareBuffers(buffer1, buffer2, bytesToRead)) {
                return false; // Возвращаем false при несовпадении
            }
            position += bytesToRead; // Переходим к следующему блоку
        }
        return true; // Файлы идентичны
    }

    // Вспомогательный метод для сравнения содержимого двух больших файлов
    private static boolean compareLargeFileContents(FileChannel channel1, FileChannel channel2, long size) throws InterruptedException {
        //ExecutorService executor = Executors.newFixedThreadPool((int) (Runtime.getRuntime().availableProcessors() * 1.25));  // Создаем ExecutorService с фиксированным пулом потоков
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Boolean>> futures = new ArrayList<>(); // Список задач
        long position = 0;

        try {
            while (position < size) { // Пока не достигнем конца файла
                long remaining = size - position; // Оставшееся количество байт
                long bytesToRead = Math.min(BLOCK_SIZE, remaining); // Размер блока для чтения
                TaskCompareFileContents task = new TaskCompareFileContents(channel1, channel2, position, bytesToRead); // Создаем задачу
                futures.add(executor.submit(task)); // Добавляем задачу в пул
                position += bytesToRead; // Переходим к следующему блоку
            }
            // Ждем завершения всех задач и проверяем результаты
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    return false; // Возвращаем false при несовпадении
                }
            }
        } catch (ExecutionException e) {
            System.err.println("Ошибка при выполнении задачи: " + e.getCause());
            return false; // Возвращаем false в случае ошибки выполнения
        } finally {
            executor.shutdown(); // Завершаем работу потоков
        }
        return true; // Возвращаем true, если все блоки совпадают
    }

    // Вспомогательный метод для чтения блока данных из канала
    static ByteBuffer readFileBlock(FileChannel channel, long position, long bytesToRead) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((int) bytesToRead); // Создаем буфер
        channel.read(buffer, position); // Читаем данные из канала
        buffer.flip(); // Переводим буфер в режим чтения
        return buffer;
    }

    // Вспомогательный метод для сравнения содержимого двух буферов
    static boolean compareBuffers(ByteBuffer buffer1, ByteBuffer buffer2, long bytesToRead) {
//        for (int i = 0; i < bytesToRead; i++) {
//            if (buffer1.get() != buffer2.get()) {
//                return false; // Возвращаем false при несовпадении
//            }
//        }
//        return true; // Буферы идентичны
        // Убедитесь, что оба буфера имеют достаточный размер
        if (buffer1.remaining() < bytesToRead || buffer2.remaining() < bytesToRead) {
            return false; // Один из буферов слишком мал
        }

        for (int i = 0; i < bytesToRead; i++) {
            if (buffer1.get() != buffer2.get()) {
                return false; // Возвращаем false при несовпадении
            }
        }
        return true; // Буферы идентичны
    }


    //---------------------------------------------------

    // Предварительное быстрое сравнение файлов по первым  байтам
    public static boolean quickCompareFiles(File file1, File file2) throws IOException {
        // Если оба файла имеют нулевой размер, они одинаковые
        if (file1.length() == 0) {
            return true;
        }

        return Files.mismatch(file1.toPath(), file2.toPath()) == -1; // -1 означает, что файлы идентичны
    }



    public static void main(String[] args) throws InterruptedException {

        long startTime = System.currentTimeMillis();

        try {
    // File file1 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (середина изменена).mp4");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/BiglargeFile.txt");
    // File file1 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат.zip");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат1.zip");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (пустой).zip");  // Разные файлы
    // File file1 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (копия)");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия)");
    // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена)");
           // File file1 = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)");
           // File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат");
     File file1 = new File("/home/alek7ey/Рабочий стол/largeFile (Копия).txt");
     File file2 = new File("/home/alek7ey/Рабочий стол/largeFile.txt");
    // System.out.println(areFilesEqual(file1, file2));

//           System.out.println(quickCompareFiles(file1, file2));
            System.out.println(compareLargeFiles(file1, file2, file1.length()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(" Размер блока - " + BLOCK_SIZE);
        System.out.println(" Порог для больших файлов - " + LARGE_FILE_THRESHOLD);
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