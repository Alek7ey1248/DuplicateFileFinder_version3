package processing;

import java.io.File;
import java.util.*;

public class Printer {

    // Вывод групп дубликатов файлов в консоль (compare1)
    public static void duplicatesByContent(List<Set<File>> filesByContent) {
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

}
