package slackapp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.github.seratch.jslack.api.model.Action;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.github.seratch.jslack.app_backend.interactive_messages.ResponseSender;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.AttachmentActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.BlockActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.PayloadTypeDetector;
import com.github.seratch.jslack.app_backend.interactive_messages.response.ActionResponse;
import com.github.seratch.jslack.app_backend.message_actions.payload.MessageActionPayload;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.PayloadExtractor;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.util.SlackSignatureVerifier;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Interactivity
 * <p>
 * see "https://api.slack.com/messaging/interactivity"
 */
@Slf4j
public class InteractivityHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    // NOTE: Verification Token is already deprecated
    // Validate X-Slack-Signature is the appropriate way to make sure if a request comes from Slack Platform
    // https://api.slack.com/docs/verifying-requests-from-slack
    private final SlackSignatureVerifier signatureVerifier = new SlackSignatureVerifier();

    private final PayloadExtractor payloadExtractor = new PayloadExtractor();
    private final PayloadTypeDetector payloadTypeDetector = new PayloadTypeDetector();

    // You can call a restricted set of Web API methods by a bot token.
    // https://api.slack.com/bot-users#methods
    // If your application uses only the above, using a bot token is fine.
    private final String slackApiToken = System.getenv("SLACK_API_TOKEN");
    private final Slack slack = Slack.getInstance();

    private final ResponseSender responder = new ResponseSender(slack);

    private final Gson gson = GsonFactory.createSnakeCase();

    private static class Actions {
        private Actions() {
        }

        public static final String ClickRemoveButton = "click_remove_button";
        public static final String ChooseAnOptionInSelection = "choose_an_option";
    }

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        if (log.isDebugEnabled()) {
            log.debug("received: {}", request);
        }

        if (signatureVerifier.isValid(request)) {
            String payloadString = payloadExtractor.extractPayloadJsonAsString(request);
            String type = payloadTypeDetector.detectType(payloadString);
            if (BlockActionPayload.TYPE.equals(type)) {
                // Interactive Messages (BlockKit)
                // * https://api.slack.com/messaging/interactivity
                BlockActionPayload payload = gson.fromJson(payloadString, BlockActionPayload.class);

                // Interaction with the following outmoded `attachments` will be handled by the case for `AttachmentActionPayload.TYPE` below
                // * https://api.slack.com/docs/message-buttons
                // * https://api.slack.com/docs/messages/builder
                Action.Option opt1 = new Action.Option();
                opt1.setText("Image");
                opt1.setValue("image");
                Action.Option opt2 = new Action.Option();
                opt2.setText("Emoji");
                opt2.setValue("emoji");

                List<Attachment> attachments = Arrays.asList(
                        Attachment.builder()
                                .callbackId("the_callback_id")
                                .text("Thank you for confirming the request!")
                                .actions(Arrays.asList(
                                        Action.builder()
                                                .name(Actions.ClickRemoveButton)
                                                .text("Remove this message")
                                                .type(Action.Type.BUTTON)
                                                .build(),
                                        Action.builder()
                                                .name(Actions.ChooseAnOptionInSelection)
                                                .text("Replace this with...")
                                                .type(Action.Type.SELECT)
                                                .options(Arrays.asList(opt1, opt2))
                                                .build()
                                )).build()
                );

                ActionResponse actionResponse = ActionResponse.builder()
                        .replaceOriginal(true)
                        .responseType("in_channel")
                        .attachments(attachments)
                        .build();
                return sendActionResponse(payload.getResponseUrl(), actionResponse);

            } else if (AttachmentActionPayload.TYPE.equals(type)) {
                // Interactive Messages (attachments; outmoded)
                // * https://api.slack.com/docs/message-buttons
                AttachmentActionPayload payload = gson.fromJson(payloadString, AttachmentActionPayload.class);

                if (payload.getActions() == null || payload.getActions().size() == 0) {
                    log.warn("Invalid payload - no actions detected: {}", payload);
                    return ApiGatewayResponse.builder().statusCode(400).build();
                }
                if (payload.getActions().size() > 1) {
                    log.warn("Unexpectedly got 2+ actions: {}", payload);
                }
                AttachmentActionPayload.Action action = payload.getActions().get(0);
                if (Actions.ClickRemoveButton.equals(action.getName())) {

                    ActionResponse actionResponse = ActionResponse.builder()
                            .replaceOriginal(true)
                            .text("Removed!")
                            .build();
                    return sendActionResponse(payload.getResponseUrl(), actionResponse);

                } else if (Actions.ChooseAnOptionInSelection.equals(action.getName())) {
                    AttachmentActionPayload.Action.SelectedOption selectedOption = action.getSelectedOptions().get(0);
                    if (selectedOption != null) {
                        String selectedOptionValue = selectedOption.getValue();
                        if (selectedOptionValue.equals("image")) {
                            ActionResponse actionResponse = ActionResponse.builder()
                                    .replaceOriginal(true)
                                    .text("https://platform.slack-edge.com/img/default_application_icon.png")
                                    .build();
                            return sendActionResponse(payload.getResponseUrl(), actionResponse);

                        } else if (selectedOptionValue.equals("emoji")) {
                            ActionResponse actionResponse = ActionResponse.builder()
                                    .replaceOriginal(true)
                                    .text("Thanks :smiley_cat:")
                                    .build();
                            return sendActionResponse(payload.getResponseUrl(), actionResponse);

                        }
                    }
                    ActionResponse actionResponse = ActionResponse.builder().text("Nothing to do?").build();
                    return sendActionResponse(payload.getResponseUrl(), actionResponse);

                } else {
                    log.error("An unknown action detected: {}", payloadString);
                    return ApiGatewayResponse.builder().statusCode(400).build();
                }

            } else if (MessageActionPayload.TYPE.equals(type)) {
                // Message Actions
                // * https://api.slack.com/actions
                MessageActionPayload payload = gson.fromJson(payloadString, MessageActionPayload.class);
                try {
                    // Bot tokens are not capable of calling this method.
                    // https://api.slack.com/bot-users#methods
                    ConversationsRepliesResponse apiResponse = slack.methods().conversationsReplies(r -> r
                                    .token(slackApiToken)
                                    .ts(payload.getMessageTs())
                                    .channel(payload.getChannel().getId())
                                    .limit(1));
                    if (apiResponse.isOk()) {
                        ActionResponse actionResponse = ActionResponse.builder()
                                .replaceOriginal(true)
                                .text(apiResponse.getMessages().get(0).getText())
                                .build();
                        return sendActionResponse(payload.getResponseUrl(), actionResponse);

                    } else {
                        log.error("Got an error from conversations.replies API: {}", apiResponse.getError());
                        return ApiGatewayResponse.builder().statusCode(500).build();
                    }
                } catch (IOException | SlackApiException e) {
                    log.error("Failed to call conversations.replies API because {}", e.getMessage(), e);
                    return ApiGatewayResponse.builder().statusCode(500).build();
                }
            } else {
                log.error("An unknown pattern detected: {}", payloadString);
                return ApiGatewayResponse.builder().statusCode(400).build();
            }

        } else {
            // invalid signature
            return ApiGatewayResponse.builder().statusCode(401).build();
        }
    }

    private ApiGatewayResponse sendActionResponse(String responseUrl, ActionResponse actionResponse) {
        try {
            WebhookResponse webhookResponse = responder.send(responseUrl, actionResponse);
            if (webhookResponse.getCode() == 200) {
                log.info("Successfully replaced the original message: {}", webhookResponse);
                return ApiGatewayResponse.builder().statusCode(200).build();
            } else {
                log.error("Failed to send a response - status: {}", webhookResponse.getCode(), webhookResponse.getMessage());
                return ApiGatewayResponse.builder().statusCode(500).build();
            }
        } catch (IOException e) {
            log.error("Failed to send a response because {}", e.getMessage(), e);
            return ApiGatewayResponse.builder().statusCode(500).build();
        }
    }

}
