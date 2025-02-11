package helperClasses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

// Класс для создания большого пустого файла
public class LargeFileCreator {

    // Метод для создания файла заданного размера
    public static void createFile(Path filePath, long size) throws IOException {
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

    // метод вставки единиц в начало файла
    public static void insertOnesInBeginning(Path filePath) throws IOException {
        // Открываем файл для чтения и записи
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw")) {
            // Получаем размер файла
            long fileSize = raf.length();

            // Создаем буфер с единицами
            byte[] ones = new byte[8192]; // 8 KB буфер с единицами
            for (int i = 0; i < ones.length; i++) {
                ones[i] = 1;
            }

            // Считываем остальное содержимое файла
            byte[] remainingContent = new byte[(int) fileSize];
            raf.readFully(remainingContent);

            // Очищаем файл
            raf.setLength(0);

            // Записываем единицы в начало файла
            raf.write(ones);

            // Записываем оставшееся содержимое обратно в файл
            raf.write(remainingContent);
        }
    }


    // Метод создания 2 одинаковых файлов заданного размера в указанной директории
    public static void createTwoFiles(String directoryPath, String fileName1, String fileName2, long size) throws IOException {
        // Создаем путь к первому файлу
        Path filePath1 = Path.of(directoryPath + fileName1);
        // Создаем путь ко второму файлу
        Path filePath2 = Path.of(directoryPath + fileName2);
        // Создаем первый файл
        createFile(filePath1, size);
        // Создаем второй файл - копию первого
        Files.copy(filePath1, filePath2);
        System.out.println("Файлы успешно созданы");
        System.out.println("file1 - " + filePath1);
        System.out.println("file2 - " + filePath2);
    }

    // Метод создания 2 разных файлов заданного размера в указанной директории
    public static void createTwoDifferentFiles(String directoryPath, String fileName1, String fileName2, long size) throws IOException {
        // Создаем путь к первому файлу
        Path filePath1 = Path.of(directoryPath + fileName1);
        // Создаем путь ко второму файлу
        Path filePath2 = Path.of(directoryPath + fileName2);
        // Создаем первый файл
        createFile(filePath1, size);
        // Создаем второй файл
        createFile(filePath2, size);
        // так как метод createFile делает одинаковые файлы, то вставляем еденицы в середину второго файла
        InsertZerosInMiddle.insertOnesInMiddle(filePath2);

        System.out.println("Файлы успешно созданы");
        System.out.println("file1 - " + filePath1);
        System.out.println("file2 - " + filePath2);
    }



    public static void main(String[] args) throws IOException {
        // Указываем путь к файлу, который будет создан
        Path filePath = Path.of("/home/alek7ey/Рабочий стол/largeFile.txt");

        insertOnesInBeginning(filePath); // Вставляем единицы в начало файла

        // Указываем размер файла в байтах
//        long fileSize = 3359325264L; // Размер в байтах
//
//        try {
//            // Вызываем метод для создания файла
//            createFile(filePath, fileSize);
//            System.out.println("Файл успешно создан.");
//        } catch (IOException e) {
//            // Обрабатываем возможные исключения при создании файла
//            e.printStackTrace();
//        }
    }
}