package helpers;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Eine Klasse die Phrases aus den Resourcen lädt und formatiert
 *
 * @author Anonymous Student
 */
public class PhraseProvider {
	private final Random rng = new Random();
	private final Map<String, String> replacements;
	private final String phrase;

	/**
	 * Konstruktor des PhraseProviders mit Pfad und Map an texten welche ersetzt werden sollen.
	 * @param phrase Der Ordner oder die Datei die verwendet werden soll.
	 * @param replacements Map an Strings welche ersetzt werden sollen.
	 */
	public PhraseProvider(String phrase, Map<String, String> replacements) {
		this.phrase = Objects.requireNonNull(phrase);
		this.replacements = Objects.requireNonNull(replacements);
	}

	private String replace(String text) {
		var buffer = text;
		for (final var entry : replacements.entrySet()) {
			buffer = buffer.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
		}
		return buffer;
	}

	private String readFile(String path) {
		try (final var reader = getClass().getResourceAsStream(path)) {
			final var buffer = Objects.requireNonNull(reader).readAllBytes();
			return new String(buffer);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Sucht eine bestimmte Datei aus Resourcen und ersetzt die Platzhalter.
	 * @return der ersetzte Text
	 */
	public String get() {
		final var content = readFile(phrase);
		return replace(content);
	}

	/**
	 * Sucht eine zufällige Datei aus dem gegebenen Resourcenordner und ersetzt die Platzhalter.
	 * @return der ersetzte Text
	 */
	public String getRandom() {
		final var resource = getClass().getResource(phrase);
		try {
			final var folderURI = resource.toURI();
			final var files = Objects.requireNonNull(new File(folderURI).listFiles());
			final var randomNumber = rng.nextInt(files.length);
			final var filePath = files[randomNumber].getName();
			final var content = readFile(phrase + filePath);
			return replace(content);
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}
}
