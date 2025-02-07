package V12;

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
        Set<File> smallFiles2Id = generateIdenticalFiles(2, 2); // Файлы почти 0 байт
        Set<File> mediumFiles2Id = generateIdenticalFiles(10, 2); // Файлыдо 10 байт
        Set<File> largeFiles2Id = generateIdenticalFiles(1000, 2); // Файлы до 1 Кб
        Set<File> hugeFiles2Id = generateIdenticalFiles(10000, 2); // Файлы до 100 Кб
        Set<File> giganticFiles2Id = generateIdenticalFiles(300000000, 2); // Файлы до 300 Мб

        // Генерация тестовых данных c 4 файлами каждого размера
        Set<File> smallFiles4Id = generateIdenticalFiles(2, 4); // Файлы почти 0 байт
        Set<File> mediumFiles4Id = generateIdenticalFiles(10, 4); // Файлыдо 10 байт
        Set<File> largeFiles4Id = generateIdenticalFiles(1000, 4); // Файлы до 1 Кб
//        Set<File> hugeFiles4Id = generateIdenticalFiles(10000, 4); // Файлы до 100 Кб
        Set<File> hugeFiles4Id = new HashSet<>();  // 4 одинаковых файлов по 346826 байт байт

        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-7-2525.pdf"));
        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-5x-7-2525.pdf"));
        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-4100.pdf"));
        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-1-2525.pdf"));

//        Set<File> giganticFiles4Id = generateIdenticalFiles(300000000, 4); // Файлы до 300 Мб
        Set<File> giganticFiles4Id = new HashSet<>();  // 4 одинаковых файлов по 3.4 Гбайт

        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-7-2525.pdf"));
        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-5x-7-2525.pdf"));
        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-4100.pdf"));
        hugeFiles4Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-1-2525.pdf"));


        // Генерация тестовых данных c 8 файлами каждого размера
        Set<File> smallFiles8Id = generateIdenticalFiles(2, 8); // Файлы почти 0 байт
        Set<File> mediumFiles8Id = generateIdenticalFiles(10, 8); // Файлыдо 10 байт
        Set<File> largeFiles8Id = generateIdenticalFiles(1000, 8); // Файлы до 1 Кб
//        Set<File> hugeFiles8Id = generateIdenticalFiles(10000, 8); // Файлы до 100 Кб
        Set<File> hugeFiles8Id = new HashSet<>();  // 8 одинаковых файлов по 346826 байт байт

        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-7-2525.pdf"));
        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-5x-7-2525.pdf"));
        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-4100.pdf"));
        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-1-2525.pdf"));
        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1-5x-2100.pdf"));
        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-75x-25.pdf"));
        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-4x-625.pdf"));
        hugeFiles8Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-7x-7-2525.pdf"));


//        Set<File> giganticFiles8Id = generateIdenticalFiles(300000000, 8); // Файлы до 300 Мб
        Set<File> giganticFiles8Id = new HashSet<>();  // 8 одинаковых файлов по 3.4 Гбайт

        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (копия)"));
        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (другая копия)"));
        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"));
        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"));
        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат"));
        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия)"));
        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия 2)"));
        giganticFiles8Id.add(new File("/home/alek7ey/Рабочий стол/Доп файлы для TestTimeFileGrouper/фильм про солдат (другая копия) (Копия)"));


        // Генерация тестовых данных c 16 файлами каждого размера
        Set<File> smallFiles16Id = generateIdenticalFiles(2, 16); // Файлы почти 0 байт
        Set<File> mediumFiles16Id = generateIdenticalFiles(10, 16); // Файлыдо 10 байт
        Set<File> largeFiles16Id = generateIdenticalFiles(1000, 16); // Файлы до 1 Кб
