package processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/* Класс для группировки файлов по содержимому.
 * Параметр files - список файлов, одинакового размера.
 * Считывает по буферно хеш каждого файла из списка files,
 * сравнивает хеши между собой и если они равны, то добавляет в списки,
 * удаляет списки в которых в только один файл,
 * остальные списки отправляются рекурсивно в метод groupByContent,
 * до тех пор пока не будут прочитаны все файлы до конца.
 */
public class FileGrouperNew {

    private static int i = 0;  // счетчик проходов

    //private final List<Set<File>> filesByContent;  // Карта для хранения сгруппированных файлов по их хешам
    //public List<Set<File>> getFilesByContent() {
    //    return filesByContent;
    //}

    private static final long BUFFER_SIZE = 8192L; // Размер буфера 8 КБ

    // Конструктор
    public FileGrouperNew() {
        //this.filesByContent = new ArrayList<>(); // Инициализация списка для хранения групп файлов
    }


    /* Основной рекурсивный метод
    * Принимает множество файлов ОДИНАКОВЫХ РАЗМЕРОВ и сравнивая постепенно куски хешей,
    * группирует их по хешам в карту filesByContent.
    */
    public List<Set<File>> groupByContent(Set<File> files) {
        List<Set<File>> filesByContent = new ArrayList<>(); // Инициализация списка для хранения групп файлов

        long size = files.iterator().next().length(); // Получаем размер первого файла, азначит и остальных
        //  создаем новую очередь для хранения групп файлов, помещаем в нее files
        Queue<Set<File>> fileGroupsQueue = new LinkedList<>();
        Queue<Set<File>> fileGroupsQueueCurrent = new LinkedList<>();
        fileGroupsQueueCurrent.add(files);

        for(long offset = 0; (offset + 8192L) <= size; offset += 8192L) {

            fileGroupsQueue.addAll(fileGroupsQueueCurrent); // добавляем в очередь fileGroupsQueueCurrent
            fileGroupsQueueCurrent.clear(); // очищаем fileGroupsQueueCurrent
            // пока очередь не пуста, извлекаем из нее группу файлов
            while (!fileGroupsQueue.isEmpty()) {
                // извлекаем и удаляем группу файлов из очереди
                Set<File> currentGroup = fileGroupsQueue.poll();
                // вычисляем их хеши и группируем по хешам типа FileKeyHashNew
                // методом groupFiles в карту currentFilesByContent
                Map<FileKeyHashNew, Set<File>> currentFilesByContent = groupFiles(currentGroup, offset);
                // получившуюся карту currentFilesByContent добавляем в fileGroupsQueueCurrent
                for (Set<File> fileSet : currentFilesByContent.values()) {
                    // если в Set<File> только 1 файл, то пропускаем его
                    if (fileSet.size() > 1) {
                        // Set<File> добавляем в очередь
                        fileGroupsQueueCurrent.add(fileSet);
                    }
                }
            }
        }
        // после завершения обработки всех файлов, добавляем оставшиеся группы в список filesByContent
        while (!fileGroupsQueueCurrent.isEmpty()) {

            // извлекаем и удаляем группу файлов из очереди
            Set<File> group = fileGroupsQueue.poll();
            System.out.println("Добавляем в результат группу файлов: --------------");
            for (File file : group) {
                System.out.println(file.getName());
            }
            filesByContent.add(group); // добавляем в список filesByContent
        }
        return filesByContent;
    }


    /* Метод вычисляет хеш для каждого файла в переданном множестве файлов,
     * начиная с указанного смещения offset, и группирует файлы по их хешам.
     * Возвращает карту, в которой ключ - хеш файла, значение - множество файлов с этим хешем.
     */
    private Map<FileKeyHashNew, Set<File>> groupFiles(Set<File> files, long offset) {
        Map<FileKeyHashNew, Set<File>> newFilesByContent = new ConcurrentHashMap<>();
        for (File file : files) {
            //System.out.println(file.getName());
            try {
                // Вычисляем хеш для текущего файла, начиная с указанного смещения
                FileKeyHashNew fileHash = new FileKeyHashNew(file, offset, BUFFER_SIZE);
                newFilesByContent.computeIfAbsent(fileHash, k -> ConcurrentHashMap.newKeySet()).add(file);
            } catch (Exception e) {
                System.out.println("Ошибка при чтении файла " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        return newFilesByContent;
    }



    public static void main(String[] args) throws IOException {
        FileGrouperNew fileGrouper = new FileGrouperNew();
        Set<File> files = new HashSet<>();
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена)"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена) (Копия)"));

//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (Копия 2)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (копия)"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия 3)"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия 2)"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"));


        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат1.zip"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат.zip"));

        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/largeFile.txt"));

        //files.add(new File("/home/alek7ey/Рабочий стол/largeFile (Копия).txt"));

        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4"));

        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (середина изменена).mp4"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/BiglargeFile.txt"));

//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test01.txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test02.txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test03.txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test04.txt"));
//
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11.txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11 (копия).txt"));
//
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21.txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (другая копия).txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (копия).txt"));
//
//
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (другая копия).txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (3-я копия).txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (копия).txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31.txt"));

//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test1одинтакой.txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test2одинтакой.txt"));
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test3одинтакой.txt"));


        //System.out.println("размер первого файла: " + files.iterator().next().length());
        long startTime = System.currentTimeMillis();

        File dir = new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder");
        File[] filesArray = dir.listFiles();
        files = new HashSet<>(Arrays.asList(filesArray));

        for (Set<File> res : fileGrouper.groupByContent(files)) {
            System.out.println(" Группа файлов: --------------------");
            for (File file : res) {
                System.out.println(file.getAbsolutePath());
            }
        }


        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директории --- " + duration + " ms       ");
    }

}
