package v3;

import java.io.File;
import java.util.Comparator;
import java.util.List;

// Comparator interface definitions how to sort lists by the size of their attachments
// Used in the DuplicateFileGroup class in the printDuplicateGroups method using Collections.sort(...
public class FileListSizeComparator implements Comparator<List<File>> {
    @Override
    public int compare(List<File> fileList1, List<File> fileList2) {
        long size1 = fileList1.get(0).length();
        long size2 = fileList2.get(0).length();
        return Long.compare(size1, size2);
    }
}
