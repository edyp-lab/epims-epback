package cea.edyp.epims.transfer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(InstrumentConfiguration.class);
	public  static final String INSTRUMENT_CONFIGURATION_FILEPATH = "./conf/instruments.xml";
	
	private final String label;
	private String name;
	private String sourcePath;
	private String format;
	private Boolean removeFiles;
	private Integer transferMode;

	public static Map<String, InstrumentConfiguration> readInstrumentsXMLConfiguration() throws ConfigurationException {

		Map<String, InstrumentConfiguration> instrumentsConfig = new HashMap<String, InstrumentConfiguration>();
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class).configure(new Parameters().properties()
						.setFileName(INSTRUMENT_CONFIGURATION_FILEPATH));
		XMLConfiguration instrConfigXML = builder.getConfiguration();
		logger.info("Loading instruments configuration from " + INSTRUMENT_CONFIGURATION_FILEPATH);
		Object o = instrConfigXML.getProperty("instrument.label");
		if (o instanceof Collection) {
			Collection<String> instruments = (Collection<String>) o;
			for (int i = 0; i < instruments.size(); i++) {
				String label = instrConfigXML.getString("instrument(" + i + ").label");
				InstrumentConfiguration configuration = new InstrumentConfiguration(label);
				configuration.name = instrConfigXML.getString("instrument(" + i + ").name");
				configuration.sourcePath = instrConfigXML.getString("instrument(" + i + ").src");
				configuration.format = instrConfigXML.getString("instrument(" + i + ").format");
				configuration.removeFiles = instrConfigXML.getBoolean("instrument(" + i + ").removeFiles");
				configuration.transferMode = instrConfigXML.getInt("instrument(" + i + ").transfer_mode");
				instrumentsConfig.put(label, configuration);
			}
		} else {
			logger.warn("Only one element, not a collection!");
			String label = instrConfigXML.getString("instrument.label");
			InstrumentConfiguration configuration = new InstrumentConfiguration(label);
			configuration.name = instrConfigXML.getString("instrument.name");
			configuration.sourcePath = instrConfigXML.getString("instrument.src");
			configuration.format = instrConfigXML.getString("instrument.format");
			configuration.removeFiles = instrConfigXML.getBoolean("instrument.removeFiles");
			configuration.transferMode = instrConfigXML.getInt("instrument.transfer_mode");
			instrumentsConfig.put(label, configuration);
		}

		return instrumentsConfig;
	}

	public InstrumentConfiguration(String label, String name, String format, Boolean removeFiles, Integer transferMode) {
		super();
		this.label = label;
		this.name = name;
		this.format = format;
		this.removeFiles = removeFiles;
		this.transferMode = transferMode;
	}

	public InstrumentConfiguration(String label) {
		super();
		this.label = label;
	}

	public static Logger getLogger() {
		return logger;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}

	public String getFormat() {
		return format;
	}

	public Boolean getRemoveFiles() {
		return removeFiles;
	}

	public Integer getTransferMode() {
		return transferMode;
	}

	public String getSourcePath() {
		return sourcePath;
	}

}
