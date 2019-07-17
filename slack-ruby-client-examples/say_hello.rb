# SLACK_API_TOKEN=xoxb-xxx bundle exec ruby say_hello.rb
require 'slack-ruby-client'
require 'logger'

Slack.configure do |config|
  config.token = ENV['SLACK_API_TOKEN']
end
client = Slack::Web::Client.new
client.logger.level = Logger::DEBUG

require 'json'

auth_test_res = client.auth_test
puts JSON.pretty_generate(auth_test_res)

post_res = client.chat_postMessage(
  channel: '#random',
  text: 'Hello World!',
  as_user: true
)
puts JSON.pretty_generate(post_res)