'use strict';

const config = require('./config.js');

// ------------------------------------------------------
// Bot app
// https://slack.dev/bolt/
const { App, ExpressReceiver } = require('@slack/bolt');
const expressReceiver = new ExpressReceiver({
  signingSecret: config.SLACK_SIGNING_SECRET
});
const app = new App({
  token: config.SLACK_BOT_TOKEN,
  receiver: expressReceiver
});
const expressApp = expressReceiver.app;

// ------------------------------------------------------
// If you need to use API methods that are not listed on https://api.slack.com/bot-users#methods
// you need to use user api token instead like this:
const { WebClient } = require('@slack/web-api');
app.client = new WebClient(config.SLACK_API_TOKEN);

// ------------------------------------------------------

// React to "app_mention" events
app.event('app_mention', ({ event, say }) => {
  app.client.users.info({ user: event.user })
    .then(res => {
      if (res.ok) {
        say({
          text: `Hi! <@${res.user.name}>`
        });
      } else {
        console.error(`Failed because of ${res.error}`)
      }
    }).catch(reason => {
      console.error(`Failed because ${reason}`)
    })
});

// React to message.channels event
app.message('hello', ({ message, say }) => {
  // say() sends a message to the channel where the event was triggered
  say({
    blocks: [
      {
        "type": "section",
        "text": {
          "type": "mrkdwn",
          "text": `Hey there <@${message.user}>!`
        },
        "accessory": {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": "Click Me"
          },
          "action_id": "button_click"
        }
      }
    ]
  });
});

// Handle the click event (action_id: button_click) on a message posted by the above hello handler
app.action('button_click', ({ body, ack, say }) => {
  // Acknowledge the action
  ack();
  say(`<@${body.user.id}> clicked the button`);
});

// Handle `/echo` command invocations
app.command('/echo', async ({ command, ack, say }) => {
  // Acknowledge command request
  ack();
  say(`You said "${command.text}"`);
});

// A simple example to use WebApi client
app.message('42', ({ message, say }) => {
  console.log(`Got a message: ${JSON.stringify(message)}`);
  say('The answer to life, the universe and everything');
})

// A simple example to use Webhook internally
app.message('webhook', ({ message }) => {
  const { IncomingWebhook } = require('@slack/webhook');
  const url = config.SLACK_WEBHOOK_URL;
  const webhook = new IncomingWebhook(url);
  webhook.send({
    text: message.text.split("webhook")[1],
    unfurl_links: true
  })
    .then((res) => {
      console.log(`Succeeded ${JSON.stringify(res)}`)
    }).catch(reason => {
      console.error(`Failed because ${reason}`)
    })
})

// Check the details of the error to handle cases where you should retry sending a message or stop the app
app.error((error) => {
  console.error(error);
});

// ------------------------------------------------------

function isOnGoogleCloud() {
  // https://cloud.google.com/functions/docs/env-var#nodejs_10_and_subsequent_runtimes
  return process.env.K_SERVICE && process.env.K_REVISION;
}

if (!isOnGoogleCloud()) {
  // Running on your local machine
  (async () => {
    // Start your app
    expressApp.listen(config.SLACK_APP_PORT || 3000);
    console.log('⚡️ Slack app is running!');
  })();
}

module.exports.app = function (req, res) {
  console.log(`Got a request: ${JSON.stringify(req.headers)}`)
  if (req.rawBody) {
    console.log(`Got raw request: ${req.rawBody}`)
  }
  expressApp(req, res);
};
