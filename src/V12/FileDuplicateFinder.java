package V12;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    private final Map<FileKey, Set<File>> filesByKey;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    Map<FileKey, Set<File>> getFilesByKey() {return filesByKey;}

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.filesByKey = new ConcurrentSkipListMap<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }

        removeSingles();  // Удаляем группы файлов, в которых меньше 2 файлов

        printDuplicateResults();  // Вывод групп дубликатов файлов в консоль
        
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) throws IOException {
        File directory = new File(path);  // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            for (File file : files) {  // Перебираем каждый файл и директорию в текущей директории
                if (file.isDirectory()) {    // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                    walkFileTree(file.getAbsolutePath());
                } else {
                    if (checkValid.isValidFile(file)) {  // Проверка валидности файла
                        // Если текущий файл не является директорией, добавляем его в карту
                        // Группируем файлы по их ключу - размеру и хешу первых 1024 байт
                        FileKey fileKey = null; // Создаем ключ для файла
                        try {
                            fileKey = new FileKey(file);
                        } catch (NoSuchAlgorithmException e) {
                            System.out.println("Ошибка при создании ключа файла: " + file.getAbsolutePath());
                            throw new RuntimeException(e);
                        }
                        // Используем computeIfAbsent для добавления файла в список
                        filesByKey.computeIfAbsent(fileKey, k -> new HashSet<>()).add(file);
                    }
                }
            }
        }
    }

    // Удаляет группы файлов, в которых меньше 2 файлов
    private void removeSingles() {
        filesByKey.entrySet().removeIf(entry -> entry.getValue().size() < 2);
    }


    // выводит группы дубликатов файлов
    public void printDuplicateResults() {
        // Проходим по всем записям в TreeMap fileByHash
        for (Map.Entry<FileKey, Set<File>> entry : filesByKey.entrySet()) {
            // Получаем ключ (FileKey) и значение (Set<File>) для текущей записи
            FileKey key = entry.getKey();
            Set<File> files = entry.getValue();
            // Проверяем, что в группе есть файлы
            if (files.size() > 1) {
                System.out.println();
                // Выводим информацию о группе дубликатов
                System.out.println("Группа дубликатов файла: '" + files.iterator().next().getName() + "' размера - " + key.getSize() + " байт -------------------------------");
                // Проходим по всем файлам в группе и выводим их пути
                for (File file : files) {
                    System.out.println(file.getAbsolutePath());
                }
                System.out.println();
                System.out.println("--------------------");
            }
        }
    }



}