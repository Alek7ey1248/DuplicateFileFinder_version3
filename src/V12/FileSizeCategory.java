package V12;

public enum FileSizeCategory {
    SMALL_FILES,     // 2 байта
    MEDIUM_FILES,    // 10 байт = 0,01 Кб
    LARGE_FILES,    // 1 Кб
    HUGE_FILES,     // 10 Кб
    GIGANTIC_FILES,   // 300 Мб
    ONE_MB_FILES;     // > 300 Мб


    public static FileSizeCategory getCategory(long fileSize) {
        if (fileSize <= 2) {   // 2 байта
            return SMALL_FILES;
        } else if (fileSize <= 10) {  // 10 байт = 0,01 Кб
            return MEDIUM_FILES;
        } else if (fileSize <= 1000) {  // 1 Кб
            return LARGE_FILES;
        } else if (fileSize <= 10000) {  // 10 Кб
            return HUGE_FILES;
        } else if (fileSize <= 300000000) {  // 300 Мб
            return GIGANTIC_FILES;
        } else {                      // > 300 Мб
            return ONE_MB_FILES;
        }
    }
}
