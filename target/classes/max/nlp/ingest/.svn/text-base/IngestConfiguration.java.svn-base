package max.nlp.ingest;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;


public class IngestConfiguration {
	public static final String CONFIG_FILE = "config/ingestConfig.yaml";

	private static IngestConfiguration conf;

	private static Hashtable<Object, Object> table;

	@SuppressWarnings("static-access")
	public static IngestConfiguration getInstance() {
		if (conf == null) {
			conf = new IngestConfiguration();
			table = conf.table;
		}
		return conf;
	}

	private IngestConfiguration() {
		table = new Hashtable<Object, Object>();
		InputStream io = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(CONFIG_FILE);
		Yaml yaml = new Yaml();
		@SuppressWarnings("unchecked")
		Map<String, Object> yamlProps = (Map<String, Object>) yaml.load(io);
		for (Entry<String, Object> e : yamlProps.entrySet()) {
			table.put(e.getKey(), e.getValue());
		}

		// Close the stream while ignoring exceptions
		IOUtils.closeQuietly(io);
	}

	public void setProperty(String k, Object v) {
		table.put(k, v);
	}

	public Object getProperty(String key) {
		return table.get(key);
	}

	public String getString(String key) {
		return (String) table.get(key);
	}
}
