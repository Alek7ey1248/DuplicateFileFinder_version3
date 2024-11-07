package v2;

import org.junit.Before;
import org.junit.Test;
import v1.FileComparator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * тесты
 *
 */
public class TesterUnit2 {

	// Для тестирования метода walkFileTree - обход файловой системы и группировка файлов по их размеру
	private FileDuplicateFinder2 finder;
	private Map<Long, ConcurrentLinkedQueue<Path>> expectedFilesBySize;

	// Для тестирования метода areFilesEqual - побайтное сравнение содержимого двух файлов
	private FileComparator2 fileComparator;
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
	//Map<Long, ConcurrentLinkedQueue<Path>> filesBySize;
	List<List<String>> expectedProcessSameSizeFiles;

	@Before
	public void setUp() throws Exception {

		// Для тестирования метода walkFileTree класса FileDuplicateFinder
		// Обходит файловую систему, начиная с указанного пути и группирует файлы по их размеру в HashMap filesBySize
		// expectedFilesBySize - ожидаемый результат
		//finder = new FileDuplicateFinder2();
		expectedFilesBySize = new HashMap<>();

		ConcurrentLinkedQueue<Path> queue32 = new ConcurrentLinkedQueue<>();
		queue32.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/d2.txt"));
		queue32.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/d1.txt"));
		expectedFilesBySize.put(32L, queue32);

		ConcurrentLinkedQueue<Path> queue0 = new ConcurrentLinkedQueue<>();
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.sudo_as_admin_successful"));
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"));
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa"));
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"));
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/aaaaaaaa"));
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/g1.txt"));
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"));
		queue0.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa"));
		expectedFilesBySize.put(0L, queue0);

		ConcurrentLinkedQueue<Path> queue20 = new ConcurrentLinkedQueue<>();
		queue20.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"));
		queue20.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2.txt"));
		queue20.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/a1.txt"));
		expectedFilesBySize.put(20L, queue20);

		ConcurrentLinkedQueue<Path> queue94869 = new ConcurrentLinkedQueue<>();
		queue94869.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"));
		queue94869.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg"));
		expectedFilesBySize.put(94869L, queue94869);

		ConcurrentLinkedQueue<Path> queue23 = new ConcurrentLinkedQueue<>();
		queue23.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/c1.txt"));
		queue23.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c1.txt"));
		queue23.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c2.txt"));
		expectedFilesBySize.put(23L, queue23);

		ConcurrentLinkedQueue<Path> queue136 = new ConcurrentLinkedQueue<>();
		queue136.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file2.txt"));
		queue136.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file2.txt"));
		queue136.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file1.txt"));
		queue136.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file1.txt"));
		expectedFilesBySize.put(136L, queue136);

		ConcurrentLinkedQueue<Path> queue3771 = new ConcurrentLinkedQueue<>();
		queue3771.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc"));
		queue3771.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc (копия)"));
		expectedFilesBySize.put(3771L, queue3771);

		ConcurrentLinkedQueue<Path> queue3359325264 = new ConcurrentLinkedQueue<>();
		queue3359325264.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат"));
		queue3359325264.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)"));
		expectedFilesBySize.put(3359325264L, queue3359325264);

		ConcurrentLinkedQueue<Path> queue13 = new ConcurrentLinkedQueue<>();
		queue13.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/b1.txt"));
		queue13.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/b2.txt"));
		expectedFilesBySize.put(13L, queue13);

		ConcurrentLinkedQueue<Path> queue29 = new ConcurrentLinkedQueue<>();
		queue29.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/e2.txt"));
		queue29.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/e1.txt"));
		expectedFilesBySize.put(29L, queue29);


		// Для тестирования метода areFilesEqual - побайтное сравнение содержимого двух файлов
		//fileComparator = new FileComparator2();
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



