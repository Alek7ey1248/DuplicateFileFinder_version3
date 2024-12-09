package v1;

import org.junit.Before;
import org.junit.Test;

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

	// Для тестирования метода walkFileTree - обход файловой системы и группировка файлов по их размеру
	private FileDuplicateFinder finder;
	private Map<Long, Set<Path>> expectedFilesBySize;

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
	Map<Long, Set<Path>> filesBySize;
	List<Set<String>> expectedProcessSameSizeFiles;

	@Before
	public void setUp() throws Exception {

		// Для тестирования метода walkFileTree класса FileDuplicateFinder
		// Обходит файловую систему, начиная с указанного пути и группирует файлы по их размеру в HashMap filesBySize
		// expectedFilesBySize - ожидаемый результат
		finder = new FileDuplicateFinder();
		expectedFilesBySize = new HashMap<>();

		expectedFilesBySize.put(32L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/d2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/d1.txt")
		));
		expectedFilesBySize.put(0L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.sudo_as_admin_successful"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/g1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa")
		));
		expectedFilesBySize.put(20L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/a1.txt")
		));
		expectedFilesBySize.put(94869L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg")
		));
		expectedFilesBySize.put(23L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/c1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c2.txt")
		));
		expectedFilesBySize.put(136L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file1.txt")
		));
		expectedFilesBySize.put(3771L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc (копия)")
		));
		expectedFilesBySize.put(3359325264L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)")
		));
		expectedFilesBySize.put(13L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/b1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/b2.txt")
		));
		expectedFilesBySize.put(29L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/e2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/e1.txt")
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



		// Для тестирования метода findDuplicateGroups класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
		// Создаем тестовые данные
		filesBySize = new HashMap<>();
		filesBySize.put(3359325264L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)")
		));
		filesBySize.put(94869L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg")
		));
		filesBySize.put(3771L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc (копия)")
		));
		filesBySize.put(136L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file1.txt")
		));
		filesBySize.put(32L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/d2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/d1.txt")
		));
		filesBySize.put(29L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/e1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/e2.txt")
		));
		filesBySize.put(23L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/c1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c1.txt")
		));
		filesBySize.put(20L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/a1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2.txt")
		));
		filesBySize.put(13L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/b2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/b1.txt")
		));
		filesBySize.put(0L, Set.of(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.sudo_as_admin_successful"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/g1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa")
		));

		// Ожидаемый результат метода findDuplicateGroups
		expectedProcessSameSizeFiles = Arrays.asList(
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc (копия)"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file2.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file2.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file1.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file1.txt"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/d2.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/d1.txt"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/e1.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/e2.txt"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/c1.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c2.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c1.txt"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/a1.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2.txt"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/b2.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/b1.txt"),
				Set.of("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/g2.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.sudo_as_admin_successful", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/g1.txt", "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa")
		);
	}



	/**  Тестирования метода walkFileTree класса FileDuplicateFinder
	    Обходит файловую систему, начиная с указанного пути и группирует файлы по их размеру в HashMap filesBySize
	    expectedFilesBySize - ожидаемый результат*/
	@Test
	public void testWalkFileTree() throws IOException {

		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder");
		Map<Long, Set<Path>> filesBySize = finder.getFilesBySize();

		// Сортируем проверяемые и проверочные списки файлов одинакового размера что бы при сравнении у них совпадали порядки
		for (Long key : filesBySize.keySet()) {
			Set<Path> actualSet = filesBySize.get(key);
			List<Path> actualList = new ArrayList<>(actualSet);
			Set<Path> expectedSet = expectedFilesBySize.get(key);
			List<Path> expectedList = new ArrayList<>(expectedSet);

			if (actualList != null && expectedList != null) {
				Collections.sort(actualList);
				Collections.sort(expectedList);
			}
		}
		// сверяем содержание полученого filesBySize и ожидаемого expectedFilesBySize
		assertEquals(expectedFilesBySize, filesBySize);
	}


	/** Тестирование метода areFilesEqual - побайтное сравнение содержимого двух файлов */
	@Test
	public void testAreFilesEqual() throws IOException, ExecutionException, InterruptedException {
		// Проверка на равенство файлов с одинаковым содержимым
		assertEquals(true, FileComparator.areFilesEqual(file1, file2));
		assertEquals(true, FileComparator.areFilesEqual(file5, file6));
		assertEquals(true, FileComparator.areFilesEqual(file7, file8));
		assertEquals(true, FileComparator.areFilesEqual(file9, file10));
		// Проверка на равенство файлов с разным содержимым
		assertEquals(false, FileComparator.areFilesEqual(file3, file4));
		assertEquals(false, FileComparator.areFilesEqual(file5, file3));
		// Проверка на равенство файлов нулевого размера
		assertEquals(true, FileComparator.areFilesEqual(file11, file12));
		assertEquals(true, FileComparator.areFilesEqual(file13, file11));
		// Проверка на равенство очень больших файлов
		assertEquals(true, FileComparator.areFilesEqual(file14, file15));
	}


	/** Тестирование метода findDuplicateGroups класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
	 * Этот код сначала проверяет, что все ожидаемые группы присутствуют в фактическом результате, а затем проверяет, что в фактическом
	 * результате нет лишних групп. Если лишняя группа найдена, тест выводит сообщение с информацией о ней.*/
	@Test
	public void testFindDuplicateGroups() throws IOException {
		//FileDuplicateFinder finder = new FileDuplicateFinder();
		//Map<Long, List<Path>> filesBySize = new HashMap<>();
		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder");
		finder.findDuplicateGroups();
		List<List<String>> actual = finder.getDuplicates();

		for (Set<String> expectedGroup : expectedProcessSameSizeFiles) {
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
			for (Set<String> expectedGroup : expectedProcessSameSizeFiles) {
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

	//* Тестирование метода findDuplicatesInSameSizeFiles класса FileDuplicateFinder -
	// из списка файлов одинакового размера находит дубликаты.
	// Это вспомогательный метод, который используется в методе findDuplicateGroups.
	@Test
	public void testFindDuplicatesInSameSizeFiles() throws IOException {
		FileDuplicateFinder finder = new FileDuplicateFinder();

		// Создаем список файлов одинакового размера
		Set<Path> files = new HashSet<>();
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test01.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test02.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test03.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test04.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11 (копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (другая копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (3-я копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (другая копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test1одинтакой.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test2одинтакой.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test3одинтакой.txt"));

		// Ожидаемый результат
		List<List<String>> expected = new ArrayList<>();
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test01.txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test02.txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test03.txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test04.txt"
		));
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11.txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11 (копия).txt"
		));
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21.txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (другая копия).txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (копия).txt"
		));
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31.txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (3-я копия).txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (другая копия).txt",
				"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (копия).txt"
		));

		// Результат работы метода
		finder.findDuplicatesInSameSizeFiles(files);
		List<List<String>> actual = finder.getDuplicates();

//		System.out.println("Результат: ");
//		for (List<String> s : actual) {
//			System.out.println("Группа: ---------- ");
//			for (String s1 : s) {
//				System.out.println(s1);
//			}
//		}

		// Проверка результата
		assertEquals(expected.size(), actual.size());
		for (List<String> expectedGroup : expected) {
			boolean found = false;
			Set<String> expectedSet = new HashSet<>(expectedGroup);
			for (List<String> actualGroup : actual) {
				Set<String> actualSet = new HashSet<>(actualGroup);
				if (actualSet.equals(expectedSet)) {
					found = true;
					break;
				}
			}
			assertEquals(true, found);

		}
	}
	
}
