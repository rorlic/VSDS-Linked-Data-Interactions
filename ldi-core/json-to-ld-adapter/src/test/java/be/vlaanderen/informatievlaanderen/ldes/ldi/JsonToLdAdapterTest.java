package be.vlaanderen.informatievlaanderen.ldes.ldi;

import be.vlaanderen.informatievlaanderen.ldes.ldi.exceptions.ParseToJsonException;
import be.vlaanderen.informatievlaanderen.ldes.ldi.exceptions.UnsupportedMimeTypeException;
import be.vlaanderen.informatievlaanderen.ldes.ldi.types.LdiAdapter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static org.junit.jupiter.api.Assertions.*;

@WireMockTest(httpPort = 10101)
class JsonToLdAdapterTest {

	private static final String CORE_CONTEXT = "http://localhost:10101/core-context.json";
	private static final String LOCAL_CONTEXT = "http://localhost:10101/context.json";
	private static final String MIMETYPE = "application/json";

	private JsonToLdAdapter translator;

	@BeforeEach
	void setUp() {
		translator = new JsonToLdAdapter(CORE_CONTEXT, LOCAL_CONTEXT);
	}

	@Test
	void when_ValidJson_Then_ModelIsIsomorphic() throws IOException {
		String data = Files.readString(Path.of("src/test/resources/example.json"));
		Model expected = readModelFromFile("src/test/resources/expected-ld.json");

		Model actual = translator.apply(new LdiAdapter.Content(data, MIMETYPE)).toList().get(0);

		assertTrue(expected.isIsomorphicWith(actual));
	}

	@Test
	void when_InValidJson_Then_ExceptionIsThrown() throws IOException {
		String data = Files.readString(Path.of("src/test/resources/invalid.json"));
		LdiAdapter.Content content = new LdiAdapter.Content(data, MIMETYPE);

		Exception e = assertThrows(ParseToJsonException.class,
				() -> translator.apply(content));

		assertEquals("Could not parse string to JSON. String with value:\n"
				+ data + "\nCause: " + "Illegal: [null]", e.getMessage());
	}

	@Test
	void when_NoLocalContext_Then_AddOnlyCoreContext() throws IOException {
		translator = new JsonToLdAdapter(CORE_CONTEXT);
		String data = Files.readString(Path.of("src/test/resources/example.json"));
		Model expected = readModelFromFile("src/test/resources/expected-ld-single-context.json");

		Model actual = translator.apply(new LdiAdapter.Content(data, MIMETYPE)).toList().get(0);

		assertTrue(expected.isIsomorphicWith(actual));
	}

	@ParameterizedTest
	@ValueSource(strings = { "application/json", "application/json;charset=utf-8" })
	void when_CorrectMimeType(String mimeType) {
		assertDoesNotThrow(() -> translator.apply(new LdiAdapter.Content("{}", mimeType)));

	}

	@ParameterizedTest
	@ValueSource(strings = { "text/plain", "nonsense" })
	void when_InCorrectMimeType(String mimeType) {
		LdiAdapter.Content content = new LdiAdapter.Content("{}", mimeType);
		Exception e = assertThrows(UnsupportedMimeTypeException.class, () -> translator.apply(content));
		assertEquals("Unsupported MIME type was provided: " + mimeType + ". Supported MIME type is: application/json",
				e.getMessage());
	}

	@Test
	void when_CoreContextNull_Then_ThrowException() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> new JsonToLdAdapter(null));
		assertEquals("Core context can't be null", e.getMessage());
	}

	private Model readModelFromFile(String path) throws IOException {
		String data = Files.readString(Path.of(path));
		Model model = ModelFactory.createDefaultModel();
		RDFParser.fromString(data)
				.lang(Lang.JSONLD)
				.parse(model);
		return model;
	}
}
