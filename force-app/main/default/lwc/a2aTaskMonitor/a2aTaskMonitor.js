import { LightningElement, api, track, wire } from 'lwc';
import { refreshApex } from '@salesforce/apex';
import { ShowToastEvent } from 'lightning/platformShowToastEvent';
import { subscribe, unsubscribe, onError } from 'lightning/empApi';
import getTasks from '@salesforce/apex/A2ATaskMonitorController.getTasks';
import getTaskDetails from '@salesforce/apex/A2ATaskMonitorController.getTaskDetails';
import cancelTask from '@salesforce/apex/A2ATaskMonitorController.cancelTask';

const columns = [
    { label: 'Task ID', fieldName: 'taskId', type: 'text' },
    { label: 'Status', fieldName: 'status', type: 'text', cellAttributes: {
        class: { fieldName: 'statusClass' }
    }},
    { label: 'Created', fieldName: 'createdDate', type: 'date', typeAttributes: {
        year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit'
    }},
    { label: 'Updated', fieldName: 'lastModifiedDate', type: 'date', typeAttributes: {
        year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit'
    }},
    { type: 'action', typeAttributes: { rowActions: [
        { label: 'View', name: 'view' },
        { label: 'Cancel', name: 'cancel' }
    ]}}
];

export default class A2aTaskMonitor extends LightningElement {
    @api recordId;
    @track tasks = [];
    @track filteredTasks = [];
    @track selectedTask;
    @track activeFilter = 'ALL';
    
    columns = columns;
    isLoading = false;
    subscription = {};
    channelName = '/event/A2A_Task_Event__e';
    
    wiredTasksResult;
    
    @wire(getTasks)
    wiredTasks(result) {
        this.wiredTasksResult = result;
        if (result.data) {
            this.tasks = this.processTaskData(result.data);
            this.applyFilter();
        } else if (result.error) {
            this.showToast('Error', 'Failed to load tasks', 'error');
        }
    }
    
    connectedCallback() {
        this.handleSubscribe();
        this.registerErrorListener();
    }
    
    disconnectedCallback() {
        this.handleUnsubscribe();
    }
    
    handleSubscribe() {
        const messageCallback = (response) => {
            // Handle platform event
            this.handleTaskEvent(response);
        };
        
        subscribe(this.channelName, -1, messageCallback).then(response => {
            this.subscription = response;
        });
    }
    
    handleUnsubscribe() {
        unsubscribe(this.subscription, response => {
            console.log('Unsubscribed from channel:', response);
        });
    }
    
    registerErrorListener() {
        onError(error => {
            console.error('EMP API error:', error);
        });
    }
    
    handleTaskEvent(event) {
        // Refresh the task list when we receive an event
        this.handleRefresh();
    }
    
    handleRefresh() {
        this.isLoading = true;
        refreshApex(this.wiredTasksResult)
            .then(() => {
                this.isLoading = false;
                this.showToast('Success', 'Tasks refreshed', 'success');
            })
            .catch(error => {
                this.isLoading = false;
                this.showToast('Error', 'Failed to refresh tasks', 'error');
            });
    }
    
    handleFilterClick(event) {
        this.activeFilter = event.target.name;
        this.applyFilter();
    }
    
    applyFilter() {
        if (this.activeFilter === 'ALL') {
            this.filteredTasks = this.tasks;
        } else {
            this.filteredTasks = this.tasks.filter(task => task.status === this.activeFilter);
        }
    }
    
    handleRowAction(event) {
        const action = event.detail.action;
        const row = event.detail.row;
        
        switch (action.name) {
            case 'view':
                this.viewTask(row);
                break;
            case 'cancel':
                this.cancelTaskAction(row);
                break;
        }
    }
    
    viewTask(task) {
        this.isLoading = true;
        getTaskDetails({ taskId: task.taskId })
            .then(result => {
                this.selectedTask = {
                    ...result,
                    statusVariant: this.getStatusVariant(result.status),
                    artifactsJson: result.artifacts ? JSON.stringify(result.artifacts, null, 2) : null
                };
                this.isLoading = false;
            })
            .catch(error => {
                this.isLoading = false;
                this.showToast('Error', 'Failed to load task details', 'error');
            });
    }
    
    cancelTaskAction(task) {
        if (task.status === 'COMPLETED' || task.status === 'FAILED' || task.status === 'CANCELLED') {
            this.showToast('Warning', 'Cannot cancel a task that is already ' + task.status.toLowerCase(), 'warning');
            return;
        }
        
        this.isLoading = true;
        cancelTask({ taskId: task.taskId })
            .then(result => {
                this.isLoading = false;
                this.showToast('Success', 'Task cancelled successfully', 'success');
                this.handleRefresh();
            })
            .catch(error => {
                this.isLoading = false;
                this.showToast('Error', 'Failed to cancel task', 'error');
            });
    }
    
    handleCancelTask() {
        this.cancelTaskAction(this.selectedTask);
        this.closeModal();
    }
    
    closeModal() {
        this.selectedTask = null;
    }
    
    showToast(title, message, variant) {
        const event = new ShowToastEvent({
            title: title,
            message: message,
            variant: variant
        });
        this.dispatchEvent(event);
    }
    
    processTaskData(data) {
        return data.map(task => ({
            ...task,
            statusClass: 'slds-text-color_' + this.getStatusColor(task.status)
        }));
    }
    
    getStatusColor(status) {
        switch(status) {
            case 'SUBMITTED': return 'default';
            case 'RUNNING': return 'info';
            case 'COMPLETED': return 'success';
            case 'FAILED': return 'error';
            case 'CANCELLED': return 'warning';
            default: return 'default';
        }
    }
    
    getStatusVariant(status) {
        switch(status) {
            case 'SUBMITTED': return 'neutral';
            case 'RUNNING': return 'info';
            case 'COMPLETED': return 'success';
            case 'FAILED': return 'error';
            case 'CANCELLED': return 'warning';
            default: return 'neutral';
        }
    }
    
    get hasTasks() {
        return this.filteredTasks && this.filteredTasks.length > 0;
    }
    
    get canCancelTask() {
        return this.selectedTask && 
               (this.selectedTask.status === 'SUBMITTED' || this.selectedTask.status === 'RUNNING');
    }
    
    get statusFilters() {
        return [
            { value: 'ALL', label: 'All', variant: 'neutral', icon: 'standard:all', iconVariant: 'neutral' },
            { value: 'SUBMITTED', label: 'Submitted', variant: 'neutral', icon: 'utility:clock', iconVariant: 'neutral' },
            { value: 'RUNNING', label: 'Running', variant: 'info', icon: 'utility:spinner', iconVariant: 'info' },
            { value: 'COMPLETED', label: 'Completed', variant: 'success', icon: 'utility:success', iconVariant: 'success' },
            { value: 'FAILED', label: 'Failed', variant: 'error', icon: 'utility:error', iconVariant: 'error' },
            { value: 'CANCELLED', label: 'Cancelled', variant: 'warning', icon: 'utility:warning', iconVariant: 'warning' }
        ];
    }
}