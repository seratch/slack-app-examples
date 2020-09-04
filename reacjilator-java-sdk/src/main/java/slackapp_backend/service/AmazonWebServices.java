package slackapp_backend.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.GsonBuilder;
import com.slack.api.bolt.aws_lambda.request.ApiGatewayRequest;

public class AmazonWebServices {

    public Regions region() {
        // TODO: Use your own region here
        return Regions.AP_NORTHEAST_1;
    }

    public AWSLambda createLambdaClient() {
        return AWSLambdaClient.builder().withRegion(region().getName()).build();
    }

    public InvokeResult invokeLambdaFunction(Context context, ApiGatewayRequest request) {
        InvokeResult result = createLambdaClient().invoke(new InvokeRequest()
                .withFunctionName(context.getFunctionName())
                .withPayload(new GsonBuilder().create().toJson(request))
                .withInvocationType(InvocationType.Event)
        );
        return result;
    }

    public String getServerlessStage() {
        return System.getenv("SERVERLESS_STAGE");
    }

    // when running by `sam local start-api`
    public boolean isLocalDev(Context context) {
        return context != null && context.getFunctionName().equals("test");
    }

}