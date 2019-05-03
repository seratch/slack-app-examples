package slackapp_backend.service;

import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.app_backend.events.EventsDispatcher;
import com.github.seratch.jslack.app_backend.events.EventsDispatcherFactory;
import com.github.seratch.jslack.app_backend.events.handler.ReactionAddedHandler;
import com.github.seratch.jslack.app_backend.events.payload.ReactionAddedPayload;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SlackEventsOperator {

    private final SlackWebApi slackWebApi;
    private final GoogleTranslateApi googleApi;
    private final EventsDispatcher dispatcher;
    private final LangCodes langCodes;

    private static final SlackEventsOperator INSTANCE;

    static {
        try {
            INSTANCE = new SlackEventsOperator(
                    EventsDispatcherFactory.getInstance(),
                    new SlackWebApi(),
                    new GoogleTranslateApi(),
                    new LangCodes()
            );
        } catch (Exception e) {
            log.error("Failed to initialize SlackEventsOperator", e);
            throw new RuntimeException(e);
        }
    }

    public static SlackEventsOperator getInstance() {
        return INSTANCE;
    }

    public SlackEventsOperator(
            EventsDispatcher dispatcher,
            SlackWebApi slackWebApi,
            GoogleTranslateApi googleApi,
            LangCodes langCodes) {
        this.slackWebApi = slackWebApi;
        this.googleApi = googleApi;
        this.langCodes = langCodes;

        dispatcher.register(reactionAddedHandler);
        this.dispatcher = dispatcher;
    }

    public void handleSynchronously(String json) {
        this.dispatcher.dispatch(json);
    }

    private ReactionAddedHandler reactionAddedHandler = new ReactionAddedHandler() {
        @Override
        public void handle(ReactionAddedPayload payload) {
            log.info("payload: {}", payload);
            if (!payload.getEvent().getItem().getType().equals("message")) {
                log.info("Skipped: event type is not 'message' ({})", payload.getEvent().getType());
                return;
            }
            String reactionName = payload.getEvent().getReaction();
            String country;
            if (reactionName.startsWith("flag-")) {
                country = reactionName.substring(5, 7);
            } else {
                if (langCodes.getAllKeys().contains(reactionName)) {
                    country = reactionName;
                } else {
                    // unsupported
                    log.info("Skipped: no country detected");
                    return;
                }
            }
            String lang = langCodes.getAllData().get(country);
            if (lang == null) {
                log.info("Skipped: no lang detected");
                return;
            }

            try {
                String channelId = payload.getEvent().getItem().getChannel();
                String threadTs = payload.getEvent().getItem().getTs();

                ConversationsRepliesResponse repliesResponse = slackWebApi.fetchConversationsReplies(channelId, threadTs);
                if (repliesResponse.isOk()) {
                    List<Message> messages = repliesResponse.getMessages();
                    Message firstMessage = messages.get(0);
                    String translatedText = googleApi.translate(firstMessage.getText(), lang);
                    for (Message message : messages) {
                        if (message.getText() != null && message.getText().equals(translatedText)) {
                            log.info("Skipped posting {} translation to the thread: already posted", lang);
                            return;
                        }
                    }
                    ChatPostMessageResponse postResponse = slackWebApi.postMessage(channelId, threadTs, translatedText);
                    if (postResponse.isOk()) {
                        log.info("The translation message (lang:{}) has been successfully posted (ts: {})", lang, postResponse.getTs());
                    } else {
                        log.error("Failed to post a message because {}", postResponse.getError());
                    }
                } else {
                    log.error("Failed to fetch replies because {}", repliesResponse.getError());
                }
            } catch (IOException | SlackApiException e) {
                log.error("Failed to call APIs because {}", e.getMessage(), e);
            }
        }
    };

}
