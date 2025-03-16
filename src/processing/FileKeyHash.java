package processing;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileKeyHash implements Comparable<FileKeyHash> {
    private final long size;
    public long getSize() {
        return size;
    }
    //private final String partialContentHash;   // Хеш первых 1024 байт файла
    private final byte[] fullContentHash;      // Хеш всего файла
    // (На больш компе) - calculateHashLargeFile становиться быстрее calculateHashSmallFile на 512000(500Кб)
    // Значит порог для больших файлов - LARGE_FILE_SIZE = 524812288 надо делить на 1025
    private static final int LARGE_FILE_SIZE = getOptimalLargeFileSize()/1025; // порог для больших файлов - после тестирований скорее всего так и оставлю
    private static final int BUFFER_SIZE = getOptimalBufferSize();  // 8192 - оптимальный размер буфера на основе доступной памяти используемый в java; // Оптимальный размер буфера на основе доступной памяти
    private static final int NUM_BLOCKS = (int) (Runtime.getRuntime().availableProcessors() * 1.25); // Получаем количество блоков одновременно работающих = кол-во доступных процессоров

    // конструктор по умолчанию
    public FileKeyHash() {
        this.size = 0;
        this.fullContentHash = new byte[0];
    }

    // Конструктор для создания ключа файла на основе размера и части содержимого
    public FileKeyHash(File file) throws IOException, NoSuchAlgorithmException {
        this.size = file.length();
        this.fullContentHash = calculateHash(file);
    }

    /* Метод для расчета хеша файла
     * @param file - файл, для которого нужно рассчитать хеш
     */
    public static byte[] calculateHash(File file) {
        // если файл пустой, возвращаем -1
        if (file.length() == 0) {
            //return "-1";
            return new byte[0];
        }

        if (file.length() < LARGE_FILE_SIZE) {
            // если файл меньше порога, используем bufferSize для не больших файлов
            return calculateHashSmallFile(file);
        }
        // если файл больше порога, используем bufferSize для больших файлов
        return calculateHashLargeFile(file);
    }


    // метод для вычисления хеша файла
    static byte[] calculateHashSmallFile(File file) {
        //System.out.println("вычисление хеша  - " + file.getAbsolutePath());

        try {
            //MessageDigest digest = MessageDigest.getInstance("SHA-256");
            MessageDigest digest = createMessageDigest(); // Создаем объект MessageDigest для финального хеширования
            FileInputStream fis = new FileInputStream(file.getAbsoluteFile());
            byte[] byteArray = new byte[8192];
            int bytesCount;

            // Чтение файла и обновление хеша
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();

            // возвращаем хеш в виде байтового массива
            return digest.digest();
        } catch (IOException e) {
            System.out.println("Ошибка при вычислении хеша файла " + file + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    // Метод для вычисления хеша болшьшого файла
    /* Метод для расчета хеша большого файла
     * @param file - файл, для которого нужно рассчитать хеш
     */
    // Метод для вычисления хеша большого файла
    static byte[] calculateHashLargeFile(File file) {
        //System.out.println("вычисление хеша LargeFile - " + file);
        try {
            return updateDigestWithLargeFileContent(file); // Обновляем хеш содержимым файла
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла " + file + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    // Вспомогательный метод для обновления хеша содержимым большого файла
    private static byte[] updateDigestWithLargeFileContent(File file) throws IOException {

        MessageDigest finalDigest = createMessageDigest(); // Создаем объект MessageDigest для финального хеширования
        long fileSize = file.length(); // Получаем размер файла
        long partSize = (long) Math.ceil((double) fileSize / NUM_BLOCKS); // Размер каждой части файла

        //ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Создаем пул потоков
        ExecutorService executor = Executors.newFixedThreadPool(NUM_BLOCKS); // Создаем пул потоков
        List<CompletableFuture<MessageDigest>> futures = new ArrayList<>(); // Список для хранения CompletableFuture

        for (int i = 0; i < NUM_BLOCKS; i++) {
            long start = i * partSize; // Начало части
            long end = Math.min(start + partSize, fileSize); // Конец части
            futures.add(CompletableFuture.supplyAsync(() -> {
                //System.out.println("-------------------------start = " + start + "  end = " + end);
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {  // Открываем файл для чтения
                    MessageDigest digest = createMessageDigest(); // Создаем объект MessageDigest для хеширования
                    byte[] buffer = new byte[BUFFER_SIZE]; // Буфер для чтения файла
                    raf.seek(start); // Переходим к началу части файла
                    long bytesReadTotal = 0; // Общее количество прочитанных байт
                    int bytesRead; // Количество байт, прочитанных из файла

                    // Пока есть байты в файле, читаем их и обновляем хеш
                    while (bytesReadTotal < (end - start) && (bytesRead = raf.read(buffer, 0, (int) Math.min(BUFFER_SIZE, end - start - bytesReadTotal))) != -1) {
                        digest.update(buffer, 0, bytesRead); // Обновляем хеш
                        bytesReadTotal += bytesRead; // Обновляем общее количество прочитанных байт
                    }

                    return digest; // Возвращаем объект MessageDigest
                } catch (IOException e) {
                    System.err.println("Ошибка при чтении файла " + file.getAbsolutePath() + ": " + e.getMessage());
                    throw new UncheckedIOException(e);
                }
            }, executor));
        }

        // Ожидаем завершения всех задач и обновляем финальный хеш
        for (CompletableFuture<MessageDigest> future : futures) {
            try {
                MessageDigest partDigest = future.join(); // Получаем результат задачи
                //synchronized (finalDigest) {
                    finalDigest.update(partDigest.digest()); // Обновляем финальный хеш
                //}
            } catch (CompletionException e) {
                System.err.println("Ошибка при обновлении хеша: " + e.getMessage());
                e.printStackTrace(); // Выводим стек вызовов для диагностики
            }
        }

        executor.shutdown();
        // Получение финального хеша
        return finalDigest.digest();
    }


    // Вспомогательный метод для создания объекта MessageDigest
    private static MessageDigest createMessageDigest() {
        try {
//            return MessageDigest.getInstance("SHA-256"); // Создаем объект MessageDigest для SHA-256
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Алгоритм хеширования SHA-256 не найден");
            throw new RuntimeException(e);
        }
    }


    // Переопределение методов equals и hashCode для корректного сравнения объектов FileKey
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKeyHash fileKeyHash = (FileKeyHash) o;
        return size == fileKeyHash.size && Arrays.equals(fullContentHash, fileKeyHash.fullContentHash);
    }

    // Переопределение метода hashCode для корректного сравнения объектов FileKey
    @Override
    public int hashCode() {
        return Objects.hash(size) ^ Arrays.hashCode(fullContentHash); // Вычисляем хеш объекта FileKey на основе размера и хеша содержимого
    }
    
    // Переопределение метода compareTo для корректного сравнения объектов FileKey
    @Override
    public int compareTo(FileKeyHash other) {
        int sizeComparison = Long.compare(this.size, other.size);
        if (sizeComparison != 0) {
            return sizeComparison;
        }
        return Arrays.compare(this.fullContentHash, other.fullContentHash);
    }


    /* Метод для определения оптимального размера большого файла для многопоточного хеширования
     * Оптимальный размер файла - это 1/4 от максимальной п��мяти, деленной на количество процессоров
     */
    private static int getOptimalLargeFileSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // Устанавливаем оптимальный размер файла как 1/4 от максимальной памяти, деленной на количество процессоров
        return (int) (maxMemory / (availableProcessors));
    }



    /* Метод для определения оптимального размера буфера на основе доступной памяти
     * Метод для определения оптимального размера буфера на основе доступной памяти и количества процессоров
     * Оптимальный размер буфера - это 1/8 от максимальной памяти, деленной на количество процессоров
     */
    private static int getOptimalBufferSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        long bsLong = maxMemory / (availableProcessors * 8192L);
        int bs = (int)bsLong ;

        int minBufferSize = 1024 * availableProcessors / 2 ;
        return Math.max(bs, minBufferSize);
    }


    public static void main(String[] args)  {

        System.out.println(" LARGE_FILE_SIZE = " + LARGE_FILE_SIZE);
        System.out.println(" BUFFER_SIZE = " + BUFFER_SIZE);
        long startTime = System.currentTimeMillis();

        File file1 = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)");
        File file2 = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат");

        System.out.println("  размер файла - " + file1.length());

//        String hf1 = calculateHashSmallFile(file1);
//        String hf2 = calculateHashLargeFile(file2);

        CompletableFuture<byte[]> hashFuture1 = CompletableFuture.supplyAsync(() -> {
            return FileKeyHash.calculateHashLargeFile(file1);
        });

        CompletableFuture<byte[]> hashFuture2 = CompletableFuture.supplyAsync(() -> {
            return FileKeyHash.calculateHashLargeFile(file2);
        });

        // Ожидаем завершения обоих вычислений
        byte[] hash1 = hashFuture1.join();
        byte[] hash2 = hashFuture2.join();

        long endTime = System.currentTimeMillis();

        if (Arrays.equals(hash1, hash2)) {
            System.out.println("Файлы одинаковые");
        } else {
            System.out.println("Файлы разные");
        }


        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
    }
}