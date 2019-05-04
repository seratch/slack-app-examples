package slackapp.handler

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.github.seratch.jslack.app_backend.events.payload.UrlVerificationPayload
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.util.SlackSignatureVerifier
import com.github.seratch.jslack.common.json.GsonFactory
import com.google.gson.{ JsonElement, JsonObject }
import org.slf4j.LoggerFactory
import slackapp.service.{ AmazonWebServices, SlackEventsOperator }

import scala.collection.JavaConverters._

class SlackEventsHandler extends RequestHandler[ApiGatewayRequest, ApiGatewayResponse] {
  import SlackEventsHandler._

  override def handleRequest(req: ApiGatewayRequest, context: Context): ApiGatewayResponse = {
    if (!signatureVerifier.isValid(req) && !aws.isLocalDev(context)) {
      response.setStatusCode(401).build
    } else {
      req.getBody match {
        case WarmupHandler.payloadString => // internal warmup request
          successResponse

        case body =>
          val payload: JsonObject = toJsonObject(body)
          payload.get("type").getAsString match {
            case UrlVerificationPayload.TYPE =>
              // url_verification: https://api.slack.com/events/url_verification
              response
                .setStatusCode(200)
                .setHeaders(Map("Content-Type" -> "text/plain").asJava)
                .setObjectBody(payload.get("challenge").getAsString)
                .build

            case _ if aws.isLocalDev(context) => // local dev
              slackEventsOperator.handleSynchronously(body)
              // may be timed out towards requests from Slack Platform
              successResponse

            case _ => // on AWS
              if (req.getPath == null) { // this means this is an internal request
                // do blocking here
                slackEventsOperator.handleSynchronously(body)
                // actually not in a hurry here
                successResponse
              } else {            // Kick this function asynchronously
                req.setPath(null) // The "path" can be modified only here

                val invokeResult = aws.invokeLambdaFunction(context, req)
                if (invokeResult.getStatusCode != 200) {
                  log.error("Failed to invoke a function because {}", invokeResult.getFunctionError)
                }
                // NOTE: You need to return 200 OK within 3 seconds
                successResponse
              }
          }
      }
    }
  }

}

object SlackEventsHandler {

  private val log                 = LoggerFactory.getLogger(classOf[SlackEventsHandler])
  private val aws                 = new AmazonWebServices
  private val slackEventsOperator = SlackEventsOperator.getInstance
  private val signatureVerifier   = new SlackSignatureVerifier

  private def response = ApiGatewayResponse.builder

  private def toJsonObject(str: String): JsonObject = {
    GsonFactory.createSnakeCase
      .fromJson(str, classOf[JsonElement])
      .getAsJsonObject
  }

  private val successResponse: ApiGatewayResponse = {
    response
      .setStatusCode(200)
      .setObjectBody(Map("ok" -> true).asJava)
      .build
  }

}
