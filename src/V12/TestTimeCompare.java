package V12;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class TestTimeCompare {

    File file1;
    File file2;

    // конструктор
    public TestTimeCompare(File file1, File file2) {
        this.file1 = file1;
        this.file2 = file2;
    }


    //  метод для определения списков файлов одинакового размера для последующего тестирования
    private void testWalkFileTree() {
        FileDuplicateFinder finder = new FileDuplicateFinder();
        finder.walkFileTree("/home/alek7ey/snap");
        Set<File> files = finder.getFileBySize().get(10376644L);
        for (File file : files) {
            System.out.println(file);
        }
    }


    // тест времени сравнения 2 файлов методом compareFiles
    private void testCompareFiles() throws IOException {

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            FileComparator.compareFiles(file1.toPath(), file2.toPath());
        }
        boolean result = FileComparator.compareFiles(file1.toPath(), file2.toPath());
        System.out.println(result);
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения сравнения 2 файлов методом compareFiles - " + duration + " ms       ");
    }

    // тест времени сравнения 2 файлов методом compareLargeFiles
    private void testcompareLargeFiles() throws IOException, InterruptedException {

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            FileComparator.compareLargeFiles(file1.toPath(), file2.toPath());
        }
        boolean result = FileComparator.compareLargeFiles(file1.toPath(), file2.toPath());
        System.out.println(result);
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения сравнения 2 файлов методом compareLargeFiles - " + duration + " ms       ");
    }

    // тест времени сравнения 2 файлов методом хеширования
    private void testHashFiles() throws IOException, NoSuchAlgorithmException {

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            FileKeyHash fileKeyHash1 = new FileKeyHash(file1);
            FileKeyHash fileKeyHash2 = new FileKeyHash(file2);
        }
        if (new FileKeyHash(file1).equals(new FileKeyHash(file2))) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения сравнения 2 файлов методом хеширования FileKeyHash - " + duration + " ms       ");
    }


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {

        // одинаковые файлы для сравнения размер - 49460288 байт compareFiles - 146370 ms, compareLargeFiles -  17752 ms, хеш - 73238 ms
//        File file1 = new File("/home/alek7ey/snap/flutter/common/flutter/bin/cache/artifacts/engine/linux-x64/frontend_server.dart.snapshot");
//        File file2 = new File("/home/alek7ey/snap/flutter/common/flutter/bin/cache/dart-sdk/bin/snapshots/frontend_server.dart.snapshot");

        // разные файлы для сравнения размер - 49460288 байт compareFiles - 69 ms, compareLargeFiles - 7127  ms, хеш -   43903 ms
//        File file1 = new File("/home/alek7ey/snap/flutter/common/flutter/bin/cache/dart-sdk/bin/snapshots/frontend_server.dart.snapshot");
//        File file2 = new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/40/2BC5EBB40B2C");
        // ---------------------------------------------------------------------------

        // одинаковые файлы для сравнения размер - 8388724 байт  - compareFiles - 23441 ms, compareLargeFiles - 2848 ms, FileKeyHash - 12278 ms
//        File file1 = new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/E8/707358D8E54B");
//        File file2 = new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/E8/707358D8E54B");

        // разные файлы для сравнения размер - 8388724 байт  - compareFiles - 80 ms, compareLargeFiles - 1737 ms, FileKeyHash - 12211 ms
//        File file1 = new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/E8/707358D8E54B");
//        File file2 = new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/A6/44381DBC74F1");
        // ---------------------------------------------------------------------------

        // одинаковые файлы для сравнения размер - 1048772 байт  - compareFiles - 3155 ms, compareLargeFiles - 602 ms, FileKeyHash - 1869 ms
//        File file1 = new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/9A/373CD675761B");
//        File file2 = new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/9A/373CD675761B");

        // разные файлы для сравнения размер - 1048772 байт  - compareFiles - 62 ms, compareLargeFiles - 421 ms, FileKeyHash - 1897 ms
        File file1 = new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/9A/373CD675761B");
        File file2 = new File("/home/alek7ey/snap/telegram-desktop/6504/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/43/7E931D2FB771");
        // ---------------------------------------------------------------------------

        // одинаковые файлы для сравнения размер - 524452 байт  - compareFiles - 1712 ms, compareLargeFiles - 524 ms, FileKeyHash - 1056 ms
//        File file1 = new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/43/5BCD56E45FBA");
//        File file2 = new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/43/5BCD56E45FBA");

        // разные файлы для сравнения размер - 524452 байт  - compareFiles - 76 ms, compareLargeFiles - 272 ms, FileKeyHash - 1140 ms
//        File file1 = new File("/home/alek7ey/snap/telegram-desktop/6495/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/43/5BCD56E45FBA");
//        File file2 = new File("/home/alek7ey/snap/telegram-desktop/current/.local/share/TelegramDesktop/tdata/user_data/media_cache/0/63/3FC93CE2F9A3");



        // одинаковые файлы для сравнения размер - 35288 байт  -   все методы сравнения одинаково по времени +-
//    File file1 = new File("/home/alek7ey/snap/mysql-workbench-community/13/.local/share/icons/Adwaita/256x256/legacy/security-medium.png");
//    File file2 = new File("/home/alek7ey/snap/mysql-workbench-community/current/.local/share/icons/Adwaita/256x256/legacy/security-medium.png");





        TestTimeCompare testTime = new TestTimeCompare(file1, file2);
        //testTime.testWalkFileTree();
        testTime.testCompareFiles();
        testTime.testcompareLargeFiles();
        testTime.testHashFiles();

    }

}
