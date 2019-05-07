package slackapp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.app_backend.events.EventsDispatcher;
import com.github.seratch.jslack.app_backend.events.EventsDispatcherFactory;
import com.github.seratch.jslack.app_backend.events.handler.AppMentionHandler;
import com.github.seratch.jslack.app_backend.events.handler.ReactionAddedHandler;
import com.github.seratch.jslack.app_backend.events.payload.AppMentionPayload;
import com.github.seratch.jslack.app_backend.events.payload.ReactionAddedPayload;
import com.github.seratch.jslack.app_backend.events.payload.UrlVerificationPayload;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.util.SlackSignatureVerifier;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * Events Subscription
 */
@Slf4j
public class EventsHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    // NOTE: Verification Token is already deprecated
    // Validate X-Slack-Signature is the appropriate way to make sure if a request comes from Slack Platform
    // https://api.slack.com/docs/verifying-requests-from-slack
    private final SlackSignatureVerifier signatureVerifier = new SlackSignatureVerifier();

    private final Gson gson = GsonFactory.createSnakeCase();
    private final EventsDispatcher eventsDispatcher = EventsDispatcherFactory.getInstance();

    public EventsHandler() {
        eventsDispatcher.register(new ReactionAddedHandler() {
            @Override
            public void handle(ReactionAddedPayload payload) {
                log.info("payload: {}", payload);
            }
        });
        eventsDispatcher.register(new AppMentionHandler() {
            @Override
            public void handle(AppMentionPayload payload) {
                log.info("payload: {}", payload);
            }
        });
    }

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        if (log.isDebugEnabled()) {
            log.debug("received: {}", request);
        }

        if (signatureVerifier.isValid(request)) {
            JsonObject payload = gson.fromJson(request.getBody(), JsonElement.class).getAsJsonObject();
            String eventType = payload.get("type").getAsString();
            if (UrlVerificationPayload.TYPE.equals(eventType)) {
                // url_verification: https://api.slack.com/events/url_verification
                ApiGatewayResponse response = ApiGatewayResponse.builder()
                        .setStatusCode(200)
                        .setHeaders(Collections.singletonMap("Content-Type", "text/plain"))
                        .setRawBody(payload.get("challenge").getAsString())
                        .build();
                log.info("response: {}", gson.toJson(response));
                return response;
            } else {
                // Synchronously handle this event
                // If the handler takes long, invoking another lambda function may be better
                eventsDispatcher.dispatch(request.getBody());

                // Need to return 200 OK within 3 seconds here
                return ApiGatewayResponse.builder()
                        .setStatusCode(200)
                        .setHeaders(Collections.singletonMap("Content-Type", "application/json"))
                        .setObjectBody(Collections.singletonMap("ok", true))
                        .build();
            }
        } else {
            // invalid signature
            return ApiGatewayResponse.builder().setStatusCode(401).build();
        }
    }

}
