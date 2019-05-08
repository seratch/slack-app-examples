package slackapp.handler

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.util.SlackSignatureVerifier
import org.slf4j.LoggerFactory
import slackapp.service.AmazonWebServices

import scala.collection.JavaConverters._

// This is just a minimal working example
class EchoHandler extends RequestHandler[ApiGatewayRequest, ApiGatewayResponse] {
  import EchoHandler._

  override def handleRequest(req: ApiGatewayRequest, context: Context): ApiGatewayResponse = {
    log.info(s"request: ${req}")

    if (!signatureVerifier.isValid(req) && !aws.isLocalDev(context)) {
      response.statusCode(401).build
    } else {
      req.getBody match {
        case WarmupHandler.payloadString =>
          response.statusCode(200).build
        case _ =>
          response
            .statusCode(200)
            .headers(Map("Content-Type" -> "application/json").asJava)
            .objectBody(req.getQueryStringParameters)
            .build
      }
    }
  }

}

object EchoHandler {

  private val log               = LoggerFactory.getLogger(classOf[EchoHandler])
  private val signatureVerifier = new SlackSignatureVerifier
  private val aws               = new AmazonWebServices

  private def response = ApiGatewayResponse.builder

}
