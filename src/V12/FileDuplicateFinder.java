package V12;

import V11.FileComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();  // Количество процессоров

    private static final int LARGE_FILE_SIZE = getOptimalLargeFileSize()/1; // порог для больших файлов

    private final CheckValid checkValid;

    // класс для проверки схожести имен файлов
    private final FileNameSimilarityChecker fileNameSimilarityChecker;

    private final Map<Long, Set<File>> fileBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру
    Map<Long, Set<File>> getFileBySize() {return fileBySize;}

    private final FileGrouper fileGrouper;

    //private final Map<FileKeyHash, Set<File>> filesByKey;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    //Map<FileKeyHash, Set<File>> getFilesByKey() {return filesByKey;}

    //private final Map<String, Set<File>> filesByContent;  /* HashMap filesBySize - для хранения файлов, сгруппированных по одинаковому контенту */

    private final List<Set<File>> duplicates;
    List<Set<File>> getDuplicates() {return duplicates;}

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.fileNameSimilarityChecker = new FileNameSimilarityChecker();
        this.fileBySize = new HashMap<>();
        this.fileGrouper = new FileGrouper();
//        this.filesByKey = new ConcurrentSkipListMap<>();
//        this.filesByContent = new ConcurrentSkipListMap<>();
        this.duplicates = new ArrayList<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }

        processGroupFiles();  // Добавляем файлы в карту fileByKey из HashMap fileBySize
        removeSingleFiles();  // Удаляем списки по одному файлу из filesByKey

        printSortedFileGroups();  // Вывод групп дубликатов файлов в консоль
        //printDuplicateResults();  // Вывод групп дубликатов файлов в консоль
        
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        if (!checkValid.isValidDirectoryPath(path)) {
            System.out.println("Невалидная директория: " + path);
            return;
        }

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            for (File file : files) {  // Перебираем каждый файл и директорию в текущей директории
                if (file.isDirectory()) {  // Если текущий файл является директорией, создаем новый поток для рекурсивного вызова walkFileTree
                    walkFileTree(file.getAbsolutePath());
                } else {
                    if (!checkValid.isValidFile(file)) {  // Проверяем, что текущий файл является валидным
                        continue;
                    }
                    // Добавляем файл в карту fileBySize по его размеру
                    long fileSize = file.length();
                    fileBySize.computeIfAbsent(fileSize, k -> new HashSet<>()).add(file);
                }
            }
        }
    }


    // Добавление файлов в карту fileByKey или filesByContent в зависимости от логики метода processGroupFiles
    // Перед этим смотрим если файлов меньше 2, то пропускаем
    public void processGroupFiles() {

        fileBySize.entrySet().forEach(entry -> {

            long sizeFile = entry.getKey();     // Размер файла
            Set<File> files = entry.getValue();  // Список файлов одинакового размера
            int numFiles = files.size();       // Количество файлов одинакового размера

            if (numFiles < 2) {  // Пропускаем списки файлов, которых меньше 2
                return;
            }

            boolean areFileNamesSimilar = fileNameSimilarityChecker.areFileNamesSimilar(files);
            if(
                    (numFiles > NUM_PROCESSORS && areFileNamesSimilar) ||
                            (numFiles > (NUM_PROCESSORS*1.5)) ||
                    (sizeFile > LARGE_FILE_SIZE && areFileNamesSimilar)
            ) {
                //System.out.println("Файлы схожи по именам: ");
                //fileGrouper.groupByHesh(files);
                fileGrouper.groupByHeshParallel(files);
            } else {
                //System.out.println("Файлы не схожи по именам: ");
                try {
                    //fileGrouper.groupByContent(files);
                    fileGrouper.groupByContentParallel(files);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }


    // Групировка файлов по хешу и добавление в filesByKey - группы дубликатов
//    private void groupByHesh(Set<File> files) {
//        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//
//        files.forEach(file -> {
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                try {
//                    FileKeyHash key = new FileKeyHash(file);
//                    filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
//                } catch (IOException | NoSuchAlgorithmException e) {
//                    System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
//                    e.printStackTrace();
//                }
//            }, executor);
//            futures.add(future);
//        });
//
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        allOf.join(); // Блокируем текущий поток до завершения всех задач
//
//        executor.shutdown();
//        try {
//            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                executor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            executor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//
//
//    private void groupByContent(Set<File> files) throws IOException {
//        while (files.size() > 1) {  // Пока в списке файлов одинакового размера есть хотя бы два файла
//            Iterator<File> iterator = files.iterator();  // Извлекаем первый файл из списка
//            File file = iterator.next();
//            iterator.remove();   // Удаляем первый файл из списка
//            System.out.println(" Поиск дубликатов файла: " + file.getAbsolutePath());
//
//            Set<File> group = new HashSet<>();
//            group.add(file);   // Добавляем первый файл в группу дубликатов
//            String key = file.getAbsolutePath();  // Ключ для группы дубликатов по содержимому - путь к файлу
//            Set<File> toRemove = ConcurrentHashMap.newKeySet();   // Список для удаления дубликатов из files
//
//            //List<CompletableFuture<Void>> fileComparisons = new ArrayList<>(); // Список для хранения CompletableFuture
//
//            for (File anotherFile : files) {  // Перебираем оставшиеся файлы в списке
//                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
//                    continue;
//                }
//                // Отправляем задачу на сравнение файлов в пул потоков
//                //CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                    try {
//                        if (FileComparator.areFilesEqual(file.toPath(), anotherFile.toPath())) {
//                            //synchronized (group) { // Синхронизация при добавлении в группу
//                                group.add(anotherFile);  // Добавляем путь к дубликату в группу дубликатов
//                            //}
//                            toRemove.add(anotherFile);  // Добавляем путь к дубликату в список для удаления
//                        }
//                    } catch (IOException e) {
//                        System.err.println("Ошибка при сравнении файлов: " + file.getAbsolutePath() + " и " + anotherFile.getAbsolutePath());
//                    }
////                }, executor);
////                fileComparisons.add(future); // Добавляем CompletableFuture в список
//            }
//
////            // Ждем завершения всех задач
////            CompletableFuture<Void> allOf = CompletableFuture.allOf(fileComparisons.toArray(new CompletableFuture[0]));
////            allOf.join(); // Блокируем до завершения всех задач
//
//            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера
//
//            if (group.size() > 1) { // Добавляем группу дубликатов в список дубликатов по ключу (если группа содержит более одного файла)
//                filesByContent.put(key, group);
////                System.out.println(" группа дубликатов : ----------" );
////                for (File f : group) {
////                    System.out.println(f.getAbsolutePath() + "    ");
////                }
////                System.out.println("----------" );
//            }
//        }
//    }



    // Удаление списков по одному файлу
    public void removeSingleFiles() {
        for (Iterator<Map.Entry<FileKeyHash, Set<File>>> iterator = fileGrouper.getFilesByKey().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<FileKeyHash, Set<File>> entry = iterator.next();
            if (entry.getValue().size() < 2) {
                iterator.remove();
            }
        }
    }


    public void printSortedFileGroups() {

        // Добавляем все Set<File> из filesByKey
        for (Set<File> fileSet : fileGrouper.getFilesByKey().values()) {
            duplicates.add(fileSet);
        }

        // Добавляем все Set<File> из filesByContent
        for (Set<File> fileSet : fileGrouper.getFilesByContent().values()) {
            duplicates.add(fileSet);
        }

        // Сортируем список по размеру первого файла в каждом Set<File>
        duplicates.sort(Comparator.comparingLong(set -> set.iterator().next().length()));

        // Выводим отсортированные группы в консоль
        for (Set<File> fileSet : duplicates) {
            // Извлекаем размер первого файла для вывода
            File firstFile = fileSet.iterator().next();
            System.out.println("-------------------------------------------------");
            System.out.println("Группа дубликатов размером: " + firstFile.length() + " байт");
            for (File file : fileSet) {
                System.out.println("    " + file.getAbsolutePath());
            }
            System.out.println("-------------------------------------------------");
        }
        System.out.println("-  LARGE_FILE_SIZE = " + LARGE_FILE_SIZE);
    }

    // выводит группы дубликатов файлов
//    public void printDuplicateResults() {
//        // Проходим по всем записям в TreeMap fileByHash
//        for (Map.Entry<FileKeyHash, Set<File>> entry : filesByKey.entrySet()) {
//            // Получаем ключ (FileKey) и значение (Set<File>) для текущей записи
//            FileKeyHash key = entry.getKey();
//            Set<File> files = entry.getValue();
//            // Проверяем, что в группе есть файлы
//            if (files.size() > 1) {
//                System.out.println();
//                // Выводим информацию о группе дубликатов
//                System.out.println("Группа дубликатов файла: '" + files.iterator().next().getName() + "' размера - " + key.getSize() + " байт -------------------------------");
//                // Проходим по всем файлам в группе и выводим их пути
//                for (File file : files) {
//                    System.out.println(file.getAbsolutePath());
//                }
//                System.out.println();
//                System.out.println("--------------------");
//            }
//        }
//    }

    /* Метод для определения оптимального размера большого файла для многопоточного хеширования
     * Оптимальный размер файла - это 1/4 от максимальной п��мяти, деленной на количество процессоров
     */
    private static int getOptimalLargeFileSize() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Количество доступных процессоров
        return (int) (maxMemory / (availableProcessors * 4L));
    }




}