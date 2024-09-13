package v2;// Main: Точка входа в программу. Обрабатывает аргументы командной строки и запускает процесс поиска дубликатов.
//FileDuplicateFinder: Основной класс, который выполняет поиск дубликатов файлов.
//FileComparator: Класс для побайтного сравнения содержимого файлов.

import java.io.IOException;

// Получить список начальных путей из аргументов командной строки.
//Создать экземпляр FileDuplicateFinder и передать ему список путей.
//Запустить метод поиска дубликатов и вывести результаты.
public class Main2 {
    public static void main(String[] args) throws IOException {

        // Проверка наличия аргументов командной строки
        if (args.length == 0) {
            System.out.println("Укажите хотя бы один путь для поиска дубликатов.");
            return;
        }

        // Создание экземпляра класса FileDuplicateFinder
        FileDuplicateFinder2 finder = new FileDuplicateFinder2();

    }
}