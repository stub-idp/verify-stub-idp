package stubidp.stubidp.repositories.jdbc;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.jdbi.v3.core.Jdbi;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.SessionRepositoryBase;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class JDBIIdpSessionRepository extends SessionRepositoryBase<IdpSession> implements IdpSessionRepository {

	private final SessionDBCollector sessionDBCollector;

	@Inject
	public JDBIIdpSessionRepository(Jdbi jdbi) {
		super(IdpSession.class, jdbi);
		this.sessionDBCollector = new SessionDBCollector(this).register();
	}

	public JDBIIdpSessionRepository(Jdbi jdbi, boolean withMetrics) {
		super(IdpSession.class, jdbi);
		if(withMetrics) {
			this.sessionDBCollector = new SessionDBCollector(this).register();
		} else {
			this.sessionDBCollector = null;
		}
	}

	public SessionId createSession(IdpSession session) {
		return insertSession(session.getSessionId(), session);
	}

	/**
	 * This is here instead of anywhere else because:
	 *   * this can't be instantiated statically (it needs `this`)
	 *   * all sessions for idp + eidas are in the same table
	 *   * this is a singleton and will be present even if eidas is off
	 */
	private static class SessionDBCollector extends Collector {
		private final JDBIIdpSessionRepository jdbiIdpSessionRepository;

		SessionDBCollector(JDBIIdpSessionRepository jdbiIdpSessionRepository) {
			this.jdbiIdpSessionRepository = jdbiIdpSessionRepository;
		}

		@Override
		public List<MetricFamilySamples> collect() {
			GaugeMetricFamily sessionsGauge = new GaugeMetricFamily("stubidp_db_sessions_total", "Total number of active sessions (idp + eidas).", jdbiIdpSessionRepository.countSessionsInDatabase());
			return List.of(sessionsGauge);
		}
	}
}
