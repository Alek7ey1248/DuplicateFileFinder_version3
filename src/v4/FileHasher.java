package v4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;


// Класс для группировки файлов по их хэшам
public class FileHasher {

    // Мапа для хранения хэша и соответствующего файла
    private HashMap<String, Set<File>> fileMap = new HashMap<>();
    public HashMap<String, Set<File>> getFileMap() {
        return fileMap;
    }

    // Метод для добавления файла в мапу по ключу - хэшу
    public void addFile(File file) {
        System.out.println("обрабатывается - : " + file.getName());
        // Вычисляем хэш файла
        String fileHash = computeFileHash(file);

        // Если хэш уже есть в мапе, добавляем файл к существующему списку
        if (fileMap.containsKey(fileHash)) {
            fileMap.get(fileHash).add(file);
        } else {
            // Иначе создаем новый список и добавляем файл
            Set<File> fileSet = new HashSet<>();
            fileSet.add(file);
            fileMap.put(fileHash, fileSet);
        }
    }

    // Вспомогательный метод для вычисления хэша файла
    private String computeFileHash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            // Используем SHA-256 для вычисления хэша
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesRead;

            // Читаем файл и обновляем хэш
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }

            // Конвертируем хэш в строку
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) {
                sb.append(String.format("%02x", b)); // Форматируем в шестнадцатичный вид
            }
            return sb.toString(); // Возвращаем хэш как строку
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace(); // Обрабатываем исключения
            return null; // Если произошла ошибка, возвращаем null
        }
    }

    // Пример использования ForkJoinPool для обработки множества файлов
    public void processFiles(File[] files) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new FileProcessor(files, this));
    }
}