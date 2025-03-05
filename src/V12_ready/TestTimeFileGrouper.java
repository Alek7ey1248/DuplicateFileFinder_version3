package V12_ready;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

// FileGrouperTestTime: Класс для тестирования времени выполнения методов класса FileGrouper.
// Генерирует наборы файлов разного размера и тестирует методы класса FileGrouper на них.
public class TestTimeFileGrouper {
    public static void main(String[] args) {

   // ОДИНАКОВЫЕ ФАЙЛЫ------------------------------------------------
        // Генерация тестовых данных c 2 файлами каждого размера
//        Set<File> smallFiles2Id = generateIdenticalFiles(2, 2); // Файлы почти 0 байт
//        Set<File> mediumFiles2Id = generateIdenticalFiles(10, 2); // Файлыдо 10 байт
//        Set<File> largeFiles2Id = generateIdenticalFiles(1000, 2); // Файлы до 1 Кб
//        Set<File> hugeFiles2Id = generateIdenticalFiles(10000, 2); // Файлы до 100 Кб
//        Set<File> giganticFiles2Id = generateIdenticalFiles(300000000, 2); // Файлы до 300 Мб
//        Set<File> oneMbFiles2Id = generateIdenticalFiles(1000000000, 2); // Файлы - 1 Гб примерно

//        // Генерация тестовых данных c 4 файлами каждого размера
//        Set<File> smallFiles4Id = generateIdenticalFiles(2, 4); // Файлы почти 0 байт
//        Set<File> mediumFiles4Id = generateIdenticalFiles(10, 4); // Файлыдо 10 байт
//        Set<File> largeFiles4Id = generateIdenticalFiles(1000, 4); // Файлы до 1 Кб
//        Set<File> hugeFiles4Id = generateIdenticalFiles(10000, 4); // Файлы до 100 Кб
//        Set<File> giganticFiles4Id = generateIdenticalFiles(300000000, 4); // Файлы до 300 Мб
//        Set<File> oneMbFiles4Id = generateIdenticalFiles(1000000000, 4); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 8 файлами каждого размера
//        Set<File> smallFiles8Id = generateIdenticalFiles(2, 8); // Файлы почти 0 байт
//        Set<File> mediumFiles8Id = generateIdenticalFiles(10, 8); // Файлыдо 10 байт
//        Set<File> largeFiles8Id = generateIdenticalFiles(1000, 8); // Файлы до 1 Кб
//        Set<File> hugeFiles8Id = generateIdenticalFiles(10000, 8); // Файлы до 100 Кб
//        Set<File> giganticFiles8Id = generateIdenticalFiles(300000000, 8); // Файлы до 300 Мб
//        Set<File> oneMbFiles8Id = generateIdenticalFiles(1000000000, 8); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 10 файлами каждого размера
        Set<File> smallFiles20Id = generateIdenticalFiles(2, 20); // Файлы почти 0 байт
        Set<File> mediumFiles20Id = generateIdenticalFiles(10, 20); // Файлыдо 10 байт
        Set<File> largeFiles20Id = generateIdenticalFiles(1000, 20); // Файлы до 1 Кб
        Set<File> hugeFiles20Id = generateIdenticalFiles(10000, 20); // Файлы до 100 Кб
        Set<File> giganticFiles20Id = generateIdenticalFiles(300000000, 20); // Файлы до 300 Мб
        Set<File> oneMbFiles20Id = generateIdenticalFiles(1000000000, 20); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 12 файлами каждого размера
        Set<File> smallFiles25Id = generateIdenticalFiles(2, 25); // Файлы почти 0 байт
        Set<File> mediumFiles25Id = generateIdenticalFiles(10, 25); // Файлыдо 10 байт
        Set<File> largeFiles25Id = generateIdenticalFiles(1000, 25); // Файлы до 1 Кб
        Set<File> hugeFiles25Id = generateIdenticalFiles(10000, 25); // Файлы до 100 Кб
        Set<File> giganticFiles25Id = generateIdenticalFiles(300000000, 25); // Файлы до 300 Мб
        Set<File> oneMbFiles25Id = generateIdenticalFiles(1000000000, 25); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 14 файлами каждого размера
        Set<File> smallFiles30Id = generateIdenticalFiles(2, 30); // Файлы почти 0 байт
        Set<File> mediumFiles30Id = generateIdenticalFiles(10, 30); // Файлыдо 10 байт
        Set<File> largeFiles30Id = generateIdenticalFiles(1000, 30); // Файлы до 1 Кб
        Set<File> hugeFiles30Id = generateIdenticalFiles(10000, 30); // Файлы до 100 Кб
        Set<File> giganticFiles30Id = generateIdenticalFiles(300000000, 30); // Файлы до 300 Мб
        Set<File> oneMbFiles30Id = generateIdenticalFiles(1000000000, 30); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 16 файлами каждого размера
//        Set<File> smallFiles16Id = generateIdenticalFiles(2, 16); // Файлы почти 0 байт
//        Set<File> mediumFiles16Id = generateIdenticalFiles(10, 16); // Файлыдо 10 байт
//        Set<File> largeFiles16Id = generateIdenticalFiles(1000, 16); // Файлы до 1 Кб
//        Set<File> hugeFiles16Id = generateIdenticalFiles(10000, 16); // Файлы до 100 Кб
//        Set<File> giganticFiles16Id = generateIdenticalFiles(300000000, 16); // Файлы до 300 Мб
//        Set<File> oneMbFiles16Id = generateIdenticalFiles(1000000000, 16); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 32 файлами каждого размера
//        Set<File> smallFiles32Id = generateIdenticalFiles(2, 32); // Файлы почти 0 байт
//        Set<File> mediumFiles32Id = generateIdenticalFiles(10, 32); // Файлыдо 10 байт
//        Set<File> largeFiles32Id = generateIdenticalFiles(1000, 32); // Файлы до 1 Кб
//        Set<File> hugeFiles32Id = generateIdenticalFiles(10000, 32); // Файлы до 100 Кб
//        Set<File> giganticFiles32Id = generateIdenticalFiles(300000000, 32); // Файлы до 300 Мб
//        Set<File> oneMbFiles32Id = generateIdenticalFiles(1000000000, 32); // Файлы - 1 Гб примерно



        // РАЗНЫЕ ФАЙЛЫ------------------------------------------------
        // Генерация тестовых данных c 2 файлами каждого размера
//        Set<File> smallFiles2Di = generateDifferentFiles(2, 2); // Файлы почти 0 байт
//        Set<File> mediumFiles2Di = generateDifferentFiles(10, 2); // Файлыдо 10 байт
//        Set<File> largeFiles2Di = generateDifferentFiles(1000, 2); // Файлы до 1 Кб
//        Set<File> hugeFiles2Di = generateDifferentFiles(10000, 2); // Файлы до 100 Кб
//        Set<File> giganticFiles2Di = generateDifferentFiles(300000000, 2); // Файлы до 300 Мб
//        Set<File> oneMbFiles2Di = generateDifferentFiles(1000000000, 2); // Файлы - 1 Гб примерно


        // Генерация тестовых данных c 4 файлами каждого размера
//        Set<File> smallFiles4Di = generateDifferentFiles(2, 4); // Файлы почти 0 байт
//        Set<File> mediumFiles4Di = generateDifferentFiles(10, 4); // Файлыдо 10 байт
//        Set<File> largeFiles4Di = generateDifferentFiles(1000, 4); // Файлы до 1 Кб
//        Set<File> hugeFiles4Di = generateDifferentFiles(10000, 4); // Файлы до 100 Кб
//        Set<File> giganticFiles4Di = generateDifferentFiles(300000000, 4); // Файлы до 300 Мб
//        Set<File> oneMbFiles4Di = generateDifferentFiles(1000000000, 4); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 8 файлами каждого размера
//        Set<File> smallFiles8Di = generateDifferentFiles(2, 8); // Файлы почти 0 байт
//        Set<File> mediumFiles8Di = generateDifferentFiles(10, 8); // Файлыдо 10 байт
//        Set<File> largeFiles8Di = generateDifferentFiles(1000, 8); // Файлы до 1 Кб
//        Set<File> hugeFiles8Di = generateDifferentFiles(10000, 8); // Файлы до 100 Кб
//        Set<File> giganticFiles8Di = generateDifferentFiles(300000000, 8); // Файлы до 300 Мб
//        Set<File> oneMbFiles8Di = generateDifferentFiles(1000000000, 8); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 10 файлами каждого размера
        Set<File> smallFiles20Di = generateDifferentFiles(2, 20); // Файлы почти 0 байт
        Set<File> mediumFiles20Di = generateDifferentFiles(10, 20); // Файлыдо 10 байт
        Set<File> largeFiles20Di = generateDifferentFiles(1000, 20); // Файлы до 1 Кб
        Set<File> hugeFiles20Di = generateDifferentFiles(10000, 20); // Файлы до 100 Кб
        Set<File> giganticFiles20Di = generateDifferentFiles(300000000, 20); // Файлы до 300 Мб
        Set<File> oneMbFiles20Di = generateDifferentFiles(1000000000, 20); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 12 файлами каждого размера
        Set<File> smallFiles25Di = generateDifferentFiles(2, 25); // Файлы почти 0 байт
        Set<File> mediumFiles25Di = generateDifferentFiles(10, 25); // Файлыдо 10 байт
        Set<File> largeFiles25Di = generateDifferentFiles(1000, 25); // Файлы до 1 Кб
        Set<File> hugeFiles25Di = generateDifferentFiles(10000, 25); // Файлы до 100 Кб
        Set<File> giganticFiles25Di = generateDifferentFiles(300000000, 25); // Файлы до 300 Мб
        Set<File> oneMbFiles25Di = generateDifferentFiles(1000000000, 25); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 14 файлами каждого размера
        Set<File> smallFiles30Di = generateDifferentFiles(2, 30); // Файлы почти 0 байт
        Set<File> mediumFiles30Di = generateDifferentFiles(10, 30); // Файлыдо 10 байт
        Set<File> largeFiles30Di = generateDifferentFiles(1000, 30); // Файлы до 1 Кб
        Set<File> hugeFiles30Di = generateDifferentFiles(10000, 30); // Файлы до 100 Кб
        Set<File> giganticFiles30Di = generateDifferentFiles(300000000, 30); // Файлы до 300 Мб
        Set<File> oneMbFiles30Di = generateDifferentFiles(1000000000, 30); // Файлы - 1 Гб примерно


        // Генерация тестовых данных c 16 файлами каждого размера
//        Set<File> smallFiles16Di = generateDifferentFiles(2, 16); // Файлы почти 0 байт
//        Set<File> mediumFiles16Di = generateDifferentFiles(10, 16); // Файлыдо 10 байт
//        Set<File> largeFiles16Di = generateDifferentFiles(1000, 16); // Файлы до 1 Кб
//        Set<File> hugeFiles16Di = generateDifferentFiles(10000, 16); // Файлы до 100 Кб
//        Set<File> giganticFiles16Di = generateDifferentFiles(300000000, 16); // Файлы до 300 Мб
//        Set<File> oneMbFiles16Di = generateDifferentFiles(1000000000, 16); // Файлы - 1 Гб примерно

        // Генерация тестовых данных c 32 файлами каждого размера
//        Set<File> smallFiles32Di = generateDifferentFiles(2, 32); // Файлы почти 0 байт
//        Set<File> mediumFiles32Di = generateDifferentFiles(10, 32); // Файлыдо 10 байт
//        Set<File> largeFiles32Di = generateDifferentFiles(1000, 32); // Файлы до 1 Кб
//        Set<File> hugeFiles32Di = generateDifferentFiles(10000, 32); // Файлы до 100 Кб
//        Set<File> giganticFiles32Di = generateDifferentFiles(300000000, 32); // Файлы до 300 Мб
//        Set<File> oneMbFiles32Di = generateDifferentFiles(1000000000, 32); // Файлы - 1 Гб примерно




        // Список для хранения результатов тестов
        List<String> results = new ArrayList<>();

        // ТЕСТ РАЗНЫХ ФАЙЛОВ ==============================================================================================
        // Тестирование методов с наборами по 2 файлов каждого размера
//        results.add("Testing Small Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles2Di, results);
//        results.add("Testing Medium Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles2Di, results);
//        results.add("Testing Large Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles2Di, results);
//        results.add("Testing Huge Files 2 -РАЗНЫЕ ФАЙЛЫ-----------------------");
//        testFileGroup(hugeFiles2Di, results);
//        results.add("Testing Gigantic Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles2Di, results);
//        results.add("Testing OneMbFiles Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles2Di, results);


        // Тестирование методов с наборами по 4 файлов каждого размера
//        results.add("Testing Small Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles4Di, results);
//        results.add("Testing Medium Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles4Di, results);
//        results.add("Testing Large Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles4Di, results);
//        results.add("Testing Huge Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles4Di, results);
//        results.add("Testing Gigantic Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles4Di, results);
//        results.add("Testing OneMbFiles Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles4Di, results);


        // Тестирование методов с наборами по 8 файлов каждого размера
//        results.add("Testing Small Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles8Di, results);
//        results.add("Testing Medium Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles8Di, results);
//        results.add("Testing Large Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles8Di, results);
//        results.add("Testing Huge Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles8Di, results);
//        results.add("Testing Gigantic Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles8Di, results);
//        results.add("Testing OneMbFiles Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles8Di, results);

        // Тестирование методов с наборами по 10 файлов каждого размера
        results.add("Testing Small Files 20 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(smallFiles20Di, results);
        results.add("Testing Medium Files 20 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(mediumFiles20Di, results);
        results.add("Testing Large Files 20 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(largeFiles20Di, results);
        results.add("Testing Huge Files 20 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(hugeFiles20Di, results);
        results.add("Testing Gigantic Files 20 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(giganticFiles20Di, results);
        results.add("Testing OneMbFiles Files 20 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(oneMbFiles20Di, results);


        // Тестирование методов с наборами по 12 файлов каждого размера
        results.add("Testing Small Files 25 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(smallFiles25Di, results);
        results.add("Testing Medium Files 25 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(mediumFiles25Di, results);
        results.add("Testing Large Files 25 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(largeFiles25Di, results);
        results.add("Testing Huge Files 25 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(hugeFiles25Di, results);
        results.add("Testing Gigantic Files 25 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(giganticFiles25Di, results);
        results.add("Testing OneMbFiles Files 25 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(oneMbFiles25Di, results);

        // Тестирование методов с наборами по 14 файлов каждого размера
        results.add("Testing Small Files 30 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(smallFiles30Di, results);
        results.add("Testing Medium Files 30 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(mediumFiles30Di, results);
        results.add("Testing Large Files 30 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(largeFiles30Di, results);
        results.add("Testing Huge Files 30 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(hugeFiles30Di, results);
        results.add("Testing Gigantic Files 30 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(giganticFiles30Di, results);
        results.add("Testing OneMbFiles Files 30 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(oneMbFiles30Di, results);


        // Тестирование методов с наборами по 16 файлов каждого размера
//        results.add("Testing Small Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles16Di, results);
//        results.add("Testing Medium Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles16Di, results);
//        results.add("Testing Large Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles16Di, results);
//        results.add("Testing Huge Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles16Di, results);
//        results.add("Testing Gigantic Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles16Di, results);
//        results.add("Testing OneMbFiles Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles16Di, results);


        // Тестирование методов с наборами по 32 файлов каждого размера
//        results.add("Testing Small Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles32Di, results);
//        results.add("Testing Medium Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles32Di, results);
//        results.add("Testing Large Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles32Di, results);
//        results.add("Testing Huge Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles32Di, results);
//        results.add("Testing Gigantic Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles32Di, results);
//        results.add("Testing OneMbFiles Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles32Di, results);




        // ТЕСТ ОДИНАКОВЫХ ФАЙЛОВ =======================================================================================
        // Тестирование методов с наборами по 2 файлов каждого размера
//        results.add("Testing Small Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles2Id, results);
//        results.add("Testing Medium Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles2Id, results);
//        results.add("Testing Large Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles2Id, results);
//        results.add("Testing Huge Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles2Id, results);
//        results.add("Testing Gigantic Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles2Id, results);
//        results.add("Testing OneMbFiles Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles2Id, results);


        // Тестирование методов с наборами по 4 файлов каждого размера
//        results.add("Testing Small Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles4Id, results);
//        results.add("Testing Medium Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles4Id, results);
//        results.add("Testing Large Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles4Id, results);
//        results.add("Testing Huge Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles4Id, results);
//        results.add("Testing Gigantic Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles4Id, results);
//        results.add("Testing OneMbFiles Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles4Id, results);


        // Тестирование методов с наборами по 8 файлов каждого размера
//        results.add("Testing Small Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles8Id, results);
//        results.add("Testing Medium Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles8Id, results);
//        results.add("Testing Large Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles8Id, results);
//        results.add("Testing Huge Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles8Id, results);
//        results.add("Testing Gigantic Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles8Id, results);
//        results.add("Testing OneMbFiles Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles8Id, results);

        // Тестирование методов с наборами по 10 файлов каждого размера
        results.add("Testing Small Files 20 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(smallFiles20Id, results);
        results.add("Testing Medium Files 20 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(mediumFiles20Id, results);
        results.add("Testing Large Files 20 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(largeFiles20Id, results);
        results.add("Testing Huge Files 20 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(hugeFiles20Id, results);
        results.add("Testing Gigantic Files 20 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(giganticFiles20Id, results);
        results.add("Testing OneMbFiles Files 20 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(oneMbFiles20Id, results);


        // Тестирование методов с наборами по 12 файлов каждого размера
        results.add("Testing Small Files 25 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(smallFiles25Id, results);
        results.add("Testing Medium Files 25 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(mediumFiles25Id, results);
        results.add("Testing Large Files 25 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(largeFiles25Id, results);
        results.add("Testing Huge Files 25 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(hugeFiles25Id, results);
        results.add("Testing Gigantic Files 25 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(giganticFiles25Id, results);
        results.add("Testing OneMbFiles Files 25 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(oneMbFiles25Id, results);


        // Тестирование методов с наборами по 14 файлов каждого размера
        results.add("Testing Small Files 30 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(smallFiles30Id, results);
        results.add("Testing Medium Files 30 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(mediumFiles30Id, results);
        results.add("Testing Large Files 30 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(largeFiles30Id, results);
        results.add("Testing Huge Files 30 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(hugeFiles30Id, results);
        results.add("Testing Gigantic Files 30 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(giganticFiles30Id, results);
        results.add("Testing OneMbFiles Files 30 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(oneMbFiles30Id, results);




        // Тестирование методов с наборами по 16 файлов каждого размера
//        results.add("Testing Small Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles16Id, results);
//        results.add("Testing Medium Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles16Id, results);
//        results.add("Testing Large Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles16Id, results);
//        results.add("Testing Huge Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles16Id, results);
//        results.add("Testing Gigantic Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles16Id, results);
//        results.add("Testing OneMbFiles Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles16Id, results);

        // Тестирование методов с наборами по 32 файлов каждого размера
//        results.add("Testing Small Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(smallFiles32Id, results);
//        results.add("Testing Medium Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(mediumFiles32Id, results);
//        results.add("Testing Large Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(largeFiles32Id, results);
//        results.add("Testing Huge Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(hugeFiles32Id, results);
//        results.add("Testing Gigantic Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(giganticFiles32Id, results);
//        results.add("Testing OneMbFiles Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
//        testFileGroup(oneMbFiles32Id, results);



        // Вывод результатов всех тестов
        for (String result : results) {
            System.out.println(result);
        }
    }

    // Генерация набора одинаковых файлов заданного размера и количества
    public static Set<File> generateIdenticalFiles(int fileSize, int numberOfFiles) {
        Set<File> fileSet = new HashSet<>();
        String content = generateRandomString(fileSize); // Генерируем строку для содержимого

        for (int i = 0; i < numberOfFiles; i++) {
            try {
                // Создаем временный файл
                Path tempFile = Files.createTempFile("identicalFile" + i + "_", ".txt");
                // Записываем содержимое в файл
                Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
                fileSet.add(tempFile.toFile());   // Добавляем файл в набор
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileSet;
    }


    // Генерация набора разных файлов заданного размера и количества
    public static Set<File> generateDifferentFiles(int fileSize, int numberOfFiles) {
        Set<File> fileSet = new HashSet<>();

        for (int i = 0; i < numberOfFiles; i++) {
            try {
                // Создаем временный файл
                Path tempFile = Files.createTempFile("differentFile" + i + "_", ".txt");
                String content = generateRandomString(fileSize); // Генерируем строку для содержимого
                Files.write(tempFile, content.getBytes());
                fileSet.add(tempFile.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileSet;
    }

    // Вспомогательный метод для генерации строки заданной длины
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()"; // Символы для генерации
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }


    // Тестирование методов класса FileGrouper на наборе файлов и запись результатов в список
    private static void testFileGroup(Set<File> files, List<String> results) {
        long startTime1 = System.nanoTime();
        for (int i = 0; i < 30; i++) {
            Set<File> copyFiles = new HashSet<>(files);
            FileGrouper fileGrouper = new FileGrouper();
            fileGrouper.groupByHesh(copyFiles);    // Тестирование времени выполнения метода `groupByHesh`
        }
        long endTime1 = System.nanoTime();
        long duration1 = endTime1 - startTime1;
        results.add("groupByHesh executed in " + duration1/1000000 + " ms");

        long startTime2 = System.nanoTime();
        for (int i = 0; i < 30; i++) {
            Set<File> copyFiles = new HashSet<>(files);
            FileGrouper fileGrouper = new FileGrouper();
            fileGrouper.groupByHeshParallel(copyFiles);   // Тестирование времени выполнения метода `groupByHeshParallel`
        }
        long endTime2 = System.nanoTime();
        long duration2 = endTime2 - startTime2;
        results.add("groupByHeshParallel executed in " + duration2/1000000 + " ms");

        long startTime3 = System.nanoTime();
        for (int i = 0; i < 30; i++) {

                Set<File> copyFiles = new HashSet<>(files);
                FileGrouper fileGrouper = new FileGrouper();
                fileGrouper.groupByContent(copyFiles);    // Тестирование времени выполнения метода `groupByContent`

        }
        long endTime3 = System.nanoTime();
        long duration3 = endTime3 - startTime3;
        results.add("groupByContent executed in " + duration3/1000000 + " ms");

        long startTime4 = System.nanoTime();
        for (int i = 0; i < 30; i++) {
                Set<File> copyFiles = new HashSet<>(files);
                FileGrouper fileGrouper = new FileGrouper();
                fileGrouper.groupByContentParallel(copyFiles);     // Тестирование времени выполнения метода `groupByContentParallel`
        }
        long endTime4 = System.nanoTime();
        long duration4 = endTime4 - startTime4;
        results.add("groupByContentParallel executed in " + duration4/1000000 + " ms");

//        testMethodExecutionTime("groupByHesh", () -> fileGrouper.groupByHesh(files), results);
//        testMethodExecutionTime("groupByHeshParallel", () -> fileGrouper.groupByHeshParallel(files), results);
//        testMethodExecutionTime("groupByContent", () -> {
//            try {
//                fileGrouper.groupByContent(files);
//            } catch (IOException e) {
//                System.out.println("Error: fileGrouper.groupByContent(files); " + e.getMessage());
//                throw new RuntimeException(e);
//            }
//        }, results);
//        // Тестирование времени выполнения метода `groupByContentParallel`
//        testMethodExecutionTime("groupByContentParallel", () -> {
//            try {
////                fileGrouper.groupByContentParallel(files);
//                fileGrouper.groupByContentParallel(files);
//            } catch (IOException e) {
//                System.out.println("Error: fileGrouper.groupByContentParallel(files); " + e.getMessage());
//                throw new RuntimeException(e);
//            }
//        }, results);

    }

    // Тестирование времени выполнения метода
    private static void testMethodExecutionTime(String methodName, Runnable method, List<String> results) {
        long startTime = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(method);
            thread.start();
            try {
                thread.join(); // Ожидаем завершения потока
            } catch (InterruptedException e) {
                System.out.println("Error !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! : thread.join(); " + e.getMessage());
                e.printStackTrace();
            }
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        results.add(methodName + " executed in " + duration/1000000 + " ms");

    }
}
