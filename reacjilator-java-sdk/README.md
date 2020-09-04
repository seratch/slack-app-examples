## reacjilator in Java + jSlack

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
npm i serverless -g
npm i
mvn package \
 && serverless sam export --output ./template.yml \
 && sam local start-api
```

### Deploy onto AWS

```bash
# setup aws-cli + configure & prepare credentials
curl -O https://bootstrap.pypa.io/get-pip.py
python3 get-pip.py --user
pip install awscli --upgrade --user
aws configure

./deploy.sh
```