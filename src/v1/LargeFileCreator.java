package v1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class LargeFileCreator {

    public static void main(String[] args) {
        // Указываем путь к файлу, который будет создан
        Path filePath = Path.of("largeFile.txt");
        // Указываем размер файла в байтах
        long fileSize = 10756058015L; // Размер в байтах

        try {
            // Вызываем метод для создания файла
            createLargeFile(filePath, fileSize);
            System.out.println("Файл успешно создан.");
        } catch (IOException e) {
            // Обрабатываем возможные исключения при создании файла
            e.printStackTrace();
        }
    }

    private static void createLargeFile(Path filePath, long size) throws IOException {
        // Преобразуем путь в объе��т File
        File file = filePath.toFile();
        // Открываем поток для записи в файл
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Создаем буфер размером 8 КБ
            byte[] buffer = new byte[8192]; // Буфер 8 КБ
            long bytesWritten = 0;

            // Пишем данные в файл до тех пор, пока не достигнем нужного размера
            while (bytesWritten < size) {
                // Определяем количество байт для записи в текущей итерации
                int bytesToWrite = (int) Math.min(buffer.length, size - bytesWritten);
                // Записываем данные из буфера в файл
                fos.write(buffer, 0, bytesToWrite);
                // Увеличиваем счетчик записанных байт
                bytesWritten += bytesToWrite;
            }
        }
    }
}