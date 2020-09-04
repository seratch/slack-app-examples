package slackapp_backend.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.slack.api.app_backend.SlackSignature;
import com.slack.api.bolt.aws_lambda.request.ApiGatewayRequest;
import com.slack.api.bolt.aws_lambda.response.ApiGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import slackapp_backend.service.AmazonWebServices;

import java.util.Collections;

// This is just a minimal working example
@Slf4j
public class EchoHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private final SlackSignature.Verifier signatureVerifier = new SlackSignature.Verifier(new SlackSignature.Generator());
    private final AmazonWebServices aws = new AmazonWebServices();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest req, Context context) {
        log.info("request: {}", req);

        String timestamp = req.getHeaders().get(SlackSignature.HeaderNames.X_SLACK_REQUEST_TIMESTAMP);
        String body = req.getBody();
        String signature = req.getHeaders().get(SlackSignature.HeaderNames.X_SLACK_SIGNATURE);
        if (!signatureVerifier.isValid(timestamp, body, signature) && !aws.isLocalDev(context)) {
            return ApiGatewayResponse.builder().statusCode(401).build();
        }

        if (body != null && body.equals(WarmupHandler.PAYLOAD_STRING)) {
            return ApiGatewayResponse.builder().statusCode(200).build();
        } else {
            return ApiGatewayResponse.builder()
                    .statusCode(200)
                    .headers(Collections.singletonMap("Content-Type", "application/json"))
                    .objectBody(req.getQueryStringParameters())
                    .build();
        }
    }

}