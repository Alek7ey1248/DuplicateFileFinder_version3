package hashNew;

import processing.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class FileDuplicateFinder {

    private static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();  // Количество процессоров
    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold()/428L; // Порог для больших файлов; // порог для больших файлов
    private final List<String> verifiedDirectories;  // Список всех абсолютных путей проверенных директорий

    private final CheckValid checkValid;  // класс для проверки валидности файлов и директорий
    private final FileNameSimilarityChecker fileNameSimilarityChecker; // класс для проверки схожести имен файлов
    private final FileGrouperNew fileGrouperNew;  // класс для группировки файлов по хешу и контенту

    private final Map<Long, Set<File>> filesBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру

    private List<Set<File>> duplicates; // Список групп дубликатов файлов - результат работы программы

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.verifiedDirectories = new ArrayList<>();
        this.fileNameSimilarityChecker = new FileNameSimilarityChecker();
        this.filesBySize = new ConcurrentHashMap<>();
        this.fileGrouperNew = new FileGrouperNew();
        this.duplicates = new ArrayList<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    */
    public void findDuplicates(String[] paths) throws IOException {

        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }

        processGroupFiles();  // Добавляем файлы в карту fileByKey из HashMap fileBySize

        // Получаем список групп дубликатов файлов, сортируем и выводим результат
        Printer.duplicatesByContent1(duplicates);
    }



    /* Ускореный метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {
        if (!checkValid.isValidDirectoryPath(path) || verifiedDirectories.contains(path)) {
            System.err.println("Невалидная директория или проверенная уже: " + path);
            return;
        }

        verifiedDirectories.add(path); // Добавляем проверенную директорию в список

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
            //CompletableFuture[] futures = new CompletableFuture[files.length]; // Массив для хранения CompletableFuture
            var futures = new CompletableFuture[files.length];

            for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
                final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной
                futures[i] = CompletableFuture.runAsync(() -> {
                        if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                            walkFileTree(file.getAbsolutePath());
                        } else {
                            if (!checkValid.isValidFile(file)) {  // Проверяем, что текущий файл является валидным
                                return;
                            }
                            // Добавляем файл в карту fileBySize по его размеру
                            long fileSize = file.length();
                            filesBySize.computeIfAbsent(fileSize, k -> ConcurrentHashMap.newKeySet()).add(file);
                        }
                }, executor);
            }

            // Ожидаем завершения всех CompletableFuture
            CompletableFuture.allOf(futures).join();
            executor.shutdown();
        }
    }


    /* Добавление файлов в карту fileByKey или filesByContent в зависимости от логики метода processGroupFiles
    */
    public void processGroupFiles() {
        Set<File> files = filesBySize.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet()); // Получаем все файлы из карты filesBySize

        // Группируем файлы по их размеру и контенту
        fileGrouperNew.groupByContent(files);
        duplicates = fileGrouperNew.getFilesByContent(); // Получаем сгруппированные файлы по их размеру и контенту
    }


    /* Метод для получения порога для больших файлов
        * @return порог для больших файлов
     */
    private static long getLargeFileThreshold() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Количество доступных процессоров
        return maxMemory / (availableProcessors * 4L); // Возвращаем порог
    }

    // Возвращает карту файлов, сгруппированных по размеру - гетер
    Map<Long, Set<File>> getFilesBySize() {return filesBySize;}

    // Возвращает список файлов, сгруппированных по одинаковому содержимому - гетер
    List<Set<File>> getDuplicates() {return duplicates;}


    public static void main(String[] args) {

        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder"};
        //String[] paths = {"/home/alek7ey"};           // 81,       сравн - очень долго,  хеш - 72 - 75 - 88 (ускоренный - 50)
        //String[] paths = {"/home/alek7ey/.local"};    // 1 - 1,6,  сравн - 1 - 1,5,      хеш - 0,9 - 1,6    (ускоренный - 0,9)
        //String[] paths = {"/home/alek7ey/.cache"};      // 6.6 - 9,  сравн - очень долго,  хеш - 6 - 14     (ускоренный - 4,4 - 6)
        //String[] paths = {"/home/alek7ey/snap"};      // 11,8,     сравн - 22 - 24,      хеш - 9,6 - 11,6   (ускоренный - 6)
        //String[] paths = {"/home/alek7ey/snap/flutter"}; // 1,5 - 1,7,  сравн - 1,5 - 1,7,  хеш - 1,5 - 1,7    (ускоренный - 1,5)
        //String[] paths = {"/home/alek7ey/snap/telegram-desktop"}; // 1,5 - 1,7,  сравн - 1,5 - 1,7,  хеш - 1,5 - 1,7    (ускоренный - 1,5)
        //String[] paths = {"/home/alek7ey/Android"};   // 3 - 4,    сравн - 2,5 - 3,9,    хеш - 2,4 - 2,6    (ускоренный - 2.4-2.6)
        String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы"}; // 24 - 27,  сравн - 24,  хеш - 33 (ускоренный - 29 - 32)
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder"};

        FileDuplicateFinder finder = new FileDuplicateFinder();
        try {
                finder.findDuplicates(paths); // Запускаем поиск дубликатов файлов
        } catch (IOException e) {
            System.err.println("Ошибка при поиске дубликатов: " + e.getMessage());
        }
    }

}