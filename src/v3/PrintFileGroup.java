package v3;

import java.io.File;
import java.util.*;

public class PrintFileGroup {

    // method for outputting res to the console
    public void printDuplicateGroups(List<List<File>> duplicateGroups) {

        for (List<File> group : duplicateGroups) {

            if (!group.isEmpty()) {
                System.out.println();
                boolean bool = true;

                for (File file : group) {
                    if (bool) {
                        System.out.println("  Group of identical files of type - " + file.getName() + ",  size - " + file.length() + " -------------------");
                        bool = false;
                    }

                    System.out.println(file.getAbsolutePath());
                }
                System.out.println();
            }
        }
    }

}