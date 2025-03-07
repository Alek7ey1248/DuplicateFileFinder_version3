package V4;

import V4.CheckValid;
import V4.FileKeyHash;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();  // Количество процессоров

    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold()/428L; // Порог для больших файлов; // порог для больших файлов

    private final CheckValid checkValid;

    ConcurrentSkipListMap<Long, CopyOnWriteArrayList<Set<File>>> fileByContent;  // Карта файлов по содержимому (ключ - хеш содержимого, значение - список групп файлов) - отсортированная по ключу и потокобезопасная

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
    public void walkFileTree(String path) {
        if (!checkValid.isValidDirectoryPath(path)) {
            //System.err.println("Невалидная директория: " + path);
            return;
        }

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
            //CompletableFuture[] futures = new CompletableFuture[files.length]; // Массив для хранения CompletableFuture
            var futures = new CompletableFuture[files.length];

            for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
                final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной
                futures[i] = CompletableFuture.runAsync(() -> {
//                    try {
                        if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                            walkFileTree(file.getAbsolutePath());
                        } else {
                            if (!checkValid.isValidFile(file)) {  // Проверяем, что текущий файл является валидным
                                return;
                            }
                            long fileSize = file.length(); // Размер файла
                            if(fileByContent.containsKey(fileSize)) {
                                synchronized (fileByContent.get(fileSize)) { // Синхронизируем доступ к карте fileByContent
                                    // Добавляем файл в карту fileByContent
                                    System.out.println("   Есть файлы с таким же размером как у файла - " + file.getAbsolutePath());
                                    List<Set<File>> fileList = new CopyOnWriteArrayList<>(fileByContent.get(fileSize)); // Получаем список групп файлов с таким же размером
                                    for(Set<File> fileSet : fileList) { // Перебираем все группы файлов
                                        System.out.println(" Будем сравнивать файл - " + file.getAbsolutePath() + " с файлом - " + fileSet.iterator().next().getAbsolutePath());
                                        try {
                                            if(FileComparator.areFilesEqual(file, fileSet.iterator().next())) { // Если файл одинаков по содержимому c первым файлом в группе
                                                fileSet.add(file);                                              // Добавляем файл в группу
                                                System.out.println("  Файл - " + file.getAbsolutePath() + " добавлен в группу");
                                                return;                                                        // Выходим из метода (файл добавлен)
                                            }
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    // Если файл не добавлен ни в одну группу, создаем новую группу
                                    Set<File> newGroup = new HashSet<>();
                                    newGroup.add(file);
                                    fileList.add(newGroup);
                                }
                            } else {
                                CopyOnWriteArrayList<Set<File>> fileList = new CopyOnWriteArrayList<>(); // Создаем список групп файлов
                                synchronized (fileList) {
                                    Set<File> newGroup = new HashSet<>();         // Создаем новую группу
                                    newGroup.add(file);                            // Добавляем файл в группу
                                    fileList.add(newGroup);                        // Добавляем группу в список
                                    fileByContent.put(fileSize, fileList);         // Добавляем список в карт
                                }
                            }
                        }
                }, executor);
            }
            // Ожидаем завершения всех CompletableFuture
            CompletableFuture.allOf(futures).join();
            executor.shutdown();

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
    }


    // Метод вкладывает файл в карту fileByContent по содержимому
    public void processGroupFiles(File file) throws IOException {

        long fileSize = file.length(); // Размер файла

        if(!fileByContent.containsKey(fileSize)) { // Если в карте нет файлов с таким же размером(ключем) как у файла

            CopyOnWriteArrayList<Set<File>> fileList = new CopyOnWriteArrayList<>(); // Создаем список групп файлов
            Set<File> newGroup = new HashSet<>();         // Создаем новую группу
            newGroup.add(file);                            // Добавляем файл в группу
            fileList.add(newGroup);                        // Добавляем группу в список
            fileByContent.put(fileSize, fileList);         // Добавляем список в карт

        } else {                      // Если в карте есть файлы с таким же размером
                System.out.println("   Есть файлы с таким же размером как у файла - " + file.getAbsolutePath());
                List<Set<File>> fileList = new CopyOnWriteArrayList<>(fileByContent.get(fileSize)); // Получаем список групп файлов с таким же размером
                for(Set<File> files : fileList) { // Перебираем все группы файлов
                    System.out.println(" Будем сравнивать файл - " + file.getAbsolutePath() + " с файлом - " + files.iterator().next().getAbsolutePath());
                    if(!files.isEmpty() && FileComparator.areFilesEqual(file, files.iterator().next())) { // Если файл одинаков по содержимому c первым файлом в группе
                        files.add(file);                                              // Добавляем файл в группу
                        System.out.println("  Файл - " + file.getAbsolutePath() + " добавлен в группу");
                        return;                                                        // Выходим из метода (файл добавлен)
                    }
                }
                // Если файл не добавлен ни в одну группу, создаем новую группу
                Set<File> newGroup = new HashSet<>();
                newGroup.add(file);
                fileList.add(newGroup);
    }   }


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
        System.out.println("-  LARGE_FILE_THRESHOLD = " + LARGE_FILE_THRESHOLD);

    }



    /* Метод для получения порога для больших файлов
        * @return порог для больших файлов
     */
    private static long getLargeFileThreshold() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Количество доступных процессоров
        return maxMemory / (availableProcessors * 4L); // Возвращаем порог
    }

}