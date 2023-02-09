package be.vlaanderen.informatievlaanderen.ldes.ldto.output;

import be.vlaanderen.informatievlaanderen.ldes.ldto.services.RdfModelConverter;
import be.vlaanderen.informatievlaanderen.ldes.ldto.types.LdtoOutput;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Objects;

import static be.vlaanderen.informatievlaanderen.ldes.ldto.LdtoConstants.CONTENT_TYPE;
import static be.vlaanderen.informatievlaanderen.ldes.ldto.LdtoConstants.DEFAULT_OUTPUT_LANG;
import static be.vlaanderen.informatievlaanderen.ldes.ldto.services.RdfModelConverter.getLang;

public class LdtoConsoleOut implements LdtoOutput {
	private final Logger LOGGER = LoggerFactory.getLogger(LdtoConsoleOut.class);

	private Lang outputLanguage = DEFAULT_OUTPUT_LANG;

	public LdtoConsoleOut(Map<String, String> config) {
		if (config.containsKey(CONTENT_TYPE)) {
			outputLanguage = getLang(
					Objects.requireNonNull(MediaType.valueOf(config.get(CONTENT_TYPE))));
		}
	}

	@Override
	public void sendLinkedData(Model linkedDataModel) {
		LOGGER.info(RdfModelConverter.toString(linkedDataModel, outputLanguage));
	}
}
