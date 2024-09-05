import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    // Основной метод для поиска дубликатов файлов
    public List<List<String>> findDuplicates(String path) throws IOException {
        // HashMap для хранения файлов, сгруппированных по размеру
        Map<Long, List<Path>> filesBySize = new HashMap<>();

        // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
        walkFileTree(path, filesBySize);

        // Параллельная обработка файлов одинакового размера для поиска дубликатов
        List<List<String>> duplicates = processSameSizeFiles(filesBySize);

        return duplicates;
    }


    // Метод для обработки файлов одинакового размера
    public List<List<String>> processSameSizeFiles(Map<Long, List<Path>> filesBySize) {
        // Результат - Список для хранения дубликатов файлов
        List<List<String>> duplicates = new ArrayList<>();
        FileComparator comparator = new FileComparator();
        // contentGroups используется для группировки файлов по их хэшированному содержимому.
        Map<String, List<String>> contentGroups = new ConcurrentHashMap<>();

        // Преобразует коллекцию списков файлов (сгруппированных по размеру) в параллельный поток
        // и обрабатывает каждый список (sameSizeFiles) одновременно.
        filesBySize.values().parallelStream().forEach(sameSizeFiles -> {
            // Обрабатываем каждый файл в списке sameSizeFiles
            sameSizeFiles.parallelStream().forEach(file -> {
                try {
                    // Получаем хэш содержимого файла
                    String fileHash = comparator.computeHash(file);
                    // Добавляем файл в соответствующую группу по хэшу содержимого
                    contentGroups.computeIfAbsent(fileHash, k -> Collections.synchronizedList(new ArrayList<>())).add(file.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        // Добавляем все группы, содержащие более одного файла, в список дубликатов
        contentGroups.values().forEach(group -> {
            if (group.size() > 1) {
                duplicates.add(group);
            }
        });

        // Возвращаем список групп дубликатов
        return duplicates;
    }

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