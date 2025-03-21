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

    private static int i = 0;

    private Map<String, Set<File>> filesByContent;
    public Map<String, Set<File>> getFilesByContent() {
        return filesByContent;
    }

    private static final int BUFFER_SIZE = 8192; // Размер буфера 8 КБ

    // Конструктор
    public FileGrouperNew() {
        this.filesByContent = new ConcurrentHashMap<>();
    }


    // рекурсивный основной метод
//    public void groupByContent(Set<File> files, String key, long offset) {
//
//        // filesByContent группировка файлов по хешам их первых буферов
//        filesByContent = groupFiles(files, key, offset);
//        System.out.println("---- проход --------" + i++);
//
//        // Проходим по всем сгруппированным файлам
//        for (Map.Entry<String, Set<File>> entry : filesByContent.entrySet()) {
//            Set<File> fileGroup = entry.getValue(); // Получаем набор файлов для текущего хеша
//
//            // если в результате предыдущей группировки по предыдущим хешам
//            // в группе меньше двух файлов, то удаляем группу из карты filesByContent
//            if (fileGroup.size() < 2) {
//                System.out.println("---- удаляем- filesByContent.remove(entry.getKey());");
//                filesByContent.remove(entry.getKey());
//                continue;
//            }
//
//            // Если размер первого файла в группе меньше смещения, то заканчиваем группировки filesByContent
//            if (fileGroup.iterator().next().length() < offset + BUFFER_SIZE) {
//                System.out.println("------------- Закончили упражнение");
//                continue;
//            }
//
//            // Рекурсивный вызов для дальнейшей группировки файлов, если группа больше одного
//            // Увеличиваем смещение на размер буфера, чтобы читать следующий блок данных
//            groupByContent(fileGroup, entry.getKey(), offset + BUFFER_SIZE);
//
//        }
//    }

    // не рекурсивный метод
    public void groupByContent(Set<File> files, String key, long offset) {
        // filesByContent группировка файлов по хешам их первых буферов
        filesByContent = groupFiles(files, key, offset);
        System.out.println("---- проход --------" + i++);
        // Используем стек для хранения групп файлов и их параметров
        Stack<Map.Entry<String, Set<File>>> stack = new Stack<>();
        stack.addAll(filesByContent.entrySet());
        while (!stack.isEmpty()) {
            Map.Entry<String, Set<File>> entry = stack.pop();
            Set<File> fileGroup = entry.getValue(); // Получаем набор файлов для текущего хеша
            // Если в группе меньше двух файлов, то удаляем группу из карты filesByContent
            if (fileGroup.size() < 2) {
                System.out.println("---- удаляем- filesByContent.remove(entry.getKey());");
                filesByContent.remove(entry.getKey());
                continue;
            }
            // Если размер первого файла в группе меньше смещения, то заканчиваем группировки filesByContent
            if (fileGroup.iterator().next().length() < offset + BUFFER_SIZE) {
                System.out.println("------------- Закончили упражнение");
                continue;
            }
            // Добавляем группу обратно в стек с увеличенным смещением
            stack.push(new AbstractMap.SimpleEntry<>(entry.getKey(), fileGroup));
            // Увеличиваем смещение на размер буфера для следующей обработки
            offset += BUFFER_SIZE;
            // Группируем файлы заново и добавляем новые группы в стек
            Map<String, Set<File>> newGroupedFiles = groupFiles(fileGroup, entry.getKey(), offset);
            stack.addAll(newGroupedFiles.entrySet());
        }
    }


    /* Метод вычисляет хеш для каждого файла в переданном множестве файлов,
     * начиная с указанного смещения offset, и группирует файлы по их хешам.
     * Возвращает карту, в которой ключ - хеш файла, значение - множество файлов с этим хешем.
     */
    private Map<String, Set<File>> groupFiles(Set<File> files, String key, long offset) {
        //System.out.println("---  groupFiles ---" + i);
        // Создаем новую карту для группировки файлов по их хешам
        Map<String, Set<File>> newFilesByContent = new ConcurrentHashMap<>();

        // Проходим по каждому файлу в переданном множестве файлов
        for (File file : files) {
            try {
                // Вычисляем хеш для текущего файла, начиная с указанного смещения
                String newFileHash = calculateFileHash(file, offset);
//                String newFileHash = key + calculateFileHash(file, offset);
                //System.out.println("newFileHash = " + newFileHash);

                // Создаем объект MessageDigest для объединения хешей
                //MessageDigest digest = createMessageDigest();

                // Обновляем хеш с помощью предыдущего хеша
//                digest.update(key);
//                // Обновляем хеш с помощью нового хеша
//                digest.update(newFileHash);

                // Получаем финальный хеш
//                byte[] combinedHash = digest.digest();

                // Добавляем файл в группу, соответствующую его хешу
                newFilesByContent.computeIfAbsent(newFileHash, k -> ConcurrentHashMap.newKeySet()).add(file);
            } catch (IOException | RuntimeException e) {
                // Если произошла ошибка при чтении файла, выводим сообщение об ошибке
                System.out.println("Ошибка при чтении файла " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }

//        for (Map.Entry<String, Set<File>> res : newFilesByContent.entrySet()) {
//            Set<File> fileGroup = res.getValue();
//            System.out.println(" Группа файлов: --------------------");
//            for (File file : fileGroup) {
//                System.out.println(file.getAbsolutePath());
//            }
//        }
        // Возвращаем карту сгруппированных файлов
        return newFilesByContent;
    }


    // Вычисление хеша файла с указанного смещения offset на длинну буфера BUFFER_SIZE
    private String calculateFileHash(File file, long offset) throws IOException {

        if (!file.exists() || offset < 0 || offset >= file.length()) {
            throw new IllegalArgumentException("Неверный файл или смещение");
        }

        //System.out.println("---  calculateFileHash ---" + i);
        // Создаем объект MessageDigest для вычисления хеша файла
        MessageDigest digest = createMessageDigest();

        // Открываем FileInputStream для чтения содержимого файла
        try (FileInputStream fis = new FileInputStream(file)) {
            // Пропускаем байты до указанного смещения, чтобы начать чтение с нужного места
            fis.skip(offset);

            // Создаем массив байтов для хранения данных, читаемых из файла
            byte[] byteArray = new byte[BUFFER_SIZE];

            // Читаем данные из файла в массив byteArray и сохраняем количество прочитанных байтов
            int bytesCount = fis.read(byteArray);

            // Если метод read возвращает -1, это означает, что достигнут конец файла
            if (bytesCount == -1) {
                return ""; // Возвращаем пустую строку, если файл пуст или достигнут конец
            }

            // Обновляем объект digest, добавляя к нему прочитанные байты
            digest.update(byteArray, 0, bytesCount);
        }

        // Завершаем вычисление хеша и получаем массив байтов, представляющий хеш
        byte[] hashBytes = digest.digest();

        //System.out.println("..." + Arrays.toString(hashBytes));
        // Преобразуем массив байтов хеша в строку в шестнадцатеричном формате и возвращаем ее
        return bytesToHex(hashBytes);
    }


    // Создание объекта MessageDigest для вычисления хеша
    private MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании MessageDigest", e);
        }
    }


    // Преобразование массива байтов в шестнадцатеричное представление
    private static String bytesToHex(byte[] bytes) {
        // Создаем объект StringBuilder для накопления строкового представления байтов
        StringBuilder sb = new StringBuilder();
        // Проходим по каждому байту в массиве байтов
        for (byte b : bytes) {
            // Форматируем байт как шестнадцатеричное число с двумя цифрами
            // %02x означает: 0 - добавляет ведущие нули, 2 - минимальная ширина 2 символа, x - шестнадцатеричное представление
            sb.append(String.format("%02x", b));
        }
        // Возвращаем полученную строку, представляющую все байты в шестнадцатеричном формате
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        FileGrouperNew fileGrouper = new FileGrouperNew();
        Set<File> files = new HashSet<>();
//        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена)"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (середина изменена) (Копия)"));

        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия)"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (копия)"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия 3)"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия 2)"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия) (Копия)"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"));
        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"));


        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат1.zip"));
        //files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат.zip"));

        files.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/largeFile.txt"));

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

//        File directory = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder");
//        File[] arrayFiles = directory.listFiles();
//        if (arrayFiles != null) {
//            for (File file : arrayFiles) {
//                if (file.isFile()) {
//                    files.add(file);
//                }
//            }
//        }


        //fileGrouper.groupByContentParallel(files);
        fileGrouper.groupByContent(files, "", 0);
        for (Map.Entry<String, Set<File>> res : fileGrouper.filesByContent.entrySet()) {
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
