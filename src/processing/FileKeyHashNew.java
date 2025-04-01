package processing;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class FileKeyHashNew implements Comparable<FileKeyHashNew> {

    private byte[] contentHash;      // Хеш всего файла

    // конструктор с параметрами
    public FileKeyHashNew(File file, long offset, int bufferSize) {
        try {
            this.contentHash = calculateHash(file, offset, bufferSize);  // вычисляем хеш файла
        } catch (IOException e) {
             System.out.println("Ошибка при вычислении хеша файла: " + e.getMessage());
        }
    }


    // метод для вычисления хеша куска файла
    // Вычисление хеша файла с указанного смещения offset на длинну буфера BUFFER_SIZE
    private static byte[] calculateHash(File file, long offset, int bufferSize) throws IOException {

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
            byte[] byteArray = new byte[bufferSize];

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

        return hashBytes;
    }


    // Создание объекта MessageDigest для вычисления хеша
    private static MessageDigest createMessageDigest() {
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

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {


    }
}