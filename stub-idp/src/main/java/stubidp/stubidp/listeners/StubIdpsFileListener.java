package stubidp.stubidp.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.repositories.IdpStubsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

@Singleton
public class StubIdpsFileListener implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(StubIdpsFileListener.class);

    private final Path stubIdpsYmlPath;
    private final IdpStubsRepository idpStubsRepository;
    private final WatchKey watchKey;

    @Inject
    public StubIdpsFileListener(String stubIdpsYmlFile,
                                IdpStubsRepository idpStubsRepository) {
        this.stubIdpsYmlPath = Path.of(stubIdpsYmlFile);
        this.idpStubsRepository = idpStubsRepository;

        try {
            final WatchService watchService = FileSystems.getDefault().newWatchService();
            this.watchKey = stubIdpsYmlPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.idpStubsRepository.load(stubIdpsYmlPath.toFile().getAbsolutePath());
    }

    @Override
    public void run() {
        if(watchKey.pollEvents().stream().anyMatch(x -> x.context().equals(stubIdpsYmlPath.getFileName()))) {
            LOG.info("Triggered file change on file: " + stubIdpsYmlPath.getFileName());
            idpStubsRepository.load(stubIdpsYmlPath.toFile().getAbsolutePath());
        }
    }
}
