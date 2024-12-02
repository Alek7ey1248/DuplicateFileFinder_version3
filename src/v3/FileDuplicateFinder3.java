package v3;

import v3.CheckValid;
import v1.FileComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;


/*
 * LARGE_FILE_THRESHOLD: Определяем порог для больших файлов на основе доступной памяти.
 * findDuplicates: Основной метод для поиска дубликатов файлов в указанных директориях.
 * walkFileTree: Рекурсивный обход директорий для группировки файлов по их размеру или хешу.
 * findLargeDuplicates: Метод для поиска дубликатов среди больших файлов.
 * createDuplicatesList: Метод для создания списка дубликатов из карт filesByHash и largeFilesBySize.
 * getDuplicates: Метод для получения списка дубликатов.
 * */

public class FileDuplicateFinder3 {

    /* HashMap fileByHash - для хранения файлов, сгруппированных по хешу
    * Ключ FileKey хранит размер и хеш файла
    */
    private final TreeMap<FileKey, Set<File>> fileByHash;

    /* геттер для получения карты файлов по хешу */
    public TreeMap<FileKey, Set<File>> getFilesByHash() {
        return fileByHash;
    }

    /* Конструктор */
    public FileDuplicateFinder3() {
        fileByHash = new TreeMap<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
     * */
    public void findDuplicates(String[] paths) throws IOException {

        // Рекурсивный обход директорий для группировки файлов по их хешу в карту filesByHash
        for (String path : paths) {
            walkFileTree(path);
        }

        // Вывод групп дубликатов файлов в консоль
        printDuplicateResults();
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их хешу в HasyMap filesByHash.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        // Создаем объект File(директорий) для указанного пути
        File directory = new File(path);

        // Получаем список всех файлов и директорий в указанной директории
        File[] files = directory.listFiles();

        // Проверяем, что массив не пустой
        if (files != null) {
            // Перебираем каждый файл и директорию в текущей директории
            for (File file : files) {
                // Если текущий файл является директорией, создаем новый поток для рекурсивного вызова walkFileTree
                if (file.isDirectory()) {
                    walkFileTree(file.getAbsolutePath());
                } else {
                        // Добавляем файл в мапу по хешу
                        addFileToTreeMap(file);
                }
            }

        }
    }


    // Метод для добавления файла в мапу по ключу - хэшу
    public void addFileToTreeMap(File file) {
        System.out.println("обрабатывается - : " + file.getName());

        // Вычисляем хэш файла
        //long fileHash = hashing.calculateHashWithSize(file);
        FileKey fileKey = new FileKey(file);

        // Если хэш уже есть в мапе, добавляем файл к существующему списку
        if (fileByHash.containsKey(fileKey)) {
            fileByHash.get(fileKey).add(file);
        } else {
            // Иначе создаем новый список и добавляем файл
            Set<File> fileSet = new HashSet<>();
            fileSet.add(file);
            fileByHash.put(fileKey, fileSet);
        }
    }


    // выводит группы дубликатов файлов
    public void printDuplicateResults() throws IOException {
        // Проходим по всем записям в TreeMap fileByHash
        for (Map.Entry<FileKey, Set<File>> entry : fileByHash.entrySet()) {
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