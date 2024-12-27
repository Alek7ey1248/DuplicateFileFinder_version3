package v3;


import java.io.File;
import java.util.Objects;

// класс для хранения ключа файла - размера и хеша
public class FileKey implements Comparable<FileKey> {

    private final long size;
    private final long hash;

    // конструктор для создания ключа файла на основе размера и хеша
    public FileKey(File file) {
        this.size = file.length();
        this.hash = Hashing.calculateHash(file) + size;
    }

    // переопределение методов equals и hashCode для корректного сравнения объектов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;  // если объекты равны по ссылке
        if (o == null || getClass() != o.getClass()) return false;  // если объекты разных классов
        FileKey fileKey = (FileKey) o;     // приведение объекта к типу FileKey
        return size == fileKey.size && hash == fileKey.hash;  // сравнение полей size и hash
    }

    // переопределение метода hashCode для корректного сравнения объектов
    @Override
    public int hashCode() {
        return Objects.hash(size, hash);
    } // хеширование полей size и hash

    /* переопределение метода compareTo для сортировки объектов по размеру
    * TreeMap<FileKey, Set<File>> fileByHash будет сортироваться по размеру файла автоматически
    * но учитывая хеш файла, мы можем избежать коллизий при сравнении файлов одинакового размера
    */
    @Override
    public int compareTo(FileKey other) {
        int sizeComparison = Long.compare(this.size, other.size);
        if (sizeComparison != 0) {
            return sizeComparison;
        }
        return Long.compare(this.hash, other.hash);
    }

    public long getSize() {
        return size;
    }
}
