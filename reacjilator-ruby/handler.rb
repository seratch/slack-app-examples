#
# A Ruby implementation of https://github.com/slackapi/reacjilator
#
# Author: Kazuhiro Sera @seratch
# MIT License as with the original code
#

require 'json'
require 'slack-ruby-client'
require "google/cloud/translate"

def events(event:, context:)

  # Load language code master data
  all_lang_codes = {}
  File.open('langcode.json') do |file|
    all_lang_codes = JSON.parse(file.read)
  end

  # Slack Web API
  Slack.configure do |config|
    config.token = ENV['SLACK_API_TOKEN']
  end
  Slack::Events.configure do |config|
    config.signing_secret = ENV['SLACK_SIGNING_SECRET']
  end

  slack_client = Slack::Web::Client.new

  # Google Translate API
  google_translate_api = Google::Cloud::Translate.new

  body = JSON.parse(event['body'])
  puts "body: #{body}"

  # Verify "X-Slack-Signature" header
  begin
    Slack::Events::Request.new(HttpRequest.new(event)).verify!
  rescue => e
    puts "Invalid signature #{e.to_json}"
    return { statusCode: 401 }
  end

  # Handle "url_verification" requests
  if body['type'] == 'url_verification'
    return {
      statusCode: 200,
      headers: {'Content-Type': 'text/plain'},
      body: body['challenge']
    }
  end

  # Resuable 200 OK response
  ok_response = {
    statusCode: 200,
    headers: {'Content-Type': 'application/json'},
    body: {ok: true}.to_json
  }

  payload_event = body['event']

  if payload_event.nil? ||
    payload_event['type'] != 'reaction_added' ||
    payload_event['item']['type'] != 'message'
    return ok_response
  end

  reaction_name = payload_event['reaction']

  country = nil
  if /^flag-\w{2}$/.match?(reaction_name)
    country = /^flag-(\w{2})$/.match(reaction_name).captures.first
  else
    all_flags = all_lang_codes.keys
    if all_flags.include?(reaction_name)
      country = reaction_name
    else
      puts "Skipped: no country detected"
      return ok_response
    end
  end

  lang = all_lang_codes[country]
  if lang.nil?
    puts "Skipped: no lang detected"
    return ok_response
  end
  puts "country: #{country}, lang: #{lang}"

  channel_id = payload_event['item']['channel']
  thread_ts = payload_event['item']['ts']

  replies_resp = slack_client.conversations_replies(
    channel: channel_id,
    ts: thread_ts
  )
  if replies_resp.ok
    messages = replies_resp['messages']
    first_message = messages.first
    text = first_message['text']
    if text
      translated_text = google_translate_api.translate(text, to: lang).try(:to_str)
      messages.each do |msg|
        if msg['text'] == translated_text
          puts 'Skipped: already posted'
          return ok_response
        end
      end
      post_resp = slack_client.chat_postMessage(
        channel: channel_id,
        text: translated_text,
        thread_ts: thread_ts,
        username: 'Reacjilator Bot'
      )
      if post_resp.ok
        puts "Successfully posted a translation (lang:#{lang}, ts: #{post_resp['ts']})"
      else
        puts "Failed to post a message because #{post_resp.error}"
      end
    else
      puts 'Skipped: no text'
    end
  else
    puts "Failed to fetch replies because #{replies_resp.error}"
  end

  ok_response
end

class HttpRequest
  attr_reader :headers, :body

  class Body
    def initialize(body)
      @body = body
    end

    def read
      @body
    end
  end

  def initialize(event)
    @headers = event['headers']
    @body = HttpRequest::Body.new(event['body'])
  end
end