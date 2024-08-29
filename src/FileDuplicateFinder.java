import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

public class FileDuplicateFinder {
    // Пул потоков для параллельного выполнения задач
//    private final ExecutorService executor = Executors.newCachedThreadPool();

//    // Основной метод для поиска дубликатов файлов
//    public List<List<String>> findDuplicates(String[] paths) throws IOException {
//        // ConcurrentHashMap для хранения файлов, сгруппированных по размеру
//        Map<Long, List<Path>> filesBySize = new ConcurrentHashMap<>();
//        // Список для хранения Future объектов
//        // Future<?> — это интерфейс из пакета java.util.concurrent, который представляет собой результат асинхронной вычислительной задачи. Он используется для получения результата выполнения задачи, которая была отправлена на выполнение в пул потоков.
//        List<Future<?>> futures = new ArrayList<>();
//
//        // Рекурсивный обход директорий с использованием параллельных потоков
//        for (String path : paths) {
//            // Добавляем новую задачу в пул потоков для асинхронного выполнения
//            // executor.submit() принимает задачу в виде лямбда-выражения и возвращает объект Future
//            // Лямбда-выражение () -> walkFileTree(path, filesBySize) представляет собой задачу, которая будет выполнена в отдельном потоке
//            // В данном случае задача заключается в рекурсивном обходе файловой системы, начиная с указанного пути (path)
//            // Метод (ниже в коде) walkFileTree(path, filesBySize) выполняет обход файловой системы и группирует файлы по их размеру в Map filesBySize
//            // Объект Future, возвращаемый методом submit(), добавляется в список futures для последующего отслеживания завершения всех задач
//            futures.add(executor.submit(() -> walkFileTree(path, filesBySize)));
//        }
//
//        // Ожидание завершения всех задач
//        waitForCompletion(futures);
//
//        // Параллельная обработка файлов одинакового размера
//        List<List<String>> duplicates = processSameSizeFiles(filesBySize);
//
//        // Завершение работы пула потоков
//        executor.shutdown();
//        return duplicates;
//    }


    // Методы для рекурсивного обхода директорий
    // -----------------------------------------------------------
    // Основной Метод walkFileTree выполняет рекурсивный обход файловой системы,
    // начиная с указанного пути (path). Для каждого найденного файла вызывается
    // метод processFile, который группирует файлы по их размеру в карту filesBySize.
    // Если возникает ошибка ввода-вывода, она обрабатывается и выводится в консоль.
    public void walkFileTree(String path, Map<Long, List<Path>> filesBySize) {
        try {
            Files.walkFileTree(Paths.get(path), createFileVisitor(filesBySize));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // вспомогательный метод для создания экземпляра SimpleFileVisitor
    // SimpleFileVisitor<Path> — это стандартный класс из пакета java.nio.file.
    // Он используется для обхода файловой системы.
    // Этот класс предоставляет простую реализацию интерфейса FileVisitor,
    // который можно расширить для выполнения различных операций при обходе файловой системы.
    private SimpleFileVisitor<Path> createFileVisitor(Map<Long, List<Path>> filesBySize) {
        // Возвращает новый экземпляр анонимного класса, который расширяет SimpleFileVisitor<Path>
        return new SimpleFileVisitor<Path>() {
            // Переопределение метода visitFile для обработки каждого файла
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Вызов метода processFile для обработки файла и добавления его в карту filesBySize
                processFile(file, filesBySize);
                // Возвращает CONTINUE, чтобы продолжить обход файловой системы
                return FileVisitResult.CONTINUE;
            }
        };
    }

    // вспомогательный метод для обработки файла
    private void processFile(Path file, Map<Long, List<Path>> filesBySize) throws IOException {
        // Проверка, доступен ли файл для чтения
        if (Files.isReadable(file)) {
            // Получение размера файла
            long size = Files.size(file);
            // Группировка файлов по размеру(в данном случае файла в соответств список)
            // Метод computeIfAbsent проверяет, существует ли в карте filesBySize ключ size.
            // Если ключ size уже существует, метод возвращает соответствующее значение (список путей файлов).
            // Если ключ size не существует, метод создает новый список (Collections.synchronizedList(new ArrayList<>())) и добавляет его в карту с ключом size.
            // Возвращаемое значение метода computeIfAbsent — это список путей файлов для данного размера (size).
            // Метод add(file) добавляет текущий файл (file) в список путей файлов для данного размера (size).
            filesBySize.computeIfAbsent(size, k -> Collections.synchronizedList(new ArrayList<>())).add(file);
        }
    }
   // -----------------------------------------------------------


    // Метод для ожидания завершения всех задач
//    private void waitForCompletion(List<Future<?>> futures) {
//        for (Future<?> future : futures) {
//            try {
//                future.get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    // Метод для обработки файлов одинакового размера
//    private List<List<String>> processSameSizeFiles(Map<Long, List<Path>> filesBySize) {
//        List<List<String>> duplicates = new ArrayList<>();
//        FileComparator comparator = new FileComparator();
//
//        filesBySize.values().parallelStream().forEach(sameSizeFiles -> {
//            if (sameSizeFiles.size() > 1) {
//                Map<String, List<String>> contentGroups = new ConcurrentHashMap<>();
//                for (int i = 0; i < sameSizeFiles.size(); i++) {
//                    for (int j = i + 1; j < sameSizeFiles.size(); j++) {
//                        try {
//                            // Сравнение файлов побайтно
//                            if (comparator.areFilesEqual(sameSizeFiles.get(i), sameSizeFiles.get(j))) {
//                                contentGroups.computeIfAbsent(sameSizeFiles.get(i).toString(), k -> Collections.synchronizedList(new ArrayList<>())).add(sameSizeFiles.get(j).toString());
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                duplicates.addAll(contentGroups.values());
//            }
//        });
//
//        return duplicates;
//    }


    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();

        // Создание экземпляра класса FileDuplicateFinder
        FileDuplicateFinder finder = new FileDuplicateFinder();

        Map<Long, List<Path>> filesBySize = new HashMap<>();

        finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder", filesBySize);
        //finder.walkFileTree("/home/alek7ey/Рабочий стол", filesBySize);
        //finder.walkFileTree("/home/alek7ey", filesBySize);
        //finder.walkFileTree("/home", filesBySize);


        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);

        int countFiles = 0;
        for (Map.Entry<Long, List<Path>> entry : filesBySize.entrySet()) {
            System.out.println("");
            System.out.println("размер: " + entry.getKey() + " ------------------");
            for (Path path : entry.getValue()) {
                countFiles++;
                System.out.println(path);
            }
        }

        System.out.println();
        System.out.println("Время выполнения: " + duration + " милисекунд       " + (long)duration/1000.0 + " секунд");
        System.out.println();
        System.out.println("Количество файлов: " + countFiles);
    }

}