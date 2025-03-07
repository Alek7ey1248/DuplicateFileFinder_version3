package V4;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    private final Map<Long, List<Set<File>>> fileByContent;  // Карта файлов по содержимому (ключ - хеш содержимого, значение - список групп файлов) - отсортированная по ключу и потокобезопасная

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        fileByContent = new ConcurrentSkipListMap<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }

        // Вывод групп дубликатов файлов в консоль
        printSortedFileGroups();
    }



    /* Ускореный метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) throws IOException {
        if (!checkValid.isValidDirectoryPath(path)) {
            System.err.println("Невалидная директория: " + path);
            return;
        }

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files == null) return;  // Проверяем, что массив не пустой

        for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
            final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной

            if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                walkFileTree(file.getAbsolutePath());
            } else {
                // Если файл не валиден, пропускаем его и переходим к следующему файлу
                if (!checkValid.isValidFile(file)) continue;
                processFile(file);
            }
        }
    }


    /* Метод обработки файла для добавления в fileByContent
     * @param file - файл, который нужно добавить в карту
     */
    private void processFile(File file) throws IOException {
        System.out.println(" обрабатывается файл - " + file.getAbsolutePath());
        long fileSize = file.length(); // Размер файла
        // Если в карте (есть такой ключ) есть файлы с таким же размером
        if (fileByContent.containsKey(fileSize)) {
            // Добавляем файл в одну из групп Set<File> fileSet
            boolean isAdded = addFileToSet(fileSize, file);

            if (!isAdded) {    // Если файл не добавлен ни в одну группу, создаем новую группу
                addFileToNewSetInListByKey(fileSize, file);
            }
        } else {  // Если в карте нет файлов с таким же размером(ключем) как у файла
            // Добавляем группу списков в карту и файл в группу
            addFileToNewSetToNewListByNewKey(fileSize, file);
        }
    }



    /* Вспомогательный метод для добавления файла в одну изгрупп Set<File> fileSet
     * Для этого перебираем все Set<File> в списке fileList
     * и сравниваем файл  с первым файлом в каждой группе
     * Если файл одинаков по содержимому с первым файлом в группе, добавляем файл в группу
     * и возвращаем true, иначе false
     */
    private boolean addFileToSet(long fileSize, File file) throws IOException {
        List<Set<File>> fileList = fileByContent.get(fileSize); // Получаем список групп файлов по ключу fileSize
        for (Set<File> fileSet : fileList) { // Перебираем все группы файлов по ключу fileSize
            File firstFile = fileSet.iterator().next(); // Извлекаем первый файл для сравнения
            if (FileComparator.areFilesEqual(file, firstFile)) { // Если файл одинаков по содержимому с первым файлом в группе
                fileSet.add(file); // Добавляем файл в группу
                return true; // Файл добавлен
            }
        }
        return false; // Файл не добавлен
    }


    /* Вспомогательный метод для создания нового Set<File> fileSet
    * в List<Set<File>> fileList по ключу fileSize и добавления туда файла
     */
    private void addFileToNewSetInListByKey(long fileSize, File file) {
        Set<File> newGroup = new HashSet<>();  // Создаем новую группу
        newGroup.add(file);      // Добавляем файл в группу
        fileByContent.get(fileSize).add(newGroup);  // Добавляем новый список по ключу в карту
    }


    /* Вспомогательный метод для добавления в Map<Long, List<Set<File>>> fileByContent
     * новой группы списка файлов по ключу - размер файла
     * и добавления файла в группу
     * @param fileSize - размер файла
     * @param file - файл, который нужно добавить в карту
     */
    private void addFileToNewSetToNewListByNewKey(long fileSize, File file) {
        List<Set<File>> fileList = new ArrayList<>(); // Создаем список групп файлов
        Set<File> newGroup = new HashSet<>();         // Создаем новый список файлов
        newGroup.add(file);                            // Добавляем файл в список
        fileList.add(newGroup);                        // Добавляем список в группу списков
        fileByContent.put(fileSize, fileList);         // Добавляем группу списков в карту
    }


    // Вывод групп дубликатов файлов в консоль
    public void printSortedFileGroups() {

        // Выводим отсортированные группы в консоль
        for (List<Set<File>> fileList : fileByContent.values()) {  // Перебираем все группы списков файлов

            for (Set<File> fileSet : fileList) {             // Перебираем все группы файлов
                if (fileSet.size() < 2) {                    // Если в группе только один файл, переходим к следующей группе
                    continue;
                }
                // Извлекаем размер первого файла для вывода
                File firstFile = fileSet.iterator().next();
                System.out.println("-------------------------------------------------");
                System.out.println("Группа дубликатов размером: " + firstFile.length() + " байт");
                for (File file : fileSet) {                  // Перебираем все файлы в группе
                    System.out.println("    " + file.getAbsolutePath());
                }
                System.out.println("-------------------------------------------------");
            }
        }
    }


    // Метод преобразования Map<Long, List<Set<File>>> fileByContent в  List<Set<File>> duplicates
    // Для тестирования в тестах TesterUnit
    public List<Set<File>> getDuplicates() {
        List<Set<File>> duplicates = new ArrayList<>();
        for (List<Set<File>> fileList : fileByContent.values()) {
            for (Set<File> fileSet : fileList) {
                if (fileSet.size() > 1) {
                    duplicates.add(fileSet);
                }
            }
        }
        return duplicates;
    }

}