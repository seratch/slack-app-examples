'use strict'

const config = require('./config.js');
const { App, ExpressReceiver } = require('@slack/bolt');
const expressReceiver = new ExpressReceiver({
  signingSecret: config.SLACK_SIGNING_SECRET,
  endpoints: '/events',
  processBeforeResponse: true,
});
const app = new App({
  receiver: expressReceiver,
  token: config.SLACK_BOT_TOKEN,
  processBeforeResponse: true,
});
app.message('hello', async ({ message, say }) => {
  await say({ "text": `Hey there <@${message.user}>!` });
});
app.error(console.log);

const functions = require('firebase-functions');
// https://{your domain}.cloudfunctions.net/slack/events
exports.slack = functions.https.onRequest(expressReceiver.app);
