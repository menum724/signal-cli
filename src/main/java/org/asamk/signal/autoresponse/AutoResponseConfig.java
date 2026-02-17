package org.asamk.signal.autoresponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the auto-response system.
 * Stores settings for AI-powered responses and template-based fallbacks.
 */
public class AutoResponseConfig {

    @JsonProperty
    private boolean enabled = false;

    @JsonProperty
    private String aiProvider = "openai";

    @JsonProperty
    private String apiEndpoint = "https://api.openai.com/v1/chat/completions";

    @JsonProperty
    private String apiKey = "";

    @JsonProperty
    private String model = "gpt-3.5-turbo";

    @JsonProperty
    private String systemPrompt = "You are a helpful AI assistant responding to messages on Signal. " +
            "Be concise, friendly, and helpful. Keep responses brief and conversational. " +
            "You are helping the account owner by responding to their messages when they are unavailable.";

    @JsonProperty
    private List<String> allowedSenders = new ArrayList<>();

    @JsonProperty
    private List<String> blockedSenders = new ArrayList<>();

    @JsonProperty
    private boolean respondToGroups = false;

    @JsonProperty
    private int maxResponseLength = 500;

    @JsonProperty
    private String fallbackMessage = "Thanks for your message. I'll get back to you soon!";

    @JsonProperty
    private boolean useFallbackOnError = true;

    @JsonProperty
    private boolean sendTypingIndicator = true;

    @JsonProperty
    private int typingDelaySeconds = 2;

    @JsonProperty
    private boolean sendGreetingOnFirstMessage = true;

    @JsonProperty
    private String greetingMessage = "👋 Hi! I'm an AI assistant managing messages for this account. How can I help you?";

    @JsonProperty
    private boolean identifyAsBot = true;

    @JsonProperty
    private String botSignature = "\n\n🤖 (AI Assistant)";

    @JsonProperty
    private boolean useNaturalDelay = true;

    @JsonProperty
    private int minDelaySeconds = 1;

    @JsonProperty
    private int maxDelaySeconds = 5;

    public AutoResponseConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<String> getAllowedSenders() {
        return allowedSenders;
    }

    public void setAllowedSenders(List<String> allowedSenders) {
        this.allowedSenders = allowedSenders;
    }

    public List<String> getBlockedSenders() {
        return blockedSenders;
    }

    public void setBlockedSenders(List<String> blockedSenders) {
        this.blockedSenders = blockedSenders;
    }

    public boolean isRespondToGroups() {
        return respondToGroups;
    }

    public void setRespondToGroups(boolean respondToGroups) {
        this.respondToGroups = respondToGroups;
    }

    public int getMaxResponseLength() {
        return maxResponseLength;
    }

    public void setMaxResponseLength(int maxResponseLength) {
        this.maxResponseLength = maxResponseLength;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }

    public void setFallbackMessage(String fallbackMessage) {
        this.fallbackMessage = fallbackMessage;
    }

    public boolean isUseFallbackOnError() {
        return useFallbackOnError;
    }

    public void setUseFallbackOnError(boolean useFallbackOnError) {
        this.useFallbackOnError = useFallbackOnError;
    }

    public boolean isSendTypingIndicator() {
        return sendTypingIndicator;
    }

    public void setSendTypingIndicator(boolean sendTypingIndicator) {
        this.sendTypingIndicator = sendTypingIndicator;
    }

    public int getTypingDelaySeconds() {
        return typingDelaySeconds;
    }

    public void setTypingDelaySeconds(int typingDelaySeconds) {
        this.typingDelaySeconds = typingDelaySeconds;
    }

    public boolean isSendGreetingOnFirstMessage() {
        return sendGreetingOnFirstMessage;
    }

    public void setSendGreetingOnFirstMessage(boolean sendGreetingOnFirstMessage) {
        this.sendGreetingOnFirstMessage = sendGreetingOnFirstMessage;
    }

    public String getGreetingMessage() {
        return greetingMessage;
    }

    public void setGreetingMessage(String greetingMessage) {
        this.greetingMessage = greetingMessage;
    }

    public boolean isIdentifyAsBot() {
        return identifyAsBot;
    }

    public void setIdentifyAsBot(boolean identifyAsBot) {
        this.identifyAsBot = identifyAsBot;
    }

    public String getBotSignature() {
        return botSignature;
    }

    public void setBotSignature(String botSignature) {
        this.botSignature = botSignature;
    }

    public boolean isUseNaturalDelay() {
        return useNaturalDelay;
    }

    public void setUseNaturalDelay(boolean useNaturalDelay) {
        this.useNaturalDelay = useNaturalDelay;
    }

    public int getMinDelaySeconds() {
        return minDelaySeconds;
    }

    public void setMinDelaySeconds(int minDelaySeconds) {
        this.minDelaySeconds = minDelaySeconds;
    }

    public int getMaxDelaySeconds() {
        return maxDelaySeconds;
    }

    public void setMaxDelaySeconds(int maxDelaySeconds) {
        this.maxDelaySeconds = maxDelaySeconds;
    }
}
