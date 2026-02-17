# Example Auto-Response Configuration for Signal CLI

This example shows how to configure the AI-powered auto-response feature in Signal CLI.

## Configuration File

The configuration is stored as a JSON file at:
```
$XDG_DATA_HOME/signal-cli/data/<PHONE_NUMBER>.auto-response.json
```

Example configuration:

```json
{
  "enabled": true,
  "aiProvider": "openai",
  "apiEndpoint": "https://api.openai.com/v1/chat/completions",
  "apiKey": "sk-your-api-key-here",
  "model": "gpt-3.5-turbo",
  "systemPrompt": "You are a helpful AI assistant responding to messages on Signal. Be concise, friendly, and helpful. Keep responses brief and conversational.",
  "allowedSenders": [],
  "blockedSenders": [],
  "respondToGroups": false,
  "maxResponseLength": 500,
  "fallbackMessage": "Thanks for your message. I'll get back to you soon!",
  "useFallbackOnError": true
}
```

## Quick Start

### 1. Configure API Key

```bash
signal-cli -a +1234567890 autoResponse --api-key "sk-your-openai-api-key"
```

### 2. Enable Auto-Response

```bash
signal-cli -a +1234567890 autoResponse --enable
```

### 3. Start Daemon with Auto-Response

```bash
signal-cli -a +1234567890 daemon --auto-response
```

## Configuration Options

### Enable/Disable

```bash
# Enable auto-response
signal-cli -a +1234567890 autoResponse --enable

# Disable auto-response
signal-cli -a +1234567890 autoResponse --disable
```

### API Configuration

```bash
# Set OpenAI API key
signal-cli -a +1234567890 autoResponse --api-key "sk-your-key"

# Use a different model
signal-cli -a +1234567890 autoResponse --model "gpt-4"

# Use a custom API endpoint (e.g., for local LLM)
signal-cli -a +1234567890 autoResponse --api-endpoint "http://localhost:8000/v1/chat/completions"
```

### Customize System Prompt

```bash
signal-cli -a +1234567890 autoResponse \
  --system-prompt "You are a professional assistant. Be formal and concise."
```

### Sender Filters

```bash
# Allow only specific senders
signal-cli -a +1234567890 autoResponse --allow-sender "+1111111111" "+2222222222"

# Block specific senders
signal-cli -a +1234567890 autoResponse --block-sender "+3333333333"
```

### Group Messages

```bash
# Enable responses to group messages
signal-cli -a +1234567890 autoResponse --respond-to-groups

# Disable responses to group messages (default)
signal-cli -a +1234567890 autoResponse --no-respond-to-groups
```

### Fallback Message

```bash
signal-cli -a +1234567890 autoResponse \
  --fallback-message "I'm currently unavailable. Will respond soon!"
```

### View Current Configuration

```bash
signal-cli -a +1234567890 autoResponse --show-config
```

## Using with OpenAI

1. Get an API key from https://platform.openai.com/api-keys
2. Configure Signal CLI:

```bash
signal-cli -a +1234567890 autoResponse \
  --api-key "sk-your-key" \
  --model "gpt-3.5-turbo" \
  --enable
```

3. Start daemon:

```bash
signal-cli -a +1234567890 daemon --auto-response
```

## Using with Local LLM (e.g., Ollama, LM Studio)

Many local LLM servers provide OpenAI-compatible APIs:

### Ollama

```bash
# Start Ollama with OpenAI-compatible endpoint
ollama serve

# Configure Signal CLI to use Ollama
signal-cli -a +1234567890 autoResponse \
  --api-endpoint "http://localhost:11434/v1/chat/completions" \
  --model "llama2" \
  --api-key "not-needed" \
  --enable
```

### LM Studio

```bash
# Configure for LM Studio (typically runs on port 1234)
signal-cli -a +1234567890 autoResponse \
  --api-endpoint "http://localhost:1234/v1/chat/completions" \
  --model "local-model" \
  --api-key "not-needed" \
  --enable
```

## Security Considerations

1. **API Keys**: Keep your API keys secure. The configuration file contains sensitive data.
2. **Sender Filtering**: Use `--allow-sender` to restrict who can trigger auto-responses.
3. **Group Messages**: Be cautious about enabling `--respond-to-groups` as it may cause spam.
4. **Rate Limiting**: Be aware of API rate limits from your AI provider.
5. **Privacy**: AI providers may log conversations. Use local LLMs for privacy.

## Troubleshooting

### Auto-response not working

1. Check if auto-response is enabled:
   ```bash
   signal-cli -a +1234567890 autoResponse --show-config
   ```

2. Check daemon logs for errors

3. Verify API endpoint is accessible:
   ```bash
   curl -H "Authorization: Bearer YOUR_API_KEY" \
        -H "Content-Type: application/json" \
        -d '{"model":"gpt-3.5-turbo","messages":[{"role":"user","content":"test"}]}' \
        https://api.openai.com/v1/chat/completions
   ```

### Responses are too slow

- Use a faster model (e.g., `gpt-3.5-turbo` instead of `gpt-4`)
- Use a local LLM for instant responses
- Check your network connection

### Want to clear conversation history

The conversation history is maintained in memory per sender. It resets when the daemon restarts.

## Examples

### Professional Assistant

```bash
signal-cli -a +1234567890 autoResponse \
  --system-prompt "You are a professional executive assistant. Respond politely and professionally. Keep responses under 2 sentences." \
  --enable
```

### Away Message Bot

```bash
signal-cli -a +1234567890 autoResponse \
  --api-key "" \
  --fallback-message "I'm on vacation until Monday. For urgent matters, contact +1234567890." \
  --enable
```

### Customer Support Bot

```bash
signal-cli -a +1234567890 autoResponse \
  --system-prompt "You are a customer support assistant. Be helpful and ask clarifying questions. Direct users to our website for detailed information." \
  --respond-to-groups \
  --enable
```
