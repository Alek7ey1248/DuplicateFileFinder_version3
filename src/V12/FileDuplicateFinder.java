package V12;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    private final Map<Long, List<Set<File>>> duplicatesBySize;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    public Map<Long, List<Set<File>>> getDuplicatesBySize() {return duplicatesBySize;}

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        //this.duplicatesBySize = Collections.synchronizedMap(new HashMap<>());
        this.duplicatesBySize = new ConcurrentHashMap<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }
        //removeSingleFiles();  // Удаляем группы с одним файлом и пустые группы
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) throws IOException {
        File directory = new File(path);  // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        //ExecutorService executor = Executors.newFixedThreadPool((int) (Runtime.getRuntime().availableProcessors() * 1.25));
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Boolean>> futures = new ArrayList<>();

        if (files != null) {  // Проверяем, что массив не пустой
            for (File file : files) {  // Перебираем каждый файл и директорию в текущей директории
                if (file.isDirectory()) {    // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                    walkFileTree(file.getAbsolutePath());
                } else {
                    if (checkValid.isValidFile(file)) {  // Проверка валидности файла
                        Future<Boolean> future = executor.submit(() -> {
                            try {
                                addFile(file);  // Добавляем файл в HashMap filesBySize
                            } catch (IOException e) {
                                System.out.println("Ошибка при добавлении файла в HashMap filesBySize - " + e.getMessage());
                                e.printStackTrace();
                            }
                            return true;
                        });
                        futures.add(future);
                    }
                }
            }

            for (Future<Boolean> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    System.out.println("Ошибка при получении результата выполнения задачи - " + e.getMessage());
                    e.printStackTrace();
                }
            }

            executor.shutdown();
        }
    }


    /* Метод для добавления файла в Map<Long, List<Set<File>>> duplicatesBySize
     * @param file - файл, который нужно добавить в duplicatesBySize
     */
    private void addFile(File file) throws IOException {
        System.out.println(" " + file.getAbsolutePath());
        long fileSize = file.length();

        // Проверяем, есть ли ключ с таким размером файла
        duplicatesBySize.compute(fileSize, (key, fileList) -> {
            if (fileList == null) {
                // Если нет списка для данного размера, создаем новый и добавляем туда файл
                List<Set<File>> newFileList = new CopyOnWriteArrayList<>();
                Set<File> newFileSet = new HashSet<>();
                newFileSet.add(file);
                newFileList.add(newFileSet);
                return newFileList;
            }

            // Если список существует, ищем, куда добавить файл
            boolean isAdded = false;
            for (Set<File> fileSet : fileList) {
                try {
                    if (FileComparator.areFilesEqual(file.toPath(), fileSet.iterator().next().toPath())) {
                        fileSet.add(file);
                        isAdded = true; // файл добавлен
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при сравнении файлов - " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            if (!isAdded) {
                // Если файл не был добавлен, создаем новую группу и добавляем туда файл
                Set<File> newFileSet = new HashSet<>();
                newFileSet.add(file);
                fileList.add(newFileSet);
            }
            return fileList;
        });
    }

//    private void addFile(File file) throws IOException {
//
//        System.out.println(" " + file.getAbsolutePath());
//
//        // Проверяем, есть ли ключ с таким размером файла
//        if(duplicatesBySize.containsKey(file.length())) {
//
//            //  если ключ есть, то ищем куда добавить файл
//           List<Set<File>> fileList = duplicatesBySize.get(file.length());
//           boolean isAdded = false;  // Переменная для проверки добавлен ли куда либо файл в существующую группу
//              for(Set<File> fileSet : fileList) {
//                if (FileComparator.areFilesEqual(file.toPath(), fileSet.iterator().next().toPath())) {
//                    fileSet.add(file);
//                    isAdded = true;   // файл добавлен
//                    return;
//                }
//              }
//                if(!isAdded) {  // Если файл не был добавлен в существующую группу, создаем новую группу и добавляем туда файл
//                    Set<File> newFileSet = new HashSet<>();
//                    newFileSet.add(file);
//                    fileList.add(newFileSet);
//                }
//        } else {  // Если ключа с таким размером файла нет, создаем новую группу и добавляем туда файл
//            List<Set<File>> fileList = new ArrayList<>();
//            Set<File> newFileSet = new HashSet<>();
//            newFileSet.add(file);
//            fileList.add(newFileSet);
//            duplicatesBySize.put(file.length(), fileList);
//        }
//    }


    /* Метод удаления групп Set<File> с одним файлом и пустых списков List<Set<File>>>*/
		public void removeSingleFiles() {
            // Итератор для обхода duplicatesBySize
            Iterator<Map.Entry<Long, List<Set<File>>>> iterator = duplicatesBySize.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, List<Set<File>>> entry = iterator.next();
                List<Set<File>> groups = entry.getValue();
                Iterator<Set<File>> iterator1 = groups.iterator();
                while (iterator1.hasNext()) {
                    Set<File> group = iterator1.next();
                    if (group.size() < 2) {
                        iterator1.remove();
                    }
                }
                if (groups.isEmpty()) {
                    iterator.remove();
                }
            }

        }

}