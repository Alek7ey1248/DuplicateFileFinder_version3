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

    private Map<FileKeyHashNew, Set<File>> filesByContent;  // Карта для хранения сгруппированных файлов по их хешам
    public Map<FileKeyHashNew, Set<File>> getFilesByContent() {
        return filesByContent;
    }

    private static final int BUFFER_SIZE = 8192; // Размер буфера 8 КБ

    // Конструктор
    public FileGrouperNew() {
        this.filesByContent = new ConcurrentHashMap<>();
    }


    // Основной рекурсивный метод
//    public void groupByContent(Set<File> files, FileKeyHashNew key, long offset) {
//        // filesByContent группировка файлов по хешам их первых буферов
//        filesByContent = groupFiles(files,offset);
//
//        // Используем стек для хранения групп файлов и их параметров
//        Stack<Map.Entry<FileKeyHashNew, Set<File>>> stack = new Stack<>();
//        stack.addAll(filesByContent.entrySet());
//        while (!stack.isEmpty()) {
//            Map.Entry<FileKeyHashNew, Set<File>> entry = stack.pop(); // Извлекаем элемент из стека
//            Set<File> fileGroup = entry.getValue(); // Получаем набор файлов для текущего хеша
//            // Если в группе меньше двух файлов, то удаляем группу из карты filesByContent
//            if (fileGroup.size() < 2) {
//                //System.out.println("---- удаляем- filesByContent.remove(entry.getKey());");
//                filesByContent.remove(entry.getKey());
//                continue;
//            }
//
//            if (fileGroup.size() == 3) {
//                System.out.println("**************** поймал " );
//                for (File file : fileGroup) {
//                    System.out.println("---- файл --- " + file.getAbsolutePath());
//                }
//            }
//
//            // Если размер первого файла в группе меньше смещения, то заканчиваем группировки filesByContent
//            if (fileGroup.iterator().next().length() < offset + BUFFER_SIZE) {
//                //System.out.println("------------- Закончили упражнение");
//                continue;
//            }
//            // Добавляем группу обратно в стек с увеличенным смещением
//            //stack.push(new AbstractMap.SimpleEntry<>(entry.getKey(), fileGroup));
//            // Увеличиваем смещение на размер буфера для следующей обработки
//            offset += BUFFER_SIZE;
//            // Группируем файлы заново и добавляем новые группы в стек
//            Map<FileKeyHashNew, Set<File>> newGroupedFiles = groupFiles(fileGroup, offset);
//            stack.addAll(newGroupedFiles.entrySet());
//        }
//    }

    public void groupByContent(Set<File> files) {
        long offset = 0;
        while (true) {
            Map<FileKeyHashNew, Set<File>> newGroupedFiles = groupFiles(files, offset);
            if (newGroupedFiles.isEmpty()) {
                break; // Если больше нет файлов для обработки, выходим из цикла
            }
            // Обновляем основную карту группировки
            for (Map.Entry<FileKeyHashNew, Set<File>> entry : newGroupedFiles.entrySet()) {
                filesByContent.merge(entry.getKey(), entry.getValue(), (existingSet, newSet) -> {
                    existingSet.addAll(newSet);
                    return existingSet;
                });
            }
            offset += BUFFER_SIZE; // Увеличиваем смещение для следующей обработки
        }
    }


    /* Метод вычисляет хеш для каждого файла в переданном множестве файлов,
     * начиная с указанного смещения offset, и группирует файлы по их хешам.
     * Возвращает карту, в которой ключ - хеш файла, значение - множество файлов с этим хешем.
     */
//    private Map<FileKeyHashNew, Set<File>> groupFiles(Set<File> files, long offset) {
//        // Создаем новую карту для группировки файлов по их хешам
//        Map<FileKeyHashNew, Set<File>> newFilesByContent = new ConcurrentHashMap<>();
//
//        // Проходим по каждому файлу в переданном множестве файлов
//        for (File file : files) {
//            try {
//                // Вычисляем хеш для текущего файла, начиная с указанного смещения
//                FileKeyHashNew fileHash = new FileKeyHashNew(file, offset, BUFFER_SIZE);
//                newFilesByContent.computeIfAbsent(fileHash, k -> ConcurrentHashMap.newKeySet()).add(file);
//            } catch (RuntimeException e) {
//                // Если произошла ошибка при чтении файла, выводим сообщение об ошибке
//                System.out.println("Ошибка при чтении файла " + file.getAbsolutePath() + ": " + e.getMessage());
//            }
//        }
//
//        // Возвращаем карту сгруппированных файлов
//        return newFilesByContent;
//    }

    private Map<FileKeyHashNew, Set<File>> groupFiles(Set<File> files, long offset) {
        Map<FileKeyHashNew, Set<File>> newFilesByContent = new ConcurrentHashMap<>();
        for (File file : files) {
            try {
                if (!file.exists() || file.length() < offset) {
                    continue; // Пропускаем файлы, которые не существуют или слишком короткие
                }
                // Вычисляем хеш для текущего файла, начиная с указанного смещения
                FileKeyHashNew fileHash = new FileKeyHashNew(file, offset, BUFFER_SIZE);
                newFilesByContent.computeIfAbsent(fileHash, k -> ConcurrentHashMap.newKeySet()).add(file);
            } catch (RuntimeException e) {
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

        //fileGrouper.groupByContentParallel(files);
        fileGrouper.groupByContent(files);
        for (Map.Entry<FileKeyHashNew, Set<File>> res : fileGrouper.filesByContent.entrySet()) {
            Set<File> fileGroup = res.getValue();
            System.out.println(" Группа файлов: --------------------");
            for (File file : fileGroup) {
                System.out.println(file.getAbsolutePath());
            }
        }


        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директории --- " + duration + " ms       ");
    }

}
