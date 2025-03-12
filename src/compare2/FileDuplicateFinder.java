package compare2;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    // Хранит группы файлов по ключу - размеру файла
    private final Map<Long, List<Set<File>>> fileByContent;

    ExecutorService executor;

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        //this.fileByContent = new ConcurrentSkipListMap<>();
        this.fileByContent = new ConcurrentHashMap<>();
        //this.executor = Executors.newCachedThreadPool();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }
        System.err.println(" -------- вышел из walkFileTree ---------------------- ");
        // Вывод групп дубликатов файлов в консоль
        printSortedFileGroups();
    }



    /* Ускореный метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {
        if (!checkValid.isValidDirectoryPath(path)) {
            System.err.println("Невалидная директория: " + path);
            return;
        }

        File rootDirectory = new File(path); // Создаем объект File(рут директория) для указанного пути
        Deque<File> directories = new LinkedList<>();   // Создаем очередь для обхода файловой системы
        directories.push(rootDirectory); // Добавляем в очередь рут директорию

        //ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        //List<CompletableFuture<Void>> futures = new ArrayList<>();

        while(!directories.isEmpty()) {
            System.err.println("1111111  ---------------------- ");
            File currentDirectory = directories.remove(); // Извлекаем из очереди текущую директорию
            File[] files = currentDirectory.listFiles(); // Получаем список всех файлов и директорий в текущей директории

            if(files == null) continue; // Проверяем, что массив не пустой

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for(int i = 0; i < files.length; i++) { // Перебираем каждый файл и директорию в текущей директории

                System.err.println("222222222222  ---------------------- ");
                final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной
                if(file.isDirectory() && checkValid.isValidDirectoryPath(file.getAbsolutePath())) {
                    System.err.println("333333333333333333  ---------------------- ");
                    directories.push(file); // Если текущий файл является валидной директорией, добавляем его в очередь
                } else {
                    futures.add(CompletableFuture.runAsync(() -> {
                        // Если файл валиден, то добавляем его в массив futures
                        if(checkValid.isValidFile(file)) {
                            //System.err.println("444444444444444  ---------------------- " + file.getAbsolutePath());

                                //System.err.println("41414141414141414141414141414  ---------------------- ");
                                processFileCompare(file);
                               // System.out.println(" закончил обрабатыватся файл - " + file.getAbsolutePath());

                        }
                    }));
                }

            }

            // Ожидаем завершения всех CompletableFuture
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown(); // Закрываем executor после завершения всех задач
            System.err.println("----------- закончился for(int i = 0; i < files.length; i++) ---------------------- ");
        }

        System.err.println("directories - ПУСТАЯ ---------------------- ");


        System.err.println("------------------ задачи завершены  ---------------------- ");
    }

//    public void walkFileTree(String path) throws IOException {
//        if (!checkValid.isValidDirectoryPath(path)) {
//            System.err.println("Невалидная директория: " + path);
//            return;
//        }
//
//        File directory = new File(path); // Создаем объект File(директория) для указанного пути
//        File[] files = directory.listFiles(); // Получаем список всех файлов и директорий в указанной директории
//        if (files == null) return; // Проверяем, что массив не пустой
//
//        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
//        //ExecutorService executor = Executors.newCachedThreadPool();
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//
//        for (File f : files) { // Перебираем каждый файл и директорию в текущей директории
//            final File file = f; // Сохраняем ссылку на текущий файл в локальной переменной
//
//            if (file.isDirectory()) { // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
//                walkFileTree(file.getAbsolutePath());
//            } else {
//                // Если файл валиден, то добавляем его в массив futures
//                if (checkValid.isValidFile(file)) {
//                    futures.add(CompletableFuture.runAsync(() -> {
//                        try {
//                            processFileCompare(file);
//                        } catch (IOException e) {
//                            System.err.println("Ошибка при обработке файла: " + file.getAbsolutePath());
//                            throw new RuntimeException(e);
//                        }
//                    }, executor));
//                }
//            }
//        }
//
//        // Ожидаем завершения всех CompletableFuture
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//        executor.shutdown(); // Закрываем executor после завершения всех задач
//    }


    /* Метод обработки файла для добавления в fileByContent
     * @param file - файл, который нужно добавить в карту
     */
    private void processFileCompare(File file)  {
        System.out.println("Обрабатывается файл - " + file.getAbsolutePath());
        Long fileSize = file.length();

            fileByContent.compute(fileSize, (key, fileList) -> {
                if (fileList == null) {
                    // Если нет групп для этого размера, создаем новую
                    Set<File> newGroup = ConcurrentHashMap.newKeySet();
                    newGroup.add(file);
                    List<Set<File>> newFileList = new CopyOnWriteArrayList<>();
                    newFileList.add(newGroup);
                    return newFileList;
                } else {
                    // Проверяем существующие группы
                    for (Set<File> fileSet : fileList) {
                        final File firstFile = fileSet.iterator().next();
                        try {
                            if (FileComparator.areFilesEqual(file, firstFile)) {
                                fileSet.add(file);
                                return fileList; // Файл добавлен, возвращаем список
                            }
                        } catch (IOException e) {
                            System.err.println("Ошибка при сравнении файлов: " + file.getAbsolutePath() + " и " + firstFile.getAbsolutePath());
                            e.printStackTrace();
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