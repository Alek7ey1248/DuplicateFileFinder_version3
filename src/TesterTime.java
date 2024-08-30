import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TesterTime {



    // Время выполнения метода walkFileTree класса FileDuplicateFinder
    // @param parths - пути к папкам
    public void timeWalkFileTree(String[] parths) throws IOException {

        // Засекаем время выполнения метода walkFileTree
        long startTime = System.currentTimeMillis();

        FileDuplicateFinder finder = new FileDuplicateFinder();
        Map<Long, List<Path>> filesBySize = new HashMap<>();
        String parth = parths[0]; //  берем только первый путь так как в методе walkFileTree только один путь
        finder.walkFileTree(parth, filesBySize);

        long endTime = System.currentTimeMillis();
        long duration = (long) ((endTime - startTime)/ 1000.0);
        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("Путь: " + Arrays.toString(parths) + " ---");
        System.out.println("Время выполнения walkFileTree " + duration + " милисекунд       " + (long)duration/1000.0 + " секунд");

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

    public static void main(String[] args) throws IOException {

        // тайминги метода walkFileTree для разных папок
        System.out.println();
        System.out.println("Тайминги метода walkFileTree для разных папок");
        TesterTime tester = new TesterTime();
        tester.timeWalkFileTree(new String[] {"/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder"});
        tester.timeWalkFileTree(new String[] {"/home/alek7ey/Рабочий стол"});
        tester.timeWalkFileTree(new String[] {"/home/alek7ey/IdeaProjects"});
        tester.timeWalkFileTree(new String[] {"/home/alek7ey"});
        tester.timeWalkFileTree(new String[] {"/home"});
       }

}
