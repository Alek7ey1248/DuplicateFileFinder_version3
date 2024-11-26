package v3;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;




public class Hashing {

    public Hashing() {
    }

    // Метод для расчета хеша файла с учетом размера файла
    public long calculateHashWithSize(File file) {
        System.out.println(" обработка - " + file.getAbsolutePath() );
        try {
            // Получаем экземпляр MessageDigest для алгоритма SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Определяем оптимальный размер буфера на основе доступной памяти
            int bufferSize = getOptimalBufferSize();

            // Используем BufferedInputStream для уменьшения количества операций ввода-вывода
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), bufferSize)) {
                byte[] buffer = new byte[bufferSize];
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
    private int getOptimalBufferSize() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Используем 1/256 доступной памяти для буфера, но не менее 8KB и не более 1MB
        return (int) Math.min(Math.max(maxMemory / 256, 8192), 1048576);
    }
}