package v3;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class Hashing {

    private static final int BUFFER_SIZE = getOptimalBufferSize();  // 8192 - оптимальный размер буфера на основе доступной памяти используемый в java; // Оптимальный размер буфера на основе доступной памяти
    private static final int LARGE_FILE_THRESHOLD = getOptimalLargeFileSize(); // порог для больших файлов

    // конструктор
    public Hashing() {
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
                return calculateHashLargeFile(file);
        }
    }



    /* Метод для расчета хеша маленького файла
     * @param file - файл, для которого нужно рассчитать хеш
     */
    public long calculateHashSmallFile(File file) {
        System.out.println("Обработка SmallFile - " + file.getAbsolutePath());
        try {
            MessageDigest digest = createMessageDigest(); // Создаем объект MessageDigest для хеширования
            updateDigestWithFileContent(digest, file);   // Обновляем хеш содержимым файла
            updateDigestWithFileSize(digest, file);      // Обновляем хеш размером файла
            return convertHashToLong(digest);             // Преобразуем хеш в число
        } catch (IOException | UncheckedIOException e) {
            System.err.println("Ошибка чтения файла " + file.getName() + ": " + e.getMessage());
            return -1; // Возвращаем -1 в случае ошибки
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Обрабатываем исключение, если алгоритм не найден
        }
    }



    /* Метод для расчета хеша больших файлов с использованием многопоточности
     * @param file - файл, для которого нужно рассчитать хеш
     */
    private long calculateHashLargeFile(File file) {
        System.out.println(" обработка LargeFile - " + file.getAbsolutePath());
        try {
            MessageDigest digest = createMessageDigest(); // Создаем объект MessageDigest для хеширования
            // создаем объект BufferedInputStream для чтения файла с использованием буфера размером BUFFER_SIZE
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                int count = 0;
                // пока есть байты в файле, читаем их и обновляем хеш
                while ((bytesRead = bis.read(buffer)) != -1) {
                    count++;
                    if (count % 10000 == 0) {
                        System.out.println("Прочитано " + count + " блоков по " + BUFFER_SIZE + " байт");
                    }
                    digest.update(buffer, 0, bytesRead);
                }
            }
            // добавляем размер файла в хеш
            byte[] hashBytes = digest.digest();
            long hash = 0L;
            // преобразуем массив байт в число
            for (byte b : hashBytes) {
                hash = (hash << 8) + (b & 0xff);
            }
            return hash;
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Ошибка при обработке файла " + file.getAbsolutePath() + ": " + e.getMessage());
            return -1;
        }
    }



    // Вспомогательный метод для создания объекта MessageDigest
    private MessageDigest createMessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256"); // Создаем объект MessageDigest для SHA-256
    }


    // Вспомогательный метод для обновления хеша содержимым файла
    private void updateDigestWithFileContent(MessageDigest digest, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) { // Используем try-with-resources для автоматического закрытия
            byte[] buffer = new byte[8192]; // Буфер для чтения файла
            int bytesRead; // Количество байт, прочитанных из файла
            // Читаем файл и обновляем хеш
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead); // Обновляем хеш
            }
        }
    }


    // Вспомогательный метод для обновления хеша размером файла
    private void updateDigestWithFileSize(MessageDigest digest, File file) {
        long fileSize = file.length(); // Получаем размер файла
        byte[] sizeBytes = ByteBuffer.allocate(Long.BYTES).putLong(fileSize).array(); // Преобразуем размер файла в массив байт
        digest.update(sizeBytes); // Обновляем хеш размером файла
    }



    // Вспомогательный метод для преобразования хеша в число
    private long convertHashToLong(MessageDigest digest) {
        byte[] hashBytes = digest.digest(); // Получаем хеш в виде массива байт
        long hash = 0; // Переменная для хранения итогового хеша
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
        return (int) (maxMemory / (availableProcessors * 4));
    }




    public static void main(String[] args) {
        long startTime = System.nanoTime();

        Hashing hashing = new Hashing();
        File file = new File("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/videoplayback .mp4");
        long hash = hashing.calculateHash(file);
        System.out.println("Хеш файла " + file.getName() + ": " + hash);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Перевод наносекунд в миллисекунды
        System.out.println("Время выполнения программы: " + duration + " мс");
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