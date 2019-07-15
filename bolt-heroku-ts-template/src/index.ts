import { App, ExpressReceiver } from '@slack/bolt';
import { ConsoleLogger, LogLevel } from '@slack/logger';
import { Request, Response } from 'express';

const logger = new ConsoleLogger();
logger.setLevel(LogLevel.DEBUG);
const expressReceiver = new ExpressReceiver({
    signingSecret: process.env.SLACK_SIGNING_SECRET!,
    logger: logger
});
const app = new App({
    token: process.env.SLACK_BOT_TOKEN,
    receiver: expressReceiver
});

app.message('hello', ({ message, say }) => {
    say(`Hey    there <@${message.user}>\!`);
});

(async () => {
    // Start your app

    await app.start(process.env.PORT || 3000);
    // await expressApp.listen(process.env.PORT || 3000);

    // const wrapper = express();
    // const appWrapper = wrapper((req: Request, res: Response) => {
    //     return expressReceiver.app(req, res);
    // });
    // expressReceiver.app = appWrapper;
    // await appWrapper.listen(process.env.PORT || 3000);
    // const express = require('express');
    // const expressApp: Application = express();
    // expressApp.use(function (req, _res, next) {
    //     console.log('aaaaa');
    //     console.log(req.body);
    //     next();
    // });
    // await expressApp.listen(process.env.PORT || 3000);
    console.log('⚡️ Bolt app is running!');
})();

app.message('block', ({ say }) => {
    // say() sends a message to the channel where the event was triggered
    say({
        text: '',
        blocks: [
            {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": "You can add a button alongside text in your message. "
                },
                "accessory": {
                    "type": "button",
                    "text": {
                        "type": "plain_text",
                        "text": "Button",
                        "emoji": true
                    },
                    "value": "click_me_123",
                    "action_id": "aid"
                }
            },
            {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": "Pick an item from the dropdown list"
                },
                "accessory": {
                    "type": "static_select",
                    "placeholder": {
                        "type": "plain_text",
                        "text": "Select an item",
                        "emoji": true
                    },
                    "options": [
                        {
                            "text": {
                                "type": "plain_text",
                                "text": "Choice 1",
                                "emoji": true
                            },
                            "value": "value-0"
                        },
                        {
                            "text": {
                                "type": "plain_text",
                                "text": "Choice 2",
                                "emoji": true
                            },
                            "value": "value-1"
                        },
                        {
                            "text": {
                                "type": "plain_text",
                                "text": "Choice 3",
                                "emoji": true
                            },
                            "value": "value-2"
                        }
                    ]
                }
            }
        ]
    });
});

/*
// 1) Use Block Kit
* https://api.slack.com/block-kit
* https://api.slack.com/tools/block-kit-builder

app.message('hello', ({ message, say }) => {
    // say() sends a message to the channel where the event was triggered
    say({
      text: '',
      blocks: [
        {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "You can add a button alongside text in your message. "
            },
            "accessory": {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": "Button",
                    "emoji": true
                },
                "value": "click_me_123"
            }
        },
        {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "Pick an item from the dropdown list"
            },
            "accessory": {
                "type": "static_select",
                "placeholder": {
                    "type": "plain_text",
                    "text": "Select an item",
                    "emoji": true
                },
                "options": [
                    {
                        "text": {
                            "type": "plain_text",
                            "text": "Choice 1",
                            "emoji": true
                        },
                        "value": "value-0"
                    },
                    {
                        "text": {
                            "type": "plain_text",
                            "text": "Choice 2",
                            "emoji": true
                        },
                        "value": "value-1"
                    },
                    {
                        "text": {
                            "type": "plain_text",
                            "text": "Choice 3",
                            "emoji": true
                        },
                        "value": "value-2"
                    }
                ]
            }
        }
      ]
    });
});



*/