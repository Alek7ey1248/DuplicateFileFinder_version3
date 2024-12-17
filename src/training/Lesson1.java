package training;

import java.util.concurrent.TimeUnit;

public class Lesson1 {
    public static void main(String[] args) {

//        long numProcessors = Runtime.getRuntime().availableProcessors();
//        for(int i = 0; i < numProcessors; i++) {
//            Thread thread = new Thread(() -> {
//                while (true) {}
//            });
//            thread.start();
//        }

        MyThread thread1 = new MyThread();
        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
        }


        MyRunnable myRunnable = new MyRunnable();
        Thread thread3 = new Thread(myRunnable);
        thread3.start();

        Thread thread2 = new Thread(() -> {
            for (int p = 0; p <= 100; p++) {
                System.out.print(" работает поток thread2 - имя -  " + Thread.currentThread().getName());
                System.out.println("   p = " + p);
                try {
                    System.out.println("thread2 засыпает на 1 секунду");
                    //Thread.sleep(1000);
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread2.start();

        for (int i = 0; i <= 100; i++) {
            System.out.print(" работает main процесс  ");
            System.out.println("   i = " + i);
        }

    }
}


class MyThread extends Thread {
    @Override
    public void run() {
        for (int k = 0; k <= 100; k++) {
            System.out.print(" работает поток по имени -  " + Thread.currentThread().getName());
            System.out.println("     k = " + k);
        }
    }
}

class MyRunnable implements Runnable {
    @Override
    public void run() {
        for (int j = 0; j <= 100; j++) {
            System.out.print(" работает поток по имени -  " + Thread.currentThread().getName());
            System.out.println("     j = " + j);
        }
    }
}















