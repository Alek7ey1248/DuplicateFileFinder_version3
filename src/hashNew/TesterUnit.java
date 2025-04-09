package hashNew;

import org.junit.Before;
import org.junit.Test;
import processing.FileGrouperNew;

import java.io.File;
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
	private Map<Long, Set<Path>> filesBySize;
	private List<Set<Path>> expectedProcessSameSizeFiles;

	@Before
	public void setUp() throws Exception {

		// Для тестирования метода walkFileTree класса FileDuplicateFinder
		// Обходит файловую систему, начиная с указанного пути и группирует файлы по их размеру в HashMap filesBySize
		// expectedFilesBySize - ожидаемый результат
		//finder = new FileDuplicateFinder();
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
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/фильм про солдат"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/фильм про солдат (копия)")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/photo_2021-12-09_16-12-54.jpg"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/photo_2021-12-09_16-12-54 (копия).jpg")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.bashrc (копия)")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file2.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file2.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/file1.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/file1.txt")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/d2.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/d1.txt")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/e1.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/e2.txt")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/c1.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c2.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/c1.txt")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/a1.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2 (копия).txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/a2.txt")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/b2.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/b1.txt")
				),
				Set.of(
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/aaaaaaaa"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/aaaaaaaa"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/aaaaaaaa"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/g2.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/.sudo_as_admin_successful"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test11/test12/test13/aaaaaaaa"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/g1.txt"),
						Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder/test21/test22/test23/aaaaaaaa")
				)
		);
	}



	/**  Тестирования метода walkFileTree класса FileDuplicateFinder
	    Обходит файловую систему, начиная с указанного пути и группирует файлы по их размеру в HashMap filesBySize
	    expectedFilesBySize - ожидаемый результат*/
	@Test
	public void testWalkFileTree() throws IOException {
		FileDuplicateFinder finder = new FileDuplicateFinder();

		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder");

		// переведем Set<File> в Set<Path>
		Map<Long, Set<File>> fBS = finder.getFilesBySize();
		Map<Long, Set<Path>> filesBySize = new HashMap<>();

		for (Map.Entry<Long, Set<File>> entry : fBS.entrySet()) {
			Long key = entry.getKey();
			Set<File> fileSet = entry.getValue();

			// Преобразуем Set<File> в Set<Path>
			Set<Path> pathSet = new HashSet<>();
			for (File file : fileSet) {
				pathSet.add(file.toPath()); // Преобразуем File в Path
			}

			// Добавляем в новую карту
			filesBySize.put(key, pathSet);
		}


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


	/** Тестирование метода processGroupFiles класса FileDuplicateFinder - из списка файлов одинакового размера находит дубликаты
	 * Этот код сначала проверяет, что все ожидаемые группы присутствуют в фактическом результате, а затем проверяет, что в фактическом
	 * результате нет лишних групп. Если лишняя группа найдена, тест выводит сообщение с информацией о ней.*/
	@Test
	public void testProcessGroupFiles() throws IOException {
		FileDuplicateFinder finder = new FileDuplicateFinder();

		finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder");
		finder.processGroupFiles();

		// переведем List<File> в List<Path>
		List<Set<File>> sf = finder.getDuplicates();
		List<List<Path>> actual = new ArrayList<>();
		for (Set<File> group : sf) {
			List<Path> pathGroup = new ArrayList<>();
			for (File file : group) {
				pathGroup.add(file.toPath());
			}
			actual.add(pathGroup);
		}

		for (Set<Path> expectedGroup : expectedProcessSameSizeFiles) {
			boolean found = false;
			for (List<Path> actualGroup : actual) {
				if (actualGroup.containsAll(expectedGroup) && expectedGroup.containsAll(actualGroup)) {
					found = true;
					break;
				}
			}
			assertTrue("Ожидаемая группа не найдена: " + expectedGroup, found);
		}

		// Проверка на наличие лишних групп
		for (List<Path> actualGroup : actual) {
			Set<Path> actualSet = new HashSet<>(actualGroup);
			boolean found = false;
			for (Set<Path> expectedGroup : expectedProcessSameSizeFiles) {
				Set<Path> expectedSet = new HashSet<>(expectedGroup);
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

	//* Тестирование метода groupByContent класса FileGrouperNew -
	// из списка файлов одинакового размера находит дубликаты.
	// Это вспомогательный метод, который используется в методе processGroupFiles
	@Test
	public void testGroupByContent() throws IOException {
		FileGrouperNew grouper = new FileGrouperNew();

		// Создаем список файлов одинакового размера
		Set<File> files = new HashSet<>();
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test01.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test02.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test03.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test04.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11 (копия).txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (другая копия).txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (копия).txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (3-я копия).txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (другая копия).txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (копия).txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test1одинтакой.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test2одинтакой.txt").toFile());
		files.add(Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test3одинтакой.txt").toFile());

		// Ожидаемый результат
		List<List<Path>> expected = new ArrayList<>();
		expected.add(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test01.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test02.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test03.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test04.txt")
		));

		expected.add(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test11 (копия).txt")
		));

		expected.add(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (другая копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test21 (копия).txt")
		));

		expected.add(Arrays.asList(
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31.txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (3-я копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (другая копия).txt"),
				Paths.get("/home/alek7ey/Рабочий стол/TestsDFF/ListTestDuplicateFileFinder/test31 (копия).txt")
		));

		// Результат работы метода
		Queue<Set<File>> qf = grouper.groupByContent(files);

		List<Set<File>> lf = new ArrayList<>(qf);
		List<List<Path>> actual = new ArrayList<>();
		for (Set<File> group : lf) {
			List<Path> pathGroup = new ArrayList<>();
			for (File file : group) {
				pathGroup.add(file.toPath());
			}
			actual.add(pathGroup);
		}

//		System.out.println("Результат: ");
//		for (List<String> s : actual) {
//			System.out.println("Группа: ---------- ");
//			for (String s1 : s) {
//				System.out.println(s1);
//			}
//		}

		// Проверка результата
		assertEquals(expected.size(), actual.size());
		for (List<Path> expectedGroup : expected) {
			boolean found = false;
			Set<Path> expectedSet = new HashSet<>(expectedGroup);
			for (List<Path> actualGroup : actual) {
				Set<Path> actualSet = new HashSet<>(actualGroup);
				if (actualSet.equals(expectedSet)) {
					found = true;
					break;
				}
			}
			assertEquals(true, found);

		}
	}
	
}
