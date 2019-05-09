# GAS (Google Apps Script) Slack Web API Examples

## Prerequisites

* clasp - https://github.com/google/clasp
* Slack App - https://api.slack.com/apps

## Development

Run `npm i` then edit *.ts under `src`.

## Deployment

```bash
clasp push && clasp open
```

Once the script has been created, set `SLACK_API_TOKEN` property in [ScriptProperties](https://developers.google.com/apps-script/reference/properties/properties-service#getScriptProperties())