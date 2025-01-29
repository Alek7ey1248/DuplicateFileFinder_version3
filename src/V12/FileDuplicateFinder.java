package V12;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class FileDuplicateFinder {

    private static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();  // Количество процессоров

    private static final int LARGE_FILE_SIZE = getOptimalLargeFileSize()/1; // порог для больших файлов

    private final CheckValid checkValid;

    // класс для проверки схожести имен файлов
    private final FileNameSimilarityChecker fileNameSimilarityChecker;

    private final Map<Long, Set<File>> fileBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру
    Map<Long, Set<File>> getFileBySize() {return fileBySize;}

    private final FileGrouper fileGrouper;

    private final List<Set<File>> duplicates;
    List<Set<File>> getDuplicates() {return duplicates;}

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.fileNameSimilarityChecker = new FileNameSimilarityChecker();
        this.fileBySize = new HashMap<>();
        this.fileGrouper = new FileGrouper();
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

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        fileBySize.entrySet().forEach(entry -> {
            long sizeFile = entry.getKey();     // Размер файла
            Set<File> files = entry.getValue();  // Список файлов одинакового размера
            int numFiles = files.size();       // Количество файлов одинакового размера
            if (numFiles < 2) {  // Пропускаем списки файлов, которых меньше 2
                return;
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                boolean areFileNamesSimilar = fileNameSimilarityChecker.areFileNamesSimilar(files);
                if (
                        (numFiles > (NUM_PROCESSORS * 2)) ||
                        (numFiles > (NUM_PROCESSORS * 1.5) && sizeFile > LARGE_FILE_SIZE/10) ||
                        (numFiles > NUM_PROCESSORS && areFileNamesSimilar && sizeFile > LARGE_FILE_SIZE/10) ||
                        (sizeFile > LARGE_FILE_SIZE && areFileNamesSimilar)
                ) {
                    // Обработка файлов с использованием хеширования
                    //fileGrouper.groupByHeshParallel(files);
                    fileGrouper.groupByHesh(files);
                } else {
                    // Обработка файлов по содержимому
                    try {
                        //fileGrouper.groupByContentParallel(files);
                        fileGrouper.groupByContent(files);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, executor);
            futures.add(future);
        });


        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // Блокируем текущий поток до завершения всех задач

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    // Удаление списков по одному файлу - из filesByKey
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


    /* Метод для определения оптимального размера большого файла для многопоточного хеширования
     * Оптимальный размер файла - это 1/4 от максимальной п��мяти, деленной на количество процессоров
     */
    private static int getOptimalLargeFileSize() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Количество доступных процессоров
        return (int) (maxMemory / (availableProcessors * 4L));
    }

}