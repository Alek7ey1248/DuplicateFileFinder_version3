package processing;

import java.io.File;
import java.io.IOException;
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

    private final long BUFFER_SIZE = getOptimalBufferSize(); // порог для больших файлов

    //private final List<Set<File>> filesByContent;  // Карта для хранения сгруппированных файлов по их хешам
    //public List<Set<File>> getFilesByContent() {
    //    return filesByContent;
    //}

    //private static final long BUFFER_SIZE = 8192L; // Размер буфера 8 КБ

    // Конструктор
    public FileGrouperNew() {
    }

    /* Основной рекурсивный метод
    * Принимает множество файлов ОДИНАКОВЫХ РАЗМЕРОВ и сравнивая постепенно куски хешей,
    * группирует их по хешам в карту filesByContent.
    */
    public List<Set<File>> groupByContent(Set<File> files) {

        long size = files.iterator().next().length(); // Получаем размер первого файла, азначит и остальных

        List<Set<File>> filesByContent = new ArrayList<>(); // Инициализация списка для хранения групп файлов

        long bufferSize = getBufferSize(size); // Получаем размер буфера для чтения файла
        //System.out.println("Размер буфера: " + bufferSize);

        //  создаем новую очередь для хранения групп файлов, помещаем в нее files
        Queue<Set<File>> fileGroupsQueue = new LinkedList<>();
        Queue<Set<File>> fileGroupsQueueCurrent = new LinkedList<>();
        fileGroupsQueueCurrent.add(files);

        long offset = 0L; // Начальное смещение для чтения файла

        while(offset < size) {

            fileGroupsQueue.addAll(fileGroupsQueueCurrent); // добавляем в очередь fileGroupsQueueCurrent
            fileGroupsQueueCurrent.clear(); // очищаем fileGroupsQueueCurrent

            // пока очередь не пуста, извлекаем из нее группу файлов
            while (!fileGroupsQueue.isEmpty()) {
                // извлекаем и удаляем группу файлов из очереди
                Set<File> currentGroup = fileGroupsQueue.poll();
                // вычисляем их хеши и группируем по хешам типа FileKeyHashNew
                // методом groupFiles в карту currentFilesByContent
                Map<FileKeyHashNew, Set<File>> currentFilesByContent = groupFiles(currentGroup, offset, bufferSize);
                // получившуюся карту currentFilesByContent добавляем в fileGroupsQueueCurrent
                for (Set<File> fileSet : currentFilesByContent.values()) {
                    // если в Set<File> только 1 файл, то пропускаем его
                    if (fileSet.size() > 1) {
                        // Set<File> добавляем в очередь
                        fileGroupsQueueCurrent.add(fileSet);
                    }
                }
            }
            offset += bufferSize; // увеличиваем смещение на размер буфера
        }

        // после завершения обработки всех файлов, добавляем оставшиеся группы в список filesByContent
        while (!fileGroupsQueueCurrent.isEmpty()) {
            // извлекаем и удаляем группу файлов из очереди
            Set<File> group = fileGroupsQueueCurrent.poll();
            filesByContent.add(group); // добавляем в список filesByContent
        }
        return filesByContent;
    }


    /* Метод вычисляет хеш для каждого файла в переданном множестве файлов,
     * начиная с указанного смещения offset, и группирует файлы по их хешам.
     * Возвращает карту, в которой ключ - хеш файла, значение - множество файлов с этим хешем.
     */
    private Map<FileKeyHashNew, Set<File>> groupFiles(Set<File> files, long offset, long bufferSize) {
        Map<FileKeyHashNew, Set<File>> newFilesByContent = new ConcurrentHashMap<>();
        for (File file : files) {
            //System.out.println(file.getName());
            try {
                // Вычисляем хеш для текущего файла, начиная с указанного смещения
                FileKeyHashNew fileHash = new FileKeyHashNew(file, offset, bufferSize);
                newFilesByContent.computeIfAbsent(fileHash, k -> ConcurrentHashMap.newKeySet()).add(file);
            } catch (Exception e) {
                System.out.println("Ошибка при чтении файла " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        return newFilesByContent;
    }


    /*  метод вычисления размера буфера по размеру файлов
    */
//    private long getBufferSize(long fileSize) {
//        if (fileSize < BUFFER_SIZE) {
//            return fileSize; // 512 байт
//        } else {
//            return BUFFER_SIZE;
//        }
//    }
    private long getBufferSize(long fileSize) {
        if (fileSize < 8192L) {
            return 1024L; // 512 байт
        } else if (fileSize >= 8192L && fileSize < BUFFER_SIZE) {
            return 8192L; // 8 КБ
        } else {  // если размер файла больше BUFFER_SIZE
            return BUFFER_SIZE; // 1 МБ
        }
    }

    /* Метод для получения порога для больших файлов
     * @return порог для больших файлов
     */
    /* Метод для определения оптимального размера буфера на основе доступной памяти
     * Метод для определения оптимального размера буфера на основе доступной памяти и количества процессоров
     * Оптимальный размер буфера - это 1/8 от максимальной памяти, деленной на количество процессоров
     */
    private long getOptimalBufferSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        long bsLong = maxMemory / (availableProcessors * 8192L);
        int bs = (int)bsLong ;

        int minBufferSize = 1024 * availableProcessors / 2 ;
        return Math.max(bs, minBufferSize);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
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
