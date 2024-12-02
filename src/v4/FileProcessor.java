package v4;

import java.io.File;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


// Рекурсивное действие для обработки файлов в параллельном режиме

public class FileProcessor extends RecursiveAction {
    private final File[] files;
    private final FileHasher hasher;
    private static final int THRESHOLD = 10;

    public FileProcessor(File[] files, FileHasher hasher) {
        this.files = files;
        this.hasher = hasher;
    }

    @Override
    protected void compute() {
        if (files.length <= THRESHOLD) {
            processFilesSequentially();
        } else {
            // Определяем количество доступных процессоров
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            // Разбиваем файлы на подзадачи для параллельной обработки в зависимости от количества процессоров на компьютере и порога THRESHOLD (10) файлов в подзадаче (подзадача обрабатывается последовательно)
            List<List<File>> partitions = partitionFiles(Arrays.asList(files), availableProcessors);

            // Создаем подзадачи для параллельной обработки
            List<FileProcessor> tasks = new ArrayList<>();
            // Добавляем подзадачи в список
            for (List<File> partition : partitions) {
                tasks.add(new FileProcessor(partition.toArray(new File[0]), hasher));
            }
            invokeAll(tasks);
        }
    }

    // Обработка файлов последовательно (в одном потоке)
    private void processFilesSequentially() {
        for (File file : files) {
            if (file.isFile()) {
                hasher.addFile(file); // Добавляем файл в хэширование
            }
        }
    }

    private List<List<File>> partitionFiles(List<File> files, int partitions) {
        List<List<File>> result = new ArrayList<>();
        for (int i = 0; i < partitions; i++) {
            result.add(new ArrayList<>());
        }
        for (int i = 0; i < files.size(); i++) {
            result.get(i % partitions).add(files.get(i));
        }
        return result;
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        // Пример использования
        FileHasher fileHasher = new FileHasher();
        File directory = new File("/home/alek7ey/.cache");
        //File directory = new File("/home/alek7ey/snap");


        // Получаем список всех файлов в директории
        AllFilesDirectory allFilesDirectory = new AllFilesDirectory();
        File[] files = allFilesDirectory.findFilesInDirectory(directory).toArray(new File[0]);
        System.out.println("Файлы в директории: " + files.length);

        // Обрабатываем файлы
        if (files != null) {
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(new FileProcessor(files, fileHasher));
        }

        // HashMap в List<List<File>>
        List<List<File>> duplicateFiles = new ArrayList<>();
        for (Set<File> fileSet : fileHasher.getFileMap().values()) {
            duplicateFiles.add(new ArrayList<>(fileSet));
        }
        // Сортируем по размеру
        duplicateFiles.sort((o1, o2) -> o2.size() - o1.size());

        // Выводим результаты
        System.out.println("результаты");
        for (List<File> files1 : duplicateFiles) {
            if (files1.size() > 1) {
                System.out.println("------------------------------------------------------------");
                System.out.println("Дубликаты файлов типа " + files1.get(0).getName() + " размером " + files1.get(0).length() + " байт");
                for (File file : files1) {
                    System.out.println(file.getAbsolutePath());
                }
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Перевод наносекунд в миллисекунды
        System.out.println("Program execution time: " + duration + " ms");
    }
}