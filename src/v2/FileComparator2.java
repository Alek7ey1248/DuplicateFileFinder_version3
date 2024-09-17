package v2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class FileComparator2 {

//    CheckValid2 checkValid;
//
//    public FileComparator2() {
//        checkValid = new CheckValid2();
//    }

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
//        if (!Files.exists(file1) || !Files.exists(file2)) {
//            System.err.println("Один из файлов " + file1 + " или " + file2 + " не существует.");
//            return false;
//        }

        // Проверяем еще раз, являются ли файлы валидными
//        if (!checkValid.isValidFile(file1.toFile()) || !checkValid.isValidFile(file2.toFile())) {
//            System.err.println("Один из файлов " + file1 + " или " + file2 + " не является файлом.");
//            return false;
//        }

        // Получаем размеры файлов
        long size1 = Files.size(file1);
//        long size2 = Files.size(file2);
//
//        // Если размеры файлов различаются, файлы не равны
//        if (size1 != size2) {
//            return false;
//        }

        // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
        if (size1 == 0) {
            return true;
        }

        // Проверяем сначала средние байты файлов - предварительная проверка
        // Если размер файла меньше 1024 байт, то не делаем предварительную проверку
        if (size1 > 1024) {
            try {
                if (!compareMiddleBytes(file1, file2)) {
                    return false;
                }
            } catch (FileSystemException e) {
                // Логируем и пропускаем файлы, которые не удается открыть - это на случай если нет прав доступа или типа того
                System.err.println("Не удалось открыть файл. Скорее всего нет прав доступа: " + e.getFile());
                return false;
            }
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

    // Метод для побайтного сравнения содержимого двух файлов с конца до начала
    private boolean compareFilesByteByByte(Path file1, Path file2) throws IOException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            // Получаем размер файлов
            long size = channel1.size();

            // Определяем размер блока для чтения
            long blockSize = BLOCK_SIZE;
            // Вычисляем количество блоков, необходимых для чтения всего файла
            long numBlocks = (size + blockSize - 1) / blockSize;

            // Параллельно проверяем каждый блок
            return IntStream.range(0, (int) numBlocks).parallel().allMatch(i -> {
                try {
                    // Создаем буферы для чтения блоков из обоих файлов
                    ByteBuffer buffer1 = ByteBuffer.allocate((int) blockSize);
                    ByteBuffer buffer2 = ByteBuffer.allocate((int) blockSize);

                    // Читаем блоки из файлов в буферы, начиная с конца файла
                    long position = size - (i + 1) * blockSize; // !!! Изменено для чтения с конца файла
                    if (position < 0) {
                        position = 0;
                    }

                    channel1.read(buffer1, position); // !!! Изменено для чтения с конца файла
                    channel2.read(buffer2, position); // !!! Изменено для чтения с конца файла

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
    }

    // Ускоренный метод для сравнения больших файлов с конца до начала
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

                        // Читаем блоки из файлов в буферы, начиная с конца файла
                        long position = size - (blockIndex + 1) * blockSize; // !!! Изменено для чтения с конца файла
                        if (position < 0) {
                            position = 0;
                        }

                        channel1.read(buffer1, position); // !!! Изменено для чтения с конца файла
                        channel2.read(buffer2, position); // !!! Изменено для чтения с конца файла

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

    // - не используется !!!
    // Метод для сравнения первых и последних байтов файлов
    private boolean compareFirstAndLastBytes(Path file1, Path file2) throws IOException {
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            ByteBuffer buffer1 = ByteBuffer.allocate(1024);
            ByteBuffer buffer2 = ByteBuffer.allocate(1024);

            channel1.read(buffer1);
            channel2.read(buffer2);

            if (!buffer1.equals(buffer2)) {
                return false;
            }

            buffer1.clear();
            buffer2.clear();

            channel1.position(channel1.size() - 1024);
            channel2.position(channel2.size() - 1024);

            channel1.read(buffer1);
            channel2.read(buffer2);

            return buffer1.equals(buffer2);
        }
    }


    // Вспомогательный метод для предваительной проверки файлов на равенство. Сравнивает блок в середине файла
    private boolean compareMiddleBytes(Path file1, Path file2) throws IOException {
        // Открываем каналы для чтения файлов
        try (FileChannel channel1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel channel2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            // Создаем буферы для чтения первых 1024 байт из каждого файла
            ByteBuffer buffer1 = ByteBuffer.allocate(1024);
            ByteBuffer buffer2 = ByteBuffer.allocate(1024);

            // Читаем первые 1024 байта из каждого файла в буферы
            channel1.read(buffer1);
            channel2.read(buffer2);

            // Сравниваем содержимое буферов
            if (!buffer1.equals(buffer2)) {
                return false; // Если первые 1024 байта не равны, файлы не идентичны
            }

            // Очищаем буферы для повторного использования
            buffer1.clear();
            buffer2.clear();

            // Устанавливаем позицию каналов на последние 1024 байта файлов
            channel1.position(channel1.size() - 1024);
            channel2.position(channel2.size() - 1024);

            // Читаем последние 1024 байта из каждого файла в буферы
            channel1.read(buffer1);
            channel2.read(buffer2);

            // Сравниваем содержимое буферов
            return buffer1.equals(buffer2); // Возвращаем результат сравнения последних 1024 байт
        }
    }




    public static void main(String[] args) {
        FileComparator2 fileComparator = new FileComparator2();

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
