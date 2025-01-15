package V12;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DuplicateFilePrinter {

    // Метод для вывода групп дубликатов файлов в консоль
    public static void printDuplicates(Map<Long, List<Set<File>>> duplicatesBySize) {

        // Сортируем duplicatesBySize по размеру ключей по убыванию
        duplicatesBySize = getSorted(duplicatesBySize);

        for (Map.Entry<Long, List<Set<File>>> entry : duplicatesBySize.entrySet()) {
            List<Set<File>> groups = entry.getValue();

            for (Set<File> group : groups) {
                if (group.size() < 2) {  // Если в группе меньше 2 файлов, то это не дубликаты, а одиночные файлы
//                    groups.remove(group);
//                    if (groups.isEmpty()){break;} // Если группа с группами дубликатов пустая, то прерываем цикл
                    continue; // Если группа не пустая, то переходим к следующей подгруппе дубликатов
                }
                    System.out.println("   Группа дубликатов типа :" + group.iterator().next().getName() + " размером " + entry.getKey() + " байт");
                    System.out.println("----------------------------------------");
                    for (File file : group) {
                        System.out.println(" " + file.getAbsolutePath());
                    }
                    System.out.println();
                    System.out.println("-------------------------------------------------------------");

            }
        }
    }

    // сортировка duplicatesBySize по размеру ключей по возрастанию
    public static Map<Long, List<Set<File>>> getSorted(Map<Long, List<Set<File>>> duplicatesBySize) {
        return duplicatesBySize.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // Сортировка по возрастанию ключа
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // В случае дубликатов, выбираем первое значение
                        LinkedHashMap::new // Используем LinkedHashMap для сохранения порядка
                ));
    }

    // сортировка duplicatesBySize по размеру ключей по убыванию
//    public static Map<Long, List<Set<File>>> getSorted(Map<Long, List<Set<File>>> duplicatesBySize) {
//        return duplicatesBySize.entrySet()
//                .stream()
//                .sorted(Map.Entry.<Long, List<Set<File>>>comparingByKey().reversed())
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (e1, e2) -> e1, // В случае дубликатов, выбираем первое значение
//                        LinkedHashMap::new // Используем LinkedHashMap для сохранения порядка
//                ));
//    }



    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();


        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директориях " + duration + " ms       ");

    }
}
