package stubidp.utils.rest.configuration;

public class ServiceStatus {
    private static ServiceStatus instance;
    private static Object lock = new Object();
    private volatile boolean serverStatus = true;

    private ServiceStatus() {
    }

    public static ServiceStatus getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new ServiceStatus();
            }
            return instance;
        }
    }

    public boolean isServerStatusOK() {
        return serverStatus;
    }

    public void setServiceStatus(boolean serverStatus) {
        this.serverStatus = serverStatus;
    }
}
