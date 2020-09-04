package slackapp_backend.service;

import com.amazonaws.util.IOUtils;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class LangCodes {

    private static final String FILENAME = "langcode.json";

    private final Map<String, String> allData;
    private final Set<String> allKeys;

    public LangCodes() throws IOException {
        ClassLoader classLoader = LangCodes.class.getClassLoader();
        try (InputStream fileResource = classLoader.getResourceAsStream(FILENAME)) {
            String jsonStr = IOUtils.toString(fileResource);
            this.allData = new GsonBuilder().create().fromJson(jsonStr, Map.class);
            this.allKeys = this.allData.keySet();
        }
    }

    public Map<String, String> getAllData() {
        return allData;
    }

    public Set<String> getAllKeys() {
        return allKeys;
    }

}