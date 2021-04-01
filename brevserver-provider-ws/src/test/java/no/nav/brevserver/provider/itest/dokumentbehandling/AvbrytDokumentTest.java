package no.nav.brevserver.provider.itest.dokumentbehandling;

import no.nav.brevserver.provider.itest.AbstractProviderTest;
import no.nav.brevserver.provider.support.DokumentbehandlingProvider;
import no.nav.brevserver.querydsl.TBrevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.jms.JMSAccessor;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.AvbrytDokumentRequest;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for avbrytDokument operation dokumentbehandling
 *
 * @author Nabil Fario, Visma Consulting
 */

@PrepareForTest({JMSAccessor.class})
public class AvbrytDokumentTest extends AbstractProviderTest {

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String CONTENT_TYPE_RTF = FilType.RTF.getContentType();
	private static final byte[] BREVDATA = "Dette er en rtf".getBytes();
	private static final String SYSTEM_ID_BI = SystemType.BI.toString();
	private DokumentbehandlingPortType dokumentBehandlingProvider;
	private AvbrytDokumentRequest avbrytDokumentRequest;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		insertBrevInDatabase();
		dokumentBehandlingProvider = new DokumentbehandlingProvider();
		avbrytDokumentRequest = createAvbrytDokumentRequest();
	}

	@Test
	public void shouldAvbryteBidragDokument() throws Exception {
		avbrytDokumentRequest = createAvbrytDokumentRequest();

		dokumentBehandlingProvider.avbrytDokument(avbrytDokumentRequest);

		TBrevstatus brevstatus = getBrevstatus(BREVREFERANSE, SYSTEM_ID_BI);
		assertThat(brevstatus.getStatus(), is(Konstanter.BREVSTATUS_AVBRUTT));
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("brevStatus.systemID must be set");

		dokumentBehandlingProvider.avbrytDokument(new AvbrytDokumentRequest());
	}

	private AvbrytDokumentRequest createAvbrytDokumentRequest() {
		AvbrytDokumentRequest avbrytDokumentRequest = new AvbrytDokumentRequest();
		avbrytDokumentRequest.setBrevreferanse(BREVREFERANSE);
		avbrytDokumentRequest.setSystemId(SYSTEM_ID_BI);
		avbrytDokumentRequest.setToken(TOKEN);
		return avbrytDokumentRequest;
	}

	private void insertBrevInDatabase() throws java.sql.SQLException {
		insertBrevtilgang(BREVREFERANSE, TOKEN, SYSTEM_ID_BI);
		insertBrevstatus(BREVREFERANSE, SYSTEM_ID_BI, Konstanter.BREVSTATUS_LAGRET_KLADD);
		insertBrevlager(BREVREFERANSE, SYSTEM_ID_BI, Konstanter.BREVLAGER_STATUS_KLADD, CONTENT_TYPE_RTF, BREVDATA);
	}

}