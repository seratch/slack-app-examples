package slackapp.service

import com.github.seratch.jslack.app_backend.events.{ EventsDispatcher, EventsDispatcherFactory }
import com.github.seratch.jslack.app_backend.events.handler.ReactionAddedHandler
import com.github.seratch.jslack.app_backend.events.payload.ReactionAddedPayload
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class SlackEventsOperator(
    val dispatcher: EventsDispatcher,
    val slackWebApi: SlackWebApi,
    val googleApi: GoogleTranslateApi,
    val langCodes: LangCodes
) {

  import SlackEventsOperator._

  def handleSynchronously(json: String): Unit = {
    this.dispatcher.dispatch(json)
  }

  private val reactionAddedHandler: ReactionAddedHandler = new ReactionAddedHandler() {
    override def handle(payload: ReactionAddedPayload): Unit = {
      log.info(s"payload: ${payload}")

      if (payload.getEvent.getItem.getType != "message") {
        log.info(s"Skipped: event type is not 'message' (${payload.getEvent.getType})")
        return
      }
      val reactionName: String = payload.getEvent.getReaction

      val country: Option[String] = {
        if (reactionName.startsWith("flag-")) {
          Some(reactionName.substring(5, 7))
        } else if (langCodes.allKeys.contains(reactionName)) {
          Some(reactionName)
        } else { // unsupported
          log.info("Skipped: no country detected")
          None
        }
      }
      country.flatMap(langCodes.allData.get) match {
        case Some(lang) =>
          try {
            val channelId       = payload.getEvent.getItem.getChannel
            val threadTs        = payload.getEvent.getItem.getTs
            val repliesResponse = slackWebApi.fetchConversationsReplies(channelId, threadTs)
            if (repliesResponse.isOk) {
              val messages       = repliesResponse.getMessages
              val firstMessage   = messages.get(0)
              val translatedText = googleApi.translate(firstMessage.getText, lang)
              for (message <- messages.asScala) {
                if (message.getText != null && message.getText == translatedText) {
                  log.info("Skipped posting {} translation to the thread: already posted", lang)
                  return
                }
              }
              val postResponse = slackWebApi.postMessage(channelId, threadTs, translatedText)
              if (postResponse.isOk) {
                log.info(
                  s"The translation message (lang:${lang}) has been successfully posted (ts: ${postResponse.getTs})"
                )
              } else {
                log.error(s"Failed to post a message because ${postResponse.getError}")
              }
            } else {
              log.error(s"Failed to fetch replies because ${repliesResponse.getError}")
            }
          } catch {
            case NonFatal(e) =>
              log.error(s"Failed to call APIs because ${e.getMessage}", e)
          }

        case _ =>
          log.info("Skipped: no lang detected")
          return
      }
    }
  }

  dispatcher.register(reactionAddedHandler)

}

object SlackEventsOperator {

  private val log = LoggerFactory.getLogger(classOf[SlackEventsOperator])

  lazy val getInstance: SlackEventsOperator = {
    try {
      new SlackEventsOperator(
        EventsDispatcherFactory.getInstance,
        new SlackWebApi,
        new GoogleTranslateApi,
        new LangCodes
      )
    } catch {
      case NonFatal(e) =>
        log.error("Failed to initialize SlackEventsOperator", e)
        throw new RuntimeException(e)
    }
  }

}
