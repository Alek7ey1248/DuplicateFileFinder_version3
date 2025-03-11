package compare2;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    // Хранит группы файлов по ключу - размеру файла
    private final Map<Long, List<Set<File>>> fileByContent;

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.fileByContent = new ConcurrentHashMap<>();
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

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        var futures = new CompletableFuture[files.length];

        //ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
        for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
            final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной

            // --------------------------------------------------
            futures[i] = CompletableFuture.runAsync(() -> {
                if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                    try {
                        walkFileTree(file.getAbsolutePath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Если файл валиден, то добавляем его в карту
                    if (checkValid.isValidFile(file)) {
                        try {
                            processFileCompare(file);
                        } catch (IOException e) {
                            System.err.println("Ошибка при обработке файла: " + file.getAbsolutePath());
                            throw new RuntimeException(e);
                        }
                    }
                }
            }, executor);
            // --------------------------------------------------
        }

        // Ожидаем завершения всех CompletableFuture
        CompletableFuture.allOf(futures).join();
        executor.shutdown();
    }


    /* Метод обработки файла для добавления в fileByContent
     * @param file - файл, который нужно добавить в карту
     */
    private void processFileCompare(File file) throws IOException {
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