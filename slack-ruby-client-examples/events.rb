# bundle exec ruby events.rb -p 3000
# ngrok http 3000
require 'sinatra'
require 'sinatra/reloader'
require 'json'

require 'slack-ruby-client'
require 'logger'

# Configure Slack API client
Slack::Events.configure do |config|
  config.signing_secret = ENV['SLACK_SIGNING_SECRET']
end
Slack.configure do |config|
  config.token = ENV['SLACK_BOT_TOKEN']
end
client = Slack::Web::Client.new
client.logger.level = Logger::DEBUG

# 'X-Slack-Signature' verification filter
before do
  begin
    req = HttpRequest.new(request)
    Slack::Events::Request.new(req).verify!
    request.body.rewind
  rescue => e
    puts "Invalid signature #{e.to_json}"
    halt 401, 'Invalid signature!'
  end
end

# Endpoint to receive requests from Slack
post '/slack/events' do
  body = request.body.read
  params = JSON.parse(body)
  if params['type'] == 'url_verification'
    return params['challenge']
  else
    puts "Got #{params['event']}"
  end
end

# HTTP request wrapper for compatibility with slack-ruby-client
class HttpRequest
  attr_reader :headers, :body
  def initialize(request)
    @headers = Headers.new(request.env)
    @body = request.body
  end

  class Headers
    def initialize(env)
      @env = env
    end
    def [](key)
      @env[to_env_key(key)]
    end

    private
    def to_env_key(str)
      'HTTP_' + str.gsub(/([A-Z]+)([A-Z][a-z])/, '\1_\2')
        .gsub(/([a-z\d])([A-Z])/, '\1_\2')
        .tr("-", "_")
        .upcase
    end
  end
end