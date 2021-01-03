package stubidp.utils.rest.jerseyclient;

import com.codahale.metrics.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import java.util.function.Supplier;

import static java.lang.String.format;

public class RetryCommand<T> {

    private static final Logger LOG = LoggerFactory.getLogger(RetryCommand.class);

    private int retryCounter;
    private final int maxRetries;
    private final Class<? extends Exception> exceptionClass;
    private final Meter retryMeter;

    RetryCommand(int maxRetries) {
        this(maxRetries, Exception.class, null);
    }

    RetryCommand(int maxRetries, Class<? extends Exception> exceptionClass) {
        this(maxRetries, exceptionClass, null);
    }

    RetryCommand(int maxRetries, Meter retryMeter) {
        this(maxRetries, Exception.class, retryMeter);
    }

    private RetryCommand(int maxRetries, Class<? extends Exception> exceptionClass, Meter retryMeter) {
        this.exceptionClass = exceptionClass;
        this.retryMeter = retryMeter;
        this.retryCounter = 0;
        this.maxRetries = maxRetries;
    }

    T execute(Supplier<T> function) {
        try {
            return function.get();
        } catch (Exception e) {
            if(!exceptionClass.isInstance(e)) {
                throw e;
            }
            if(retryCounter >= maxRetries) {
                return failAndStopRetry(e, function);
            }

            if(0 == retryCounter) {
                logInitialFail(e, function);
            } else {
                logRetryFail(e, function);
            }

            retryCounter++;
            if(null != retryMeter) {
                retryMeter.mark();
            }

            return execute(function);
        }
    }

    private void logRetryFail(Exception e, Supplier<T> function) {
        LOG.debug(format("Command %s failed on retry %d of %d.", function, retryCounter, maxRetries), e);
    }

    private void logInitialFail(Exception e, Supplier<T> function) {
        LOG.debug(format("Command %s failed, will be retried %d times.", function, maxRetries), e);
    }

    private T failAndStopRetry(Exception e, Supplier<T> function) {
        LOG.debug("Max retries exceeded for " + function.toString());
        throw new ProcessingException(format("Command %s failed on all of %d retries.", function, maxRetries), e);
    }
}
