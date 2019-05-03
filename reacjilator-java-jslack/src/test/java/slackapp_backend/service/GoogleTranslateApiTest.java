package slackapp_backend.service;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GoogleTranslateApiTest {

    @Test
    public void test() throws IOException {
        GoogleTranslateApi service = new GoogleTranslateApi();
        String result = service.translate("Hi there", "ja");
        assertThat(result, is("こんにちは"));
    }

}