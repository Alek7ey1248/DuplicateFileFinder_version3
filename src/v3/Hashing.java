package v3;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;




public class Hashing {

    private static int BUFFER_SIZE; // Оптимальный размер буфера на основе доступной памяти


    public Hashing() {
        BUFFER_SIZE = getOptimalBufferSize();
        //BUFFER_SIZE = 1048576;
        //BUFFER_SIZE = 8192;
        //BUFFER_SIZE = 4096;
    }

    // Метод для расчета хеша файла с учетом размера файла
    public long calculateHashWithSize(File file) {
        System.out.println(" обработка - " + file.getAbsolutePath());
        try {
            // Получаем экземпляр MessageDigest для алгоритма SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Используем BufferedInputStream для уменьшения количества операций ввода-вывода
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            // Добавляем размер файла в хэш
            long fileSize = file.length();
            byte[] sizeBytes = ByteBuffer.allocate(Long.BYTES).putLong(fileSize).array();
            digest.update(sizeBytes);

            // Получаем байты хеша и преобразуем их в целое число
            byte[] hashBytes = digest.digest();
            int hash = 0;
            for (byte b : hashBytes) {
                hash = (hash << 8) + (b & 0xff);
            }
            return hash;
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден: ------ " + file.getAbsolutePath());
            return -1;
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла " + file.getAbsolutePath() + ": " + e.getMessage());
            return -1;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-256 не найден", e);
        }
    }



    // Метод для определения оптимального размера буфера на основе доступной памяти
    // Метод для определения оптимального размера буфера на основе доступной памяти и количества процессоров
    private int getOptimalBufferSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        long bsLong = maxMemory / (availableProcessors * 8192L * 8);
        int bs = (int)bsLong ;

        int minBufferSize = 1024 * availableProcessors / 2 ;
        return Math.max(bs, minBufferSize);
    }


    public static void main(String[] args) {
        // Получаем экземпляр Runtime
        Runtime runtime = Runtime.getRuntime();

        // Получаем максимальное количество памяти, которое может быть использовано JVM
        long maxMemory = runtime.maxMemory();

        // Выводим объем памяти в гигабайтах
        System.out.println("Максимальный объем памяти (в байтах): " + maxMemory);

        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("кол-во процессоров: " + availableProcessors);



        long bsLong = maxMemory / (availableProcessors * 8192L * 8);
        int bs = (int)bsLong ;

        int minBufferSize = 1024 * availableProcessors / 2 ;
        int bufferSize = Math.max(bs, minBufferSize);
        System.out.println("bs: " + bs + "         bufferSize: " + bufferSize);
    }
}