package compare2;

import processing.CheckValid;
import processing.FileComparator;
import processing.Printer;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    // Хранит группы файлов по ключу - размеру файла
    private final Map<Long, List<List<File>>> fileByContent;

    private final List<String> verifiedDirectories;  // Список всех абсолютных путей проверенных директорий

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.fileByContent = new ConcurrentSkipListMap<>();
        this.verifiedDirectories = new ArrayList<>();
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
        Printer.duplicatesByContent2(fileByContent);
    }



    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {
        if (!checkValid.isValidDirectoryPath(path) || verifiedDirectories.contains(path)) {
            System.err.println("Невалидная директория или проверенная уже: " + path);
            return;
        }

        verifiedDirectories.add(path); // Добавляем проверенную директорию в список

        File directory = new File(path); // Создаем объект File(рут директория) для указанного пути
        File[] files = directory.listFiles(); // Получаем список всех файлов и директорий в текущей директории

        if (files == null || files.length == 0) { // Проверяем, что массив не пустой
            System.err.println(" В директории " + path + " нет файлов");
            return;
        }

        for(File file : files) { // Перебираем каждый файл и директорию в текущей директории
                if (file.isDirectory()) {
                    walkFileTree(file.getAbsolutePath()); // Если текущий файл является валидной директорией, вставляем рекурсивно в walkFileTree
                } else {
                    // Если файл валиден, то добавляем его в fileByContent
                    if(checkValid.isValidFile(file)) {
                            processFileCompare(file);
                    }
                }
        }
    }


    /* Метод обработки файла для добавления в fileByContent
     * @param file - файл, который нужно добавить в карту
     */
    private void processFileCompare(File file) {
        System.out.println(" " + file.getAbsolutePath());

        long fileSize = file.length(); // Получаем размер файла

        // Если ключа нет(то есть списка групп файлов с таким размером файла нет),
         // то добавляем новый ключ, список групп, нов группу и файл в нее
        if (!fileByContent.containsKey(fileSize)) {
            addNewKey(fileSize, file);
            return;
        }

        // Если ключ есть, то добавляем файл в группу если есть группа файлов с таким же содержимым как и у входящего файла
        if (!addFileInGroup(fileSize, file)) {
            // Если файл не добавлен в группу, то добавляем новую группу в список по ключу(размер файла) и файл в нее
            addNewGroup(fileSize, file);
        }

    }

        /*  Вспомогательный Метод добавления нового ключа в карту fileByContent
         * так же добавляет новую группу и файл в нее
         * @param fileSize - размер файла
         * @param file - файл, который нужно добавить в карту
        */
        private void addNewKey(long fileSize, File file) {
            List<File> newGroup = new CopyOnWriteArrayList<>();
            newGroup.add(file);
            List<List<File>> newListList = new CopyOnWriteArrayList<>();
            newListList.add(newGroup);
            fileByContent.put(fileSize, newListList); // Добавляем новую группу в карту
        }

        /* Вспомогательный Метод добавления новой группы в один из списков
         * в карту fileByContent в том случае если будет найдена группа файлов с одинаковым содержимым.
         * @param fileSize - размер файла
         * @param file - файл, который нужно добавить в карту
        */
         private boolean addFileInGroup(long fileSize, File file) {
             // если есть ключ, то перебираем список списков по ключу
             for (List<File> group : fileByContent.get(fileSize)) {
                 File firstFile = group.getFirst();
                 try {
                     // если первый файл в группе равен текущему файлу, то добавляем его в группу
                     if (FileComparator.areFilesEqual(file, firstFile)) {
                         group.add(file);
                         return true; // если файл добавлен в группу, то возвращаем true
                     }
                 } catch (IOException e) {
                     System.err.println("Ошибка при сравнении файлов: " + file.getAbsolutePath() + " и " + firstFile.getAbsolutePath());
                     e.printStackTrace();
                 }
             }
             return false; // если файл не добавлен в группу, то возвращаем false
         }



        /* Вспомогательный Метод добавления новой группы в один из списков
         * в карту fileByContent и файла в нее в том случае если НЕ будет найдена
         * группа файлов с таким же содержимым как и у входящего файла
         * @param fileSize - размер файла
         * @param file - файл, который нужно добавить в карту
        */
        private void addNewGroup(long fileSize, File file) {
            List<File> newGroup = new CopyOnWriteArrayList<>();
            newGroup.add(file);
            fileByContent.get(fileSize).add(newGroup);
        }


    // Метод преобразования Map<Long, List<Set<File>>> fileByContent в  List<Set<File>> duplicates
    // Для тестирования в тестах TesterUnit
    public List<Set<File>> getDuplicates() {
        List<List<File>> duplicates = new ArrayList<>();
        for (List<List<File>> fileList : fileByContent.values()) {
            for (List<File> fl : fileList) {
                if (fl.size() > 1) {
                    duplicates.add(fl);
                }
            }
        }

        // Преобразуем List<List<File>> в List<Set<File>>
        // Для тестирования в тестах TesterUnit
        List<Set<File>> duplicatesSet = duplicates.stream()
                .map(HashSet::new) // Преобразуем каждый List<File> в Set<File>
                .collect(Collectors.toList()); // Собираем результаты в List<Set<File>>

        return duplicatesSet;
    }

}