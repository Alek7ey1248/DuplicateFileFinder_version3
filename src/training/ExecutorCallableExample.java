package training;

import java.util.concurrent.*;

// Callable запущеный в ExecutorService в отличии от Runnable возвращает результат типа Future
// работает только 2 потока в пуле потоков - ДЕМОНСТРАЦИЯ как при этом запускаются 3 потока в пуле и один в main потоке
public class ExecutorCallableExample {
    public static void main(String[] args) {
        // создаем пул из двух потоков
        ExecutorService executor = Executors.newFixedThreadPool(2);


        // создаем объект Callable - анонимный класс - задача для выполнения
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int sum = 0;
                for (int i = 0; i < 10; i++) {
                    sum += i;
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(Thread.currentThread().getName() + " " + sum);
                }
                System.out.println("-------------------------- остановка потока --------------------------");
                System.out.println(Thread.currentThread().getName());
                System.out.println("--------------------------------------------------------------------");
                return sum;
            }
        };

        // создаем объект Future - результат выполнения задачи
        Future<Integer> future[] = new Future[3];

        // запускаем задачи на выполнение в пуле потоков
//        future[0] = executor.submit(callable);
//        future[1] = executor.submit(callable);
//        future[2] = executor.submit(callable);

        for (int n=0; n<3; n++) {
            future[n] = executor.submit(callable);
        }


        // результат выполнения задачи в main потоке
        Integer resMain = 0;
        // так же запустим один поток в main потоке
        try {
            resMain = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // выводим результат выполнения задачи
        try {
            System.out.println(" ============= future[0] = " + future[0].get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        try {
            System.out.println(" ============= future[1] = " + future[1].get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        try {
            System.out.println(" ============= future[2] = " + future[2].get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println(" ============= resMain = " + resMain);

        // останавливаем пул потоков
        executor.shutdown();
    }
}

