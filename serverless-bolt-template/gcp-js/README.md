# Slack App backend built with Bolt on AWS

## Run the app on your local machine

### Configure necessary env variables

```bash
cp _config.json config.json
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

### Run ngrok proxy on your local machine

https://ngrok.com/

```bash
ngrok http 3000
```

### Create a Slack App

https://api.slack.com/apps

- Create a Slack App
  - Use these information in "App Credentials" (https://api.slack.com/apps/{apiAppId}/general)
    - Client ID, Client Secret
    - Signing Secret
- Add an Incoming Webhook
  - Set the URL as the env variable: `SLACK_WEBHOOK_URL`
- Add a Slash Command
  - `/echo` command
  - Request URL: `https://{ngrok domain}/slack/events` or `https://{gcp}/{stage}/slack/events`
- Add a Bot user
  - Enable `bot` permission & add a bot user
- Enable Interactive Components
  - Request URL: `https://{ngrok domain}/slack/events` or `https://{gcp}/{stage}/slack/events`
  - Message Actions
    - Add a random action
  - Message Menus
    - Unsupported in this example; If you're interested in this outmoded one, you can return it in any messages with attachments
- Enable Event Subscriptions
  - Request URL: `https://{ngrok domain}/slack/events` or `https://{gcp}/{stage}/slack/events`
  - Enable `app_metion`, `message.channels`
- Add Necessary Permissions
  - `bot`
  - `chat:write:bot`

```bash
npm i
node app.js
```

## Deploy the app onto Google Cloud Functions

### Put ~/.gcloud/keyfile.json

https://console.cloud.google.com/apis/credentials

Create a "Service account key" and save it as `~/.gcloud/keyfile.json`.

### Run gcloud CLI

https://cloud.google.com/sdk/gcloud/

```bash
gcloud functions deploy {NAME} --runtime nodejs10 --trigger-http --entry-point app
```
