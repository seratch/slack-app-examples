'use strict'

const config = require('./config.js');
const { App, ExpressReceiver } = require('@slack/bolt');
const expressReceiver = new ExpressReceiver({
    signingSecret: config.SLACK_SIGNING_SECRET,
    endpoints: '/events'
});
const app = new App({
    receiver: expressReceiver,
    token: config.SLACK_BOT_TOKEN
});
app.message('hello', ({ message, say }) => {
    say({ "text": `Hey there <@${message.user}>!` });
});
app.error(console.log);

const functions = require('firebase-functions');
// https://{your domain}.cloudfunctions.net/slack/events
exports.slack = functions.https.onRequest(expressReceiver.app);
