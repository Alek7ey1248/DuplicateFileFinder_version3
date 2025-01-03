package V11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class FileComparatorForkJoinPool {
    private static final int BLOCK_SIZE = 64 * 1024; // Размер блока 64 КБ

    // Метод для получения порога для больших файлов
    private static long getLargeFileThreshold() {
        long maxMemory = Runtime.getRuntime().maxMemory();  // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors();    // Количество доступных процессоров
        return (int) (maxMemory / (availableProcessors * 2));
    }

    // Основной метод для сравнения файлов
    public static boolean areFilesEqual(Path file1, Path file2) throws IOException {

        if (Files.size(file1) == 0) {   // если размер файлов равен нулю, то файлы равны (так как файлы равны, то достаточно одного)
            return true;
        }

        try (FileChannel channel1 = FileChannel.open(file1);
             FileChannel channel2 = FileChannel.open(file2)) {
            long size = channel1.size();

            ForkJoinPool forkJoinPool = new ForkJoinPool();
            return forkJoinPool.invoke(new CompareTask(channel1, channel2, 0, size));
        }
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



    // Рекурсивная задача для сравнения блоков файлов
    private static class CompareTask extends RecursiveTask<Boolean> {
        private final FileChannel channel1;
        private final FileChannel channel2;
        private final long start;
        private final long end;

        public CompareTask(FileChannel channel1, FileChannel channel2, long start, long end) {
            this.channel1 = channel1;
            this.channel2 = channel2;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Boolean compute() {
            long length = end - start;
            if (length <= BLOCK_SIZE) {
                // Если длина меньше или равна размеру блока, сравниваем напрямую
                return compareBuffers(channel1, channel2, start, length);
            } else {
                // Делим задачу на две подзадачи
                long mid = start + length / 2;
                CompareTask leftTask = new CompareTask(channel1, channel2, start, mid);
                CompareTask rightTask = new CompareTask(channel1, channel2, mid, end);
                leftTask.fork(); // Запускаем левую подзадачу
                Boolean rightResult = rightTask.compute(); // Выполняем правую подзадачу
                Boolean leftResult = leftTask.join(); // Ждем завершения левой подзадачи
                return leftResult && rightResult; // Возвращаем результат
            }
        }

        private boolean compareBuffers(FileChannel channel1, FileChannel channel2, long position, long size) {
            try {
                ByteBuffer buffer1 = ByteBuffer.allocate((int) size);
                ByteBuffer buffer2 = ByteBuffer.allocate((int) size);
                channel1.read(buffer1, position);
                channel2.read(buffer2, position);
                buffer1.flip();
                buffer2.flip();
                return Arrays.equals(buffer1.array(), buffer2.array()); // Сравниваем буферы
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при сравнении блоков файлов", e);
            }
        }
    }
}
