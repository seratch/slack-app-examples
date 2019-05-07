package slackapp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest;
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import slackapp.service.OAuthStateService;

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
public class InstallationHandler implements RequestHandler<ApiGatewayRequest, ApiGatewayResponse> {

    private static final String CLIENT_ID = System.getenv("SLACK_CLIENT_ID");
    private static final String SCOPES_CSV = "commands"; // TODO
    private static final String APP_URL = "https://slack.com/oauth/authorize" +
            "?client_id=" + CLIENT_ID +
            "&scope=" + SCOPES_CSV +
            "&state=";

    private final OAuthStateService oAuthStateService = new OAuthStateService();

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayRequest request, Context context) {
        if (log.isDebugEnabled()) {
            log.debug("received: {}", request);
        }
        try {
            String state = oAuthStateService.issueNewState();
            return ApiGatewayResponse.build302Response(APP_URL + state);

        } catch (Exception e) {
            return ApiGatewayResponse.builder().setStatusCode(500).build();
        }
    }

}
