package V12;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * тесты
 *
 */
public class TesterUnit {

	// Для тестирования метода walkFileTree - обход файловой системы и группировка файлов по их размеру в списки групп дубликатов в
	private V12.FileDuplicateFinder finder;
	private Map<Long, List<Set<File>>> expectedDuplicatesBySize;

	// Для тестирования метода areFilesEqual - побайтное сравнение содержимого двух файлов
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

	// Для тестирования метода findDuplicateGroups класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
//	Map<Long, Set<Path>> filesBySize;
//	List<Set<Path>> expectedProcessSameSizeFiles;

	@Before
	public void setUp() throws Exception {

		// Для тестирования метода walkFileTree класса FileDuplicateFinder
		// Обходит файловую систему, начиная с указанного пути и группирует файлы в duplicatesBySize по их размеру группы списков дубликатов
		// expectedDuplicatesBySize - ожидаемый результат
		finder = new FileDuplicateFinder();
		expectedDuplicatesBySize = new HashMap<>();

		expectedDuplicatesBySize.put(32L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/d2.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/d1.txt")
				)
		));
		expectedDuplicatesBySize.put(0L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.sudo_as_admin_successful"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/aaaaaaaa"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/g1.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa")
				)
		));

		expectedDuplicatesBySize.put(20L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/a1.txt")
				)
		));

		expectedDuplicatesBySize.put(94869L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg")
				)
		));

		expectedDuplicatesBySize.put(23L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/c1.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c1.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c2.txt")
				)
		));

		expectedDuplicatesBySize.put(136L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file2.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file2.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file1.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file1.txt")
				)
		));

		expectedDuplicatesBySize.put(3771L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc (копия)")
				)
		));

		expectedDuplicatesBySize.put(3359325264L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)")
				)
		));

		expectedDuplicatesBySize.put(13L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/b1.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/b2.txt")
				)
		));

		expectedDuplicatesBySize.put(29L, Arrays.asList(
				Set.of(
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/e2.txt"),
						new File("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/e1.txt")
				)
		));

		// Для тестирования метода areFilesEqual - побайтное сравнение содержимого двух файлов
		file1 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/a1.txt");
		file2 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2.txt");
		file3 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c1.txt");
		file4 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/d2.txt");
		file5 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc (копия)");
		file6 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc");
		// фотки
		file7 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg");
		file8 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg");
		// фильмы
		file9 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)");
		file10 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат");
		// файлы нулевого размера
		file11 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.sudo_as_admin_successful");
		file12 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/aaaaaaaa");
		file13 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/g1.txt");
		// очень большие файлы - фильмы 10,8 Гб
		// не в тестовой папке !!!
		file14 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат");
		file15 = Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы/фильм про солдат (копия)");

	}



	/**  Тестирования метода walkFileTree класса FileDuplicateFinder */
	@Test
	public void testWalkFileTree1() throws IOException {

		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder");
		Map<Long, List<Set<File>>> duplicatesBySize = finder.getDuplicatesBySize();

			// Сортируем проверяемые и проверочные списки файлов одинакового размера что бы при сравнении у них совпадали порядки
		for (Long key : duplicatesBySize.keySet()) {
			Set<File> actualSet = duplicatesBySize.get(key).iterator().next();  // Получаем первый элемент из списка
			List<File> actualList = new ArrayList<>(actualSet);
			Set<File> expectedSet = expectedDuplicatesBySize.get(key).iterator().next(); // Получаем первый элемент из списка
			List<File> expectedList = new ArrayList<>(expectedSet);

			if (actualList != null && expectedList != null) {
				Collections.sort(actualList);
				Collections.sort(expectedList);
			}
		}
		// сверяем содержание полученого filesBySize и ожидаемого expectedFilesBySize
		assertEquals(expectedDuplicatesBySize, duplicatesBySize);
	}


	/** Тестирование метода areFilesEqual - побайтное сравнение содержимого двух файлов */
	@Test
	public void testAreFilesEqual() throws IOException, ExecutionException, InterruptedException {
		// Проверка на равенство файлов с одинаковым содержимым
		assertEquals(true, V12.FileComparator.areFilesEqual(file1, file2));
		assertEquals(true, V12.FileComparator.areFilesEqual(file5, file6));
		assertEquals(true, V12.FileComparator.areFilesEqual(file7, file8));
		assertEquals(true, V12.FileComparator.areFilesEqual(file9, file10));
		// Проверка на равенство файлов с разным содержимым
		assertEquals(false, V12.FileComparator.areFilesEqual(file3, file4));
		assertEquals(false, V12.FileComparator.areFilesEqual(file5, file3));
		// Проверка на равенство файлов нулевого размера
		assertEquals(true, V12.FileComparator.areFilesEqual(file11, file12));
		assertEquals(true, V12.FileComparator.areFilesEqual(file13, file11));
		// Проверка на равенство очень больших файлов
		boolean result = V12.FileComparator.areFilesEqual(file14, file15);
		assertEquals(true, result);
	}



	//* Тестирование метода testWalkFileTree класса FileDuplicateFinder -
	// из списка файлов одинакового размера находит дубликаты.
	// Это вспомогательный метод, который используется в методе findDuplicateGroups.
	@Test
	public void testWalkFileTree2() throws IOException {
		FileDuplicateFinder finder = new FileDuplicateFinder();

		// Ожидаемый результат
		List<Set<File>> expected = new ArrayList<>();
		expected.add(new HashSet<>(Arrays.asList(
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test01.txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test02.txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test03.txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test04.txt")
		)));

		expected.add(new HashSet<>(Arrays.asList(
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11.txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11 (копия).txt")
		)));

		expected.add(new HashSet<>(Arrays.asList(
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21.txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (другая копия).txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (копия).txt")
				)
		));

		expected.add(new HashSet<>(Arrays.asList(
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (другая копия).txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (3-я копия).txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (копия).txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31.txt")
		)));

		// Результат работы метода
		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder");
		Map<Long, List<Set<File>>> duplicatesBySizeActual = finder.getDuplicatesBySize();
		List<Set<File>> actual = duplicatesBySizeActual.get(11L);

		// Проверка результата
		//assertEquals(expected.size(), actual.size());
		for (Set<File> expectedGroup : expected) {
			boolean found = false;
			Set<File> expectedSet = new HashSet<>(expectedGroup);
			for (Set<File> actualGroup : actual) {
				Set<File> actualSet = new HashSet<>(actualGroup);
				if (actualSet.equals(expectedSet)) {
					found = true;
					break;
				}
			}
			assertEquals(true, found);

		}
	}
	
}
