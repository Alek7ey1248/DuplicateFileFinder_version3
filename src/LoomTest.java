import static java.lang.Thread.startVirtualThread;

public class LoomTest {
    public static void main(String[] args) {
        // Запускаем виртуальный поток
        Thread virtualThread = startVirtualThread(() -> {
            System.out.println("Hello from a virtual thread!");
        });

        // Задержка, чтобы дать время виртуальному потоку завершить выполнение
        try {
            virtualThread.join(); // Ждем завершения виртуального потока
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}