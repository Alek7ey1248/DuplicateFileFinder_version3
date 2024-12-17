package training;

import java.util.concurrent.locks.ReentrantLock;

// lock (или "замок") — позволяет управлять доступом к общим ресурсам,
// чтобы избежать состояния гонки (race condition) и обеспечить корректность выполнения программы.
public class LockExample {
    private final ReentrantLock lock = new ReentrantLock();

    public void lockedMethod() {
        lock.lock(); // захватываем блокировку
        try {
            // код, который требует синхронизации
        } finally {
            lock.unlock(); // освобождаем блокировку
        }
    }
}
