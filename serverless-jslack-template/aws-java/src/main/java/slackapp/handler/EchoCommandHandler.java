package slackapp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import com.github.seratch.jslack.api.model.block.element.ButtonElement;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.util.SlackSignatureVerifier;
import lombok.extern.slf4j.Slf4j;
import slackapp.service.SlackWebApi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Command Request Handler
 */
@Slf4j
public class EchoCommandHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    // NOTE: Verification Token is already deprecated
    // Validate X-Slack-Signature is the appropriate way to make sure if a request comes from Slack Platform
    // https://api.slack.com/docs/verifying-requests-from-slack
    private final SlackSignatureVerifier signatureVerifier = new SlackSignatureVerifier();
    private final SlackWebApi slackWebApi = new SlackWebApi();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        if (log.isDebugEnabled()) {
            log.debug("received: {}", request);
        }

        if (signatureVerifier.isValid(request)) {
            // https://api.slack.com/tools/block-kit-builder?blocks=%5B%7B%22type%22%3A%22section%22%2C%22text%22%3A%7B%22type%22%3A%22mrkdwn%22%2C%22text%22%3A%22This%20is%20a%20section%20block%20with%20a%20button.%22%7D%2C%22accessory%22%3A%7B%22type%22%3A%22button%22%2C%22text%22%3A%7B%22type%22%3A%22plain_text%22%2C%22text%22%3A%22Click%20Me%22%7D%2C%22value%22%3A%22click_me_123%22%2C%22action_id%22%3A%22button%22%7D%7D%2C%7B%22type%22%3A%22actions%22%2C%22block_id%22%3A%22actionblock789%22%2C%22elements%22%3A%5B%7B%22type%22%3A%22button%22%2C%22text%22%3A%7B%22type%22%3A%22plain_text%22%2C%22text%22%3A%22Link%20Button%22%7D%2C%22url%22%3A%22https%3A%2F%2Fapi.slack.com%2Fblock-kit%22%7D%5D%7D%5D
            SectionBlock sectionBlock = SectionBlock.builder()
                    .text(MarkdownTextObject.builder()
                            .text("Got a message like this: " + request.getBody())
                            .build())
                    .accessory(ButtonElement.builder()
                            .text(PlainTextObject.builder().text("Continue interaction").build())
                            .value("click_me_123")
                            .build())
                    .build();
            ActionsBlock actionsBlock = ActionsBlock.builder()
                    .elements(Arrays.asList(
                            ButtonElement.builder()
                                    .text(PlainTextObject.builder().text("Open BlockKit documents").build())
                                    .url("https://api.slack.com/block-kit")
                                    .build()
                    ))
                    .build();
            SlashCommandResponse responseBody = SlashCommandResponse.builder()
                    .blocks(Arrays.asList(sectionBlock, actionsBlock))
                    .build();

            try {
                WebhookResponse webhookResponse = slackWebApi.sendToWebhook(Payload.builder()
                        .text("The Incoming Webhook is working!")
                        .username("Customized username")
                        .build());
                if (webhookResponse.getCode() == 200) {
                    log.info("Successfully sent a message to the webhook: {}", webhookResponse);
                    return ApiGatewayResponse.builder()
                            .statusCode(200)
                            .headers(Collections.singletonMap("Content-Type", "application/json"))
                            .objectBody(responseBody)
                            .build();
                } else {
                    log.error("Got an error from the webhook: {}", webhookResponse);
                    return ApiGatewayResponse.builder().statusCode(500).build();
                }
            } catch (IOException e) {
                log.error("Failed to call the webhook because {}", e.getMessage(), e);
                return ApiGatewayResponse.builder().statusCode(500).build();
            }

        } else {
            // invalid signature
            return ApiGatewayResponse.builder().statusCode(401).build();
        }
    }

}
