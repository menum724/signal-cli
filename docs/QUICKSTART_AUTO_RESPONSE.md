# Quick Start Guide: AI Auto-Response Bot

This guide will help you set up an AI-powered auto-response bot for Signal CLI in just a few minutes.

## Prerequisites

- Signal CLI installed and registered with a phone number
- An API key from OpenAI (or access to a local LLM)
- Java 21+ installed

## Setup in 3 Steps

### Step 1: Configure the AI Bot

```bash
# Replace +1234567890 with your Signal phone number
signal-cli -a +1234567890 autoResponse \
  --api-key "sk-your-openai-api-key-here" \
  --system-prompt "You are a friendly AI assistant. Be helpful and concise." \
  --enable
```

### Step 2: Start the Daemon with Auto-Response

```bash
signal-cli -a +1234567890 daemon --auto-response
```

That's it! Your AI bot is now running and will automatically respond to incoming messages.

### Step 3: Test It

Send a message to your Signal number from another device. You should see:
1. A typing indicator appear (...)
2. A greeting message: "👋 Hi! I'm an AI assistant managing messages for this account. How can I help you?"
3. A natural delay (1-5 seconds)
4. An AI-generated response to your message
5. Each response is signed with "🤖 (AI Assistant)"

## Common Use Cases

### Personal Assistant

```bash
signal-cli -a +1234567890 autoResponse \
  --system-prompt "You are my personal assistant. Help manage my schedule, answer questions about my availability, and be professional but friendly." \
  --enable
```

### Away Message Bot

```bash
signal-cli -a +1234567890 autoResponse \
  --fallback-message "I'm away until next week. For urgent matters, email me@example.com" \
  --api-key "" \
  --enable
```

### Customer Support Bot

```bash
signal-cli -a +1234567890 autoResponse \
  --system-prompt "You are a customer support assistant for [Company Name]. Answer common questions, provide support links, and be helpful." \
  --respond-to-groups \
  --enable
```

### Privacy-Focused with Local LLM

```bash
# Start Ollama with llama2
ollama serve

# Configure Signal CLI to use Ollama
signal-cli -a +1234567890 autoResponse \
  --api-endpoint "http://localhost:11434/v1/chat/completions" \
  --model "llama2" \
  --api-key "not-needed" \
  --system-prompt "You are a helpful assistant. Be concise." \
  --enable
```

## Configuration Options Explained

### Natural Interaction Features

The bot is designed to interact naturally with Signal users:

- **Typing Indicators**: Shows "..." when generating responses (default: ON)
- **Natural Delays**: Simulates human reading/thinking time (1-5 seconds based on message length)
- **Greeting Messages**: Introduces itself on first contact (default: ON)
- **Bot Identification**: Signs messages with "🤖 (AI Assistant)" so users know they're talking to a bot

### Sender Filtering

```bash
# Only respond to specific numbers
signal-cli -a +1234567890 autoResponse --allow-sender "+11234567890" "+10987654321"

# Block specific numbers
signal-cli -a +1234567890 autoResponse --block-sender "+15555555555"
```

### Customization

```bash
# Change the greeting message
signal-cli -a +1234567890 autoResponse \
  --greeting-message "Hello! I'm an AI helping out while the owner is away."

# Change the bot signature
signal-cli -a +1234567890 autoResponse \
  --bot-signature "\n\n- Automated Assistant"

# Use a different AI model
signal-cli -a +1234567890 autoResponse --model "gpt-4"
```

## View Current Configuration

```bash
signal-cli -a +1234567890 autoResponse --show-config
```

This displays all settings including enabled status, API configuration, and behavior options.

## Disable Auto-Response

```bash
signal-cli -a +1234567890 autoResponse --disable
```

The daemon will continue running but won't send automatic responses.

## Troubleshooting

### Bot not responding

1. Check if auto-response is enabled:
   ```bash
   signal-cli -a +1234567890 autoResponse --show-config
   ```

2. Check daemon logs for errors

3. Verify API key is correct

### Responses too slow

- Use `gpt-3.5-turbo` instead of `gpt-4`
- Or use a local LLM for instant responses

### Want to test without sending to everyone

Use sender filtering to only respond to your test number:
```bash
signal-cli -a +1234567890 autoResponse --allow-sender "+1YOUR_TEST_NUMBER"
```

## Advanced: Multi-Account Setup

You can run multiple accounts with different configurations:

```bash
# Terminal 1: Personal account with friendly bot
signal-cli -a +1234567890 daemon --auto-response

# Terminal 2: Business account with professional bot
signal-cli -a +0987654321 daemon --auto-response
```

Each account maintains its own configuration file.

## Security Best Practices

1. **Protect your API key**: It's stored in the config file - keep it secure
2. **Use sender filtering**: Limit who can interact with your bot
3. **Review conversations**: Check logs regularly to ensure appropriate responses
4. **Consider local LLMs**: For maximum privacy, use Ollama or similar
5. **Be transparent**: The bot identifies itself so users know they're talking to AI

## What End Users See

When someone messages your Signal number, they experience:

1. **Natural feel**: Typing indicators and realistic delays make it feel like a real conversation
2. **Clear identification**: Every message is signed so they know it's an AI assistant
3. **Conversational**: The AI maintains context within the conversation
4. **Helpful**: The AI tries to answer questions and be genuinely useful
5. **Respectful**: The bot is configured to be polite and professional

This creates a logical and rational interaction that Signal desktop and mobile users find intuitive and helpful.

## Next Steps

- Read the full documentation: [AUTO_RESPONSE.md](AUTO_RESPONSE.md)
- Customize the system prompt for your specific use case
- Experiment with different AI models
- Set up monitoring and logging
- Consider using read receipts and delivery receipts for better UX

## Support

For issues or questions:
- Check the [FAQ section](AUTO_RESPONSE.md#troubleshooting) in AUTO_RESPONSE.md
- Review Signal CLI documentation
- Check GitHub issues

Enjoy your AI-powered Signal bot! 🤖
