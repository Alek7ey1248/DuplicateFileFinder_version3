package hash1;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class Hashing {

    private static final int BUFFER_SIZE = getOptimalBufferSize();  // 8192 - оптимальный размер буфера на основе доступной памяти используемый в java; // Оптимальный размер буфера на основе доступной памяти
    private static final int LARGE_FILE_SIZE = getOptimalLargeFileSize(); // порог для больших файлов
    private static final int NUM_BLOCKS = (int) (Runtime.getRuntime().availableProcessors() * 1.25); // Получаем количество блоков одновременно работающих = кол-во доступных процессоров
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Создаем пул потоков для многопоточности

    /* Метод для расчета хеша файла
    * @param file - файл, для которого нужно рассчитать хеш
     */
    public static long calculateHash(File file) {
        // если файл пустой, возвращаем -1
        if (file.length() == 0) {
            return -1;
        }

        if (file.length() < LARGE_FILE_SIZE) {
            // если файл меньше порога, используем bufferSize для не больших файлов
            return calculateHashSmallFile(file);
        }
        // если файл больше порога, используем bufferSize для больших файлов
        return calculateHashLargeFile(file);
    }


    /* Метод для расчета хеша небольшого файла
     * @param file - файл, для которого нужно рассчитать хеш
     */
    private static Long calculateHashSmallFile(File file) {
        System.out.println("Обработка SmallFile - " + file.getAbsolutePath());
        try {
            MessageDigest digest = createMessageDigest();  // создаем объект MessageDigest для хеширования
            updateDigestWithSmallFileContent(file, digest); // Обновляем хеш содержимым файла
            return convertHashToLong(digest); // Преобразуем хеш в число
        } catch (IOException | UncheckedIOException e) {
            System.err.println("Ошибка чтения файла " + file.getName() + ": " + e.getMessage());
            return -1L; // Возвращаем -1 в случае ошибки
        }
    }


    /* Метод для расчета хеша большого файла
     * @param file - файл, для которого нужно рассчитать хеш
     */
    public static long calculateHashLargeFile(File file) {
        System.out.println("Обработка LargeFile - " + file.getAbsolutePath());
        try {
            return updateDigestWithLargeFileContent(file);   // Обновляем хеш содержимым файла             // Преобразуем хеш в число
        } catch (IOException | UncheckedIOException e) {
            System.err.println("Ошибка чтения файла " + file.getName() + ": " + e.getMessage());
            return -1; // Возвращаем -1 в случае ошибки
        }
    }


    // Вспомогательный метод для создания объекта MessageDigest
    private static MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256"); // Создаем объект MessageDigest для SHA-256
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Алгоритм хеширования SHA-256 не найден");
            throw new RuntimeException(e);
        }
    }


    // Вспомогательный метод для обновления хеша содержимым небольшого файла
    private static void updateDigestWithSmallFileContent(File file, MessageDigest digest) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) { // Используем try-with-resources для автоматического закрытия
            byte[] buffer = new byte[8192]; // Буфер для чтения файла
            int bytesRead; // Количество байт, прочитанных из файла
            // Читаем файл и обновляем хеш
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead); // Обновляем хеш
            }
        }
    }

    // Вспомогательный метод для обновления хеша содержимым большого файла
    private static Long updateDigestWithLargeFileContent(File file) throws IOException {
        Long heshLong = 0L; // Переменная для хранения хеша
        long fileSize = file.length(); // Получаем размер файла

        long partSize = (long) Math.ceil((double) fileSize / NUM_BLOCKS);  // Размер каждой части файла (округляем в большую сторону)

        List<Future<Long>> futures = new ArrayList<>(); // Список для хранения Future

        for (int i = 0; i < NUM_BLOCKS; i++) {
            long start = i * partSize; // Начало части
            long end = (i == NUM_BLOCKS - 1) ? fileSize : (i + 1) * partSize; // Конец части
            Future<Long> future = executor.submit(() -> {
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) { // Создаем новый RandomAccessFile для каждого потока
                    //System.out.println("поток - " + Thread.currentThread());
                    return processFilePart(raf, start, end); // Обрабатываем часть файла
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            futures.add(future); // Добавляем Future в список
        }

        //executor.shutdown(); // Завершаем работу пула потоков - !!! некорректно работает с этой строкой

        // Обновляем финальный хеш
        for (Future<Long> future : futures) {
            try {
                heshLong += future.get(); // Получаем результат задачи и обновляем финальный хеш
            } catch (InterruptedException e) {
                System.err.println("Поток был прерван: " + e.getMessage() + " файл - " + file.getAbsolutePath());
                Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
                return -1L; // Возвращаем -1 в случае прерывания
            } catch (Exception e) {
                System.err.println("Ошибка при обновлении хеша: " + e.getMessage());
                e.printStackTrace(); // Выводим стек вызовов для диагностики
            }
        }

        return heshLong; // Возвращаем хеш в виде числа
    }



    // Вспомогательный метод для обработки части файла
    private static Long processFilePart(RandomAccessFile raf, long start, long end) throws IOException {
        MessageDigest digest = createMessageDigest(); // Создаем объект MessageDigest для хеширования
        byte[] buffer = new byte[BUFFER_SIZE]; // Буфер для чтения файла
        raf.seek(start); // Переходим к началу части файла
        long bytesReadTotal = 0; // Общее количество прочитанных байт
        int bytesRead; // Количество байт, прочитанных из файла

        // пока есть байты в файле, читаем их и обновляем хеш
        while (bytesReadTotal < (end - start) && (bytesRead = raf.read(buffer, 0, (int) Math.min(BUFFER_SIZE, end - start - bytesReadTotal))) != -1) {
            digest.update(buffer, 0, bytesRead); // Обновляем хеш
            bytesReadTotal += bytesRead; // Обновляем общее количество прочитанных байт
        }

        return convertHashToLong(digest); // Возвращаем хеш в виде числа
    }



    // Вспомогательный метод для обновления хеша размером файла
//    private Long updateDigestWithFileSize() {
//        MessageDigest digest = createMessageDigest(); // Создаем объект MessageDigest для хеширования
//        //long fileSize = file.length(); // Получаем размер файла
//        byte[] sizeBytes = ByteBuffer.allocate(Long.BYTES).putLong(fileSize).array(); // Преобразуем размер файла в массив байт
//        digest.update(sizeBytes); // Обновляем хеш размером файла
//        return convertHashToLong(digest); // Преобразуем хеш в число
//    }



    // Вспомогательный метод для преобразования хеша в число
    private static long convertHashToLong(MessageDigest digest) {
        byte[] hashBytes = digest.digest(); // Получаем хеш в виде массива байт
        long hash = 0L; // Переменная для хранения итогового хеша
        // Преобразуем массив байт в число
        for (byte b : hashBytes) {
            hash = (hash << 8) + (b & 0xff); // Сдвигаем влево и добавляем байт
        }
        return hash; // Возвращаем итоговый хеш
    }



    /* Метод для определения оптимального размера буфера на основе доступной памяти
    * Метод для определения оптимального размера буфера на основе доступной памяти и количества процессоров
    * Оптимальный размер буфера - это 1/8 от максимальной памяти, деленной на количество процессоров
     */
    private static int getOptimalBufferSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        long bsLong = maxMemory / (availableProcessors * 8192L);
        int bs = (int)bsLong ;

        int minBufferSize = 1024 * availableProcessors / 2 ;
        return Math.max(bs, minBufferSize);
    }


    /* Метод для определения оптимального размера большого файла для многопоточного хеширования
        * Оптимальный размер файла - это 1/4 от максимальной п��мяти, деленной на количество процессоров
     */
    private static int getOptimalLargeFileSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // Устанавливаем оптимальный размер файла как 1/4 от максимальной памяти, деленной на количество процессоров
        return (int) (maxMemory / (availableProcessors));
    }

    // Метод для расчета максимального количества виртуальных потоков
    private static int calculateVirtualThreadLimit() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // Предположим, что каждый виртуальный поток использует около 1 МБ памяти
        long memoryPerThread = 1 * 1024 * 1024;

        // Рассчитываем максимальное количество виртуальных потоков
        long maxVirtualThreads = maxMemory / memoryPerThread;

        // Ограничиваем количество виртуальных потоков количеством процессоров
        return (int) Math.min(maxVirtualThreads, availableProcessors * 1000);
    }




    public static void main(String[] args) {
        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        File file = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат");
        //Hashing hashing = new Hashing();
        long hash = Hashing.calculateHash(file);
        System.out.println("Хеш файла " + file.getName() + ": " + hash);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Перевод наносекунд в миллисекунды
        System.out.println("Время выполнения программы: " + duration + " мс");

        System.out.println(" BUFFER_SIZE - " + BUFFER_SIZE);
    }
}




