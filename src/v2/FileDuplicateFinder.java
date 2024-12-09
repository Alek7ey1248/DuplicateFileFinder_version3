package v2;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    private final Map<Long, Set<Path>> filesBySize;   /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    private final List<List<String>> duplicates;     /* Список для хранения результата - групп дубликатов файлов */

    /* Конструктор */
    public FileDuplicateFinder() {
        filesBySize = new HashMap<>();
        duplicates = new ArrayList<>();
    }

    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) { // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }
        findDuplicateGroups(); // Параллельная обработка файлов одинакового размера из карты filesBySize для поиска дубликатов в список duplicates
    }


    /*  Находитгруппы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.*/
    /**
    * @throws IOException при возникновении ошибки ввода-вывода*/
    public void findDuplicateGroups() throws IOException {

        // перебираю ключи (размеры файлов)
        for (Long size : filesBySize.keySet()) {
            // Получаю список файлов для текущего размера
            Set<Path> files = filesBySize.get(size);
            if (files.size() < 2) {  // Если файлов меньше двух, перехожу к следующему размеру
                continue;
            }
            // Находим дубликаты в группе файлов одинакового размера и добавляем их в список дубликатов duplicates
            findDuplicatesInSameSizeFiles(files);
        }
    }



    /**
     * Находит дубликаты файлов в списке файлов одинакового размера.
     *
     * @param files — список путей к файлам одинакового размера. Из HashMap filesBySize.
     * @throws IOException при возникновении ошибки ввода-вывода
     */
    public void findDuplicatesInSameSizeFiles(Set<Path> files) throws IOException {

        Set<Path> filesForReprocessing = new HashSet<>();  // файлы которые не дубликаты первого файла будем отправлять для повторной обработки рекурсивно
        List<String> group = new ArrayList<>(); // Список для хранения группы дубликатов первого файла

        //Path file = files.removeFirst();
        Iterator<Path> iterator = files.iterator();   // Извлекаем первый файл
        Path file = iterator.next();
        iterator.remove();  // Удаляем файл из списка

        System.out.println("Проверка файла: " + file);
        group.add(file.toString());  // Добавляем первый файл в группу дубликатов

        for (Path anotherFile : files) {   // Перебираем оставшиеся файлы в списке
            if (FileComparator.areFilesEqual(file, anotherFile)) {  // Если файлы равны, добавляем файл в группу
                group.add(anotherFile.toString());
            }
            else {    // Если файлы не равны, добавляем файл в список для повторной обработки
                filesForReprocessing.add(anotherFile);
            }
        }
        // Добавляем группу дубликатов в список дубликатов (если группа содержит более одного файла)
        if (group.size() > 1) {
            duplicates.add(group);
        }

        if (filesForReprocessing.size() < 2) return;
        // Рекурсивно обрабатываем файлы, которые не дубликаты первого файла
        findDuplicatesInSameSizeFiles(filesForReprocessing);
    }



    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
    * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
    * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        // Для проверки валидности папки или файла
        CheckValid checkValid = new CheckValid();

        // Создаем объект File(директорий) для указанного пути
        File directory = new File(path);

        // Проверка валидности директории
        if (!checkValid.isValidDirectoryPath(directory.getAbsolutePath())) {
            return;
        }

        // Получаем список всех файлов и директорий в указанной директории
        File[] files = directory.listFiles();

        // Проверяем, что массив не пустой
        if (files != null) {
            // Перебираем каждый файл и директорию в текущей директории
            for (File file : files) {
                // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                if (file.isDirectory()) {
                    walkFileTree(file.getAbsolutePath());
                } else {
                    // Проверка валидности файла
                    if (checkValid.isValidFile(file)) {
                        // Если текущий файл не является директорией, добавляем его в карту
                        // Группируем файлы по их размеру
                        filesBySize.computeIfAbsent(file.length(), k -> new HashSet<>()).add(file.toPath());
                    }
                }
            }
        }
    }

    /* Геттер для получения карты файлов, сгруппированных по размеру */
    public Map<Long, Set<Path>> getFilesBySize() {
        return filesBySize;
    }
    // Геттер для получения списка групп дубликатов файлов
    public List<List<String>> getDuplicates() {
        return duplicates;
    }


}