//        Set<File> hugeFiles16Id = generateIdenticalFiles(10000, 16); // Файлы до 100 Кб
        Set<File> hugeFiles16Id = new HashSet<>();  // 16 одинаковых файлов по 346826 байт байт

        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-7-2525.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-5x-7-2525.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-4100.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-1-2525.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1-5x-2100.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-75x-25.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-4x-625.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-7x-7-2525.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-5x-1-2525.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-1250.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-4x-525.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-3100.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-4100.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-3x-8-62525.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1-5x-5100.pdf"));
        hugeFiles16Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1-5x-3100.pdf"));

        Set<File> giganticFiles16Id = generateIdenticalFiles(300000000, 16); // Файлы до 300 Мб

        // Генерация тестовых данных c 32 файлами каждого размера
        Set<File> smallFiles32Id = generateIdenticalFiles(2, 32); // Файлы почти 0 байт
        Set<File> mediumFiles32Id = generateIdenticalFiles(10, 32); // Файлыдо 10 байт
        Set<File> largeFiles32Id = generateIdenticalFiles(1000, 32); // Файлы до 1 Кб
//        Set<File> hugeFiles32Id = generateIdenticalFiles(10000, 32); // Файлы до 100 Кб
        Set<File> hugeFiles32Id = new HashSet<>();  // 32 одинаковых файлов по 346826 байт байт

        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-7-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-5x-7-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-4100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-3x-1-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1-5x-2100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-75x-25.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-4x-625.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-7x-7-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-5x-1-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-1250.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-4x-525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-3100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-4100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-3x-8-62525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1-5x-5100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1-5x-3100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-7-25x-1-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-75x-2100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-9100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-9250.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-5x-7250.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-0-75x-4100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-2x-1-25100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-0-5x-3100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-2x-9-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-35.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-4x-9-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-1-25100.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-6x-9-2525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/data/all/3m-425-0-5x-6250.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-10x-10-525.pdf"));
        hugeFiles32Id.add(new File("/home/alek7ey/IdeaProjects/ManipulationWithFilesAndPicturesUpwork/out/production/ManipulationWithFilesAndPicturesUpwork/all/3m-425-1x-45.pdf"));

        Set<File> giganticFiles32Id = generateIdenticalFiles(300000000, 32); // Файлы до 300 Мб



        // РАЗНЫЕ ФАЙЛЫ------------------------------------------------
        // Генерация тестовых данных c 2 файлами каждого размера
        Set<File> smallFiles2Di = generateDifferentFiles(2, 2); // Файлы почти 0 байт
        Set<File> mediumFiles2Di = generateDifferentFiles(10, 2); // Файлыдо 10 байт
        Set<File> largeFiles2Di = generateDifferentFiles(1000, 2); // Файлы до 1 Кб
        Set<File> hugeFiles2Di = generateDifferentFiles(10000, 2); // Файлы до 100 Кб
        Set<File> giganticFiles2Di = generateDifferentFiles(300000000, 2); // Файлы до 300 Мб

        // Генерация тестовых данных c 4 файлами каждого размера
        Set<File> smallFiles4Di = generateDifferentFiles(2, 4); // Файлы почти 0 байт
        Set<File> mediumFiles4Di = generateDifferentFiles(10, 4); // Файлыдо 10 байт
        Set<File> largeFiles4Di = generateDifferentFiles(1000, 4); // Файлы до 1 Кб
//        Set<File> hugeFiles4Di = generateDifferentFiles(10000, 4); // Файлы до 100 Кб
        Set<File> hugeFiles4Di = new HashSet<>();

        hugeFiles4Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles4Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles4Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles4Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));

        Set<File> giganticFiles4Di = generateDifferentFiles(300000000, 4); // Файлы до 300 Мб

        // Генерация тестовых данных c 8 файлами каждого размера
        Set<File> smallFiles8Di = generateDifferentFiles(2, 8); // Файлы почти 0 байт
        Set<File> mediumFiles8Di = generateDifferentFiles(10, 8); // Файлыдо 10 байт
        Set<File> largeFiles8Di = generateDifferentFiles(1000, 8); // Файлы до 1 Кб
