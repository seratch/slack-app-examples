## reacjilator in TypeScript

Original: https://github.com/slackapi/reacjilator

## How to run the app

### Prerequisites

* Google Translate API Token
  * https://console.cloud.google.com/apis/api/translate.googleapis.com/credentials

* Slack App
  * Create a new one here: https://api.slack.com/apps
  * Bot Users: https://api.slack.com/apps/{apiAppId}/bots
  * Scopes: https://api.slack.com/apps/{apiAppId}/oauth

![Scopes](https://raw.githubusercontent.com/seratch/slack-app-examples/master/reacjilator-typescript/scopes.png "Scopes")

* https://ngrok.com/

### Launch the app from the Terminal

```bash
npm i serveless -g
npm i
cp _env .env
# edit .env
source .env
serverless offline --printOutput
```
