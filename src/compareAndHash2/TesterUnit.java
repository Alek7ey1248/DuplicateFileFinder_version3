package compareAndHash2;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * тесты
 *
 */
public class TesterUnit {

	private FileDuplicateFinder finder;
	// Для тестирования метода walkFileTree - обход файловой системы и группировка файлов по их размеру в списки групп дубликатов в
	private Map<Long, Set<Path>> expectedFilesBySize;
	// Для тестирования метода findDuplicates - обход файловой системы и группировка файлов по их размеру в списки групп дубликатов в
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


		// Для тестирования метода findDuplicates класса FileDuplicateFinder
		// Обходит файловую систему, начиная с указанного пути и группирует файлы в duplicatesBySize по их размеру группы списков дубликатов
		// expectedDuplicatesBySize - ожидаемый результат
		//finder = new FileDuplicateFinder();
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

	/** Тестирование метода walkFileTree - обход файловой системы и группировка файлов по их размеру в списки групп дубликатов в */
	@Test
	public void findDuplicates1() throws IOException {
		finder.findDuplicates(new String[]{"/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder"});
		List<Set<File>> duplicates = finder.getDuplicates();

		// Проверяем, что размеры ожидаемых и фактических групп дубликатов совпадают
		assertEquals(expectedDuplicatesBySize.size(), duplicates.size());

		// Создаем список для хранения фактических наборов файлов
		List<Set<File>> actualSets = new ArrayList<>(duplicates);

		// Сравниваем фактические наборы с ожидаемыми
		for (List<Set<File>> expectedList : expectedDuplicatesBySize.values()) {
			for (Set<File> expectedSet : expectedList) {
				// Проверяем, что фактический набор файлов совпадает с ожидаемым
				assertTrue(actualSets.remove(expectedSet));
			}
		}

		// Если остались какие-то фактические наборы, которые не были проверены, это ошибка
		assertTrue(actualSets.isEmpty());
	}

	/** Тестирование метода findDuplicates для проверки групп дубликатов */
	@Test
	public void findDuplicates2() throws IOException {
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
		)));
		expected.add(new HashSet<>(Arrays.asList(
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (другая копия).txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (3-я копия).txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (копия).txt"),
				new File("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31.txt")
		)));

		// Результат работы метода
		finder.findDuplicates(new String[]{"/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder"});
		List<Set<File>> duplicates = finder.getDuplicates();

		// Проверяем, что размеры ожидаемых и фактических групп дубликатов совпадают
		assertEquals(expected.size(), duplicates.size());

		for (Set<File> expectedGroup : expected) {
			boolean found = false;
			for (Set<File> actualGroup : duplicates) {
				if (actualGroup.equals(expectedGroup)) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}


	/** Тестирование метода areFilesEqual - побайтное сравнение содержимого двух файлов */
	@Test
	public void testAreFilesEqual() throws IOException, ExecutionException, InterruptedException {
		// Проверка на равенство файлов с одинаковым содержимым
		assertEquals(true, FileComparator.areFilesEqual(file1.toFile(), file2.toFile()));
		assertEquals(true, FileComparator.areFilesEqual(file5.toFile(), file6.toFile()));
		assertEquals(true, FileComparator.areFilesEqual(file7.toFile(), file8.toFile()));
		assertEquals(true, FileComparator.areFilesEqual(file9.toFile(), file10.toFile()));
		// Проверка на равенство файлов с разным содержимым
		assertEquals(false, FileComparator.areFilesEqual(file3.toFile(), file4.toFile()));
		assertEquals(false, FileComparator.areFilesEqual(file5.toFile(), file3.toFile()));
		// Проверка на равенство файлов нулевого размера
		assertEquals(true, FileComparator.areFilesEqual(file11.toFile(), file12.toFile()));
		assertEquals(true, FileComparator.areFilesEqual(file13.toFile(), file11.toFile()));
		// Проверка на равенство очень больших файлов
		boolean result = FileComparator.areFilesEqual(file14.toFile(), file15.toFile());
		assertEquals(true, result);
	}
	
}
