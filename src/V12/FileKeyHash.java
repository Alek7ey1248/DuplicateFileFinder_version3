package V12;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class FileKeyHash implements Comparable<FileKeyHash> {
    private final long size;
    public long getSize() {
        return size;
    }
    //private final String partialContentHash;   // Хеш первых 1024 байт файла
    private final String fullContentHash;      // Хеш всего файла
    private static final int LARGE_FILE_SIZE = getOptimalLargeFileSize()/1000; // порог для больших файлов
    private static final int BUFFER_SIZE = getOptimalBufferSize();  // 8192 - оптимальный размер буфера на основе доступной памяти используемый в java; // Оптимальный размер буфера на основе доступной памяти
    private static final int NUM_BLOCKS = (int) (Runtime.getRuntime().availableProcessors() * 1.25); // Получаем количество блоков одновременно работающих = кол-во доступных процессоров

    // конструктор по умолчанию
    public FileKeyHash() {
        this.size = 0;
        this.fullContentHash = "";
    }

    // Конструктор для создания ключа файла на основе размера и части содержимого
    public FileKeyHash(File file) throws IOException, NoSuchAlgorithmException {
        this.size = file.length();
        //this.partialContentHash = calculatePartialHash(file);
        this.fullContentHash = calculateHash(file);
    }

    /* Метод для расчета хеша файла
     * @param file - файл, для которого нужно рассчитать хеш
     */
    public static String calculateHash(File file) {
        // если файл пустой, возвращаем -1
        if (file.length() == 0) {
            return "-1";
        }

        if (file.length() < LARGE_FILE_SIZE) {
            // если файл меньше порога, используем bufferSize для не больших файлов
            return calculateHashSmallFile(file);
        }
        // если файл больше порога, используем bufferSize для больших файлов
        return calculateHashLargeFile(file);
    }


    // метод для вычисления хеша файла
    static String calculateHashSmallFile(File file) {
        System.out.println("вычисление хеша  - " + file.getAbsolutePath());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file.getAbsoluteFile());
            byte[] byteArray = new byte[8192];
            int bytesCount;

            // Чтение файла и обновление хеша
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();

            // Получение хеша в виде байтового массива
            byte[] bytes = digest.digest();

            // Преобразование байтов в шестнадцатеричную строку
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Метод для вычисления хеша болшьшого файла
    /* Метод для расчета хеша большого файла
     * @param file - файл, для которого нужно рассчитать хеш
     */
    // Метод для вычисления хеша большого файла
    static String calculateHashLargeFile(File file) {
        System.out.println("вычисление хеша LargeFile - " + file);
        try {
            return updateDigestWithLargeFileContent(file); // Обновляем хеш содержимым файла
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла " + file + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Вспомогательный метод для обновления хеша содержимым большого файла
    private static String updateDigestWithLargeFileContent(File file) throws IOException {
        //long hash = 0L; // Переменная для хранения хеша
        MessageDigest finalDigest = createMessageDigest(); // Создаем объект MessageDigest для финального хеширования
        long fileSize = file.length(); // Получаем размер файла
        long partSize = (long) Math.ceil((double) fileSize / NUM_BLOCKS); // Размер каждой части файла

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Создаем пул потоков
        List<CompletableFuture<MessageDigest>> futures = new ArrayList<>(); // Список для хранения CompletableFuture

        for (int i = 0; i < NUM_BLOCKS; i++) {
            long start = i * partSize; // Начало части
            long end = Math.min(start + partSize, fileSize); // Конец части
            futures.add(CompletableFuture.supplyAsync(() -> {
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
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
                    throw new UncheckedIOException(e);
                }
            }, executor));
        }

        // Ожидаем завершения всех задач и обновляем финальный хеш
        for (CompletableFuture<MessageDigest> future : futures) {
            try {
                MessageDigest partDigest = future.join(); // Получаем результат задачи
                synchronized (finalDigest) {
                    finalDigest.update(partDigest.digest()); // Обновляем финальный хеш
                }
            } catch (CompletionException e) {
                System.err.println("Ошибка при обновлении хеша: " + e.getMessage());
                e.printStackTrace(); // Выводим стек вызовов для диагностики
            }
        }

        executor.shutdown();
        // Получение финального хеша в виде строки
        return convertHashToString(finalDigest);
    }

    // Вспомогательный метод для преобразования хеша в строку
    private static String convertHashToString(MessageDigest digest) {
        byte[] bytes = digest.digest(); // Получение хеша в виде байтового массива
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // Преобразование байтов в шестнадцатеричную строку
        }
        return sb.toString();
    }

    // Вспомогательный метод для преобразования хеша в число
    private static long convertHashToLong(MessageDigest digest) {
        byte[] hashBytes = digest.digest(); // Получаем хеш в виде массива байт
        long hash = 0L; // Переменная для хранения итогового хеша
        // Преобразуем массив байт в число
        for (byte b : hashBytes) {
            hash = (hash << 8) + (b & 0xff); // Сдвигаем влево и добавляем байт
        }
        return hash; // Возвращаем итоговый хеш
    }

    // Вспомогательный метод для создания объекта MessageDigest
    private static MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256"); // Создаем объект MessageDigest для SHA-256
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
        return size == fileKeyHash.size && fullContentHash.equals(fileKeyHash.fullContentHash);
    }

    // Переопределение метода hashCode для корректного сравнения объектов FileKey
    @Override
    public int hashCode() {
        return Objects.hash(size, fullContentHash);
    }

    // Переопределение метода compareTo для корректного сравнения объектов FileKey
    @Override
    public int compareTo(FileKeyHash other) {
        int sizeComparison = Long.compare(this.size, other.size);
        if (sizeComparison != 0) {
            return sizeComparison;
        }
        return this.fullContentHash.compareTo(other.fullContentHash);
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
    }
}