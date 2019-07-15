# Slack Bolt app on Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/seratch/bolt-on-heroku/tree/master)

## Slack ⚡️ Bolt

A framework to build Slack apps, fast.

* https://slack.dev/bolt
* https://github.com/SlackAPI/bolt

## How to build

### Create a Slack App

https://api.slack.com/apps

* Features > OAuth & Permissions:
  * Scopes:
    * "channels:history"
    * "chat:write:bot"
    * "bot"
  * Click "Save Changes"
* Features > Bot User:
  * Click "Add a Bot User"
  * Click "Add Bot User"
* Settings > Install App:
  * Complete "Install App"

### Deploy to Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/seratch/bolt-on-heroku/tree/master)

* Set env variables on Heroku
  * (Slack) Settings > Basic Information > App Credentials > Siginging Secret
  * (Slack) Settings > Install App > Bot User OAuth Access Token

[![Heroku deployment page](https://raw.githubusercontent.com/seratch/bolt-on-heroku/master/deploy_to_heroku.png)](https://heroku.com/deploy?template=https://github.com/seratch/bolt-on-heroku/tree/master)

### Enable Slack Events Subscription

* Features > Event Subscriptions:
  * Enable Events:
    * Change from "Off" to "On"
  * Request URL:
    * Set "https://{your app name}.herokuapp.com/slack/events"
  * Subscribe to Workspace Events:
    * Add "message.channels"
  * Click "Save Changes"

### Try the Slack App

* Invite your bot to a Slack channel
* Post "hello" in the channel
* You'll receive a response from the bot

![hello](https://raw.githubusercontent.com/seratch/bolt-on-heroku/master/hello.png)

