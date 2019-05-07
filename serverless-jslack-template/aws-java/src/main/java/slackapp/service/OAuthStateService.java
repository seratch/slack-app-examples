package slackapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class OAuthStateService {

    private static final String S3_BUCKET_NAME = "something-unique";
    private static final String S3_OAUTH_STATE_KEY_PREFIX = "oauth_token/";
    private static final Gson gson = GsonFactory.createSnakeCase();

    private boolean s3Enabled = false;

    public String issueNewState() {
        // TODO: generate a random value and store it on the server side
        if (s3Enabled) {
            // This is a simple implementation using Amazon S3
            // I recommend you to have a "Lifecycle Rule" for this bucket to set expiration to the created S3 objects
            AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
            String state = System.currentTimeMillis() + "-" + new SecureRandom().nextInt(256);
            PutObjectResult result = s3.putObject(S3_BUCKET_NAME, S3_OAUTH_STATE_KEY_PREFIX + state, "");
            if (log.isDebugEnabled()) {
                log.debug("PutObject result with state: {} - {}", state, gson.toJson(result));
            }
            return state;
        } else {
            return "something-123";
        }
    }

    public boolean validateAndConsume(String state) {
        // TODO: verify the value and consume it
        if (s3Enabled) {
            try {
                AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
                S3Object s3Object = s3.getObject(S3_BUCKET_NAME, S3_OAUTH_STATE_KEY_PREFIX + state);
                boolean isValid = s3Object != null;
                if (isValid) {
                    s3.deleteObject(S3_BUCKET_NAME, S3_OAUTH_STATE_KEY_PREFIX + state);
                }
                return isValid;
            } catch (Exception e) {
                String name = S3_BUCKET_NAME + "/" + S3_OAUTH_STATE_KEY_PREFIX + state;
                log.error("Failed to get the S3 object {} because {}", name, e.getMessage(), e);
                return false;
            }
        } else {
            return state != null;
        }
    }

}
