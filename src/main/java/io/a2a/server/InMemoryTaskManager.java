package io.a2a.server;

public class InMemoryTaskManager implements TaskManager {
//    private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();
//    private final Map<String, PushNotificationConfig> pushNotificationInfos = new ConcurrentHashMap<>();
//
//    @Override
//    public GetTaskResponse onGetTask(GetTaskRequest request) {
//        TaskQueryParams params = request.getParams();
//
//        Task task = tasks.get(params.id());
//        if (task == null) {
//            return new GetTaskResponse(request.getId(), new TaskNotFoundError());
//        }
//
//        synchronized (task) {
//            Task result = appendTaskHistory(task, params.historyLength());
//            return new GetTaskResponse(request.getId(), result);
//        }
//    }
//
//    @Override
//    public CancelTaskResponse onCancelTask(CancelTaskRequest request) {
//        TaskIdParams params = request.getParams();
//        Task task = tasks.get(params.id());
//        if (task == null) {
//            return new CancelTaskResponse(request.getId(), new TaskNotFoundError());
//        }
//
//        Task cancelled = updateTask(task, t -> new Task.Builder(t)
//                .status(new TaskStatus(CANCELED))
//                .build());
//
//        return new CancelTaskResponse(request.getId(), cancelled);
//    }
    //
    //    @Override
    //    public SendTaskResponse onSendTask(SendTaskRequest request) {
    //        Object taskId = request.getId();
    //        Task task = tasks.get(taskId);
    //
    //        if (task == null) {
    //            task = new Task.Builder()
    //                    .id(taskId.toString())
    //                    .sessionId(request.getParams().sessionId())
    //                    .history(List.of(request.getParams().message()))
    //                    .status(new TaskStatus(SUBMITTED))
    //                    .build();
    //            tasks.put(taskId.toString(), task);
    //        } else {
    //            Task finalTask = task;
    //            task = updateTask(task, t -> {
    //                List<Message> newHistory = finalTask.history() == null ? new ArrayList<>() : new ArrayList<>(finalTask.history());
    //                newHistory.add(request.getParams().message());
    //                return new Task.Builder(finalTask)
    //                        .history(newHistory)
    //                        .build();
    //            });
    //        }
    //
    //        performTask(taskId, task, request.getParams());
    //
    //        return new SendTaskResponse(taskId, task);
    //    }
//
//    @Override
//    public Object onSendTaskStreamingRequest(SendTaskStreamingRequest request) {
//        return null;
//    }
//
//    @Override
//    public SetTaskPushNotificationResponse onSetTaskPushNotification(SetTaskPushNotificationRequest request) {
//        TaskPushNotificationConfig params = request.getParams();
//        try {
//            setPushNotificationInfo(params.id(), params.pushNotificationConfig());
//        } catch (Exception e) {
//            return new SetTaskPushNotificationResponse(
//                    request.getId(),
//                    new InternalError("An error occurred while setting push notification info"));
//        }
//        return new SetTaskPushNotificationResponse(request.getId(), params);
//    }
//
//    @Override
//    public GetTaskPushNotificationResponse onGetTaskPushNotification(GetTaskPushNotificationRequest request) {
//        TaskIdParams params = request.getParams();
//        PushNotificationConfig config;
//        try {
//            config = getPushNotificationInfo(params.id());
//        } catch (Exception e) {
//            return new GetTaskPushNotificationResponse(
//                    params.id(),
//                    new InternalError("An error occurred while getting push notification info"));
//        }
//
//        return new GetTaskPushNotificationResponse(params.id(), new TaskPushNotificationConfig(params.id(), config));
//    }
//
//    @Override
//    public Object onResubscribeToTask(TaskResubscriptionRequest request) {
//        return null;
//    }
//
//    protected Task appendTaskHistory(Task task, int historyLength) {
//        List<Message> history = new ArrayList<>();
//        if (historyLength >= 0 && historyLength >= task.history().size()) {
//            return task;
//        }
//        if (historyLength >= 0) {
//            int from = task.history().size() - 1 - historyLength;
//            if (from < 0) {
//                from = 0;
//            }
//            history = task.history().subList(from, task.history().size());
//        }
//        Task newTask = new Task.Builder(task)
//                .history(history)
//                .build();
//        return newTask;
//    }
//
//    protected PushNotificationConfig setPushNotificationInfo(String taskId, PushNotificationConfig notificationConfig) {
//        Task task = tasks.get(taskId);
//        if (task == null) {
//            throw new IllegalStateException("No task found for " + taskId);
//        }
//        return pushNotificationInfos.put(taskId, notificationConfig);
//    }
//
//    protected PushNotificationConfig getPushNotificationInfo(String taskId) {
//        Task task = tasks.get(taskId);
//        if (task == null) {
//            throw new IllegalStateException("Task not found for " + taskId);
//        }
//        return pushNotificationInfos.get(taskId);
//    }
//
//    protected Task updateTask(Task original, Function<Task, Task> updater) {
//        while (true) {
//            Task updated = updater.apply(original);
//            Task old = tasks.put(original.id(), updated);
//            if (old == original) {
//                return updated;
//            }
//            original = old;
//        }
//    }
//
//    private Task performTask(Object taskId, Task task, TaskSendParams params) {
//        // TODO
//        return task;
//    }
//
//    void addTask(Task task) {
//        // Hook for tests to add a task
//        tasks.put(task.id(), task);
//    }

}
