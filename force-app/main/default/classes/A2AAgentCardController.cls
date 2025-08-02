/**
 * Controller for A2A Agent Card Lightning Web Component
 */
public with sharing class A2AAgentCardController {
    
    /**
     * Get agent card from URL
     */
    @AuraEnabled(cacheable=true)
    public static Map<String, Object> getAgentCard(String agentUrl) {
        try {
            A2ACardResolver resolver = new A2ACardResolver(new A2AHttpClient(), agentUrl);
            A2AAgentCard card = resolver.getAgentCard();
            return card.serialize();
        } catch (Exception e) {
            throw new AuraHandledException('Failed to retrieve agent card: ' + e.getMessage());
        }
    }
    
    /**
     * Refresh agent card (non-cacheable)
     */
    @AuraEnabled
    public static Map<String, Object> refreshAgentCard(String agentUrl) {
        try {
            A2ACardResolver resolver = new A2ACardResolver(new A2AHttpClient(), agentUrl);
            A2AAgentCard card = resolver.getAgentCard();
            return card.serialize();
        } catch (Exception e) {
            throw new AuraHandledException('Failed to refresh agent card: ' + e.getMessage());
        }
    }
    
    /**
     * Get agent cards from static resources
     */
    @AuraEnabled(cacheable=true)
    public static List<Map<String, Object>> getAgentCardsFromStaticResources() {
        try {
            List<Map<String, Object>> cards = new List<Map<String, Object>>();
            
            // Query static resources that contain agent cards
            List<StaticResource> resources = [
                SELECT Name, Body 
                FROM StaticResource 
                WHERE Name LIKE 'A2AAgent%' 
                ORDER BY Name
                LIMIT 50
            ];
            
            for (StaticResource resource : resources) {
                try {
                    String jsonContent = resource.Body.toString();
                    A2AAgentCard card = A2AAgentCard.deserialize(jsonContent);
                    cards.add(card.serialize());
                } catch (Exception e) {
                    // Skip invalid agent cards
                    System.debug('Failed to parse agent card from resource ' + resource.Name + ': ' + e.getMessage());
                }
            }
            
            return cards;
        } catch (Exception e) {
            throw new AuraHandledException('Failed to retrieve agent cards: ' + e.getMessage());
        }
    }
    
    /**
     * Send a message to an agent
     */
    @AuraEnabled
    public static Map<String, Object> sendMessage(String agentUrl, String messageJson) {
        try {
            // Parse message
            A2AMessage message = A2AMessage.deserialize(messageJson);
            
            // Create client
            A2AClient client = new A2AClient(agentUrl);
            
            // Send message
            A2AClient.A2AMessageSendParams params = new A2AClient.A2AMessageSendParams(message);
            A2AClient.A2ASendMessageResponse response = client.sendMessage(params);
            
            // Return response
            Map<String, Object> result = new Map<String, Object>();
            if (response.error != null) {
                result.put('error', response.error);
            } else {
                result.put('result', response.result);
            }
            
            return result;
        } catch (Exception e) {
            throw new AuraHandledException('Failed to send message: ' + e.getMessage());
        }
    }
    
    /**
     * Get task by ID
     */
    @AuraEnabled
    public static Map<String, Object> getTask(String agentUrl, String taskId) {
        try {
            A2AClient client = new A2AClient(agentUrl);
            A2AClient.A2AGetTaskResponse response = client.getTask(taskId);
            
            Map<String, Object> result = new Map<String, Object>();
            if (response.error != null) {
                result.put('error', response.error);
            } else {
                result.put('result', response.result);
            }
            
            return result;
        } catch (Exception e) {
            throw new AuraHandledException('Failed to get task: ' + e.getMessage());
        }
    }
    
    /**
     * Cancel task
     */
    @AuraEnabled
    public static Map<String, Object> cancelTask(String agentUrl, String taskId) {
        try {
            A2AClient client = new A2AClient(agentUrl);
            A2AClient.A2ACancelTaskResponse response = client.cancelTask(taskId);
            
            Map<String, Object> result = new Map<String, Object>();
            if (response.error != null) {
                result.put('error', response.error);
            } else {
                result.put('result', response.result);
            }
            
            return result;
        } catch (Exception e) {
            throw new AuraHandledException('Failed to cancel task: ' + e.getMessage());
        }
    }
}