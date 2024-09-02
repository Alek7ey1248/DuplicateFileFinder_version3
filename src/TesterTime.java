import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TesterTime {



    // Время выполнения метода walkFileTree класса FileDuplicateFinder
    // @param parths - пути к папкам
    public void timeWalkFileTree(String[] paths) throws IOException {

        // Засекаем время выполнения метода walkFileTree
        long startTime = System.currentTimeMillis();

        FileDuplicateFinder finder = new FileDuplicateFinder();
        Map<Long, List<Path>> filesBySize = new HashMap<>();
        String directory = paths[0]; //  берем только первый путь так как в методе walkFileTree только один путь
        finder.walkFileTree(directory, filesBySize);

        long endTime = System.currentTimeMillis();
        long duration = (long) ((endTime - startTime)/ 1000.0);

        //long sizePath = getDirectorySize(Paths.get(directory));

        System.out.println();
        System.out.println("Путь: " + directory + " ---");
        System.out.println("Время выполнения walkFileTree " + duration + " секунд       " + (long)duration * 1000.0 + " милисекунд");

        // Подсчет количества файлов
        int countFiles = 0;
        for (Map.Entry<Long, List<Path>> entry : filesBySize.entrySet()) {
            for (Path path : entry.getValue()) {
                countFiles++;
            }
        }
        System.out.println("Количество файлов: " + countFiles);
        System.out.println();
    }


    // Тайминги метода areFilesEqual - сравнение файлов
    public void timeAreFilesEqual(Path file1, Path file2) throws IOException {
        // Засекаем время выполнения метода areFilesEqual
        long startTime = System.currentTimeMillis();

        System.out.println("-------------------------------------------------");

        FileComparator fileComparator = new FileComparator();

        fileComparator.areFilesEqual(file1, file2);

        long endTime = System.currentTimeMillis();
        long duration = (long) ((endTime - startTime)/ 1000.0);
        System.out.println("Время выполнения areFilesEqual на сравнении файлов " + file1.getFileName() + " и " + file2.getFileName() + " , размер файлов - " + Files.size(Path.of(file1.toString())) + " --- " + duration + " секунд       " + (long)duration * 1000.0 + " милисекунд");
    }



    public static void main(String[] args) throws IOException {

        // тайминги метода walkFileTree для разных папок
        System.out.println();
        System.out.println("Тайминги метода walkFileTree для разных папок");
        TesterTime tester = new TesterTime();

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println(" Размер - 37 объектов, всего 6,7 ГБ ");
        tester.timeWalkFileTree(new String[] {"/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder"});

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println(" Размер - 1183 объекта, всего 10,5 ГБ ");
        tester.timeWalkFileTree(new String[] {"/home/alek7ey/Рабочий стол"});

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println(" Размер - 5933 объекта, всего 289,7 МБ ");
        tester.timeWalkFileTree(new String[] {"/home/alek7ey/IdeaProjects"});

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println(" Размер - 169805 объектов, всего 102,2 ГБ ");
        tester.timeWalkFileTree(new String[] {"/home/alek7ey"});



        // тайминги метода areFilesEqual - сравнение файлов
        System.out.println();
        System.out.println("******************************************");
        System.out.println("*******************************************");
        System.out.println("Тайминги метода areFilesEqual - сравнение файлов");
        System.out.println();
        tester.timeAreFilesEqual(Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc"), Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc (копия)"));
        tester.timeAreFilesEqual(Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"), Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg"));
        tester.timeAreFilesEqual(Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"), Path.of("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат"));
       }

}
