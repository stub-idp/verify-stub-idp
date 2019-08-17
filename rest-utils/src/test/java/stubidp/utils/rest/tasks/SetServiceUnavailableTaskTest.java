package stubidp.utils.rest.tasks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.utils.rest.configuration.ServiceStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class SetServiceUnavailableTaskTest {

    ServiceStatus instance;
    SetServiceUnavailableTask setServiceUnavailableTask;

    @BeforeEach
    public void setUp() {
        instance = ServiceStatus.getInstance();
        setServiceUnavailableTask = new SetServiceUnavailableTask(instance);
    }

    @Test
    public void shouldCreateSetServiceTaskAndReturnFalseWhenExecuted() throws Exception {
        assertThat(setServiceUnavailableTask.getName()).isEqualTo("set-service-unavailable");
        assertThat(instance.isServerStatusOK()).isEqualTo(true);
        setServiceUnavailableTask.execute(null, null);
        assertThat(instance.isServerStatusOK()).isEqualTo(false);
    }

    @AfterEach
    public void tearDown(){
        instance.setServiceStatus(true);
    }
}