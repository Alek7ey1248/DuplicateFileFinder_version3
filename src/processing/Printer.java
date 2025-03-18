package processing;

import java.io.File;
import java.util.*;

public class Printer {

    // Вывод групп дубликатов файлов в консоль (compare1)
    public static void duplicatesByContent1(List<Set<File>> filesByContent) {
        // Сортировка по размеру первого файла в каждом сетe
        Collections.sort(filesByContent, new Comparator<Set<File>>() {
            @Override
            public int compare(Set<File> set1, Set<File> set2) {
                // Получаем первый файл из каждого сета
                File file1 = set1.iterator().next();
                File file2 = set2.iterator().next();

                // Сравниваем размеры файлов
                long size1 = file1.length();
                long size2 = file2.length();

                return Long.compare(size1, size2);
            }
        });

        // Вывод отсортированного списка
        for (Set<File> set : filesByContent) {
            String firstFileName = set.iterator().next().getName();
            long size = set.iterator().next().length();
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.println(" Группа дубликатов типа - " + firstFileName);
            System.out.println(" pазмер - " + size + " ----------------------------------------------------------------");
            for (File file : set) {
                System.out.println(file.getAbsolutePath());
            }
        }
    }


    // Вывод групп дубликатов файлов в консоль (compare2)
    public static void duplicatesByContent2(Map<Long, List<List<File>>> fileByContent) {

        // Выводим отсортированные группы в консоль
        for (List<List<File>> fileList : fileByContent.values()) {  // Перебираем все группы списков файлов

            for (List<File> fl : fileList) {             // Перебираем все группы файлов
                if (fl.size() < 2) {                    // Если в группе только один файл, переходим к следующей группе
                    continue;
                }
                // Извлекаем размер первого файла для вывода
                File firstFile = fl.getFirst();
                System.out.println("-------------------------------------------------");
                System.out.println("Группа дубликатов типа - " + firstFile.getName());
                System.out.println(" размер: " + firstFile.length());
                System.out.println();
                for (File file : fl) {                  // Перебираем все файлы в группе
                    System.out.println("    " + file.getAbsolutePath());
                }
                System.out.println("-------------------------------------------------");
            }
        }
    }


    // Вывод групп дубликатов файлов в консоль (hash1)
    // выводит группы дубликатов файлов
    public static void duplicatesByHash(Map<FileKeyHash, Set<File>> filesByKeyHash) {
        // Проходим по всем записям в TreeMap fileByHash
        for (Map.Entry<FileKeyHash, Set<File>> entry : filesByKeyHash.entrySet()) {
            // Получаем ключ (FileKey) и значение (Set<File>) для текущей записи
            FileKeyHash key = entry.getKey();
            Set<File> files = entry.getValue();
            // Проверяем, что в группе есть файлы
            if (files.size() > 1) {
                // Выводим информацию о группе дубликатов
                System.out.println("-------------------------------------------------------------------");
                System.out.println("Группа дубликатов файла типа - " + files.iterator().next().getName());
                System.out.println(" размера - " + key.getSize());
                System.out.println("----------------------------");
                // Проходим по всем файлам в группе и выводим их пути
                for (File file : files) {
                    System.out.println(file.getAbsolutePath());
                }
                System.out.println();
            }
        }
    }



    // Формирует duplicates из getFilesByKey и getFilesByContent, сортирует и выводит в консоль (compareAndHash2)
    public static List<Set<File>> duplicatesByHashAndContent(Map<FileKeyHash, Set<File>> filesByKey, List<Set<File>> filesByContent) {

        List<Set<File>> duplicates = new ArrayList<>();

        // Добавляем все Set<File> из filesByKey
        for (Set<File> fileSet : filesByKey.values()) {
            if (fileSet.size() > 1) {
                duplicates.add(fileSet);
            }
        }
        //duplicates.addAll(fileGrouper.getFilesByKey().values());

        // Добавляем все Set<File> из filesByContent
//        for (Set<File> fileSet : fileGrouper.getFilesByContent()) {
//            duplicates.add(fileSet);
//        }
        duplicates.addAll(filesByContent);

        // Сортируем список по размеру первого файла в каждом Set<File>
        duplicates.sort(Comparator.comparingLong(set -> set.iterator().next().length()));

        // Выводим отсортированные группы в консоль
        for (Set<File> fileSet : duplicates) {
            // Извлекаем размер первого файла для вывода
            File firstFile = fileSet.iterator().next();
            System.out.println("-------------------------------------------------");
            System.out.println("Группа дубликатов размером: " + firstFile.length() + " байт");
            for (File file : fileSet) {
                System.out.println("    " + file.getAbsolutePath());
            }
            System.out.println("-------------------------------------------------");
        }

        return duplicates;
    }



}
