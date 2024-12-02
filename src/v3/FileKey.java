package v3;


import java.io.File;
import java.util.Objects;

// класс для хранения ключа файла - размера и хеша
public class FileKey implements Comparable<FileKey> {
    private final long size;
    public long getSize() {
        return size;
    }
    private final long hash;
    public long getHash() {
        return hash;
    }

    private Hashing hashing = new Hashing();

    // конструктор для создания ключа файла на основе размера и хеша
    public FileKey(File file) {
        this.size = file.length();
        //this.hash = hashing.calculateHashWithSize(file);
        this.hash = hashing.calculateHashWithSize(file);
    }

    // переопределение методов equals и hashCode для корректного сравнения объектов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKey fileKey = (FileKey) o;
        return size == fileKey.size && hash == fileKey.hash;
    }

    // переопределение метода hashCode для корректного сравнения объектов
    // Метод hashCode нужен для того, чтобы предоставить уникальное целочисленное представление объекта, что необходимо для корректной работы коллекций, основанных на хешировании, таких как HashMap и HashSet. Он гарантирует, что объекты, которые считаются равными (через метод equals), имеют одинаковый хеш-код, что позволяет этим коллекциям эффективно хранить и извлекать объекты.
    @Override
    public int hashCode() {
        return size == 0 ? 0 : Objects.hash(size, hash);
    }

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
}
