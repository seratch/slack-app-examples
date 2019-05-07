package slackapp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.oauth.OAuthAccessResponse;
import com.github.seratch.jslack.app_backend.oauth.OAuthFlowOperator;
import com.github.seratch.jslack.app_backend.oauth.payload.VerificationCodePayload;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import slackapp.service.AccountService;
import slackapp.service.OAuthStateService;
import slackapp.service.SlackWebApi;

import java.io.IOException;

/**
 * OAuth flow handler.
 * The endpoint which runs this handler needs to be exposed to end users.
 * <p>
 * If you submit your Slack apps to Slack App Directory, implementing OAuth flow is mandatory
 * even if your app doesn't require OAuth access tokens to run.
 * <p>
 * see also "https://api.slack.com/docs/oauth"
 */
@Slf4j
public class OAuthCallbackHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    // TODO: modify these URLs
    private static final String CANCELLATION_URL = "https://www.example.com/cancellation";
    private static final String ERROR_URL = "https://www.example.com/error";
    private static final String COMPLETION_URL = "https://www.example.com/thank-you";

    private final SlackWebApi slackWebApi = new SlackWebApi();
    private final OAuthStateService oAuthStateService = new OAuthStateService();
    private final AccountService accountService = new AccountService();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        if (log.isDebugEnabled()) {
            log.debug("received: {}", request);
        }

        VerificationCodePayload payload = VerificationCodePayload.from(request.getQueryStringParameters());
        if (payload.getError() != null) {
            // The user cancelled the installation on the confirmation page
            log.info("The installation is cancelled - state: {}, error: {}", payload.getState(), payload.getError());
            return ApiGatewayResponse.build302Response(CANCELLATION_URL);
        }

        // OAuth "state" parameter
        if (!oAuthStateService.validateAndConsume(payload.getState())) {
            // In the case that "state" parameter is invalid
            log.info("Invalid state parameter detected - code: {}, state: {}", payload.getCode(), payload.getState());
            return ApiGatewayResponse.build302Response(ERROR_URL);
        }

        try {
            // Fetch an OAuth access token using the given temporary code
            OAuthFlowOperator oAuthFlowOperator = slackWebApi.createOAuthFlowOperator();
            OAuthAccessResponse accessTokenResponse = oAuthFlowOperator.callOAuthAccessMethod(payload);
            if (accessTokenResponse.isOk()) {
                // TODO: If your app needs this access token for further operations, store it at this timing
                accountService.store(accessTokenResponse);

                // Redirect the user to the completion page
                return ApiGatewayResponse.build302Response(COMPLETION_URL);

            } else {
                // For some reasons, the API call failed
                log.error("Failed to call oauth.access API - error: {}", accessTokenResponse.getError());
                return ApiGatewayResponse.build302Response(ERROR_URL);
            }

        } catch (IOException | SlackApiException e) {
            // For some reasons, the API call failed
            log.error("Failed to handle an OAuth request error: {}, {}", e.getClass().getCanonicalName(), e.getMessage());
            return ApiGatewayResponse.build302Response(ERROR_URL);
        }
    }

}
