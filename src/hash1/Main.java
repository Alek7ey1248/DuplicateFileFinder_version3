package hash1;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        long startTime = System.nanoTime();

        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder"};
        //String[] paths = {"/home/alek7ey"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы"};
        //String[] paths = {"/home/alek7ey/.cache"};
        //String[] paths = {"/home/alek7ey/snap"};
        //String[] paths = {"/home/alek7ey/.jdks"};
        //String[] paths = {"/home/alek7ey/.config"};
        String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF"};
        //String[] paths = {"/home/alek7ey/.local"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21"};

        FileDuplicateFinder fdf = new FileDuplicateFinder();;
        fdf.findDuplicates(paths);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Перевод наносекунд в миллисекунды
        System.out.println("Program execution time: " + duration + " ms");

    }
}
