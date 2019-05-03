package slackapp_backend.handler;

import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.app_backend.events.payload.UrlVerificationPayload;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import slackapp_backend.service.AmazonWebServices;
import slackapp_backend.service.SlackEventsOperator;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SlackEventsHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private final AmazonWebServices AWS = new AmazonWebServices();
    private final SlackEventsOperator slackEventsOperator = SlackEventsOperator.getInstance();
    private final Gson gson = GsonFactory.createSnakeCase();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest req, Context context) {
        logRequest(req, context);

        String body = req.getBody();
        if (body == null || body.equals(WarmupHandler.PAYLOAD_STRING)) {
            // internal warmup request
            return OK_RESPONSE;
        } else {
            JsonObject payload = gson.fromJson(body, JsonElement.class).getAsJsonObject();
            String eventType = payload.get("type").getAsString();
            if (UrlVerificationPayload.TYPE.equals(eventType)) {
                // url_verification: https://api.slack.com/events/url_verification
                return handleUrlVerification(payload);
            } else {
                // events subscription
                if (context.getFunctionName().equals("test")) { // when running by `sam local start-api`
                    // local dev
                    slackEventsOperator.handleSynchronously(body);
                    // may be timed out towards requests from Slack Platform
                    return OK_RESPONSE;

                } else {
                    // on AWS
                    if (req.getPath() == null) { // this means this is an internal request
                        // do blocking here
                        slackEventsOperator.handleSynchronously(body);
                        // actually not in a hurry here
                        return OK_RESPONSE;

                    } else {
                        // Kick this function asynchronously
                        req.setPath(null); // The "path" can be modified only here
                        InvokeResult invokeResult = AWS.invokeLambdaFunction(context, req);
                        if (invokeResult.getStatusCode() != 200) {
                            log.error("Failed to invoke a function because {}", invokeResult.getFunctionError());
                        }
                        // NOTE: You need to return 200 OK within 3 seconds
                        return OK_RESPONSE;
                    }
                }
            }
        }
    }

    private void logRequest(ApiGatewayRequest req, Context context) {
        if (log.isDebugEnabled()) {
            log.debug("request: {}, context: {}", req, gson.toJson(context));
        } else {
            log.debug("request: {}", req);
        }
    }

    private static ApiGatewayResponse handleUrlVerification(JsonObject payload) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setHeaders(headers)
                .setObjectBody(payload.get("challenge").getAsString())
                .build();
    }

    private static final ApiGatewayResponse OK_RESPONSE;

    static {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        OK_RESPONSE = ApiGatewayResponse.builder().setStatusCode(200).setObjectBody(body).build();
    }

}