package slackapp_backend.service;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;

import java.io.IOException;

public class SlackWebApi {

    private final Slack slack = Slack.getInstance();
    private final String token = System.getenv("SLACK_API_TOKEN");

    public ConversationsRepliesResponse fetchConversationsReplies(String channelId, String threadTs) throws IOException, SlackApiException {
        return slack.methods().conversationsReplies(ConversationsRepliesRequest.builder()
                .token(token)
                .channel(channelId)
                .ts(threadTs)
                .build());
    }

    public ChatPostMessageResponse postMessage(String channelId, String threadTs, String text) throws IOException, SlackApiException {
        return slack.methods().chatPostMessage(ChatPostMessageRequest.builder()
                .token(token)
                .channel(channelId)
                .threadTs(threadTs)
                .text(text)
                .username("Reacjilator Bot")
                .build());
    }

}
