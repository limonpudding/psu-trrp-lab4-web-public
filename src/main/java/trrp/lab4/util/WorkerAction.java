package trrp.lab4.util;

public enum WorkerAction {

    NEW_WORKER,
    CREATE_CONNECTION,
    SEND_OGRN_DATA;

    public static WorkerAction getByName(String name) {
        for (WorkerAction workerAction:WorkerAction.values()) {
            if (workerAction.name().equals(name)) {
                return workerAction;
            }
        }
        return null;
    }
}