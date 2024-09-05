import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

/**
 * тесты
 *
 */
public class TesterUnit {

	// Для тестирования метода walkFileTree - обход файловой системы и группировка файлов по их размеру
	private FileDuplicateFinder finder;
	private Map<Long, List<Path>> expectedFilesBySize;

	// Для тестирования метода areFilesEqual - побайтное сравнение содержимого двух файлов
	private FileComparator fileComparator;
	private Path file1;
	private Path file2;
	private Path file3;
	private Path file4;
	private Path file5;
	private Path file6;
	private Path file7;
	private Path file8;
	private Path file9;
	private Path file10;
	private Path file11;
	private Path file12;
	private Path file13;
	private Path file14;
	private Path file15;

	// Для тестирования метода processSameSizeFiles класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
	Map<Long, List<Path>> filesBySize;
	List<List<String>> expectedProcessSameSizeFiles;

	@Before
	public void setUp() throws Exception {

		// Для тестирования метода walkFileTree класса FileDuplicateFinder
		// Обходит файловую систему, начиная с указанного пути и группирует файлы по их размеру в HashMap filesBySize
		// expectedFilesBySize - ожидаемый результат
		finder = new FileDuplicateFinder();
		expectedFilesBySize = new HashMap<>();

		expectedFilesBySize.put(32L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/d2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/d1.txt")
		));
		expectedFilesBySize.put(0L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.sudo_as_admin_successful"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/g1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa")
		));
		expectedFilesBySize.put(20L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/a1.txt")
		));
		expectedFilesBySize.put(94869L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg")
		));
		expectedFilesBySize.put(23L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/c1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c2.txt")
		));
		expectedFilesBySize.put(136L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file1.txt")
		));
		expectedFilesBySize.put(3771L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc (копия)")
		));
		expectedFilesBySize.put(3359325264L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)")
		));
		expectedFilesBySize.put(13L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/b1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/b2.txt")
		));
		expectedFilesBySize.put(29L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/e2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/e1.txt")
		));


		// Для тестирования метода areFilesEqual - побайтное сравнение содержимого двух файлов
		fileComparator = new FileComparator();
		file1 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/a1.txt");
		file2 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2.txt");
		file3 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c1.txt");
		file4 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/d2.txt");
		file5 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc (копия)");
		file6 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc");
		// фотки
		file7 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg");
		file8 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg");
		// фильмы
		file9 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)");
		file10 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат");
		// файлы нулевого размера
		file11 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.sudo_as_admin_successful");
		file12 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/aaaaaaaa");
		file13 = Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/g1.txt");
		// очень большие файлы - фильмы 10,8 Гб
		// не в тестовой папке !!!
		file14 = Paths.get("/home/alek7ey/Рабочий стол/filmsTestDuplicateFileFinder/videoplayback.mp4");
		file15 = Paths.get("/home/alek7ey/Рабочий стол/filmsTestDuplicateFileFinder/filmCopy/videoplayback (копия).mp4");



		// Для тестирования метода processSameSizeFiles класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
		// Создаем тестовые данные
		filesBySize = new HashMap<>();
		filesBySize.put(3359325264L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)")
		));
		filesBySize.put(94869L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg")
		));
		filesBySize.put(3771L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc (копия)")
		));
		filesBySize.put(136L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file1.txt")
		));
		filesBySize.put(32L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/d2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/d1.txt")
		));
		filesBySize.put(29L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/e1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/e2.txt")
		));
		filesBySize.put(23L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/c1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c1.txt")
		));
		filesBySize.put(20L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/a1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2.txt")
		));
		filesBySize.put(13L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/b2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/b1.txt")
		));
		filesBySize.put(0L, Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.sudo_as_admin_successful"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/g1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa")
		));

		// Ожидаемый результат метода processSameSizeFiles
		expectedProcessSameSizeFiles = Arrays.asList(
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc (копия)"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file2.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file2.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file1.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file1.txt"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/d2.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/d1.txt"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/e1.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/e2.txt"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/c1.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c2.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c1.txt"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/a1.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2.txt"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/b2.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/b1.txt"),
				Arrays.asList("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/g2.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.sudo_as_admin_successful", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/g1.txt", "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa")
		);
	}



	/**  Тестирования метода walkFileTree класса FileDuplicateFinder
	    Обходит файловую систему, начиная с указанного пути и группирует файлы по их размеру в HashMap filesBySize
	    expectedFilesBySize - ожидаемый результат*/
	@Test
	public void testWalkFileTree() throws IOException {
		Map<Long, List<Path>> filesBySize = new HashMap<>();
		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder", filesBySize);

		// сверяем содержание полученого filesBySize и ожидаемого expectedFilesBySize
		assertEquals(expectedFilesBySize, filesBySize);
	}


	/** Тестирование метода areFilesEqual - побайтное сравнение содержимого двух файлов */
	@Test
	public void testAreFilesEqual() throws IOException {
		// Проверка на равенство файлов с одинаковым содержимым
		assertEquals(true, fileComparator.areFilesEqual(file1, file2));
		assertEquals(true, fileComparator.areFilesEqual(file5, file6));
		assertEquals(true, fileComparator.areFilesEqual(file7, file8));
		assertEquals(true, fileComparator.areFilesEqual(file9, file10));
		// Проверка на равенство файлов с разным содержимым
		assertEquals(false, fileComparator.areFilesEqual(file3, file4));
		assertEquals(false, fileComparator.areFilesEqual(file5, file3));
		// Проверка на равенство файлов нулевого размера
		assertEquals(true, fileComparator.areFilesEqual(file11, file12));
		assertEquals(true, fileComparator.areFilesEqual(file13, file11));
		// Проверка на равенство очень больших файлов
		assertEquals(true, fileComparator.areFilesEqual(file14, file15));
	}


	/** Тестирование метода processSameSizeFiles класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
	 * Этот код сначала проверяет, что все ожидаемые группы присутствуют в фактическом результате, а затем проверяет, что в фактическом результате нет лишних групп. Если лишняя группа найдена, тест выводит сообщение с информацией о ней.*/
	@Test
	public void testProcessSameSizeFiles() {
		Map<Long, List<Path>> filesBySize = new HashMap<>();
		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder", filesBySize);
		List<List<String>> actual = finder.processSameSizeFiles(filesBySize);

		for (List<String> expectedGroup : expectedProcessSameSizeFiles) {
			boolean found = false;
			for (List<String> actualGroup : actual) {
				if (actualGroup.containsAll(expectedGroup) && expectedGroup.containsAll(actualGroup)) {
					found = true;
					break;
				}
			}
			assertTrue("Ожидаемая группа не найдена: " + expectedGroup, found);
		}

		// Проверка на наличие лишних групп
		for (List<String> actualGroup : actual) {
			Set<String> actualSet = new HashSet<>(actualGroup);
			boolean found = false;
			for (List<String> expectedGroup : expectedProcessSameSizeFiles) {
				Set<String> expectedSet = new HashSet<>(expectedGroup);
				if (actualSet.equals(expectedSet)) {
					found = true;
					break;
				}
			}
			if (!found) {
				System.out.println("Найдена лишняя группа: " + actualGroup);
				fail("Найдена лишняя группа: " + actualGroup);
			}
		}
	}
	
}
