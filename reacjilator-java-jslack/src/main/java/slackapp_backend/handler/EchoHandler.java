package slackapp_backend.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

// This is just a minimal working example
@Slf4j
public class EchoHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private static final Map<String, String> HEADERS = new HashMap<>();

    static {
        HEADERS.put("Content-Type", "application/json");
    }

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest req, Context context) {
        log.info("request: {}", req);

        String body = req.getBody();
        if (body != null && body.equals(WarmupHandler.PAYLOAD_STRING)) {
            return ApiGatewayResponse.builder().setStatusCode(200).build();
        } else {
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setHeaders(HEADERS)
                    .setObjectBody(req.getQueryStringParameters())
                    .build();
        }
    }

}