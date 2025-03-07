package V4;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<Set<File>>> fileByContent = new ConcurrentHashMap<>();

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
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

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
            final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной

            if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                walkFileTree(file.getAbsolutePath());
            } else {
                // Если файл не валиден, пропускаем его и переходим к следующему файлу
                if (!checkValid.isValidFile(file)) continue;
                executorService.submit(() -> {
                    try {
                        processFile(file);
                    } catch (IOException e) {
                        System.err.println("Ошибка при обработке файла: " + file.getAbsolutePath());
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        executorService.shutdown(); // Завершаем работу пула потоков
        while (!executorService.isTerminated()) {
            // Ожидаем завершения всех задач
        }
    }


    /* Метод обработки файла для добавления в fileByContent
     * @param file - файл, который нужно добавить в карту
     */
    private void processFile(File file) throws IOException {
        System.out.println("Обрабатывается файл - " + file.getAbsolutePath());
        long fileSize = file.length();

        fileByContent.compute(fileSize, (key, fileList) -> {
            if (fileList == null) {
                // Если нет групп для этого размера, создаем новую
                Set<File> newGroup = ConcurrentHashMap.newKeySet();
                newGroup.add(file);
                CopyOnWriteArrayList<Set<File>> newFileList = new CopyOnWriteArrayList<>();
                newFileList.add(newGroup);
                return newFileList;
            } else {
                // Проверяем существующие группы
                for (Set<File> fileSet : fileList) {
                    File firstFile = fileSet.iterator().next();
                    try {
                        if (FileComparator.areFilesEqual(file, firstFile)) {
                            fileSet.add(file);
                            return fileList; // Файл добавлен, возвращаем список
                        }
                    } catch (IOException e) {
                        System.err.println("Ошибка при сравнении файлов: " + file.getAbsolutePath() + " и " + firstFile.getAbsolutePath());
                        throw new RuntimeException(e);
                    }
                }
                // Если файл не был добавлен, создаем новую группу
                Set<File> newGroup = ConcurrentHashMap.newKeySet();
                newGroup.add(file);
                fileList.add(newGroup);
                return fileList;
            }
        });
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
        CopyOnWriteArrayList<Set<File>> fileList = new CopyOnWriteArrayList<>(); // Создаем список групп файлов
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