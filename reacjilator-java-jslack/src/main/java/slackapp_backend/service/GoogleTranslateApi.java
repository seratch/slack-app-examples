package slackapp_backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.io.InputStream;

public class GoogleTranslateApi {

    private final Translate translateApi;

    // Replacing "google-service-account.json" under src/main/resources is expected here
    // To enable this service to run anywhere, we need to avoid using
    // the env variable "GOOGLE_APPLICATION_CREDENTIALS" pointing the filepath of a JSON file
    private static final String SERVICE_ACCOUNT_JSON_FILENAME = "google-service-account.json";

    public GoogleTranslateApi() throws IOException {
        ClassLoader classLoader = GoogleTranslateApi.class.getClassLoader();
        try (InputStream jsonFileResource = classLoader.getResourceAsStream(SERVICE_ACCOUNT_JSON_FILENAME)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(jsonFileResource);
            TranslateOptions options = TranslateOptions.newBuilder().setCredentials(credentials).build();
            this.translateApi = options.getService();
        }
    }

    public String translate(String text, String lang) {
        Translation translationResult = translateApi.translate(text, TranslateOption.targetLanguage(lang));
        return translationResult.getTranslatedText();
    }

}
