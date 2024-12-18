package training;

import java.util.concurrent.atomic.*;

public class AtomicExample2 {

    // Atomic — это класс, который предоставляет атомарные операции над переменными.
    // Эти переменные потокобезопасны так как при операции над ними они блокируются
    // так же как если использовать Lock или synchronized.
    AtomicInteger atomicInteger = new AtomicInteger(0);
    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    AtomicLong atomicLong = new AtomicLong(0);
    AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(10);
    AtomicLongArray atomicLongArray = new AtomicLongArray(10);
}
