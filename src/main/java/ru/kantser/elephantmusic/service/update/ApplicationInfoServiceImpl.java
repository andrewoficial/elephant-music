package ru.kantser.elephantmusic.service.update;

import com.google.inject.Singleton;

import java.io.InputStream;
import java.util.Properties;

@Singleton
public class ApplicationInfoServiceImpl implements ApplicationInfoService {
    private final String version;

    public ApplicationInfoServiceImpl() {
        String v = getClass().getPackage().getImplementationVersion();
        if (v == null) {
            v = loadVersionFromProperties();
        }
        this.version = v != null ? v : "1.0.1";
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getName() {
        return "ElephantMusic";
    }

    private String loadVersionFromProperties() {
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("version");
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
