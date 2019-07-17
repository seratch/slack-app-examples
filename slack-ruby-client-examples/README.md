# Ruby examples

## Prerequisites

### Create your Slack App

https://api.slack.com/apps

![Create a Slack App](https://camo.qiitausercontent.com/af01ba95b0e78b77765fca029c56b1eb6a878379/68747470733a2f2f71696974612d696d6167652d73746f72652e73332e61702d6e6f727468656173742d312e616d617a6f6e6177732e636f6d2f302f3333393239332f66663433336433332d353364642d356461362d393036372d6565626137646166326363642e706e67)

```bash
export SLACK_SIGNING_SECRET=xxx
export SLACK_BOT_TOKEN=xoxb-xxx
```

### Install Gems

```bash
bundle i
```

### ngrok account

https://ngrok.com/

## say_hello.rb

### How run

```bash
SLACK_BOT_TOKEN=xoxb-xxx bundle exec ruby say_hello.rb
```

## channels.rb

### How run

```bash
SLACK_BOT_TOKEN=xoxb-xxx bundle exec ruby channels.rb
```

## events.rb

### Event Subscriptions

- `reaction_added` in workspace events or bot events
- Set the ngrok URL for Request URL

### How run

```bash
export SLACK_SIGNING_SECRET=xxx
export SLACK_BOT_TOKEN=xoxb-xxx

bundle exec ruby events.rb -p 3000
ngrok http 3000
```
