package no.nav.brevserver.dokumentbehandling;

import no.nav.brevserver.AbstractBrevserviceTest;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.nais.DokumentbehandlingProvider;
import no.nav.brevserver.nais.support.LagreDokumentRequestMapper;
import no.nav.brevserver.nais.support.impl.DefaultLagreDokumentRequestMapper;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.exception.BrevException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for DefaultLagreDokumentService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
public class DefaultLagreDokumentServiceTest extends AbstractBrevserviceTest {

	@Mock
	private BrevlagerService brevlagerService;
	@Spy
	private LagreDokumentRequestMapper lagreDokumentRequestMapper = new DefaultLagreDokumentRequestMapper();

	@InjectMocks
	private DokumentbehandlingProvider dokumentbehandlingProvider;

	@Captor
	private ArgumentCaptor<BrevVO> brevCaptor;

	@Captor
	private ArgumentCaptor<BrevStatusVO> brevStatusCaptor;

	@Captor
	private ArgumentCaptor<SystemType> systemTypeCaptor;

	@Rule
	public ExpectedException thrown = ExpectedException.none();


	@Test
	public void shouldLagreKladdDokumentRtf() throws Exception {
		dokumentbehandlingProvider.lagreDokument(createLagreDokumentRequest(CONTENT_TYPE_RTF));

		verify(brevlagerService).lagreDokument(
				brevCaptor.capture(),
				brevStatusCaptor.capture(),
				systemTypeCaptor.capture()
		);

		assertBrev(brevCaptor.getValue(), CONTENT_TYPE_RTF, Konstanter.BREVLAGER_STATUS_KLADD);
		assertBrevStatus(brevStatusCaptor.getValue(), Konstanter.BREVSTATUS_LAGRET_KLADD);
		assertThat(systemTypeCaptor.getValue(), is(SystemType.PE));
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() throws BrevException {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.systemID must be set");

		dokumentbehandlingProvider.lagreDokument(new LagreDokumentRequest());
	}


	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() throws BrevException, IOException {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.returKoe must be set if newDocument is true");

		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest(CONTENT_TYPE_RTF);
		lagreDokumentRequest.setNyttDokument(true);
		lagreDokumentRequest.setKvitteringskoe(null);

		dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() throws BrevException, IOException {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("brevStatus.brevmal must be set if newDocument is true");

		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest(CONTENT_TYPE_RTF);
		lagreDokumentRequest.setNyttDokument(true);
		lagreDokumentRequest.setMalpakke(null);

		dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest);
	}

	private void assertBrev(BrevVO brev, String contentType, String lagerStatus) {
		assertThat(brev.getSystemID(), is(SYSTEM_ID));
		assertThat(brev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brev.getContentType(), is(contentType));
		assertThat(brev.getBrevdata(), is(DOKUMENTDATA_RTF));
		assertThat(brev.getBrukerID(), is(BRUKER_ID));
		assertThat(brev.getLagerStatus(), is(lagerStatus));
	}

	private void assertBrevStatus(BrevStatusVO brevStatus, String status) {
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getBrevmal(), is(MALPAKKE));
		assertThat(brevStatus.getReturKoe(), is(KVITTERINGSKOE));
		assertThat(brevStatus.getStatus(), is(status));
	}

	private LagreDokumentRequest createLagreDokumentRequest(String contentType) throws IOException {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();
		lagreDokumentRequest.setBrukerId(BRUKER_ID);
		lagreDokumentRequest.setSystemId(SYSTEM_ID);
		lagreDokumentRequest.setBrevreferanse(BREVREFERANSE);
		lagreDokumentRequest.setDokumentData(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_RTF, FilType.RTF.getContentType())));
		lagreDokumentRequest.setToken(TOKEN);
		lagreDokumentRequest.setMalpakke(MALPAKKE);
		lagreDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		return lagreDokumentRequest;
	}


}
