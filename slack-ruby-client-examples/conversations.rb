# SLACK_BOT_TOKEN=xoxb-xxx bundle exec ruby converstaions.rb
require 'slack-ruby-client'
require 'logger'

Slack.configure do |config|
  config.token = ENV['SLACK_BOT_TOKEN']
end
client = Slack::Web::Client.new
client.logger.level = Logger::DEBUG

require 'json'

channels = client.conversations_list.channels
channels.take(10).each do |channel|
  # puts JSON.pretty_generate(channel)
  puts channel.name
end
