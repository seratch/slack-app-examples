## Bolt app running on Cloud Functions for Firebase

This is a simple Bolt app which runs on Cloud Functions for Firebase.

* https://slack.dev/bolt/
* https://firebase.google.com/docs/functions

## Setup

Use node 10.x and its corresponding npm.

```
vi .firebaserc # set your own project

npm install -g firebase-tools
cd functions
npm i
cp -p _config.js config.js # and modify config.js
cd -
```

## How to run the app on your laptop

```bash
firebase serve
```

## How to deploy

```bash
firebase deploy
```

## How to configure Slack apps/GCP

### Slack App

Set `https://{your domain}.cloudfunctions.net/slack/events` as the Request URL for event subscriptions.

### Cloud Functions for Firebase

You have nothing to configure. Don't forget enabling billing info if it's your first time to use it.

## How to make sure if it works

Post a message including `hello`. Then you'll receive a message saying `Hey there @yourname` from your bot user!

## LICENSE  

The MIT License
