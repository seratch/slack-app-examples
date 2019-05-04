package slackapp.service

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.{ AWSLambda, AWSLambdaClient }
import com.amazonaws.services.lambda.model.{ InvocationType, InvokeRequest, InvokeResult }
import com.amazonaws.services.lambda.runtime.Context
import com.github.seratch.jslack.app_backend.vendor.aws.lambda.request.ApiGatewayRequest
import com.google.gson.GsonBuilder

class AmazonWebServices {

  def region: Regions = { // TODO: Use your own region here
    Regions.AP_NORTHEAST_1
  }

  def newLambdaClient: AWSLambda = {
    AWSLambdaClient.builder
      .withRegion(region.getName)
      .build
  }

  def invokeLambdaFunction(context: Context, request: ApiGatewayRequest): InvokeResult = {
    val lambdaReq: InvokeRequest = {
      new InvokeRequest()
        .withFunctionName(context.getFunctionName)
        .withPayload(new GsonBuilder().create.toJson(request))
        .withInvocationType(InvocationType.Event)
    }
    val lambdaResult: InvokeResult = newLambdaClient.invoke(lambdaReq)

    lambdaResult
  }

  def serverlessStage: String = sys.env("SERVERLESS_STAGE")

  // when running by `sam local start-api`
  def isLocalDev(context: Context): Boolean = {
    context != null &&
    context.getFunctionName == "test"
  }
}
