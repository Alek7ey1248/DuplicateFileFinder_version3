import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FileDuplicateFinder {

    // Основной метод для поиска дубликатов файлов
    public List<List<String>> findDuplicates(String path) throws IOException {
        // HashMap для хранения файлов, сгруппированных по размеру
        Map<Long, List<Path>> filesBySize = new HashMap<>();

        // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
        walkFileTree(path, filesBySize);

        // Параллельная обработка файлов одинакового размера для поиска дубликатов
        List<List<String>> duplicates = findDuplicateGroups(filesBySize);

        return duplicates;
    }


    //*  * Находитгруппы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.
    // *
    // * @param filesBySize — карта, где ключом является размер файла, а значением — список путей к файлам этого размера.
    // * @return список групп повторяющихся файлов
    // * @throws IOException при возникновении ошибки ввода-вывода*/
    public List<List<String>> findDuplicateGroups(Map<Long, List<Path>> filesBySize) throws IOException {
        // Результат - Список для хранения дубликатов файлов
        List<List<String>> duplicates = new ArrayList<>();
        FileComparator comparator = new FileComparator();

        // перебираю ключи (размеры файлов)
        for (Long size : filesBySize.keySet()) {
            // Получаю список файлов для текущего размера
            List<Path> files = filesBySize.get(size);
            // метод findDuplicatesInSameSizeFiles из списка files делает список групп дубликатов файлов (метод рекурсивный)
            findDuplicatesInSameSizeFiles(files, duplicates, comparator);
            //duplicates.addAll(groups);
        }

        // Возвращаем список групп дубликатов
        return duplicates;
    }


    // Вспомогательный метод для метода findDuplicateGroups
    // Аргумент: List<Path> files = filesBySize.get(size); -
    // список файлов одного размера, взятый из HashMap - filesBySize
    // Возвращает: список групп дубликатов файлов (побайтно одинаковых файлов)
//    public List<List<String>> findDuplicatesInSameSizeFiles(List<Path> files, FileComparator comparator) throws IOException {
//        // Результат - Список для хранения групп дубликатов файлов
//        List<List<String>> groups = new ArrayList<>();
//        // Множество для хранения уже обработанных файлов
//        Set<Path> processedFiles = new HashSet<>();
//
//        // Перебираем все файлы в списке files
//        for (Path file : files) {
//            // Если файл уже обработан, переходим к следующему файлу
//            if (processedFiles.contains(file)) {
//                continue;
//            }
//
//            // Создаем новую группу дубликатов
//            List<String> group = new ArrayList<>();
//            // Добавляем текущий файл в группу
//            group.add(file.toString());
//
//            // Перебираем оставшиеся файлы в списке files
//            for (Path anotherFile : files) {
//                // Если файл уже обработан или это тот же файл, переходим к следующему файлу
//                if (processedFiles.contains(anotherFile) || file.equals(anotherFile)) {
//                    continue;
//                }
//
//                // Сравниваем текущий файл с другим файлом
//                if (comparator.areFilesEqual(file, anotherFile)) {
//                    // Если файлы равны, добавляем другой файл в группу
//                    group.add(anotherFile.toString());
//                    // Добавляем другой файл в множество обработанных файлов
//                    processedFiles.add(anotherFile);
//                }
//            }
//
//            // Добавляем группу дубликатов в список групп
//            groups.add(group);
//            // Добавляем текущий файл в множество обработанных файлов
//            processedFiles.add(file);
//        }
//
//        // Возвращаем список групп дубликатов
//        return groups;
//    }

    /**
     * Находит дубликаты файлов в списке файлов одинакового размера.
     *      *
     *      * @param files — список путей к файлам одинакового размера.
     *      * @param дублирует список для хранения групп повторяющихся файлов.
     *      * @param comparator — компаратор для сравнения файлов
     *      * @throws IOException при возникновении ошибки ввода-вывода
     */
    public void findDuplicatesInSameSizeFiles(List<Path> files, List<List<String>> duplicates, FileComparator comparator) throws IOException {
        if (files.isEmpty()) {
            return;
        }

        // Преобразуем спис��к файлов в очередь
        Queue<Path> fileQueue = new ArrayDeque<>(files);

        while (!fileQueue.isEmpty()) {
            // Извлекаем первый файл из очереди
            Path file = fileQueue.poll();
            List<String> group = new ArrayList<>();
            group.add(file.toString());

            // Временный список для хранения дубликатов
            List<Path> toRemove = new ArrayList<>();

            for (Path anotherFile : fileQueue) {
                if (file.equals(anotherFile)) {
                    continue;
                }

                if (comparator.areFilesEqual(file, anotherFile)) {
                    group.add(anotherFile.toString());
                    toRemove.add(anotherFile);
                }
            }

            // Удаляем найденные дубликаты из очереди
            fileQueue.removeAll(toRemove);
            // Добавляем группу дубликатов в список дубликатов (если группа содержит более одного файла)
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }
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
        CheckValid checkValid = new CheckValid();
        // Проверка валидности файла
        if (checkValid.isValidFile(file.toFile())) {
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