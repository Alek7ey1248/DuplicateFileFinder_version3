package v1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;


// InsertZerosInMiddle: Класс для вставки нулей в середину строки.
// Что бы была копия больщого файла, но с нулями в середине.
public class InsertZerosInMiddle {
    public static void main(String[] args) {
        Path filePath = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (середина изменена).mp4");
        try {
            insertZerosInMiddle(filePath);
            System.out.println("Нули успешно вставлены.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void insertZerosInMiddle(Path filePath) throws IOException {
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
}
