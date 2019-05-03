package slackapp_backend.service;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsRepliesResponse;

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
