package slackapp.service;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.github.seratch.jslack.app_backend.config.SlackAppConfig;
import com.github.seratch.jslack.app_backend.oauth.OAuthFlowOperator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class SlackWebApi {

    private final Slack slack = Slack.getInstance();

    private final String webhookUrl;

    public SlackWebApi() {
        this(System.getenv("SLACK_WEBHOOK_URL"));
    }

    public SlackWebApi(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    private final SlackAppConfig slackAppConfig = SlackAppConfig.builder()
            .clientId(System.getenv("SLACK_APP_CLIENT_ID"))
            .clientSecret(System.getenv("SLACK_APP_CLIENT_SECRET"))
            .redirectUri(System.getenv("SLACK_APP_REDIRECT_URI"))
            .build();

    public OAuthFlowOperator createOAuthFlowOperator() {
        return new OAuthFlowOperator(slack, slackAppConfig);
    }

    public WebhookResponse sendToWebhook(Payload payload) throws IOException {
        WebhookResponse response = slack.send(this.webhookUrl, payload);
        return response;
    }

}
