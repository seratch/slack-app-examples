'use strict';

import * as A from '@slack/web-api';
import * as R from 'seratch-slack-types/web-api';

// ---------------------------
// Examples

const apiToken = PropertiesService.getScriptProperties().getProperty("SLACK_API_TOKEN");

// https://api.slack.com/methods/api.test
function apiTest(): R.ApiTestResponse {
  const httpResponse = UrlFetchApp.fetch(
    url('api.test'),
    options<A.APITestArguments>(apiToken, {
      'foo': 'bar'
    }));
  const response = as<R.ApiTestResponse>(httpResponse);
  print(response);
  return response;
}

// https://api.slack.com/methods/chat.postMessage
function chatPostMessage(text: string): R.ChatPostMessageResponse {
  const httpResponse = UrlFetchApp.fetch(
    url('chat.postMessage'),
    options<A.ChatPostMessageArguments>(apiToken, {
      channel: '#random',
      text: text || 'Hi!'
    }));
  const response = as<R.ChatPostMessageResponse>(httpResponse);
  print(response);
  console.log(`Posted: ${json(response.message)}`);
  return response;
}

// https://api.slack.com/methods/channels.list
function channelsList(): R.ChannelsListResponse {
  const response = as<R.ChannelsListResponse>(UrlFetchApp.fetch(
    url('channels.list'),
    options<A.ChannelsListArguments>(apiToken, {
      exclude_archived: true,
      exclude_members: true
    })
  ));
  // `response.channels` can be a large array
  // print(response);
  if (response.error) console.error(`Got an error from Slack API: ${response.error}`)
  return response;
}

// https://api.slack.com/methods/conversations.history
function conversationsHistory() {
  const channels = channelsList().channels;
  // #find method is not allowed on GAS
  const channelId: string = channels.reduce((prev, current) => {
    if (prev) return prev;
    else if (current.name == 'random') return current;
    else return null;
  }, null).id;
  console.log(`#random: ${channelId}`);

  const httpResponse = UrlFetchApp.fetch(
    url('conversations.history'),
    options<A.ConversationsHistoryArguments>(apiToken, {
      channel: channelId
    })
  );
  const response = as<R.ConversationsHistoryResponse>(httpResponse);
  // `response.messages` can be a large array
  // print(response);
  response.messages.forEach(message => {
    const messagePart = `${message.text} by @${message.username} ts:${message.ts}`;
    const reactions = message.reactions
      ? message.reactions.map((r) => r.name + " * " + r.count)
      : [];
    const reactionsPart = reactions.length > 0 ? ` (reactions: ${reactions} )` : '';
    console.log(`${messagePart}${reactionsPart}`)
  });
}


// ---------------------------
// Common Functions

function url(apiName: string): string {
  return `https://slack.com/api/${apiName}`;
}

function json(value: object): string {
  return JSON.stringify(value);
}

import { WebAPICallOptions } from '@slack/web-api/dist/WebClient';

function options<A extends WebAPICallOptions>(
  token: string,
  payload: A): GoogleAppsScript.URL_Fetch.URLFetchRequestOptions {
  const options: GoogleAppsScript.URL_Fetch.URLFetchRequestOptions = {
    method: 'post',
    contentType: 'application/x-www-form-urlencoded',
    headers: { 'Authorization': `Bearer ${token}` },
    payload: payload,
  };
  console.log(options);
  return options;
}

function as<T>(response: GoogleAppsScript.URL_Fetch.HTTPResponse): T {
  return JSON.parse(response.getContentText()) as T;
}

function print(response: any) {
  if (response.ok) {
    console.log(`Successfully got a response from Slack API: ${json(response)}`);
  } else {
    console.error(`Got an error from Slack API: ${response.error}`);
  }
}