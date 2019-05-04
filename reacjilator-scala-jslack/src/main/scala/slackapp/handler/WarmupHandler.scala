package slackapp.handler

import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.response.ApiGatewayResponse
import com.github.seratch.jslack.common.json.GsonFactory
import org.slf4j.LoggerFactory
import slackapp.service.AmazonWebServices

import scala.collection.JavaConverters._

/**
  * To address cold start problems
  */
class WarmupHandler extends RequestHandler[ApiGatewayRequest, ApiGatewayResponse] {
  import WarmupHandler._

  override def handleRequest(req: ApiGatewayRequest, context: Context): ApiGatewayResponse = {
    log.info(s"request: ${req}")

    val lambda = aws.newLambdaClient
    val functionNames: Seq[String] = {
      val serverlessStage = aws.serverlessStage
      Seq(
        s"reacjilator-${serverlessStage}-echo",
        s"reacjilator-${serverlessStage}-events"
      )
    }
    for (functionName <- functionNames) {
      val result = lambda.invoke(invokeRequest(functionName, payloadString))
      if (result.getStatusCode != 200) {
        log.error(s"Failed to warmup the function: ${functionName}, result: ${result.getFunctionError}")
      }
    }
    response
      .setStatusCode(200)
      .setObjectBody("Done")
      .build
  }

}

object WarmupHandler {

  val payload       = Map("warmup" -> true)
  val payloadString = GsonFactory.createSnakeCase.toJson(payload.asJava)

  private val log = LoggerFactory.getLogger(classOf[WarmupHandler])
  private val aws = new AmazonWebServices

  def invokeRequest(functionName: String, payload: String) = {
    new InvokeRequest()
      .withFunctionName(functionName)
      .withPayload(payload)
  }

  def response = ApiGatewayResponse.builder

}
