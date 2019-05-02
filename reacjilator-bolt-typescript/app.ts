//
// A TypeScript implementation of https://github.com/slackapi/reacjilator
//
// Author: Kazuhiro Sera @seratch
// MIT License as with the original code
//

// ----------------
// Slack Web API Types
import * as SlackWebApi from 'seratch-slack-types/web-api';

// NOTE: may remove this dependency when app.client gets to work in the future
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

import { App } from '@slack/bolt';

export const app = new App({
  signingSecret: process.env.SLACK_SIGNING_SECRET,
  token: process.env.SLACK_BOT_TOKEN
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
    slackApiClient.conversations.replies({
      channel: channelId,
      ts: messageTs,
      inclusive: true
    }) // The returned value is a Promise - chaining operations started here
      .then((repliesRes: SlackWebApi.ConversationsRepliesResponse) => {
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
      })
      .catch(reason => {
        console.error(`Failed to fetch message replies because ${reason}`);
      });
  }
  catch (error) {
    console.error(error);
  }
});