'use strict';

const config = require('./config.json');

// ------------------------------------------------------
// If you need to use API methods that are not listed on https://api.slack.com/bot-users#methods
// you need to use user api token instead like this:
const { WebClient } = require('@slack/web-api');
const { IncomingWebhook } = require('@slack/webhook');
const { verifyRequestSignature } = require('@slack/events-api/dist/');

const slackWebApi = new WebClient(config.SLACK_API_TOKEN);

function handler(req, res) {
  // https://cloud.google.com/functions/docs/writing/http
  const body = isOnGoogleCloud() ? req.rawBody.toString() : req.body.toString();
  console.log(`Request body: ${body}`);
  try {
    console.log(`X-Slack-Signature: ${req.get('X-Slack-Signature')}`);
    // https://github.com/slackapi/node-slack-events-api/blob/v2.2.0/src/http-handler.js#L22-L58
    verifyRequestSignature({
      signingSecret: config.SLACK_SIGNING_SECRET,
      requestSignature: req.get('X-Slack-Signature'),
      requestTimestamp: req.get('X-Slack-Request-Timestamp'),
      body: body
    });
  } catch (verificationErr) {
    console.error(`Slack signature validation failed: ${verificationErr}`)
    return res.status(401).json({ ok: false });
  }

  if (body.startsWith('{')) {
    // application/json
    const payload = JSON.parse(body);
    if (payload.type === 'url_verification') {
      // ------------------------------------
      // Events API: url_verification
      // https://api.slack.com/events/url_verification
      if (payload.challenge) {
        return res.status(200).header("Content-Type", "text/plain").send(payload.challenge);
      } else {
        // invalid payload
        return res.status(400).json({ ok: false });
      }
    } else if (payload.type === 'event_callback' && payload.event) {
      // Events API: Event Subscription
      if (payload.event.type === 'app_mention') {
        // React to "app_mention" events
        slackWebApi.chat.postMessage({
          channel: payload.event.channel,
          text: `Hi! <@${payload.event.user}>`
        })
          .then(handleApiResponse)
          .catch(handleApiError)

      } else if (payload.event.type === 'message') {
        // React to message.channels event
        if (payload.event.text.match('hello')) {
          slackWebApi.chat.postMessage({
            channel: payload.event.channel,
            blocks: [
              {
                "type": "section",
                "text": {
                  "type": "mrkdwn",
                  "text": `Hey there <@${payload.event.user}>!`
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
          })
            .then(handleApiResponse)
            .catch(handleApiError)

        } else if (payload.event.text.match('42')) {
          // use chat.postMessage over say method
          slackWebApi.chat.postMessage({
            channel: payload.event.channel,
            text: 'The answer to life, the universe and everything',
            thread_ts: payload.event.ts
          })
            .then(handleApiResponse)
            .catch(handleApiError)

        } else if (payload.event.text.match('webhook')) {
          const url = process.env.SLACK_WEBHOOK_URL;
          const webhook = new IncomingWebhook(url);
          webhook.send({
            text: payload.event.text.split("webhook")[1],
            unfurl_links: true
          })
            .then((apiRes) => {
              console.log(`Succeeded ${JSON.stringify(apiRes)}`)
            }).catch(reason => {
              console.error(`Failed because ${reason}`)
            })
        }
      }
    }

  } else {
    // application/x-www-form-urlencoded
    const qs = require('querystring')
    const parsedBody = qs.parse(body);
    if (parsedBody.payload) {
      // payload=%7B%22type%22%3A%22block_actions%22%2C%22team%22...
      const payload = JSON.parse(parsedBody.payload);
      if (payload.type === 'interactive_message') {
        // ------------------------------------
        // Interactive Messages (Attachments)

      } else if (payload.type === 'block_actions') {
        // ------------------------------------
        // Interactive Messages (BlockKit)
        if (payload.actions.length == 1) {
          const action = payload.actions[0];
          if (action.action_id === 'button_click') {
            // Handle the click event (action_id: button_click) on a message posted by the above hello handler
            slackWebApi.chat.postMessage({
              channel: payload.channel.id,
              text: `<@${payload.user.id}> clicked the button`,
              thread_ts: payload.message.ts
            })
              .then(handleApiResponse)
              .catch(handleApiError);
          }
        }
      }

    } else {
      const payload = parsedBody;
      if (payload.command) {
        // ------------------------------------
        // Slash Commands
        if (payload.command === '/echo') {
          // Handle `/echo` command invocations
          return res.status(200).json({
            text: `You said "${payload.text}"`
          });
        }
      }
    }
  }

  return res.status(200).json({ ok: true });
};

function handleApiResponse(apiRes) {
  if (apiRes.ok) {
    console.log(`Succeeded ${JSON.stringify(apiRes)}`);
  } else {
    console.error(`Failed because of error: ${apiRes.error}`)
  }
}

function handleApiError(reason) {
  console.error(`Failed because of reason: ${reason}`)
}

function isOnGoogleCloud() {
  // https://cloud.google.com/functions/docs/env-var#nodejs_10_and_subsequent_runtimes
  return process.env.K_SERVICE && process.env.K_REVISION;
}

// ------------------------------------------------------
if (isOnGoogleCloud()) {
  // Running on Google Cloud Platform
  exports.slackAppSample = handler;

} else {
  // Running on your local machine
  const express = require('express');
  const app = express();
  var concat = require('concat-stream');
  app.use(function (req, res, next) {
    req.pipe(concat(function (data) {
      req.body = data;
      next();
    }));
  });
  app.post('/slack/events', (req, res) => {
    return handler(req, res);
  });
  (async () => {
    // Start your app
    app.listen(config.SLACK_APP_PORT || 3000);
    console.log('⚡️ Slack app is running!');
  })();
}