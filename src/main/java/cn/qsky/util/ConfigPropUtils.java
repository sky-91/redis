package cn.qsky.util;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigPropUtils {

    private static Properties properties = new Properties();

    static {
        ClassLoader loader = ConfigPropUtils.class.getClassLoader();
        InputStreamReader reader = null;
        InputStream inputStream = null;
        if (loader != null) {
            try {
                inputStream = loader.getResourceAsStream("config/hsm-config.properties");
                assert inputStream != null;
                reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                properties.load(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ConfigPropUtils() {
    }

    public static String getStringConfig(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getStringConfig(String key) {
        return properties.getProperty(key);
    }

    public static int getIntegerConfig(String key, Integer defaultValue) {
        String value = properties.getProperty(key);
        return StringUtils.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }

    public static int getIntegerConfig(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static String[] getStringArray(String key, String splitChar) {
        String str = getStringConfig(key);
        return str.split(splitChar);
    }

    public static int[] getIntegerArray(String key, String splitChar) {
        String[] stringArr = getStringConfig(key).split(splitChar);
        int[] result = new int[stringArr.length];
        for (int i = 0; i < stringArr.length; i++) {
            result[i] = Integer.parseInt(stringArr[i]);
        }
        return result;
    }
}
