package training;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Runnable запущеный в ExecutorService ничего не возвращает в отличии от Callable
public class ExecutorRunnableExample {
    public static void main(String[] args) {
        // создаем пул из двух потоков
        ExecutorService executor = Executors.newFixedThreadPool(60);

        // создаем объект Runnable - анонимный класс - задача для выполнения
        Runnable runnable = new MyRunnable() {
            @Override
            public void run() {
                int sum = 0;
                for (int i = 0; i < 100; i++) {
                    sum += i;
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(Thread.currentThread().getName() + " " + sum);
                }
                System.out.println("-------------------------- остановка потока --------------------------");
                System.out.println(Thread.currentThread().getName() + " " + sum);
                System.out.println("--------------------------------------------------------------------");
            }
        };

        // запускаем задачи на выполнение
        for (int p = 0; p < 60; p++) {
            executor.execute(runnable);
        }

        // так же запустим один поток в main потоке
        runnable.run();

        // останавливаем пул потоков
        executor.shutdown();
    }
}
