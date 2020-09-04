package slackapp_backend.handler;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.slack.api.bolt.aws_lambda.request.ApiGatewayRequest;
import com.slack.api.bolt.aws_lambda.response.ApiGatewayResponse;
import com.slack.api.util.json.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import slackapp_backend.service.AmazonWebServices;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * To address cold start problems
 */
@Slf4j
public class WarmupHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private static final AmazonWebServices AWS = new AmazonWebServices();

    private static final Map<String, Object> PAYLOAD;
    public static final String PAYLOAD_STRING;

    static {
        PAYLOAD = new HashMap<>();
        PAYLOAD.put("warmup", true);
        PAYLOAD_STRING = GsonFactory.createSnakeCase().toJson(PAYLOAD);
    }

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest req, Context context) {
        log.info("request: {}", req);

        AWSLambda lambda = AWS.createLambdaClient();
        String serverlessStage = AWS.getServerlessStage();
        List<String> functionNames = Arrays.asList(
                "reacjilator-" + serverlessStage + "-echo",
                "reacjilator-" + serverlessStage + "-events"
        );
        for (String functionName : functionNames) {
            InvokeRequest invokeReq = new InvokeRequest().withFunctionName(functionName).withPayload(PAYLOAD_STRING);
            InvokeResult result = lambda.invoke(invokeReq);
            if (result.getStatusCode() != 200) {
                log.error("Failed to warmup the function: {}, result: {}", functionName, result.getFunctionError());
            }
        }

        return ApiGatewayResponse.builder()
                .statusCode(200)
                .objectBody("Done")
                .build();
    }
}