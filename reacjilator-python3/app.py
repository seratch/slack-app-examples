#
# A Python implementation of https://github.com/slackapi/reacjilator
#
# Author: Kazuhiro Sera @seratch
# MIT License as with the original code
#

import html
import six
from google.cloud import translate
import json
import re
import os

# --------------------
# Slack API
import slack
from slackeventsapi import SlackEventAdapter

# Slack Web API Client with sufficient permissions
slack_web_api_client = slack.WebClient(token=os.environ['SLACK_API_TOKEN'])

# In order for our application to verify the authenticity
# of requests from Slack, we'll compare the request signature
slack_signing_secret = os.environ["SLACK_SIGNING_SECRET"]

# Create an instance of SlackEventAdapter, passing in our Flask server so it can bind the
# Slack specific routes. The `endpoint` param specifies where to listen for Slack event traffic.
slack_events_adapter = SlackEventAdapter(
    slack_signing_secret,
    endpoint="/slack/events"
)

# --------------------
# Google Translate API

# https://console.cloud.google.com/apis/credentials?project={project id}
# export GOOGLE_APPLICATION_CREDENTIALS=/path/to/{service-account-key}.json
translate_client = translate.Client()

# --------------------
# Load language mapping data
langcode: dict = {}
with open('./langcode.json') as langcode_file:
    langcode = json.load(langcode_file)

# --------------------
# reaction_added event handler


@slack_events_adapter.on("reaction_added")
def reaction_added(event_data):
    event = event_data["event"]

    # Get the reactji name from the event payload
    emoji_name = event["reaction"]

    country: str = None
    # Check the reaction name if it is a country flag
    if re.match(r"flag-", emoji_name):  # when the name has flag- prefix
        country = re.search(r"flag-(\w{2})$", emoji_name).group(1)
    else:  # jp, fr, etc.
        if emoji_name in langcode.keys():
            country = emoji_name
        else:
            return
    # Finding a lang based on a country is not the best way but oh well
    # Matching ISO 639-1 language code
    lang: str = langcode[country]
    if lang is None:
        return
    print(u'country: {}, lang: {}'.format(country, lang))

    channel = event["item"]["channel"]
    thread_ts = event['item']['ts']

    # Fetch all the messages in the thread
    replies_response = slack_web_api_client.conversations_replies(
        channel=channel,
        ts=thread_ts
    )
    if replies_response["ok"]:
        messages = replies_response["messages"]
        first_message = messages[0]
        if first_message["text"]:
            text: str = first_message["text"]
            # Call Google Translate API
            if isinstance(text, six.binary_type):
                text = text.decode('utf-8')
            translation_result = translate_client.translate(
                text,
                target_language=lang,
                model='nmt')
            translated_text = html.unescape(
                translation_result["translatedText"])

            # Make sure if the translated text is not posted yet
            for msg in messages:
                if msg["text"] and msg["text"] == translated_text:
                    print("The translation seems to be already posted")
                    return

            # Post the translation as a new message in the thread
            post_response = slack_web_api_client.chat_postMessage(
                channel=channel,
                thread_ts=thread_ts,
                username="Reacjilator Bot",
                text=translated_text)
            if post_response["ok"]:
                print(u'The translated text for {} has been successfully posted: {}'
                      .format(lang, post_response["ts"]))
            else:
                print(u'Failed to post a translated text: {}'
                      .format(post_response["error"]))
        else:
            print(u'Skipped because the message doesn\'t contain text: {}'
                  .format(first_message))
            return
    else:
        print(u'Failed to fetch message replies: {}'
              .format(replies_response["error"]))


# --------------------
# python app.py
slack_events_adapter.start(port=3000)
