package slackapp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * To deal with AWS Lambda's cold start problems.
 * The issue is more likely to happen for lambda functions written in Java.
 */
@Slf4j
public class WarmupHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    // refer to serverless.yml
    private static final String SERVERLESS_SERVICE = System.getenv("SERVERLESS_SERVICE");
    private static final String SERVERLESS_STAGE = System.getenv("SERVERLESS_STAGE");

    private static String functionNamePrefix() {
        return SERVERLESS_SERVICE + "-" + SERVERLESS_STAGE + "-";
    }

    private static List<String> functionNamesToWarmup = Stream.of(
            "echo-command", // functions.{name} in serverless.yml
            "events",
            "installation",
            "interactivity",
            "oauth-callback"
    ).map(name -> functionNamePrefix() + name).collect(toList());

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        if (log.isDebugEnabled()) {
            log.debug("received: {}", request);
        }

        return null;
    }

}
