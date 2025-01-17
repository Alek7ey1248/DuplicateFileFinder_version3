package V12;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class FileKey implements Comparable<FileKey> {
    private final long size;
    private final String partialContentHash;   // Хеш первых 1024 байт файла

    // Конструктор для создания ключа файла на основе размера и части содержимого
    public FileKey(File file) throws IOException, NoSuchAlgorithmException {
        this.size = file.length();
        this.partialContentHash = calculatePartialHash(file);
    }

    // Метод для вычисления хеша части содержимого файла
    private String calculatePartialHash(File file) throws IOException, NoSuchAlgorithmException {
        System.out.println("Вычисление хеша файла: " + file.getAbsolutePath());
        byte[] buffer = new byte[1024]; // Размер буфера для чтения файла
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(buffer);
            if (bytesRead != -1) {
                // Обновляем хеш с прочитанными байтами
                digest.update(buffer, 0, bytesRead);
            }
        }

        // Получаем хеш в виде байтового массива
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();

        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }


    // Переопределение методов equals и hashCode для корректного сравнения объектов FileKey
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKey fileKey = (FileKey) o;
        return size == fileKey.size && partialContentHash.equals(fileKey.partialContentHash);
    }

    // Переопределение метода hashCode для корректного сравнения объектов FileKey
    @Override
    public int hashCode() {
        return Objects.hash(size, partialContentHash);
    }

    // Переопределение метода compareTo для корректного сравнения объектов FileKey
    @Override
    public int compareTo(FileKey other) {
        int sizeComparison = Long.compare(this.size, other.size);
        if (sizeComparison != 0) {
            return sizeComparison;
        }
        return this.partialContentHash.compareTo(other.partialContentHash);
    }

    public long getSize() {
        return size;
    }
}