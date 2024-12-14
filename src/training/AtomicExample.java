package training;

import java.util.concurrent.atomic.AtomicInteger;

/* приимер использования атомарной неблокирующей операции.
* Атомарная - самая маленькая операция, которая не может быть разделена на более мелкие операции.
* Неблокирующая - операция, которая не блокирует поток исполнения. Например не использует synchronized.
*
* Класс AtomicExample в приведенном примере демонстрирует параллельное выполнение
*  двух потоков (thread1 и thread2). Оба потока выполняют одну и ту же задачу —
* инкрементируют значение AtomicInteger (переменной atomicCounter).
*
 */
public class AtomicExample {

    public static void main(String[] args) {
        // создаем атомарный счетчик
        // AtomicInteger - это класс, который обеспечивает атомарные операции над целыми числами.
        AtomicInteger atomicCounter = new AtomicInteger(0);

        // создаем задачу, которая увеличивает счетчик на 1
        // Лямбда-выражение для инкремента
        // Runnable - интерфейс, который представляет собой задачу в отдельном потоке.
        Runnable incrementTask = () -> {
            for (int i = 0; i < 1000; i++) {
                // увеличиваем счетчик на 1
                // incrementAndGet() - метод класса AtomicInteger, который увеличивает значение на 1 и возвращает новое значение.
                atomicCounter.incrementAndGet(); // атомарная операция
            }
        };

        // создаем два потока, которые выполняют задачу увеличения счетчика
        Thread thread1 = new Thread(incrementTask);
        Thread thread2 = new Thread(incrementTask);

        // запускаем потоки
        // start() - метод класса Thread, который запускает поток на выполнение.
        thread1.start();
        thread2.start();

        // ждем завершения потоков
        // join() - метод класса Thread, который останавливает текущий поток до тех пор, пока поток, для которого вызван метод, не завершится.
        // использование join() позволяет гарантировать, что основной поток будет ждать завершения работы дочерних потоков, прежде чем продолжить выполнение, что является важным аспектом при работе с многопоточностью.
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {  // InterruptedException - исключение, которое генерируется, когда поток прерывается.
            e.printStackTrace();
            System.err.println(" поток прерван " + e);
        }

        System.out.println("Final counter value: " + atomicCounter.get());
    }
}
