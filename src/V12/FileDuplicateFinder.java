package V12;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    private final Map<Long, Set<File>> filesBySize;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    //Map<Long, Set<File>> getFilesBySize() {return filesBySize;}

    private final Map<Long, List<Set<File>>> duplicatesBySize;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    public Map<Long, List<Set<File>>> getDuplicatesBySize() {return duplicatesBySize;}

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.filesBySize = new ConcurrentHashMap<>();
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
        mapToMap();  // Перегруппировка файлов по их содержимому в группы дубликатов
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) throws IOException {
        File directory = new File(path);  // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            for (File file : files) {  // Перебираем каждый файл и директорию в текущей директории
                if (file.isDirectory()) {    // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                    walkFileTree(file.getAbsolutePath());
                } else {
                    if (checkValid.isValidFile(file)) {  // Проверка валидности файла
                        // Если текущий файл не является директорией, добавляем его в карту
                        // Группируем файлы по их размеру
                        filesBySize.computeIfAbsent(file.length(), k -> new HashSet<>()).add(file);
                    }
                }
            }
        }
    }

    /* Метод для перегруппировки файлов по их содержимому в группы дубликатов
     * Перегруппировка файлов происходит из карты filesBySize в карту duplicatesBySize
     */
    private void mapToMap() {
        ExecutorService executor1 = Executors.newVirtualThreadPerTaskExecutor();
        ExecutorService executor2 = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Void>> futures = new ArrayList<>();

        for (Map.Entry<Long, Set<File>> entry : filesBySize.entrySet()) {
            // Если в группе меньше 2 файлов, пропускаем
            if (entry.getValue().size() < 2) {
                continue;
            }

            Set<File> files = entry.getValue();

            // Создаем задачу для executor2
            futures.add(executor2.submit(() -> {
                List<Future<Void>> innerFutures = new ArrayList<>();

                for (File file : files) {
                    // Создаем задачу для executor1
                    innerFutures.add(executor1.submit(() -> {
                        try {
                            addFile(file, entry.getKey());
                            return null;
                        } catch (IOException e) {
                            // Обработка ошибки
                            System.err.println("Ошибка при добавлении файла: " + file.getPath());
                            return null; // Возвращаем null или обрабатываем ошибку
                        }
                    }));
                }

                // Ждем завершения всех задач innerFutures
                for (Future<Void> innerFuture : innerFutures) {
                    try {
                        innerFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println("Ошибка при получении результата выполнения задачи: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                return null;
            }));
        }

        // Ждем завершения всех задач в futures
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Ошибка при получении результата выполнения задачи: " + e.getMessage());
                e.printStackTrace();
            }
        }

        executor1.shutdown();
        executor2.shutdown();
    }



    /* Метод для добавления файла в Map<Long, List<Set<File>>> duplicatesBySize
     * @param file - файл, который нужно добавить в duplicatesBySize
     */
    private void addFile(File file, long keySize) throws IOException {
        System.out.println(" " + file.getAbsolutePath());
        //long fileSize = file.length();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Void>> futures = new ArrayList<>();

        // Проверяем, есть ли ключ с таким размером файла
        duplicatesBySize.compute(keySize, (key, fileList) -> {
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