//        Set<File> hugeFiles8Di = generateDifferentFiles(10000, 8); // Файлы до 100 Кб
        Set<File> hugeFiles8Di = new HashSet<>();

        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));
        hugeFiles8Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));

        Set<File> giganticFiles8Di = generateDifferentFiles(300000000, 8); // Файлы до 300 Мб

        // Генерация тестовых данных c 16 файлами каждого размера
        Set<File> smallFiles16Di = generateDifferentFiles(2, 16); // Файлы почти 0 байт
        Set<File> mediumFiles16Di = generateDifferentFiles(10, 16); // Файлыдо 10 байт
        Set<File> largeFiles16Di = generateDifferentFiles(1000, 16); // Файлы до 1 Кб
//        Set<File> hugeFiles16Di = generateDifferentFiles(10000, 16); // Файлы до 100 Кб
        Set<File> hugeFiles16Di = new HashSet<>();

        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_3"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_3"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_3"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_6"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_6"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_6"));
        hugeFiles16Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_4"));

        Set<File> giganticFiles16Di = generateDifferentFiles(300000000, 16); // Файлы до 300 Мб

        // Генерация тестовых данных c 32 файлами каждого размера
        Set<File> smallFiles32Di = generateDifferentFiles(2, 32); // Файлы почти 0 байт
        Set<File> mediumFiles32Di = generateDifferentFiles(10, 32); // Файлыдо 10 байт
        Set<File> largeFiles32Di = generateDifferentFiles(1000, 32); // Файлы до 1 Кб
