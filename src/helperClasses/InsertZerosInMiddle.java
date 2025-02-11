package helperClasses;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;


// helperClasses.InsertZerosInMiddle: Класс для вставки нулей в середину строки.
// Что бы была копия больщого файла, но с нулями в середине.
public class InsertZerosInMiddle {
    public static void main(String[] args) {
        Path filePath = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена)");
        try {
            insertZerosInMiddle(filePath);
            System.out.println("Нули успешно вставлены.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для вставки нулей в середину файла
     static void insertZerosInMiddle(Path filePath) throws IOException {
        // Открываем файл для чтения и записи
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw")) {
            // Получаем размер файла
            long fileSize = raf.length();
            // Определяем середину файла
            long middle = fileSize / 2;

            // Переходим к середине файла
            raf.seek(middle);

            // Создаем буфер с нулями
            byte[] zeros = new byte[8192]; // 8 KB буфер с нулями

            // Записываем нули в середину файла
            raf.write(zeros);
        }
    }

    // метод вставки едеинц в середину файла
    static void insertOnesInMiddle(Path filePath) throws IOException {
        // Открываем файл для чтения и записи
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw")) {
            // Получаем размер файла
            long fileSize = raf.length();
            // Определяем середину файла
            long middle = fileSize / 2;

            // Переходим к середине файла
            raf.seek(middle);

            // Создаем буфер с единицами
            byte[] ones = new byte[8192]; // 8 KB буфер с единицами
            for (int i = 0; i < ones.length; i++) {
                ones[i] = 1;
            }

            // Записываем единицы в середину файла
            raf.write(ones);
        }
    }

}
