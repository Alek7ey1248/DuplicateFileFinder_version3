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
        //String[] paths = {"/home/alek7ey"};           // 81,       сравн - очень долго,  хеш - 72 - 75 - 88 (ускоренный - 50)
        //String[] paths = {"/home/alek7ey/.local"};    // 1 - 1,6,  сравн - 1 - 1,5,      хеш - 0,9 - 1,6    (ускоренный - 0,9)
        //String[] paths = {"/home/alek7ey/.cache"};      // 6.6 - 9,  сравн - очень долго,  хеш - 6 - 14     (ускоренный - 4,4 - 6)
        //String[] paths = {"/home/alek7ey/snap"};      // 11,8,     сравн - 22 - 24,      хеш - 9,6 - 11,6   (ускоренный - 6)
        //String[] paths = {"/home/alek7ey/Android"};   // 3 - 4,    сравн - 2,5 - 3,9,    хеш - 2,4 - 2,6    (ускоренный - 2.4-2.6)
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы"}; // 24 - 27,  сравн - 24,  хеш - 33 (ускоренный - 29 - 32)
        String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF"};
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