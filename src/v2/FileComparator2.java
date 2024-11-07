package v2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


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
    public boolean areFilesEqual(Path file1, Path file2) throws IOException {

        // Получаем размеры файлов. Достаточно одного файла, так как их размеры равны
        long size1 = Files.size(file1);

        // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
        if (size1 == 0) {
            return true;
        }

        // Используем метод для больших файлов
//        if (size1 > LARGE_FILE_THRESHOLD) {
//            // для тестов кол-ва больш файлов -------------------------
//            DuplicateFilePrinter2.largeFileSet.add(file1.getFileName());
//            DuplicateFilePrinter2.largeFileSet.add(file2.getFileName());
//            // для тестов-------------------------
//            return compareLargeFiles(file1, file2);
//            //return false;
//        }

        // Используем побайтное сравнение для небольших файлов
        return compareFilesByteByByte(file1, file2);
    }



    // Метод для побайтного сравнения содержимого двух НЕБОЛЬШИХ файлов
    public static boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            // Получаем размеры файлов
            long size = channel1.size();

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




    // Ускоренный метод для сравнения больших файлов с конца до начала
//    private static boolean compareLargeFiles(Path file1, Path file2) throws IOException {
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        // Открываем каналы для чтения файлов
//        FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
//        FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ);
//
//        // Получаем размер файлов
//        long size = channel1.size();
//        // Определяем размер блока для чтения
//        long blockSize = BLOCK_SIZE * 2L;
//        // Вычисляем количество блоков, необходимых для чтения всего файла
//        long numBlocks = (size + blockSize - 1) / blockSize;
//
//        // Создаем пул потоков для параллельного чтения блоков
//        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        // Используем CountDownLatch для ожидания завершения всех задач
//        CountDownLatch latch = new CountDownLatch((int) numBlocks);
//
//        // Параллельно проверяем каждый блок
//        for (int i = 0; i < numBlocks; i++) {
//            final int blockIndex = i;
//            // Подаем задачу на выполнение
//            executor.submit(() -> {
//                try {
//                    // Создаем буферы для чтения блоков из обоих файлов
//                    ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
//                    ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);
//
//                    // Читаем блоки из файлов в буферы, начиная с конца файла
//                    long position = size - (blockIndex + 1) * blockSize;
//                    if (position < 0) {
//                        position = 0;
//                    }
//
//                    // Читаем блоки из файлов в буферы
//                    channel1.read(buffer1, position);
//                    channel2.read(buffer2, position);
//
//                    // Переводим буферы в режим чтения
//                    buffer1.flip();
//                    buffer2.flip();
//
//                    // если буферы не равны, то завершаем все задачи и пул потоков, возвращаем false
//                    if (!buffer1.equals(buffer2)) {
//                        executor.shutdownNow();
//                        return false;
//                    }
//                } catch (FileSystemException e) {
//                    System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
//                    return false;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return false;
//                } finally {
//                    latch.countDown();
//                }
//                return true;
//            });
//        }
//
//        // закрываем пул потоков
//        executor.shutdown();
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } finally {
//            channel1.close();
//            channel2.close();
//        }
//        return true;
//    }


    // Ускоренный метод для сравнения больших файлов
    private static boolean compareLargeFiles(Path file1, Path file2) throws IOException {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // Открываем каналы для чтения файлов
        FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
        FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ);

        // Получаем размер файлов
        long size = channel1.size();
        // Определяем размер блока для чтения
        long blockSize = BLOCK_SIZE * 2L;
        // Вычисляем количество блоков, необходимых для чтения всего файла
        long numBlocks = (size + blockSize - 1) / blockSize;

        // Создаем пул потоков для параллельного чтения блоков
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // Используем CountDownLatch для ожидания завершения всех задач
        CountDownLatch latch = new CountDownLatch((int) numBlocks);
        // Флаг для прерывания выполнения задач
        AtomicBoolean mismatchFound = new AtomicBoolean(false);

        // Параллельно проверяем каждый блок
        for (int i = 0; i < numBlocks; i++) {
            final int blockIndex = i;
            // Подаем задачу на выполнение
            executor.submit(() -> {
                try {
                    if (mismatchFound.get()) {
                        return false;
                    }

                    // Создаем буферы для чтения блоков из обоих файлов
                    ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
                    ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);

                    // Читаем блоки из файлов в буферы, начиная с конца файла
                    long position = size - (blockIndex + 1) * blockSize;
                    if (position < 0) {
                        position = 0;
                    }

                    // Читаем блоки из файлов в буферы
                    channel1.read(buffer1, position);
                    channel2.read(buffer2, position);

                    // Переводим буферы в режим чтения
                    buffer1.flip();
                    buffer2.flip();

                    // если буферы не равны, то завершаем все задачи и пул потоков, возвращаем false
                    if (!buffer1.equals(buffer2)) {
                        mismatchFound.set(true);
                        executor.shutdownNow();
                        return false;
                    }
                } catch (FileSystemException e) {
                    System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    latch.countDown();
                }
                return true;
            });
        }

        // закрываем пул потоков
        executor.shutdown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            channel1.close();
            channel2.close();
        }
        return !mismatchFound.get();
    }


    public static void main(String[] args) {
        FileComparator2 fileComparator = new FileComparator2();

        System.out.println(" Размер блока для поблочного чтения (8 KB * количество процессоров): " + BLOCK_SIZE);
        //System.out.println(" Порог для маленьких файлов (10% от доступной памяти): " + SMALL_FILE_THRESHOLD);
        System.out.println(" Порог для больших файлов (30% от доступной памяти): " + LARGE_FILE_THRESHOLD);


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

    }
}
