'use strict';

import * as A from '@slack/web-api';
import * as R from 'seratch-slack-types/web-api';

// ---------------------------
// Examples

const apiToken = PropertiesService.getScriptProperties().getProperty("SLACK_API_TOKEN");

// https://api.slack.com/methods/api.test
function apiTest() {
  const httpResponse = UrlFetchApp.fetch(
    url('api.test'),
    options<A.APITestArguments>(apiToken, {
      'foo': 'bar'
    }));
  const response = as(httpResponse) as R.ApiTestResponse;
  print(response);
}

// https://api.slack.com/methods/chat.postMessage
function chatPostMessage(text: string) {
  const httpResponse = UrlFetchApp.fetch(
    url('chat.postMessage'),
    options<A.ChatPostMessageArguments>(apiToken, {
      channel: '#random',
      text: 'Hi!'
    }));
  const response = as<R.ChatPostMessageResponse>(httpResponse);
  print(response);
  console.log(`Posted: ${json(response.message)}`);
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
  return {
    method: 'post',
    contentType: 'application/json; charset=utf-8',
    headers: { 'Authorization': `Bearer ${token}` },
    payload: JSON.stringify(payload),
  };
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