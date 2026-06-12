package no.nav.brevserver.ws.dokumentbehandling;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import no.nav.brevserver.AbstractBrevserviceTest;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.ws.dokumentbehandling.support.LagreDokumentRequestMapper;
import no.nav.brevserver.ws.dokumentbehandling.support.impl.DefaultLagreDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_KLADD;
import static no.nav.brevserver.core.constants.Konstanter.BREVSTATUS_LAGRET_KLADD;
import static no.nav.brevserver.core.constants.SystemType.PE;
import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
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

	@Test
	public void shouldLagreKladdDokumentRtf() throws Exception {
		dokumentbehandlingProvider.lagreDokument(createLagreDokumentRequest());

		verify(brevlagerService).lagreDokument(brevCaptor.capture(), brevStatusCaptor.capture(), systemTypeCaptor.capture());

		var brev = brevCaptor.getValue();
		assertThat(brev.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brev.getContentType()).isEqualTo(CONTENT_TYPE_RTF);
		assertThat(brev.getBrevdata()).isEqualTo(DOKUMENTDATA_RTF);
		assertThat(brev.getBrukerID()).isEqualTo(BRUKER_ID);
		assertThat(brev.getLagerStatus()).isEqualTo(BREVLAGER_STATUS_KLADD);

		var brevstatus = brevStatusCaptor.getValue();
		assertThat(brevstatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevstatus.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brevstatus.getToken()).isEqualTo(TOKEN);
		assertThat(brevstatus.getBrevmal()).isEqualTo(MALPAKKE);
		assertThat(brevstatus.getReturKoe()).isEqualTo(KVITTERINGSKOE);
		assertThat(brevstatus.getStatus()).isEqualTo(BREVSTATUS_LAGRET_KLADD);

		assertThat(systemTypeCaptor.getValue()).isEqualTo(PE);
	}

	@Test
	public void shouldThrowExceptionIfValidationFails() {
		assertThatNullPointerException()
				.isThrownBy(() -> dokumentbehandlingProvider.lagreDokument(new LagreDokumentRequest()))
				.withMessage("brevStatus.systemID must be set");
	}

	@Test
	public void shouldThrowExceptionIfKvitteringskoeMissingAndNewDokumentSet() {
		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest();
		lagreDokumentRequest.setNyttDokument(true);
		lagreDokumentRequest.setKvitteringskoe(null);

		assertThatNullPointerException()
				.isThrownBy(() -> dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest))
				.withMessage("brevStatus.returKoe must be set if newDocument is true");
	}

	@Test
	public void shouldThrowExceptionIfMalpakkeMissingAndNewDokumentSet() {
		LagreDokumentRequest lagreDokumentRequest = createLagreDokumentRequest();
		lagreDokumentRequest.setNyttDokument(true);
		lagreDokumentRequest.setMalpakke(null);

		assertThatNullPointerException()
				.isThrownBy(() -> dokumentbehandlingProvider.lagreDokument(lagreDokumentRequest))
				.withMessage("brevStatus.brevmal must be set if newDocument is true");
	}

	private LagreDokumentRequest createLagreDokumentRequest() {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();
		lagreDokumentRequest.setBrukerId(BRUKER_ID);
		lagreDokumentRequest.setSystemId(SYSTEM_ID);
		lagreDokumentRequest.setBrevreferanse(BREVREFERANSE);
		lagreDokumentRequest.setDokumentData(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA_RTF, RTF.getContentType())));
		lagreDokumentRequest.setToken(TOKEN);
		lagreDokumentRequest.setMalpakke(MALPAKKE);
		lagreDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		return lagreDokumentRequest;
	}

}