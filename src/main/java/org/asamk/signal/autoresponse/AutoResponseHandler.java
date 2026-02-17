package org.asamk.signal.autoresponse;

import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.MessageEnvelope;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message handler that automatically responds to incoming messages using AI.
 * This handler intercepts messages and generates appropriate responses in a natural way
 * that feels logical and rational to end users on Signal mobile/desktop apps.
 */
public class AutoResponseHandler implements Manager.ReceiveMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutoResponseHandler.class);

    private final Manager manager;
    private final AutoResponseConfig config;
    private final AIResponseService aiService;
    private final Manager.ReceiveMessageHandler wrappedHandler;
    private final Set<String> greetedSenders = ConcurrentHashMap.newKeySet();
    private final Random random = new Random();

    public AutoResponseHandler(
            Manager manager,
            AutoResponseConfig config,
            Manager.ReceiveMessageHandler wrappedHandler
    ) {
        this.manager = manager;
        this.config = config;
        this.aiService = new AIResponseService(config);
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void handleMessage(MessageEnvelope envelope, Throwable exception) {
        // Always pass message to wrapped handler first (for logging, etc.)
        if (wrappedHandler != null) {
            wrappedHandler.handleMessage(envelope, exception);
        }

        // Don't auto-respond if disabled or if there was an exception
        if (!config.isEnabled() || exception != null) {
            return;
        }

        // Only respond to data messages with text body
        if (envelope.data().isEmpty() || envelope.data().get().body().isEmpty()) {
            return;
        }

        var dataMessage = envelope.data().get();
        var messageBody = dataMessage.body().get();

        // Check if message is from a group
        boolean isGroupMessage = dataMessage.groupContext().isPresent();
        if (isGroupMessage && !config.isRespondToGroups()) {
            logger.debug("Ignoring group message (group responses disabled)");
            return;
        }

        // Get sender information
        Optional<String> senderNumber = envelope.sourceAddress().map(addr -> addr.getLegacyIdentifier());
        if (senderNumber.isEmpty()) {
            logger.debug("Ignoring message with no sender");
            return;
        }

        String sender = senderNumber.get();

        // Check allowed/blocked lists
        if (!isAllowedSender(sender)) {
            logger.debug("Sender {} is not in allowed list or is blocked", sender);
            return;
        }

        // Process message and send response in a separate thread to avoid blocking
        new Thread(() -> {
            try {
                processAndRespond(envelope, messageBody, sender, isGroupMessage);
            } catch (Exception e) {
                logger.error("Failed to process auto-response", e);
            }
        }, "AutoResponse-" + sender).start();
    }

    private void processAndRespond(MessageEnvelope envelope, String messageBody, String sender, boolean isGroupMessage) {
        try {
            // Send greeting message if this is the first interaction
            boolean isFirstMessage = !greetedSenders.contains(sender);
            if (isFirstMessage && config.isSendGreetingOnFirstMessage()) {
                sendTypingIndicator(envelope);
                Thread.sleep(getTypingDelay() * 1000L);
                sendMessage(envelope, config.getGreetingMessage());
                greetedSenders.add(sender);
                
                // Short delay before main response
                Thread.sleep(1000);
            }

            // Send typing indicator to make it feel more natural
            if (config.isSendTypingIndicator()) {
                sendTypingIndicator(envelope);
            }

            // Add natural delay based on message length (simulate human reading/typing time)
            if (config.isUseNaturalDelay()) {
                int delay = calculateNaturalDelay(messageBody);
                Thread.sleep(delay * 1000L);
            }

            // Generate AI response
            String response = aiService.generateResponse(messageBody, sender);

            // Add bot signature if enabled
            if (config.isIdentifyAsBot() && !response.contains(config.getBotSignature())) {
                response = response + config.getBotSignature();
            }

            // Send the response
            sendMessage(envelope, response);
            
            logger.info("Auto-response sent to {} (group: {}, length: {} chars)", 
                    sender, isGroupMessage, response.length());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Auto-response interrupted for {}", sender);
        } catch (Exception e) {
            logger.error("Failed to generate or send auto-response", e);
            
            // Try to send fallback message if configured
            if (config.isUseFallbackOnError()) {
                try {
                    String fallback = config.getFallbackMessage();
                    if (config.isIdentifyAsBot()) {
                        fallback = fallback + config.getBotSignature();
                    }
                    sendMessage(envelope, fallback);
                } catch (Exception fallbackError) {
                    logger.error("Failed to send fallback message", fallbackError);
                }
            }
        }
    }

    private int calculateNaturalDelay(String message) {
        // Calculate delay based on message length to simulate reading/thinking time
        int baseDelay = config.getMinDelaySeconds();
        int maxDelay = config.getMaxDelaySeconds();
        
        // Add delay based on message length (roughly 1 second per 100 characters)
        int lengthDelay = message.length() / 100;
        int calculatedDelay = baseDelay + lengthDelay;
        
        // Cap at max delay
        calculatedDelay = Math.min(calculatedDelay, maxDelay);
        
        // Add small random variance (±20%) to feel more human
        int variance = (int) (calculatedDelay * 0.2);
        int randomVariance = random.nextInt(variance * 2 + 1) - variance;
        
        return Math.max(1, calculatedDelay + randomVariance);
    }

    private int getTypingDelay() {
        return config.getTypingDelaySeconds();
    }

    private boolean isAllowedSender(String sender) {
        // If blocked list contains sender, deny
        if (!config.getBlockedSenders().isEmpty() && config.getBlockedSenders().contains(sender)) {
            return false;
        }

        // If allowed list is empty, allow all (except blocked)
        if (config.getAllowedSenders().isEmpty()) {
            return true;
        }

        // Otherwise, sender must be in allowed list
        return config.getAllowedSenders().contains(sender);
    }

    private void sendTypingIndicator(MessageEnvelope envelope) {
        try {
            var dataMessage = envelope.data().get();
            RecipientIdentifier recipient = getRecipient(envelope, dataMessage);
            
            if (recipient != null) {
                manager.sendTypingMessage(
                        org.asamk.signal.manager.api.TypingAction.START,
                        Set.of(recipient)
                );
                logger.debug("Typing indicator sent to {}", recipient);
            }
        } catch (Exception e) {
            logger.debug("Failed to send typing indicator: {}", e.getMessage());
        }
    }

    private void sendMessage(MessageEnvelope envelope, String response) {
        try {
            var dataMessage = envelope.data().get();
            RecipientIdentifier recipient = getRecipient(envelope, dataMessage);
            
            if (recipient == null) {
                logger.warn("Cannot determine recipient for response");
                return;
            }

            // Create message with the response text
            var message = new org.asamk.signal.manager.api.Message(
                    response,
                    java.util.List.of(),
                    false,
                    java.util.List.of(),
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.List.of(),
                    java.util.Optional.empty(),
                    java.util.List.of()
            );

            // Send the response
            manager.sendMessage(message, java.util.Set.of(recipient), false);
            
            // Send stopped typing indicator
            if (config.isSendTypingIndicator()) {
                try {
                    manager.sendTypingMessage(
                            org.asamk.signal.manager.api.TypingAction.STOP,
                            Set.of(recipient)
                    );
                } catch (Exception e) {
                    logger.debug("Failed to send stopped typing indicator: {}", e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.error("Failed to send auto-response", e);
            throw new RuntimeException("Failed to send message", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending auto-response", e);
            throw new RuntimeException("Unexpected error", e);
        }
    }

    private RecipientIdentifier getRecipient(MessageEnvelope envelope, MessageEnvelope.Data dataMessage) {
        if (dataMessage.groupContext().isPresent()) {
            // Reply to group
            return new RecipientIdentifier.Group(dataMessage.groupContext().get().groupId());
        } else if (envelope.sourceAddress().isPresent()) {
            // Reply to individual
            return RecipientIdentifier.Single.fromAddress(envelope.sourceAddress().get());
        }
        return null;
    }

    /**
     * Clear conversation history in the AI service.
     */
    public void clearHistory() {
        aiService.clearHistory();
        greetedSenders.clear();
    }
}
