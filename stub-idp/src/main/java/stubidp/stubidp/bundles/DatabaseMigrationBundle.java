package stubidp.stubidp.bundles;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.repositories.jdbc.migrations.DatabaseMigrationRunner;

public class DatabaseMigrationBundle implements ConfiguredBundle<StubIdpConfiguration> {
    @Override
    public void run(StubIdpConfiguration configuration, Environment environment) {
        new DatabaseMigrationRunner().runMigration(configuration.getDatabaseConfiguration().getUrl());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
