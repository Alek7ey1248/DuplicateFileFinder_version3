package V11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileComporatorNew {


    public static boolean areFilesEqual(Path file1, Path file2) throws IOException {
        long size = Files.size(file1);  // Получаем размеры файлов
        if (size == 0) {   // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
            return true;
        }
        byte[] file1Bytes = Files.readAllBytes(file1);  // Считываем содержимое файлов
        byte[] file2Bytes = Files.readAllBytes(file2);
        return compareFiles(file1Bytes, file2Bytes);
    }

    // Сравниваем содержимое файлов
    private static boolean compareFiles(byte[] file1Bytes, byte[] file2Bytes) {
        return Arrays.equals(file1Bytes, file2Bytes);
    }


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        try {
            //Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback (копия).mp4");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4");

            Path file1 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат.zip");
            Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат1.zip");
            //Path file2 = Path.of("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (Копия 3).zip");
            System.out.println(areFilesEqual(file1, file2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения сравнения файлов --- " + duration + " ms       ");
    }
}