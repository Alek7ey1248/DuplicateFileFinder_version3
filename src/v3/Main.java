package v3;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        long startTime = System.nanoTime();

        //String[] paths = "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder";
        String[] paths = {"/home/alek7ey"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы"};
        //String[] paths = {"/home/alek7ey/.cache"};
        //String[] paths = {"/home/alek7ey/snap"};
        //String[] paths = {"/home/alek7ey/Рабочий стол"};
        //String[] paths = {"/swapfile"};
        //String[] paths = {"/home/alek7ey/Рабочий стол/TestsDFF"};



        // проверим валидность аргументов
            //CheckValid isValid = new CheckValid();
            //List<String> validPaths = isValid.getValidDirectoryPaths(paths);   !!!!!! НАДО СДЕЛАТЬ !!!!!

            FileDuplicateFinder3 fdf = new FileDuplicateFinder3();;
            fdf.findDuplicates(paths);


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Перевод наносекунд в миллисекунды
        System.out.println("Program execution time: " + duration + " ms");



    }
}
