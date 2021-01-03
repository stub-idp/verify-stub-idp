package stubidp.utils.common.logging;

class LevelLoggerFactory<T> {
    public LevelLogger<T> createLevelLogger(Class<T> clazz) {
        return LevelLogger.getLevelLogger(clazz);
    }
}
