package training;

import java.util.concurrent.*;

// Пример использования класса ThreadLocal в классе SumCounter ниже

// переменная класса ThreadLocal дает возможность при работе нескольких потоков
// в каждом потоке работать со своей переменной типа ThreadLocal.
// Если бы мы использовали обычную переменную, то при работе нескольких потоков
// они бы конфликтовали между собой, так как обращались бы к одной и той же переменной класса.
public class ThreadLocalExample {
    public static void main(String[] args) {
        // создаем пул из двух потоков
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // массив объектов Future - результат выполнения задачи
        Future<Integer> future[] = new Future[3];

        // создаем 3 экземпляра класса SumCounter для 3 задач - 3 потоков
        SumCounter counter1 = new SumCounter(" Поток 1 ");
        SumCounter counter2 = new SumCounter(" Поток 2 ");
        SumCounter counter3 = new SumCounter(" Поток 3 ");

        // запускаем задачи на выполнение в пуле потоков
        future[0] = executor.submit(counter1);
        future[1] = executor.submit(counter2);
        future[2] = executor.submit(counter3);


        SumCounter counterMain = new SumCounter(" Поток Main ");

        // результат выполнения задачи в main потоке
        Integer resMain = 0;
        // так же запустим один поток в main потоке
        try {
            resMain = counterMain.call();
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


// переменная класса ThreadLocal дает возможность при работе нескольких потоков
// в каждом потоке работать со своей переменной типа ThreadLocal.
// Если бы мы использовали обычную переменную, то при работе нескольких потоков
// они бы конфликтовали между собой, так как обращались бы к одной и той же переменной класса.
class SumCounter implements Callable {
    private String nameThread;
    private ThreadLocal<Integer> sum;
    public SumCounter(String nameThread) {
        this.nameThread = nameThread;
        sum = new ThreadLocal<>();
    }

    @Override
    public Integer call() throws Exception {
        sum.set(0);    // устанавливаем начальное значение суммы
        for (int i = 0; i < 100; i++) {
            sum.set(sum.get() + i);  // увеличиваем сумму на i
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName() + " имя потока - "  + nameThread + " сумма - " + sum.get());
        }
        System.out.println("-------------------------- остановка потока --------------------------");
        System.out.println(Thread.currentThread().getName());
        System.out.println("--------------------------------------------------------------------");
        return sum.get();
    }
}
