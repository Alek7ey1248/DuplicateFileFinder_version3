package v3;

import java.io.File;
import java.util.*;

public class FileDuplicateFinder {

    private final Hashing hashing;

    public FileDuplicateFinder() {
        this.hashing = new Hashing();
    }

    //   main method - splits duplicates into groups according to the key that
    //     calculated by the calculateHashWithSize method of the Hashing class
    //     (hashing the file contents).
    //     Then using the filterAndSortDuplicateGroups method it distills
    //     in List<List<File>> and sorts.
    public List<List<File>> findDuplicates(List<File> fileList) {
        Map<Integer, List<File>> contentHashGroups = Collections.synchronizedMap(new HashMap<>());

        fileList.parallelStream().forEach(file -> {
            int hash = hashing.calculateHashWithSize(file);
            if (hash != -1) {
                synchronized (contentHashGroups) {
                    contentHashGroups.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
                }
            }
        });

        return filterAndSortDuplicateGroups(contentHashGroups);
    }


    // from HashMap all groups (> 1 file) into List<List<File>> and sorts
    private List<List<File>> filterAndSortDuplicateGroups(Map<Integer, List<File>> contentHashGroups) {

        List<List<File>> duplicateGroups = new ArrayList<>();

        for (List<File> group : contentHashGroups.values()) {
            if (group.size() > 1) {
                duplicateGroups.add(group);
            }
        }

        Collections.sort(duplicateGroups, new FileListSizeComparator());

        return duplicateGroups;
    }

//   first version of the method - !!! identical keys are possible due to multithreading

//        public List<List<File>> findDuplicates(List<File> fileList) {
//
//        Map<Integer, List<File>> contentHashGroups = fileList.parallelStream()
//                .collect(Collectors.groupingByConcurrent(hashing::calculateHashWithSize));
//
//        contentHashGroups.remove(-1); // в методе calculateContentHash -1 если невалидный файл
//
//        return filterAndSortDuplicateGroups(contentHashGroups);
//    }




  // the second version of the method so that there are no identical keys (using merge)

//    public List<List<File>> findDuplicates(List<File> fileList) {
//        ConcurrentHashMap<Integer, List<File>> contentHashGroups = new ConcurrentHashMap<>();
//
//        fileList.parallelStream().forEach(file -> {
//            int hash = hashing.calculateHashWithSize(file);
//            if (hash != -1) {
//                contentHashGroups.merge(hash, new CopyOnWriteArrayList<>(Arrays.asList(file)), (list1, list2) -> {
//                    list1.addAll(list2);
//                    return list1;
//                });
//            }
//        });
//
//        return filterAndSortDuplicateGroups(contentHashGroups);
//    }
//
}

