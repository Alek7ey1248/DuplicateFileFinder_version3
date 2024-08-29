import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TesterTime {



    // Время выполнения метода walkFileTree класса FileDuplicateFinder
    // @param parths - пути к папкам
    public void timeWalkFileTree(String[] parths) throws IOException {

        long startTime = System.currentTimeMillis();

        FileDuplicateFinder finder = new FileDuplicateFinder();
        Map<Long, List<Path>> filesBySize = new HashMap<>();

        long endTime = System.currentTimeMillis();
        long duration = (long) ((endTime - startTime)/ 1000.0);
        System.out.println("Время выполнения: " + duration + " s");
    }

    public static void main(String[] args) throws IOException {

       }

}
