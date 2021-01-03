package stubidp.utils.common.logging;

public class LevelLoggerFactory<T> {
    public LevelLogger<T> createLevelLogger(Class<T> clazz) {
        return LevelLogger.getLevelLogger(clazz);
    }
}