//        Set<File> hugeFiles32Di = generateDifferentFiles(10000, 32); // Файлы до 100 Кб
        Set<File> hugeFiles32Di = new HashSet<>();

        // Добавляем файлы из вашего списка
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_2"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_1"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_0"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_3"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_3"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_3"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_6"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_6"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_6"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_4"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_4"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_4"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/emoji/cache_36_5"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/emoji/cache_36_5"));
        hugeFiles32Di.add(new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/emoji/cache_36_5"));
        Set<File> files = generateDifferentFiles(2654256, 11); // дополнительно генерируем 11 файлов по 2654256 Мб
        hugeFiles32Di.addAll(files); // добавляем их в список

        Set<File> giganticFiles32Di = generateDifferentFiles(300000000, 32); // Файлы до 300 Мб




        FileGrouper fileGrouper = new FileGrouper();

        // Список для хранения результатов тестов
        List<String> results = new ArrayList<>();

        // ТЕСТ РАЗНЫХ ФАЙЛОВ ==============================================================================================
        // Тестирование методов с наборами по 2 файлов каждого размера
        results.add("Testing Small Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles2Di, results);
        results.add("Testing Medium Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles2Di, results);
        results.add("Testing Large Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles2Di, results);
        results.add("Testing Huge Files 2 -РАЗНЫЕ ФАЙЛЫ-----------------------");
        testFileGroup(fileGrouper, hugeFiles2Di, results);
        results.add("Testing Gigantic Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles2Di, results);

        // Тестирование методов с наборами по 4 файлов каждого размера
        results.add("Testing Small Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles4Di, results);
        results.add("Testing Medium Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles4Di, results);
        results.add("Testing Large Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles4Di, results);
        results.add("Testing Huge Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles4Di, results);
        results.add("Testing Gigantic Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles4Di, results);

        // Тестирование методов с наборами по 8 файлов каждого размера
        results.add("Testing Small Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles8Di, results);
        results.add("Testing Medium Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles8Di, results);
        results.add("Testing Large Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles8Di, results);
        results.add("Testing Huge Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles8Di, results);
        results.add("Testing Gigantic Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles8Di, results);

        // Тестирование методов с наборами по 16 файлов каждого размера
        results.add("Testing Small Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles16Di, results);
        results.add("Testing Medium Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles16Di, results);
        results.add("Testing Large Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles16Di, results);
        results.add("Testing Huge Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles16Di, results);
        results.add("Testing Gigantic Files 16 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles16Di, results);

        // Тестирование методов с наборами по 32 файлов каждого размера
        results.add("Testing Small Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles32Di, results);
        results.add("Testing Medium Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles32Di, results);
        results.add("Testing Large Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles32Di, results);
        results.add("Testing Huge Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles32Di, results);
        results.add("Testing Gigantic Files 32 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles32Di, results);




        // ТЕСТ ОДИНАКОВЫХ ФАЙЛОВ =======================================================================================
        // Тестирование методов с наборами по 2 файлов каждого размера
        results.add("Testing Small Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles2Id, results);
        results.add("Testing Medium Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles2Id, results);
        results.add("Testing Large Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles2Id, results);
        results.add("Testing Huge Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles2Id, results);
        results.add("Testing Gigantic Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles2Id, results);

        // Тестирование методов с наборами по 4 файлов каждого размера
        results.add("Testing Small Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles4Id, results);
        results.add("Testing Medium Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles4Id, results);
        results.add("Testing Large Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles4Id, results);
        results.add("Testing Huge Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles4Id, results);
        results.add("Testing Gigantic Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles4Id, results);

        // Тестирование методов с наборами по 8 файлов каждого размера
        results.add("Testing Small Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles8Id, results);
        results.add("Testing Medium Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles8Id, results);
        results.add("Testing Large Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles8Id, results);
        results.add("Testing Huge Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles8Id, results);
        results.add("Testing Gigantic Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles8Id, results);

        // Тестирование методов с наборами по 16 файлов каждого размера
        results.add("Testing Small Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles16Id, results);
        results.add("Testing Medium Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles16Id, results);
        results.add("Testing Large Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles16Id, results);
        results.add("Testing Huge Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles16Id, results);
        results.add("Testing Gigantic Files 16 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles16Id, results);

        // Тестирование методов с наборами по 32 файлов каждого размера
        results.add("Testing Small Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles32Id, results);
        results.add("Testing Medium Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles32Id, results);
        results.add("Testing Large Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles32Id, results);
        results.add("Testing Huge Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles32Id, results);
        results.add("Testing Gigantic Files 32 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles32Id, results);



        // Вывод результатов всех тестов
        for (String result : results) {
            System.out.println(result);
        }
    }

    // Генерация набора одинаковых файлов заданного размера и количества
    private static Set<File> generateIdenticalFiles(int fileSize, int numberOfFiles) {
        Set<File> fileSet = new HashSet<>();
        String content = generateRandomString(fileSize); // Генерируем строку для содержимого

        for (int i = 0; i < numberOfFiles; i++) {
            try {
                // Создаем временный файл
                Path tempFile = Files.createTempFile("identicalFile" + i + "_", ".txt");
                // Записываем содержимое в файл
                Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
                fileSet.add(tempFile.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileSet;
    }


    // Генерация набора разных файлов заданного размера и количества
    private static Set<File> generateDifferentFiles(int fileSize, int numberOfFiles) {
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
    private static void testFileGroup(FileGrouper fileGrouper, Set<File> files, List<String> results) {
        long startTime = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            fileGrouper.groupByHesh(files);    // Тестирование времени выполнения метода `groupByHesh`
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        results.add("groupByHesh executed in " + duration/1000000 + " ms");

        startTime = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            fileGrouper.groupByHeshParallel(files);   // Тестирование времени выполнения метода `groupByHeshParallel`
        }
        endTime = System.nanoTime();
        duration = endTime - startTime;
        results.add("groupByHeshParallel executed in " + duration/1000000 + " ms");

        startTime = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            try {
                fileGrouper.groupByContent(files);    // Тестирование времени выполнения метода `groupByContent`
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        endTime = System.nanoTime();
        duration = endTime - startTime;
        results.add("groupByContent executed in " + duration/1000000 + " ms");

        startTime = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            try {
                fileGrouper.groupByContentParallel(files);     // Тестирование времени выполнения метода `groupByContentParallel`
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        endTime = System.nanoTime();
        duration = endTime - startTime;
        results.add("groupByContentParallel executed in " + duration/1000000 + " ms");

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
