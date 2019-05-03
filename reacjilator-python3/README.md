## reacjilator in Python

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
pip3 install --user virtualenv
virtualenv venv --python=python3
source venv/bin/activate
pip3 install -r requirements.txt

cp _env .env
# edit .env
source .env
python app.py # POST http://127.0.0.1:3000/slack/events is available now
```
