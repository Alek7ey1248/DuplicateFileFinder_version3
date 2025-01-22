package V12;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Класс для проверки схожести имен файлов
public class FileNameSimilarityChecker {

    // Порог схожести
    private static final double SIMILARITY_THRESHOLD = 0.6; // 60%

    // Метод для проверки схожести имен файлов - больше 50% пар имеют схожесть 60% или более
    public boolean areFileNamesSimilar(Set<File> files) {
        List<String> fileNames = files.stream()
                .map(File::getName)
                .collect(Collectors.toList());

        int similarCount = 0;
        int totalPairs = 0;

        for (int i = 0; i < fileNames.size(); i++) {
            for (int j = i + 1; j < fileNames.size(); j++) {
                totalPairs++;
                if (getSimilarityPercentage(fileNames.get(i), fileNames.get(j)) >= SIMILARITY_THRESHOLD) {
                    similarCount++;
                }
            }
        }

        // Проверяем, если больше 50% пар имеют схожесть 60% или более
        return (totalPairs > 0 && (similarCount * 100) / totalPairs > 50);
    }

    // Метод для вычисления процента схожести между двумя строками
    private double getSimilarityPercentage(String str1, String str2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        int distance = levenshteinDistance.apply(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());

        // Избегаем деления на ноль
        if (maxLength == 0) {
            return 100.0; // Если обе строки пустые, считаем их идентичными
        }

        return (1.0 - (double) distance / maxLength);
    }
}
