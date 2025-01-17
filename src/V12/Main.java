package V12;// Main: Точка входа в программу. Обрабатывает аргументы командной строки и запускает процесс поиска дубликатов.
//FileDuplicateFinder: Основной класс, который выполняет поиск дубликатов файлов.
//FileComparator: Класс для побайтного сравнения содержимого файлов.


import java.io.IOException;
import java.util.Arrays;

// Получить список начальных путей из аргументов командной строки.
//Создать экземпляр FileDuplicateFinder и передать ему список путей.
//Запустить метод поиска дубликатов и вывести результаты.
public class Main {
    public static void main(String[] args) throws IOException {

        // Пример использования
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder"};
        String[] paths = {"/home/alek7ey"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы"};
        //String[] paths = {"/home/alek7ey/.cache"};
        //String[] paths = {"/home/alek7ey/snap"};
        //String[] paths = {"/home/alek7ey"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder"};

        // Проверка валидности путей
        CheckValid checkValid = new CheckValid();
        if(!checkValid.getValidDirectoryPaths(paths)) {
            return;
        }

        long startTime = System.currentTimeMillis();

        FileDuplicateFinder finder = new FileDuplicateFinder();

        // ищем дубликаты файлов в директории
        finder.findDuplicates(paths);

        // выводим отсортированные результаты поиска дубликатов
        //DuplicateFilePrinter.printDuplicates(finder.getDuplicatesBySize());

        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директории " + Arrays.toString(paths) + " --- " + duration + " ms       ");

    }
}