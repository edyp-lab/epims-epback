package cea.edyp.epims.transfer.model;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFormatFactory {

	private static final String DATAFORMATS_FILEPATH = "/cea/edyp/epims/transfer/dataformats.xml";
	private static final Logger logger = LoggerFactory.getLogger(DataFormatFactory.class);
	private static Hashtable<String, DataFormat> formats;
	
	private static void readDataFormats() {
		try {
			logger.info("Reading data format from "+DATAFORMATS_FILEPATH);
			URL configURL = DataFormatFactory.class.getResource(DATAFORMATS_FILEPATH);
			FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class).configure(new Parameters().properties()
							.setURL(configURL));
			XMLConfiguration dataConfig = builder.getConfiguration();
			Object o = dataConfig.getProperty("dataformats.dataformat.name");
			formats = new Hashtable<>();
			if (o instanceof Collection) {
				Collection c = (Collection) o;
				for (int i = 0; i < c.size(); i++) {
					String name = dataConfig.getString("dataformats.dataformat(" + i + ").name");
					String className = dataConfig.getString("dataformats.dataformat(" + i + ").class");
					logger.info("  - Read data format: "+ name+ " (" + className+")");
					try {
						Class dfClass = Class.forName(className);
						DataFormat df = (DataFormat) dfClass.getDeclaredConstructor().newInstance();
						formats.put(name, df);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						logger.error("ERROR {} ", e.getMessage());
					} catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
          }
        }
			} else {
				logger.debug(" Dataformats is not a collection : " + o);
			}

		} catch (ConfigurationException e) {
			logger.debug("Can't get data format configuration file  "+DATAFORMATS_FILEPATH, e);
		}
	}
	
	public static DataFormat getDataFormat(String name) {
		if (formats == null) {
			readDataFormats();
		}
		return formats.get(name);
	}
	
}
