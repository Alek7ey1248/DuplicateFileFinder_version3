package V12_ready;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

public class FileGrouper {
    // Хранит группы файлов по хешу
    private final Map<FileKeyHash, Set<File>> filesByKey;
    public Map<FileKeyHash, Set<File>> getFilesByKey() {
        return filesByKey;
    }

    // Хранит группы файлов по содержимому
//    private final Map<String, Set<File>> filesByContent;
//    public Map<String, Set<File>> getFilesByContent() {
//        return filesByContent;
//    }
    private final List<Set<File>> filesByContent;
    public List<Set<File>> getFilesByContent() {
        return filesByContent;
    }

    // Конструктор
    public FileGrouper() {
        this.filesByKey = new ConcurrentHashMap<>();
//        this.filesByContent = new ConcurrentHashMap<>();
        this.filesByContent = new CopyOnWriteArrayList<>();
    }


    // Групировка файлов по хешу и добавление в filesByKey - группы дубликатов
    void groupByHesh(Set<File> files) throws IOException, NoSuchAlgorithmException {
        System.out.println(" вычислениеа хеша списка файдов - " + Arrays.toString(files.toArray()));

        files.forEach(file -> {
                try {
                    FileKeyHash key = new FileKeyHash(file);
                    //System.out.println(" вычислениеа хеш - " + file.getAbsolutePath());
                    filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
        });
    }


    // Групировка файлов по хешу и добавление в filesByKey- группы дубликатов
    // (Ускоренный потоками)
    void groupByHeshParallel(Set<File> files) throws IOException, NoSuchAlgorithmException {
        System.out.println(" вычислениеа хеша списка файлов - " + Arrays.toString(files.toArray()));

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальныепотоки
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        files.forEach(file -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    FileKeyHash key = new FileKeyHash(file);
                    filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
                    e.printStackTrace();
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


    // Групировка файлов по содержимому методом сравнения файлов
    // и добавление в filesByContent - группы дубликатов
    void groupByContent(Set<File> files) {
        while (files.size() > 1) {  // Пока в списке файлов одинакового размера есть хотя бы два файла
            Iterator<File> iterator = files.iterator();  // Извлекаем первый файл из списка
            File file = iterator.next();
            iterator.remove();   // Удаляем первый файл из списка
            System.out.println(" Поиск дубликатов файла: " + file.getAbsolutePath());

            Set<File> group = new HashSet<>();
            //Set<File> group = ConcurrentHashMap.newKeySet();
            group.add(file);   // Добавляем первый файл в группу дубликатов
            //String key = file.getAbsolutePath();  // Ключ для группы дубликатов по содержимому - путь к файлу
            //Set<File> toRemove = ConcurrentHashMap.newKeySet();   // Список для удаления дубликатов из files
            Set<File> toRemove = new HashSet<>();

            for (File anotherFile : files) {  // Перебираем оставшиеся файлы в списке
//                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
//                    continue;
//                }
                try {
                    if (FileComparator.areFilesEqual(file, anotherFile)) {
                        group.add(anotherFile);  // Добавляем путь к дубликату в группу дубликатов
                        toRemove.add(anotherFile);  // Добавляем путь к дубликату в список для удаления
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при сравнении файлов: " + file.getAbsolutePath() + " и " + anotherFile.getAbsolutePath());
                }
            }

            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера

            if (group.size() > 1) { // Добавляем группу дубликатов в список дубликатов по ключу (если группа содержит более одного файла)
//                filesByContent.put(key, group);
                filesByContent.add(group);
            }
        }
    }


    // Групировка файлов по содержимому методом сравнения файлов
    // и добавление в filesByContent - группы дубликатов
    // (Ускоренный потоками)
    void groupByContentParallel(Set<File> files) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки

        while (files.size() > 1) {  // Пока в списке файлов одинакового размера есть хотя бы два файла
            Iterator<File> iterator = files.iterator();  // Извлекаем первый файл из списка
            File file = iterator.next();
            iterator.remove();   // Удаляем первый файл из списка
            System.out.println(" Поиск дубликатов файла: " + file.getAbsolutePath());

            //Set<File> group = new HashSet<>();
            Set<File> group = ConcurrentHashMap.newKeySet();
            group.add(file);   // Добавляем первый файл в группу дубликатов
            //String key = file.getAbsolutePath();  // Ключ для группы дубликатов по содержимому - путь к файлу
            Set<File> toRemove = ConcurrentHashMap.newKeySet();   // Список для удаления дубликатов из files

            List<CompletableFuture<Void>> fileComparisons = new ArrayList<>(); // Список для хранения CompletableFuture

            for (File anotherFile : files) {  // Перебираем оставшиеся файлы в списке
//                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
//                    continue;
//                }
                // Отправляем задачу на сравнение файлов в пул потоков
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    if (FileComparator.areFilesEqual(file, anotherFile)) {
                        group.add(anotherFile);  // Добавляем путь к дубликату в группу дубликатов
                        toRemove.add(anotherFile);  // Добавляем путь к дубликату в список для удаления
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при сравнении файлов: " + file.getAbsolutePath() + " и " + anotherFile.getAbsolutePath());
                }
                }, executor);
                fileComparisons.add(future); // Добавляем CompletableFuture в список
            }

            // Ждем завершения всех задач
            CompletableFuture<Void> allOf = CompletableFuture.allOf(fileComparisons.toArray(new CompletableFuture[0]));
            allOf.join(); // Блокируем до завершения всех задач

            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера

            if (group.size() > 1) { // Добавляем группу дубликатов в список дубликатов по ключу (если группа содержит более одного файла)
//                filesByContent.put(key, group);
                filesByContent.add(group);
            }
        }
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

    public static void main(String[] args) throws IOException {
        FileGrouper fileGrouper = new FileGrouper();
        Set<File> files = new HashSet<>();
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена)"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена) (Копия)"));

        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия 3)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия 2)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"));

//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 2)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 3)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 4)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 5)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 6)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 7)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 8)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 9)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 10)"));


//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат1.zip"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат.zip"));

        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/largeFile.txt"));

        //files.add(new File("/home/alek7ey/Рабочий стол/largeFile (Копия).txt"));

        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4"));

        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (середина изменена).mp4"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/BiglargeFile.txt"));

        System.out.println("размер первого файла: " + files.iterator().next().length());
        long startTime = System.currentTimeMillis();

        //fileGrouper.groupByHesh(files);
        //fileGrouper.groupByHeshParallel(files);
        //fileGrouper.groupByContent(files);
        fileGrouper.groupByContentParallel(files);

        //Thread.currentThread().interrupt();

        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директории --- " + duration + " ms       ");
    }

}
