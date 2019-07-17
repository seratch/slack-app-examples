# RTM API example
# SLACK_API_TOKEN=xoxb-xxx ; bundle exec ruby pongbot.rb
# Post a message "{bot name} ping" on a channel having the bot user as a member

require 'slack-ruby-bot'

class PongBot < SlackRubyBot::Bot
  command 'ping' do |client, data, match|
    client.say(text: 'pong', channel: data.channel)
  end
end

PongBot.run

=begin

$ SLACK_API_TOKEN=xoxb-xxx bundle exec ruby pongbot.rb
I, [2019-07-15T17:30:07.646363 #39423]  INFO -- request: POST https://slack.com/api/rtm.start
D, [2019-07-15T17:30:07.646422 #39423] DEBUG -- request: Accept: "application/json; charset=utf-8"
User-Agent: "Slack Ruby Client/0.14.2"
Content-Type: "application/x-www-form-urlencoded"
I, [2019-07-15T17:30:08.212685 #39423]  INFO -- response: Status 200
D, [2019-07-15T17:30:08.212775 #39423] DEBUG -- response: content-type: "application/json; charset=utf-8"
transfer-encoding: "chunked"
connection: "close"
date: "Mon, 15 Jul 2019 08:30:07 GMT"
server: "Apache"
x-content-type-options: "nosniff"
x-slack-req-id: "6baa23d9-daed-427a-8fe9-xxx"
x-oauth-scopes: "identify,bot:basic"
expires: "Mon, 26 Jul 1997 05:00:00 GMT"
cache-control: "private, no-cache, no-store, must-revalidate"
access-control-expose-headers: "x-slack-req-id, retry-after"
x-xss-protection: "0"
x-accepted-oauth-scopes: "rtm:stream,client"
vary: "Accept-Encoding"
pragma: "no-cache"
access-control-allow-headers: "slack-route, x-slack-version-ts"
strict-transport-security: "max-age=31536000; includeSubDomains; preload"
referrer-policy: "no-referrer"
access-control-allow-origin: "*"
x-via: "haproxy-www-r4vb"
x-cache: "Miss from cloudfront"
via: "1.1 xxx.cloudfront.net (CloudFront)"
x-amz-cf-pop: "NRT20-C2"
x-amz-cf-id: "7zgE1TXats3_xxx"
D, [2019-07-15T17:30:08.575278 #39423] DEBUG -- Slack::RealTime::Concurrency::Async::Socket#connect!: Slack::RealTime::Concurrency::Async::Client
D, [2019-07-15T17:30:08.598564 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::OpenEvent
D, [2019-07-15T17:30:08.933543 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::MessageEvent, {"type": "hello"}
D, [2019-07-15T17:30:08.933637 #39423] DEBUG -- SlackRubyBot::Client#dispatch: type=hello
I, [2019-07-15T17:30:08.933680 #39423]  INFO -- : Successfully connected team xxx (T01234567) to https://xxx.slack.com.
D, [2019-07-15T17:30:11.801694 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::MessageEvent, {"type":"user_typing","channel":"C01234567","user":"U01234567"}
D, [2019-07-15T17:30:11.801882 #39423] DEBUG -- SlackRubyBot::Client#dispatch: channel=C01234567, type=user_typing, user=U01234567
D, [2019-07-15T17:30:13.081899 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::MessageEvent, {"client_msg_id":"36357b6a-ff10-4251-884d-xxx","suppress_notification":false,"type":"message","text":"bot ping","user":"U01234567","team":"T01234567","user_team":"T01234567","source_team":"T01234567","channel":"C01234567","event_ts":"1563179412.003900","ts":"1563179412.003900"}
D, [2019-07-15T17:30:13.082108 #39423] DEBUG -- SlackRubyBot::Client#dispatch: channel=C01234567, client_msg_id=36357b6a-ff10-4251-884d-de89011d7d82, event_ts=1563179412.003900, source_team=T01234567, suppress_notification=false, team=T01234567, text=bot ping, ts=1563179412.003900, type=message, user=U01234567, user_team=T01234567
D, [2019-07-15T17:30:26.351723 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::MessageEvent, {"type":"user_typing","channel":"C01234567","user":"U01234567"}
D, [2019-07-15T17:30:26.351803 #39423] DEBUG -- SlackRubyBot::Client#dispatch: channel=C01234567, type=user_typing, user=U01234567
D, [2019-07-15T17:30:34.485296 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::MessageEvent, {"type":"user_typing","channel":"C01234567","user":"U01234567"}
D, [2019-07-15T17:30:34.485489 #39423] DEBUG -- SlackRubyBot::Client#dispatch: channel=C01234567, type=user_typing, user=U01234567
D, [2019-07-15T17:30:36.121611 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::MessageEvent, {"client_msg_id":"a808604e-5d63-4c9a-9484-xxx","suppress_notification":false,"type":"message","text":"jslacksample ping","user":"U01234567","team":"T01234567","user_team":"T01234567","source_team":"T01234567","channel":"C01234567","event_ts":"1563179435.004200","ts":"1563179435.004200"}
D, [2019-07-15T17:30:36.121760 #39423] DEBUG -- SlackRubyBot::Client#dispatch: channel=C01234567, client_msg_id=a808604e-5d63-4c9a-9484-xxx, event_ts=1563179435.004200, source_team=T01234567, suppress_notification=false, team=T01234567, text=jslacksample ping, ts=1563179435.004200, type=message, user=U01234567, user_team=T01234567
D, [2019-07-15T17:30:36.122345 #39423] DEBUG -- SlackRubyBot::Client#send_json: {:type=>"message", :id=>1, :text=>"pong", :channel=>"C01234567"}
D, [2019-07-15T17:30:36.125273 #39423] DEBUG -- Slack::RealTime::Concurrency::Async::Socket#send_data: {"type":"message","id":1,"text":"pong","channel":"C01234567"}
D, [2019-07-15T17:30:36.332184 #39423] DEBUG -- SlackRubyBot::Client#run_loop: WebSocket::Driver::MessageEvent, {"ok":true,"reply_to":1,"ts":"1563179436.004300","text":"pong"}
=end
