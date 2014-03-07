package com.sohu.goldmine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JobConfig {

	private static final Log LOG = LogFactory.getLog(JobConfig.class);

	private static Properties p = new Properties();

	public static boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		return p.containsKey(key) ? Boolean.parseBoolean(p.getProperty(key))
				: defaultValue;
	}

	public static String getString(String key) {
		return getString(key, "");
	}

	public static String getString(String key, String defaultValue) {
		return p.containsKey(key) ? p.getProperty(key) : defaultValue;
	}

	public static int getInt(String key) {
		return getInt(key, 0);
	}

	public static int getInt(String key, int defaultValue) {
		return p.containsKey(key) ? Integer.parseInt(p.getProperty(key))
				: defaultValue;
	}

	static {
		ClassLoader cL = Thread.currentThread().getContextClassLoader();
		if (cL == null) {
			cL = JobConfig.class.getClassLoader();
		}
		URL url = cL.getResource("config.properties");
		if (url != null)
			try {
				InputStream in = url.openStream();
				p.load(in);
				in.close();
			} catch (IOException e) {
				LOG.error("reading config.properties", e);
			}
		else
			LOG.error("config.properties not found");
	}

}
