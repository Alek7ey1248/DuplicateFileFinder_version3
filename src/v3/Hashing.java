package v3;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;


public class Hashing {

    private static int BUFFER_SIZE; // Оптимальный размер буфера на основе доступной памяти
    private static MessageDigest digest;  // экземпляр MessageDigest для алгоритма SHA-256
    private static int LARGE_FILE_THRESHOLD; // Порог для больших файлов
    private final ForkJoinPool forkJoinPool;
    // много поточность

    // конструктор
    public Hashing() {
        BUFFER_SIZE = getOptimalBufferSize();
        LARGE_FILE_THRESHOLD = getOptimalLargeFileSize();
        try { // Получаем экземпляр MessageDigest для алгоритма SHA-256
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-256 не найден", e);
        }
        this.forkJoinPool = new ForkJoinPool();
        //this.forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    /* Метод для расчета хеша файла
    * @param file - файл, для которого нужно рассчитать хеш
     */
    public long calculateHash(File file) {
        // если файл пустой, возвращаем -1
        if (file.length() == 0) {
            return -1;
        }

        if (file.length() < LARGE_FILE_THRESHOLD) {
            return calculateHashSmallFile(file);
        } else {
            try {
                return calculateHashLargeFile(file);
            } catch (IOException | NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
                System.err.println("Ошибка при обработке файла " + file.getAbsolutePath() + ": " + e.getMessage());
                return -1;
            }
        }
    }


    /* Метод для расчета хеша файла с учетом размера файла
    * @param file - файл, для которого нужно рассчитать хеш
     */
    private long calculateHashSmallFile(File file) {
        System.out.println(" обработка SmallFile - " + file.getAbsolutePath());
            // Используем BufferedInputStream для уменьшения количества операций ввода-вывода
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Файл не найден: ------ " + file.getAbsolutePath() + ": " + e.getMessage());
                return -1;
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла " + file.getAbsolutePath() + ": " + e.getMessage());
                return -1;
            }
        // Получаем байты хеша и преобразуем их в целое число
            byte[] hashBytes = digest.digest();
            Long hash = 0l;
            for (byte b : hashBytes) {
                hash = (hash << 8) + (b & 0xff);
            }
            return hash;
    }



    /* Метод для расчета хеша больших файлов с использованием многопоточности
    * @param file - файл, для которого нужно рассчитать хеш
     */

    private long calculateHashLargeFile(File file) throws IOException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        System.out.println(" обработка LargeFile - " + file.getAbsolutePath());
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            ForkJoinTask<byte[]> task = forkJoinPool.submit(() -> {
                try {
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        byte[] chunk = new byte[bytesRead];
                        System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                        MessageDigest chunkDigest = MessageDigest.getInstance("SHA-256");
                        chunkDigest.update(chunk);
                        byte[] chunkHash = chunkDigest.digest();
                        digest.update(chunkHash);
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                return digest.digest();
            });

            byte[] hashBytes = task.get();
            long hash = 0L;
            for (byte b : hashBytes) {
                hash = (hash << 8) + (b & 0xff);
            }
            return hash;
        }
    }


    /* этот вариант пока не работает - перегружает память
//    private long calculateHashLargeFile(File file) throws IOException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
//        System.out.println(" обработка - " + file.getAbsolutePath());
//        List<Future<byte[]>> futures = new ArrayList<>();
//        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
//            byte[] buffer = new byte[BUFFER_SIZE];
//            int bytesRead;
//            while ((bytesRead = bis.read(buffer)) != -1) {
//                byte[] chunk = new byte[bytesRead];
//                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
//                futures.add(executorService.submit(() -> {
//                    MessageDigest chunkDigest = MessageDigest.getInstance("SHA-256");
//                    chunkDigest.update(chunk);
//                    return chunkDigest.digest();
//                }));
//            }
//        }
//        for (Future<byte[]> future : futures) {
//            byte[] chunkHash = future.get();
//            digest.update(chunkHash);
//        }
//        byte[] hashBytes = digest.digest();
//        long hash = 0L;
//        for (byte b : hashBytes) {
//            hash = (hash << 8) + (b & 0xff);
//        }
//        return hash;
//    }



    /* Метод для определения оптимального размера буфера на основе доступной памяти
    * Метод для определения оптимального размера буфера на основе доступной памяти и количества процессоров
    * Оптимальный размер буфера - это 1/8 от максимальной памяти, деленной на количество процессоров
     */
    private int getOptimalBufferSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        long bsLong = maxMemory / (availableProcessors * 8192L * 8);
        int bs = (int)bsLong ;

        int minBufferSize = 1024 * availableProcessors / 2 ;
        return Math.max(bs, minBufferSize);
    }


    /* Метод для определения оптимального размера большого файла для многопоточного хеширования
        * Оптимальный размер файла - это 1/4 от максимальной п��мяти, деленной на количество процессоров
     */
    private int getOptimalLargeFileSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // Устанавливаем оптимальный размер файла как 1/4 от максимальной памяти, деленной на количество процессоров
        return (int) (maxMemory / (availableProcessors * 4));
    }




    public static void main(String[] args) {
//        // Получаем экземпляр Runtime
//        Runtime runtime = Runtime.getRuntime();
//
//        // Получаем максимальное количество памяти, которое может быть использовано JVM
//        long maxMemory = runtime.maxMemory();
//
//        // Выводим объем памяти в гигабайтах
//        System.out.println("Максимальный объем памяти (в байтах): " + maxMemory);
//
//        // Получаем количество доступных процессоров
//        int availableProcessors = Runtime.getRuntime().availableProcessors();
//        System.out.println("кол-во процессоров: " + availableProcessors);
//
//
//
//        long bsLong = maxMemory / (availableProcessors * 8192L * 8);
//        int bs = (int)bsLong ;
//
//        int minBufferSize = 1024 * availableProcessors / 2 ;
//        int bufferSize = Math.max(bs, minBufferSize);
//        System.out.println("bs: " + bs + "         bufferSize: " + bufferSize);


//        Hashing hashing = new Hashing(executorService);
//        //File emptyFile = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/aaaaaaaa");
//        //File emptyFile = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/aaaaaaaa");
//        //File emptyFile = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/g1.txt");
//        File emptyFile = new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.sudo_as_admin_successful");
//
//
//        long hash = hashing.calculateHash(emptyFile);
//        System.out.println("Hash пустого  file: " + hash);
    }
}