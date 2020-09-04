//
// A TypeScript implementation of https://github.com/slackapi/reacjilator
//
// Author: Kazuhiro Sera @seratch
// MIT License as with the original code
//

// ----------------
// Slack Web API Types
import * as SlackWebApi from 'seratch-slack-types/web-api';

// Slack Web Client with sufficient scopes
import * as SlackClient from '@slack/web-api';
const slackApiClient = new SlackClient.WebClient(process.env.SLACK_API_TOKEN);

// ----------------
// Google Translate API
import { Translate as GoogleTranslateApi } from '@google-cloud/translate';
// https://console.cloud.google.com/apis/api/translate.googleapis.com/credentials?project={Project ID}
// $ export GOOGLE_PROJECT_ID={Project ID}
// $ export GOOGLE_KEY={API Key}
const googleApiCredentials = {
  projectId: process.env.GOOGLE_PROJECT_ID,
  key: process.env.GOOGLE_KEY
}
const googleApi: GoogleTranslateApi = new GoogleTranslateApi(googleApiCredentials);

// ----------------
// Enable debug logging if true
const debug: boolean = true;
// lang code mapping data
import { langcode } from './langcode';

// ----------------
// Slack Bolt App
import { App, ExpressReceiver } from '@slack/bolt';

const expressReceiver = new ExpressReceiver({
  signingSecret: process.env.SLACK_SIGNING_SECRET
  // Endpoints will be attached later by calling the declared methods in Bolt's App
});
export const expressApp = expressReceiver.app;

const app: App = new App({
  token: process.env.SLACK_BOT_TOKEN,
  receiver: expressReceiver
});

app.event('reaction_added', async ({ event }) => {
  try {
    if (debug) {
      console.log(event);
    }
    if (event.item['type'] !== 'message') {
      // Skip any events apart from reactions put on messages
      return;
    }
    const reactionName = event.reaction;
    let country: string = null;
    // Check the reaction name if it is a country flag
    if (reactionName.match(/flag-/)) { // when the name has flag- prefix
      country = reactionName.match(/(?!flag-\b)\b\w+/)[0];
    } else { // jp, fr, etc.
      const flags = Object.keys(langcode.All); // array
      if (flags.includes(reactionName)) {
        country = reactionName;
      } else {
        return;
      }
    }
    // Finding a lang based on a country is not the best way but oh well
    // Matching ISO 639-1 language code
    const lang: string = langcode.All[country];
    if (!lang) {
      return;
    }
    if (debug) {
      console.log(`Detected country: ${country}, lang: ${lang} from reaction: ${reactionName}`);
    }

    const channelId: string = event.item['channel'];
    const messageTs: string = event.item['ts'];

    // Fetch all the messages in the thread
    const repliesRes: SlackWebApi.ConversationsRepliesResponse =
      await slackApiClient.conversations.replies({
        channel: channelId,
        ts: messageTs,
        inclusive: true
      });
    if (debug) {
      console.log(repliesRes.messages);
    }
    const messages = repliesRes.messages;
    const message = messages[0];
    if (message.text) {
      // Call Google Translate API to get a translated text
      googleApi.translate(message.text, lang)
        .then((array) => {
          const [translatedText, googleApiRes] = array; // [string, r.Response]
          if (debug) {
            console.log(`Response from Google Translate API: ${JSON.stringify(googleApiRes)}`);
          }

          // To avoid posting same messages several times, make sure if a same message in the thread doesn't exist
          let alreadyPosted: boolean = false;
          messages.forEach(messageInTheThread => {
            if (!alreadyPosted && messageInTheThread.text && messageInTheThread.text === translatedText) {
              alreadyPosted = true;
            }
          });
          if (alreadyPosted) {
            return;
          }

          // Post the translated text as a following message in the thread
          slackApiClient.chat.postMessage({
            channel: channelId,
            text: translatedText,
            as_user: false,
            username: "Reacjilator Bot",
            thread_ts: message.thread_ts ? message.thread_ts : message.ts
          })
            .then((postRes: SlackWebApi.ChatPostMessageResponse) => {
              if (postRes.ok) {
                console.log(`Successfully posted a translated message (ts: ${postRes.ts})`);
              } else {
                if (debug) {
                  console.error(postRes);
                }
                console.error(`Got an error from chat.postMessage (error: ${postRes.error})`);
              }
            })
            .catch(reason => {
              console.error(`Failed to post a message because ${reason}`);
            })

        })
        .catch(reason => {
          console.error(`Failed to call Google Translate API because ${reason}`);
        })

    } else {
      console.log(`Skipped the message because it doesn't have text property (ts: ${message.ts})`);
    }
  }
  catch (error) {
    console.error(error);
  }
});