package stubidp.utils.common.logging;

public class LevelLoggerFactory<T> {
    @SuppressWarnings("rawtypes")
    public LevelLogger createLevelLogger(Class<T> clazz) {
        return LevelLogger.getLevelLogger(clazz);
    }
}
