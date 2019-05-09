# Simple Slack App on Google Cloud Functions

This is a tiny Slack App backend which runs on Google Cloud Functions.

## App Configuration

```bash
cp _config.json config.json
# then modify config.json
```

```json
{
  "SLACK_API_TOKEN": "xoxp-xxxxxxxxx",
  "SLACK_BOT_TOKEN": "xoxb-xxxxxxxxx",
  "SLACK_SIGNING_SECRET": "xxx https://api.slack.com/docs/verifying-requests-from-slack",
  "SLACK_WEBHOOK_URL": "https://hooks.slack.com/xxx https://api.slack.com/apps/{apiAppId}/incoming-webhooks",
  "SLACK_APP_PORT": 3000
}
```

## Run the app on your local machine

```bash
npm i
npm start # POST http://localhost:3000/slack/events
```

## Deploy the app onto Google Cloud

### Enable Google Cloud Functions

* https://console.cloud.google.com/functions

### Google Service Account

* https://serverless.com/framework/docs/providers/google/guide/credentials/

Download json file and place it as `~/.gcloud/keyfile.json` (you can change the path by modifying serverless.yml).

### Deploy the app

```bash
npm i -g serverless
sls deploy
```

* Cloud Functions: https://console.cloud.google.com/functions/list
* Deployment Manager: https://console.cloud.google.com/dm/deployments
* Storage: https://console.cloud.google.com/storage/browser
