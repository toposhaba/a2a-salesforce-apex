import { LightningElement, api, wire, track } from 'lwc';
import { ShowToastEvent } from 'lightning/platformShowToastEvent';
import { NavigationMixin } from 'lightning/navigation';
import getAgentCard from '@salesforce/apex/A2AAgentCardController.getAgentCard';
import refreshAgentCard from '@salesforce/apex/A2AAgentCardController.refreshAgentCard';

export default class A2aAgentCard extends NavigationMixin(LightningElement) {
    @api agentUrl;
    @api cardTitle = 'A2A Agent';
    
    @track agentCard;
    @track isLoading = false;
    @track hasError = false;
    @track errorMessage = '';
    
    connectedCallback() {
        if (this.agentUrl) {
            this.loadAgentCard();
        }
    }
    
    @api
    loadAgentCard() {
        this.isLoading = true;
        this.hasError = false;
        
        getAgentCard({ agentUrl: this.agentUrl })
            .then(result => {
                this.agentCard = result;
                this.isLoading = false;
                this.showToast('Success', 'Agent card loaded successfully', 'success');
            })
            .catch(error => {
                this.hasError = true;
                this.errorMessage = error.body?.message || 'Failed to load agent card';
                this.isLoading = false;
                this.showToast('Error', this.errorMessage, 'error');
            });
    }
    
    handleRefresh() {
        this.isLoading = true;
        this.hasError = false;
        
        refreshAgentCard({ agentUrl: this.agentUrl })
            .then(result => {
                this.agentCard = result;
                this.isLoading = false;
                this.showToast('Success', 'Agent card refreshed successfully', 'success');
            })
            .catch(error => {
                this.hasError = true;
                this.errorMessage = error.body?.message || 'Failed to refresh agent card';
                this.isLoading = false;
                this.showToast('Error', this.errorMessage, 'error');
            });
    }
    
    handleSendMessage() {
        // Navigate to message composer
        this[NavigationMixin.Navigate]({
            type: 'standard__component',
            attributes: {
                componentName: 'c__a2aMessageComposer'
            },
            state: {
                c__agentUrl: this.agentUrl,
                c__agentName: this.agentCard?.name
            }
        });
    }
    
    handleViewTasks() {
        // Navigate to task list
        this[NavigationMixin.Navigate]({
            type: 'standard__objectPage',
            attributes: {
                objectApiName: 'A2A_Task__c',
                actionName: 'list'
            }
        });
    }
    
    showToast(title, message, variant) {
        const event = new ShowToastEvent({
            title: title,
            message: message,
            variant: variant
        });
        this.dispatchEvent(event);
    }
    
    // Computed properties
    get hasCapabilities() {
        return this.agentCard?.capabilities != null;
    }
    
    get hasSkills() {
        return this.agentCard?.skills && this.agentCard.skills.length > 0;
    }
    
    get hasInterfaces() {
        return this.agentCard?.interfaces && this.agentCard.interfaces.length > 0;
    }
    
    get streamingVariant() {
        return this.agentCard?.capabilities?.streaming ? 'success' : 'neutral';
    }
    
    get pushNotificationVariant() {
        return this.agentCard?.capabilities?.pushNotifications ? 'success' : 'neutral';
    }
    
    get disableSendMessage() {
        return !this.agentCard || this.isLoading;
    }
}