package training;

public class Lesson11 {
    static int potato = 0;

    // тут используется synchronized чтобы не было проблем когда с методом работают одновременно несколько потоков
    synchronized static void addPotato() {
        potato++;
    }

    public static void main(String[] args) {

        Thread petya = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                addPotato();
            }
            System.out.println("  potato: " + potato);
        });
        petya.start();

        Thread vasya = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                addPotato();
            }
            System.out.println("  potato: " + potato);
        });
        vasya.start();


    }
}
