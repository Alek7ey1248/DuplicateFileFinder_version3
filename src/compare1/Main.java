package compare1;


import processing.CheckValid;
import java.io.IOException;
import java.util.Arrays;

// Получить список начальных путей из аргументов командной строки.
//Создать экземпляр FileDuplicateFinder и передать ему список путей.
//Запустить метод поиска дубликатов и вывести результаты.
public class Main {
    public static void main(String[] args) throws IOException {

        // Пример использования
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder"};
        //String[] paths = {"/home/alek7ey"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы"};
        //String[] paths = {"/home/alek7ey/.cache"};
        //String[] paths = {"/home/alek7ey/snap"};
        String[] paths = {"/home/alek7ey"};
        //String[] paths = {"/home/alek7ey/Android"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder"};
        //String[] paths = {"/home/alek7ey/.local"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21"};

        long startTime = System.currentTimeMillis();

        FileDuplicateFinder finder = new FileDuplicateFinder();

        // ищем дубликаты файлов в директории и выводим результат
        finder.findDuplicates(paths);

        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директории " + Arrays.toString(paths) + " --- " + duration + " ms       ");

    }
}