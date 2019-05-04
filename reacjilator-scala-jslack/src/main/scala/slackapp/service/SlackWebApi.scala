package slackapp.service

import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest
import com.github.seratch.jslack.api.methods.request.conversations.ConversationsRepliesRequest
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsRepliesResponse

class SlackWebApi {
  import SlackWebApi._

  def fetchConversationsReplies(channelId: String, threadTs: String): ConversationsRepliesResponse = {
    slack.methods.conversationsReplies(
      ConversationsRepliesRequest.builder
        .token(token)
        .channel(channelId)
        .ts(threadTs)
        .build
    )
  }

  def postMessage(channelId: String, threadTs: String, text: String): ChatPostMessageResponse = {
    slack.methods.chatPostMessage(
      ChatPostMessageRequest.builder
        .token(token)
        .channel(channelId)
        .threadTs(threadTs)
        .text(text)
        .username("Reacjilator Bot")
        .build
    )
  }

}

object SlackWebApi {

  private val slack = Slack.getInstance
  private val token = sys.env("SLACK_API_TOKEN")

}