/* Метод для расчета хеша файла
 * @param file - файл, для которого нужно рассчитать хеш
 */
//    private long calculateHashSmallFile(File file) {
//        System.out.println(" обработка SmallFile - " + file.getAbsolutePath());
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");  // создаем объект MessageDigest для хеширования
//            FileInputStream fis = new FileInputStream(file);      // создаем объект FileInputStream для чтения файла
//            byte[] buffer = new byte[8192];        // буфер для чтения файла
//            int bytesRead;                       // количество байт, прочитанных из файла
//            // пока есть байты в файле, читаем их и обновляем хеш
//            while ((bytesRead = fis.read(buffer)) != -1) {
//                digest.update(buffer, 0, bytesRead);     // обновляем хеш
//            }
//            fis.close();
//
//            // добавляем размер файла в хеш
//            long fileSize = file.length();  // размер файла
//            byte[] sizeBytes = ByteBuffer.allocate(Long.BYTES).putLong(fileSize).array();  // преобразуем размер файла в массив байт
//            digest.update(sizeBytes);      // обновляем хеш размером файла - добавляем размер файла в хеш
//
//            // получаем хеш в виде массива байт
//            byte[] hashBytes = digest.digest();
//            int hash = 0;
//            // преобразуем массив байт в число
//            for (byte b : hashBytes) {
//                hash = (hash << 8) + (b & 0xff);
//            }
//            return hash;
//        } catch (IOException | UncheckedIOException e) {
//            System.err.println("Error reading file " + file.getName() + " in the method calculateContentHash: " + e.getMessage());
//            return -1;
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }



 /* Метод для расчета хеша больших файлов с использованием многопоточности
//     * @param file - файл, для которого нужно рассчитать хеш
//     */
//    private long calculateHashLargeFile(File file) {
//        System.out.println(" обработка LargeFile - " + file.getAbsolutePath());
//        try {
//            MessageDigest digest = createMessageDigest(); // Создаем объект MessageDigest для хеширования
//            // создаем объект BufferedInputStream для чтения файла с использованием буфера размером BUFFER_SIZE
//            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
//                byte[] buffer = new byte[BUFFER_SIZE];
//                int bytesRead;
//                int count = 0;
//                // пока есть байты в файле, читаем их и обновляем хеш
//                while ((bytesRead = bis.read(buffer)) != -1) {
//                    count++;
//                    if (count % 10000 == 0) {
//                        System.out.println("Прочитано " + count + " блоков по " + BUFFER_SIZE + " байт");
//                    }
//                    digest.update(buffer, 0, bytesRead);
//                }
//            }
//            // добавляем размер файла в хеш
//            byte[] hashBytes = digest.digest();
//            long hash = 0L;
//            // преобразуем массив байт в число
//            for (byte b : hashBytes) {
//                hash = (hash << 8) + (b & 0xff);
//            }
//            return hash;
//        } catch (IOException | NoSuchAlgorithmException e) {
//            System.err.println("Ошибка при обработке файла " + file.getAbsolutePath() + ": " + e.getMessage());
//            return -1;
//        }
//    }