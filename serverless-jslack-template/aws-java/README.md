# Slack App Backend Example on AWS

This "serverless" app illustrates a Slack App backend in Java which runs on AWS serverless infrastructure.

- Serverless Framework
  - https://github.com/serverless/serverless
  - https://github.com/sapessi/serverless-sam
- AWS SAM CLI
  - https://github.com/awslabs/aws-sam-cli
- jSlack (Unofficial Slack APIs library in Java)
  - https://github.com/seratch/jslack
- Amazon Web Services
  - AWS CloudFormation https://aws.amazon.com/cloudformation/
  - Amazon API Gateway https://aws.amazon.com/api-gateway/
  - AWS Lambda https://aws.amazon.com/lambda/
  - Amazon CloudWatch https://aws.amazon.com/cloudwatch/
  - Amazon S3 https://aws.amazon.com/cloudwatch/
  - AWS Identity and Access Management (IAM) https://aws.amazon.com/iam/

## Launch the app on your local machine

### Configure necessary env variables

```bash
cp _env .env_prod
cp _env .env_dev
```

```
# `service` in serverless.yml
export SERVERLESS_SERVICE=serverless-jslack-app

# `provider.stage` in serverless.ymk (e.g., dev, prod)
export SERVERLESS_STAGE=dev

# slackapp.service.SlackWebApi
export SLACK_WEBHOOK_URL=

# https://api.slack.com/docs/verifying-requests-from-slack
export SLACK_SIGNING_SECRET=

export SLACK_API_TOKEN=

# OAuth
export SLACK_APP_CLIENT_ID=
export SLACK_APP_CLIENT_SECRET=
export SLACK_APP_REDIRECT_URI=
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
      - Related implementation: `slackapp.service.InstallationHandler`, `OAuthCallbackHandler`
    - Signing Secret
      - Related implementation: Most of the handlers
- Add an Incoming Webhook
  - Set the URL as the env variable: `SLACK_WEBHOOK_URL`
  - Related implementation: `slackapp.service.SlackWebApi#sendToWebhook(payload)`
- Add a Slash Command
  - `/echo` command
  - Request URL: `https://{ngrok domain}/slack/echo-command` or `https://{aws}/{stage}/slack/echo-command`
  - Related implementation: `slackapp.handler.EchoCommandHandler`
- Add a Bot user
  - Enable `bot` permission & add a bot user
  - Related implementation: `slackapp.handler.EventsHandler`
- Enable Interactive Components
  - Related implementation: `slackapp.handler.InteractivityHandler`
  - Request URL: `https://{ngrok domain}/slack/interactivity` or `https://{aws}/{stage}/slack/interactivity`
  - Message Actions
    - Add a random action
  - Message Menus
    - Unsupported in this example; If you're interested in this outmoded one, you can return it in any messages with attachments
- Enable Event Subscriptions
  - Request URL: `https://{ngrok domain}/slack/events` or `https://{aws}/{stage}/slack/events`
  - Enable `app_mention`, `reaction_added`
  - Related implementation: `slackapp.handler.EventsHandler`
- Add Necessary Permissions
  - `bot`
  - `chat:write:bot`
  - `users:read`
  - `channels:read`
  - `reactions:read`
  - `channels:history` (add `*:history` to run on private channels, ims, etc)

### Run the app on your local machine

`sam local start-api` has a runtime performance issue. 
https://github.com/awslabs/aws-sam-cli/issues/134#issuecomment-348717959

Local app may not respond to requests from Slack Platform within 3 seconds.
Therefore, to debug the app, you may need to deploy the app onto AWS.

```bash
npm i serverless -g
npm i
mvn package \
 && serverless sam export --output ./template.yml \
 && sam local start-api
```

## Deployment

### Setup AWS account

Please refer to the official documents:

* https://serverless.com/framework/docs/providers/aws/

```bash
# setup aws-cli + configure & prepare credentials
curl -O https://bootstrap.pypa.io/get-pip.py
python3 get-pip.py --user
pip install awscli --upgrade --user
aws configure
```

### Deploy the app onto AWS

```bash
source .env_prod
export SLS_DEBUG=*
mvn clean package \
  && serverless deploy --stage ${SERVERLESS_STAGE} -v \
  && serverless invoke --function warmup
```

To delete the stack completely, run `serverless remove --stage ${SERVERLESS_STAGE} -v`.

## Known Issues

### `sam local start-api` performance issue

`sam local start-api` has a runtime performance issue. 
https://github.com/awslabs/aws-sam-cli/issues/134#issuecomment-348717959

Local app may not respond to requests from Slack Platform within 3 seconds.
Therefore, to debug the app, you may need to deploy the app onto AWS.

### Cold starts in AWS Lambda

In production, Java lambda functions don't nay performance issues apart from the cold start problem. In this example, `slackapp.handler.WarmupHandler` eliminates it in a simple way.

## License

The MIT License