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

public class FileKeyHashNew implements Comparable<FileKeyHashNew> {

    private final byte[] contentHash;      // Хеш всего файла

    private long offset; // Смещение в файле

    // (На больш компе) - calculateHashLargeFile становиться быстрее calculateHashSmallFile на 512000(500Кб)
    // Значит порог для больших файлов - LARGE_FILE_SIZE = 524812288 надо делить на 1025
    private static final int LARGE_FILE_SIZE = getOptimalLargeFileSize()/1025; // порог для больших файлов - после тестирований скорее всего так и оставлю
    private static final int BUFFER_SIZE = getOptimalBufferSize();  // 8192 - оптимальный размер буфера на основе доступной памяти используемый в java; // Оптимальный размер буфера на основе доступной памяти

    // конструктор по умолчанию
    public FileKeyHashNew() {
        this.contentHash = new byte[0];
    }

    // Конструктор для создания ключа файла на основе размера и части содержимого
    public FileKeyHashNew(File file) throws IOException, NoSuchAlgorithmException {

        long offset;

        if (file.length() < LARGE_FILE_SIZE) {
            // если файл меньше порога, используем bufferSize для не больших файлов
            offset = 8192;
        } else {
            // если файл больше порога, используем bufferSize для больших файлов
            offset = BUFFER_SIZE;
        }
        this.contentHash = calculateHash(file, offset);
    }


    // метод для вычисления хеша файла
    // Вычисление хеша файла с указанного смещения offset на длинну буфера BUFFER_SIZE
    private byte[] calculateHash(File file, long offset) throws IOException {

        if (!file.exists() || offset < 0 || offset >= file.length()) {
            throw new IllegalArgumentException("Неверный файл или смещение");
        }

        //System.out.println("---  calculateFileHash ---" + i);
        // Создаем объект MessageDigest для вычисления хеша файла
        MessageDigest digest = createMessageDigest();

        // Открываем FileInputStream для чтения содержимого файла
        try (FileInputStream fis = new FileInputStream(file)) {
            // Пропускаем байты до указанного смещения, чтобы начать чтение с нужного места
            fis.skip(offset);

            // Создаем массив байтов для хранения данных, читаемых из файла
            byte[] byteArray = new byte[BUFFER_SIZE];

            // Читаем данные из файла в массив byteArray и сохраняем количество прочитанных байтов
            int bytesCount = fis.read(byteArray);

            // Если метод read возвращает -1, это означает, что достигнут конец файла
            if (bytesCount == -1) {
                return new byte[0]; // Возвращаем пустой массив байтов
            }

            // Обновляем объект digest, добавляя к нему прочитанные байты
            digest.update(byteArray, 0, bytesCount);
        }

        // Завершаем вычисление хеша и получаем массив байтов, представляющий хеш
        byte[] hashBytes = digest.digest();

        //System.out.println("..." + Arrays.toString(hashBytes));
        // Преобразуем массив байтов хеша в строку в шестнадцатеричном формате и возвращаем ее
        return hashBytes;
    }


    // Создание объекта MessageDigest для вычисления хеша
    private MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании MessageDigest", e);
        }
    }


    // Переопределение методов equals и hashCode для корректного сравнения объектов FileKey
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKeyHashNew fileKeyHash = (FileKeyHashNew) o;
        return Arrays.equals(contentHash, fileKeyHash.contentHash);
    }

    // Переопределение метода hashCode для корректного сравнения объектов FileKey
    @Override
    public int hashCode() {
        return Arrays.hashCode(contentHash); // Вычисляем хеш объекта FileKey на основе размера и хеша содержимого
    }
    
    // Переопределение метода compareTo для корректного сравнения объектов FileKey
    @Override
    public int compareTo(FileKeyHashNew other) {
        return Arrays.compare(this.contentHash, other.contentHash);
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


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {


    }
}