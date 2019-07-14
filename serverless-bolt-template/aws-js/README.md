# Slack App backend built with Bolt on AWS

## Run the app on your local machine

### Configure necessary env variables

```bash
cp _env .env_prod
cp _env .env_dev
```

```bash
# Slack Tokens https://api.slack.com/docs/oauth
export SLACK_API_TOKEN=xoxp-xxxxxxxxx
export SLACK_BOT_TOKEN=xoxb-xxxxxxxxx

# X-Slack-Signature https://api.slack.com/docs/verifying-requests-from-slack
export SLACK_SIGNING_SECRET=xxxxxxxxx

# OAuth Credentials https://api.slack.com/apps/{apiAppId}/general
export SLACK_CLIENT_ID=xxxxxxxxx.xxxxxxxxx
export SLACK_CLIENT_SECRET=xxxxxxxxx
export SLACK_REDIRECT_URI=https://www.example.com/...

# Incoming Webhook https://api.slack.com/apps/{apiAppId}/incoming-webhooks
export SLACK_WEBHOOK_URL=https://hooks.slack.com/...

# Serverless Framework
export SERVERLESS_STAGE=dev
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
  - Request URL: `https://{ngrok domain}/slack/events` or `https://{aws}/{stage}/slack/events`
- Add a Bot user
  - Enable `bot` permission & add a bot user
- Enable Interactive Components
  - Request URL: `https://{ngrok domain}/slack/events` or `https://{aws}/{stage}/slack/events`
  - Message Actions
    - Add a random action
  - Message Menus
    - Unsupported in this example; If you're interested in this outmoded one, you can return it in any messages with attachments
- Enable Event Subscriptions
  - Request URL: `https://{ngrok domain}/slack/events` or `https://{aws}/{stage}/slack/events`
  - Subscribe to Bot Events: `app_mention`, `message.channels` 
- Add Necessary Permissions
  - `bot`
  - `chat:write:bot`

```bash
npm i
npm i serverless -g
serverless offline --printOutput
```

## Deploy the app onto AWS

```bash
serverless deploy --stage ${SERVERLESS_STAGE} -v
```
