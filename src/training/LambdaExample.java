package training;

import java.util.Arrays;
import java.util.List;


/* пример лямбда выражения - используется для лаконичного описания анонимных классов
 * в данном случае лямбда выражение описывает интерфейс с одним методом, который принимает два параметра
 * и возвращает результат
 * */
public class LambdaExample {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("John", "Alex", "Anna", "Maria", "Peter");

        names.stream()
                .filter(name -> name.startsWith("A"))
                .forEach(name -> System.out.println(name));
    }
}
