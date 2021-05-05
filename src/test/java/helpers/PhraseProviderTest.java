package helpers;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testklasse fuer PhraseProvider.
 *
 * @author Anonymous Student
 */
public class PhraseProviderTest {
	@Test
	public void test1() {
		final var sut = new PhraseProvider("/PhraseProviderTest/phrase-no-placeholder.txt", new HashMap<>());
		final var expected = "This is a phrase with no placeholders.";
		final var have = sut.get();
		assertEquals(expected, have);
	}

	@Test
	public void test2() {
		final var sut = new PhraseProvider("/PhraseProviderTest/phrase-with-placeholder.txt", new HashMap<>());
		final var expected = "This is a phrase with placeholders.\n" +
				"${a}\n" +
				"${b}\n" +
				"${someName}";
		final var have = sut.get().replace("\r\n", "\n");
		assertEquals(expected, have);
	}

	@Test
	public void test3() {
		final var replacements = new HashMap<String, String>() {{
			put("a", "test");
		}};
		final var sut = new PhraseProvider("/PhraseProviderTest/phrase-with-placeholder.txt", replacements);
		final var expected = "This is a phrase with placeholders.\n" +
				"test\n" +
				"${b}\n" +
				"${someName}";
		final var have = sut.get().replace("\r\n", "\n");
		assertEquals(expected, have);
	}

	@Test
	public void test4() {
		final var sut = new PhraseProvider("/PhraseProviderTest/random/", new HashMap<>());
		final var expected = Set.of("A", "B", "C");
		final var have = sut.getRandom();
		final var result = expected.contains(have);
		assertTrue(result);
	}

	@Test
	public void test5() {
		final var sut = new PhraseProvider("/PhraseProviderTest/random/", new HashMap<>());
		final var expected = Set.of("A", "B", "C");
		final var have = new HashSet<String>();
		IntStream.range(0, 100).forEach(i -> have.add(sut.getRandom()));
		final var result = have.containsAll(expected);
		assertTrue(result);
	}
}