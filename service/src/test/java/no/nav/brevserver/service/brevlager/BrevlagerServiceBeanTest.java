package no.nav.brevserver.service.brevlager;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.joark.JoarkService;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import no.nav.brevserver.service.config.AbstractTest;
import no.nav.brevserver.service.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.service.converter.VoTilBrevstatusConverter;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalstatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_FERDIG;
import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_KLADD;
import static no.nav.brevserver.core.vo.FilType.DOCX;
import static no.nav.brevserver.core.vo.FilType.PDF;
import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrevlagerServiceBeanTest extends AbstractTest {

	private static final byte[] BREVDATA2 = "Hest er best ingen protest".getBytes();

	private static final String PENSJON_SYSTEMID = "PE2";
	private static final String TOKEN = "12345";

	@MockitoBean
	private VoTilBrevstatusConverter voTilBrevstatusConverter;
	@MockitoBean
	private BrevstatusService brevstatusServiceMock;
	@MockitoBean
	private BrevstatusTilVoConverter brevstatusTilVoConverter;
	@MockitoBean
	private BrevtilgangService brevtilgangServiceMock;
	@MockitoBean
	private JoarkService joarkServiceMock;
	@Autowired
	private BrevlagerService brevlagerService;

	@Test
	public void shouldLagreNyttBrevAndVerifyLagret() throws Exception {
		BrevStatusVO brevStatusVO = defaultBrevStatus().build();
		Brevstatus brevStatus = defaultBrevstatusDomain().build();
		BrevVO brev = defaultBrev().build();
		when(voTilBrevstatusConverter.convert(brevStatusVO)).thenReturn(brevStatus);
		when(brevstatusServiceMock.lagreBrevStatus(brevStatusVO)).thenReturn(null);
		Journalpost journalpost = new Journalpost();
		Journalstatus journalstatus = new Journalstatus();
		journalstatus.setKode("OPPRETTET");
		journalpost.setJournalstatus(journalstatus);

		BrevStatusVO oldBrevStatus = brevlagerService.lagreBrev(brev, brevStatusVO);

		verify(brevstatusServiceMock).lagreBrevStatus(brevStatusVO);
		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);

		assertThat(oldBrevStatus).isNull();
		assertThat(persistedBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(persistedBrev.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(persistedBrev.getContentType()).isEqualTo(RTF.getContentType());
		assertThat(persistedBrev.getLagerStatus()).isEqualTo(BREVLAGER_STATUS_KLADD);
		assertThat(persistedBrev.getBrukerID()).isEqualTo(BRUKERID);
		assertThat(persistedBrev.getBrevdata()).isEqualTo(BREVDATA);
	}

	@Test
	public void shouldOppdatereEksisterendeBrevAndVerifyOppdatert() throws Exception {
		brevlagerService.lagreBrev(defaultBrev().build(), new BrevStatusVO());
		BrevStatusVO initialBrevStatus = defaultBrevStatus().build();
		initialBrevStatus.setToken(TOKEN);
		Brevstatus brevStatus = defaultBrevstatusDomain().build();
		BrevVO updatedBrev = defaultBrev().contentType(PDF.getContentType()).brevdata(BREVDATA2).build();
		when(brevstatusServiceMock.lagreBrevStatus(initialBrevStatus)).thenReturn(initialBrevStatus);
		when(brevstatusTilVoConverter.convert(brevStatus)).thenReturn(initialBrevStatus);
		when(voTilBrevstatusConverter.convert(initialBrevStatus)).thenReturn(brevStatus);

		BrevStatusVO oldBrevStatus = brevlagerService.lagreBrev(updatedBrev, initialBrevStatus);

		assertThat(oldBrevStatus).isEqualTo(initialBrevStatus);

		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(persistedBrev.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(persistedBrev.getContentType()).isEqualTo(PDF.getContentType());
		assertThat(persistedBrev.getLagerStatus()).isEqualTo(BREVLAGER_STATUS_KLADD);
		assertThat(persistedBrev.getBrukerID()).isEqualTo(BRUKERID);
		assertThat(persistedBrev.getBrevdata()).isEqualTo(BREVDATA2);
	}

	@Test
	public void shouldThrowExceptionIfBrevStatusIsFerdig() throws BrevTechnicalException {
		var brev = defaultBrev().lagerStatus(BREVLAGER_STATUS_FERDIG).build();
		var status = new BrevStatusVO();

		brevlagerService.lagreBrev(brev, status);

		assertThatExceptionOfType(BrevTechnicalException.class)
				.isThrownBy(() -> brevlagerService.lagreBrev(brev, status))
				.withMessage("Brevet har status = 'FERDIG' og kan ikke endres");
	}

	@Test
	public void shouldFerdigstilleNyttBrevAndVerifyLagret() throws Exception {
		brevRepository.deleteAll();
		BrevStatusVO brevStatusVO = defaultBrevStatus().build();
		brevStatusVO.setSystemID("BR10");
		brevStatusVO.setToken(TOKEN);
		Brevstatus brevStatus = defaultBrevstatusDomain().build();
		BrevVO redBrev = defaultBrev().contentType(RTF.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(BREVLAGER_STATUS_FERDIG).contentType(PDF.getContentType()).build();
		when(voTilBrevstatusConverter.convert(brevStatusVO)).thenReturn(brevStatus);

		brevlagerService.ferdigstillBrev(brevStatusVO, redBrev, pdfBrev);

		verify(brevstatusServiceMock).lagreBrevStatus(brevStatusVO);

		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(persistedBrev.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(persistedBrev.getContentType()).isEqualTo(PDF.getContentType());
		assertThat(persistedBrev.getLagerStatus()).isEqualTo(BREVLAGER_STATUS_FERDIG);
		assertThat(persistedBrev.getBrukerID()).isEqualTo(BRUKERID);
		assertThat(persistedBrev.getBrevdata()).isEqualTo(BREVDATA);
	}

	@Test
	public void shouldFerdigstilleEksisterendeBrevAndVerifyLagret() throws Exception {
		brevRepository.deleteAll();
		BrevVO redBrev = defaultBrev().contentType(RTF.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(BREVLAGER_STATUS_FERDIG).contentType(PDF.getContentType()).brevdata(BREVDATA2).build();
		BrevStatusVO brevStatusVO = BrevStatusVO.builder()
				.systemID("BR12")
				.brevreferanse(BREVREFERANSE)
				.token(TOKEN)
				.build();

		brevlagerService.lagreBrev(redBrev, brevStatusVO);
		brevlagerService.ferdigstillBrev(brevStatusVO, redBrev, pdfBrev);
		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);

		assertThat(persistedBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(persistedBrev.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(persistedBrev.getContentType()).isEqualTo(PDF.getContentType());
		assertThat(persistedBrev.getLagerStatus()).isEqualTo(BREVLAGER_STATUS_FERDIG);
		assertThat(persistedBrev.getBrukerID()).isEqualTo(BRUKERID);
		assertThat(persistedBrev.getBrevdata()).isEqualTo(BREVDATA2);
	}

	@Test
	public void shouldHandleDocx() throws Exception {
		BrevVO redBrev = defaultBrev().contentType(DOCX.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(BREVLAGER_STATUS_FERDIG).contentType(PDF.getContentType()).brevdata(BREVDATA2).build();
		BrevStatusVO brevStatusVO = BrevStatusVO.builder().systemID("BR12").brevreferanse(BREVREFERANSE).token(TOKEN).build();

		brevlagerService.lagreBrev(redBrev, brevStatusVO);

		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getContentType()).isEqualTo(DOCX.getContentType());

		brevlagerService.ferdigstillBrev(brevStatusVO, redBrev, pdfBrev);

		persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(persistedBrev.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(persistedBrev.getContentType()).isEqualTo(PDF.getContentType());
		assertThat(persistedBrev.getLagerStatus()).isEqualTo(BREVLAGER_STATUS_FERDIG);
		assertThat(persistedBrev.getBrukerID()).isEqualTo(BRUKERID);
		assertThat(persistedBrev.getBrevdata()).isEqualTo(BREVDATA2);
	}

	@Test
	void shouldHentDokumentFraJoarkWhenPensjon() throws BrevFunctionalException, BrevTechnicalException {
		when(brevtilgangServiceMock.sjekkTilgang(eq(PENSJON_SYSTEMID), eq(BREVREFERANSE), eq(TOKEN))).thenReturn(true);
		when(joarkServiceMock.hentDokument(eq(BREVREFERANSE))).thenReturn(BrevVO.builder().lagerStatus("A").contentType(RTF.getContentType()).build());
		BrevStatusVO brevStatusVO = BrevStatusVO.builder().systemID(PENSJON_SYSTEMID).brevreferanse(BREVREFERANSE).token(TOKEN).build();

		BrevVO brevVO = brevlagerService.hentDokumentFromBrevlagerOrJoark(brevStatusVO);

		assertThat(sha256Hex(brevVO.getBrevdata())).isEqualTo("817a0c81cecd1871e5acc07ec07fa85f31482f343b72db6402359759c9fdecda");
		assertThat(brevVO.getContentType()).isEqualTo(PDF.getContentType());
	}

	private BrevStatusVO.BrevStatusVOBuilder defaultBrevStatus() {
		return BrevStatusVO.builder()
				.brevreferanse(BREVREFERANSE)
				.systemID(SYSTEM_ID)
				.returKoe(RETURKOE)
				.bestillerBrukerID(BESTILLER_ID)
				.brevmal(BREVMAL)
				.status(STATUS)
				.format(FORMAT)
				.skrivertype(SKRIVERTYPE)
				.skriver(SKRIVER)
				.arkiver(ARKIVER)
				.skuff(SKUFF);
	}

	private Brevstatus.BrevstatusBuilder defaultBrevstatusDomain() {
		return Brevstatus.builder()
				.id(BrevreferanseSystemCompositeId.builder().brevreferanse(BREVREFERANSE).systemId(SYSTEM_ID).build())
				.returKoe(RETURKOE)
				.bestillerBrukerID(BRUKERID)
				.brevmal(BREVMAL)
				.status(STATUS)
				.format(FORMAT)
				.skrivertype(SKRIVERTYPE)
				.skriver(SKRIVER)
				.arkiver(ARKIVER)
				.skuff(SKUFF);
	}

}