		// Для тестирования метода findDuplicateGroups класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
		// Создаем тестовые данные
		//filesBySize = new HashMap<>();
		finder = new FileDuplicateFinder2();
		finder.filesBySize.put(3359325264L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/фильм про солдат"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)")
		)));
		finder.filesBySize.put(94869L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg")
		)));
		finder.filesBySize.put(3771L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.bashrc (копия)")
		)));
		finder.filesBySize.put(136L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/file1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/file1.txt")
		)));
		finder.filesBySize.put(32L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/d2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/d1.txt")
		)));
		finder.filesBySize.put(29L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/e1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/e2.txt")
		)));
		finder.filesBySize.put(23L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/c1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/c1.txt")
		)));
		finder.filesBySize.put(20L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/a1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/a2.txt")
		)));
		finder.filesBySize.put(13L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/b2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/b1.txt")
		)));
		finder.filesBySize.put(0L, new ConcurrentLinkedQueue<>(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/.sudo_as_admin_successful"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/g1.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa")
		)));


		// Ожидаемый результат метода findDuplicateGroups
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
		//Map<Long, ConcurrentLinkedQueue<Path>> filesBySize = new HashMap<>();
		finder = new FileDuplicateFinder2();
		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder");

		// Преобразуем карты в Map<Long, List<Path>> для корректного сравнения
		Map<Long, List<Path>> expectedListMap = convertToListMap(expectedFilesBySize);
		Map<Long, List<Path>> actualListMap = convertToListMap(finder.filesBySize);
		// сверяем содержание полученого filesBySize и ожидаемого expectedFilesBySize
		assertEquals(expectedListMap, actualListMap);
	}


	/** Тестирование метода areFilesEqual - побайтное сравнение содержимого двух файлов */
	@Test
	public void testAreFilesEqual() throws IOException, ExecutionException, InterruptedException {
		// Проверка на равенство файлов с одинаковым содержимым
		assertEquals(true, FileComparator2.areFilesEqual(file1, file2));
		assertEquals(true, FileComparator2.areFilesEqual(file5, file6));
		assertEquals(true, FileComparator2.areFilesEqual(file7, file8));
		assertEquals(true, FileComparator2.areFilesEqual(file9, file10));
		// Проверка на равенство файлов с разным содержимым
		assertEquals(false, FileComparator2.areFilesEqual(file3, file4));
		assertEquals(false, FileComparator2.areFilesEqual(file5, file3));
		// Проверка на равенство файлов нулевого размера
		assertEquals(true, FileComparator2.areFilesEqual(file11, file12));
		assertEquals(true, FileComparator2.areFilesEqual(file13, file11));
		// Проверка на равенство очень больших файлов
		assertEquals(true, FileComparator2.areFilesEqual(file14, file15));
	}


	/** Тестирование метода findDuplicateGroups класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
	 * Этот код сначала проверяет, что все ожидаемые группы присутствуют в фактическом результате, а затем проверяет, что в фактическом результате нет лишних групп. Если лишняя группа найдена, тест выводит сообщение с информацией о ней.*/
	@Test
	public void testFindDuplicateGroups() throws IOException {
		//Map<Long, ConcurrentLinkedQueue<Path>> filesBySize = new HashMap<>();
		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder");
		List<List<String>> actual = finder.findDuplicateGroups();

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

	//* Тестирование метода findDuplicatesInSameSizeFiles класса FileDuplicateFinder -
	// из списка файлов одинакового размера находит дубликаты.
	// Это вспомогательный метод, который используется в методе findDuplicateGroups.
	@Test
	public void testFindDuplicatesInSameSizeFiles() throws IOException {
		FileDuplicateFinder2 finder = new FileDuplicateFinder2();
		FileComparator2 comparator = new FileComparator2();

		// Создаем список файлов одинакового размера
		ConcurrentLinkedQueue<Path> files = new ConcurrentLinkedQueue<>();
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test01.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test02.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test03.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test04.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test11.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test11 (копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test21.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test21 (другая копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test21 (копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31 (3-я копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31 (другая копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31 (копия).txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test1одинтакой.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test2одинтакой.txt"));
		files.add(Paths.get("/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test3одинтакой.txt"));

		// Ожидаемый результат
		List<List<String>> expected = new ArrayList<>();
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test01.txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test02.txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test03.txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test04.txt"
		));
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test11.txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test11 (копия).txt"
		));
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test21.txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test21 (другая копия).txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test21 (копия).txt"
		));
		expected.add(List.of(
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31.txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31 (3-я копия).txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31 (другая копия).txt",
				"/home/alek7ey/Рабочий стол/ListTestDuplicateFileFinder/test31 (копия).txt"
		));

		// Результат работы метода
		List<List<String>> actual = new ArrayList<>();
		finder.findDuplicatesInSameSizeFiles(files, actual);

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

	// Вспомогательный метод для тестирования метода walkFileTree
	// Преобразует карту с ConcurrentLinkedQueue<Path> в карту с List<Path>
	private Map<Long, List<Path>> convertToListMap(Map<Long, ConcurrentLinkedQueue<Path>> originalMap) {
		Map<Long, List<Path>> listMap = new HashMap<>();
		for (Map.Entry<Long, ConcurrentLinkedQueue<Path>> entry : originalMap.entrySet()) {
			listMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		return listMap;
	}
	
}
