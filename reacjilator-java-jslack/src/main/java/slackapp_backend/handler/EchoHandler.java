package slackapp_backend.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.util.SlackSignatureVerifier;
import lombok.extern.slf4j.Slf4j;
import slackapp_backend.service.AmazonWebServices;

import java.util.Collections;

// This is just a minimal working example
@Slf4j
public class EchoHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private final SlackSignatureVerifier signatureVerifier = new SlackSignatureVerifier();
    private final AmazonWebServices aws = new AmazonWebServices();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest req, Context context) {
        log.info("request: {}", req);

        if (!signatureVerifier.isValid(req) && !aws.isLocalDev(context)) {
            return ApiGatewayResponse.builder().statusCode(401).build();
        }

        String body = req.getBody();
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