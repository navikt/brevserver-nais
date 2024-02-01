package no.nav.brevserver.service.brevlager;

import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.exception.BrevFunctionalException;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
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
import org.springframework.boot.test.mock.mockito.MockBean;

import static no.nav.brevserver.core.vo.FilType.PDF;
import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrevlagerServiceBeanTest extends AbstractTest {
	private static final byte[] BREVDATA2 = "Hest er best ingen protest".getBytes();

	private static final String PENSJON_SYSTEMID = "PE2";
	private static final String TOKEN = "12345";

	@MockBean
	private VoTilBrevstatusConverter voTilBrevstatusConverter;
	@MockBean
	private BrevstatusService brevstatusServiceMock;
	@MockBean
	private BrevstatusTilVoConverter brevstatusTilVoConverter;
	@MockBean
	private BrevtilgangService brevtilgangServiceMock;
	@MockBean
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

		assertThat(oldBrevStatus, nullValue());
		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.RTF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_KLADD));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA));
	}

	@Test
	public void shouldOppdatereEksisterendeBrevAndVerifyOppdatert() throws Exception {
		brevlagerService.lagreBrev(defaultBrev().build(), new BrevStatusVO());
		BrevStatusVO initialBrevStatus = defaultBrevStatus().build();
		initialBrevStatus.setToken(TOKEN);
		Brevstatus brevStatus = defaultBrevstatusDomain().build();
		BrevVO updatedBrev = defaultBrev().contentType(FilType.PDF.getContentType()).brevdata(BREVDATA2).build();
		when(brevstatusServiceMock.lagreBrevStatus(initialBrevStatus)).thenReturn(initialBrevStatus);
		when(brevstatusTilVoConverter.convert(brevStatus)).thenReturn(initialBrevStatus);
		when(voTilBrevstatusConverter.convert(initialBrevStatus)).thenReturn(brevStatus);
		BrevStatusVO oldBrevStatus = brevlagerService.lagreBrev(updatedBrev, initialBrevStatus);

		assertThat(oldBrevStatus, is(initialBrevStatus));

		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_KLADD));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA2));
	}

	@Test
	public void shouldThrowExceptionIfBrevStatusIsFerdig() throws BrevTechnicalException {
		var brev = defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG).build();
		var status =  new BrevStatusVO();

		brevlagerService.lagreBrev(brev, status);

		var e = assertThrows(BrevTechnicalException.class, () ->
				brevlagerService.lagreBrev(brev, status));

		assertEquals("Brevet har status = 'FERDIG' og kan ikke endres", e.getMessage());
	}

	@Test
	public void shouldFerdigstilleNyttBrevAndVerifyLagret() throws Exception {
		brevRepository.deleteAll();
		BrevStatusVO brevStatusVO = defaultBrevStatus().build();
		brevStatusVO.setSystemID("BR10");
		brevStatusVO.setToken(TOKEN);
		Brevstatus brevStatus = defaultBrevstatusDomain().build();
		BrevVO redBrev = defaultBrev().contentType(FilType.RTF.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG).contentType(FilType.PDF.getContentType()).build();
		when(voTilBrevstatusConverter.convert(brevStatusVO)).thenReturn(brevStatus);
		brevlagerService.ferdigstillBrev(brevStatusVO, redBrev, pdfBrev);

		verify(brevstatusServiceMock).lagreBrevStatus(brevStatusVO);

		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);

		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA));
	}

	@Test
	public void shouldFerdigstilleEksisterendeBrevAndVerifyLagret() throws Exception {
		brevRepository.deleteAll();
		BrevVO redBrev = defaultBrev().contentType(FilType.RTF.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG)
				.contentType(FilType.PDF.getContentType()).brevdata(BREVDATA2).build();
		BrevStatusVO brevStatusVO = BrevStatusVO.builder()
				.systemID("BR12")
				.brevreferanse(BREVREFERANSE)
				.token(TOKEN)
				.build();
		brevlagerService.lagreBrev(redBrev, brevStatusVO);
		brevlagerService.ferdigstillBrev(brevStatusVO, redBrev, pdfBrev);

		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);

		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA2));
	}

	@Test
	public void shouldHandleDocx() throws Exception {
		BrevVO redBrev = defaultBrev().contentType(FilType.DOCX.getContentType()).build();
		BrevVO pdfBrev = defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG)
				.contentType(FilType.PDF.getContentType()).brevdata(BREVDATA2).build();
		BrevStatusVO brevStatusVO = BrevStatusVO.builder().systemID("BR12").brevreferanse(BREVREFERANSE).token(TOKEN).build();
		brevlagerService.lagreBrev(redBrev, brevStatusVO);
		BrevVO persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getContentType(), is(FilType.DOCX.getContentType()));

		brevlagerService.ferdigstillBrev(brevStatusVO, redBrev, pdfBrev);
		persistedBrev = brevlagerService.getBrev(SYSTEM_ID, BREVREFERANSE);
		assertThat(persistedBrev.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(persistedBrev.getSystemID(), is(SYSTEM_ID));
		assertThat(persistedBrev.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(persistedBrev.getLagerStatus(), is(Konstanter.BREVLAGER_STATUS_FERDIG));
		assertThat(persistedBrev.getBrukerID(), is(BRUKERID));
		assertThat(persistedBrev.getBrevdata(), is(BREVDATA2));
	}

	@Test
	void shouldHentDokumentFraJoarkWhenPensjon() throws BrevFunctionalException, BrevTechnicalException {
		when(brevtilgangServiceMock.sjekkTilgang(eq(PENSJON_SYSTEMID), eq(BREVREFERANSE), eq(TOKEN))).thenReturn(true);
		when(joarkServiceMock.hentDokument(eq(BREVREFERANSE))).thenReturn(BrevVO.builder().lagerStatus("A").contentType(RTF.getContentType()).build());

		BrevStatusVO brevStatusVO = BrevStatusVO.builder().systemID(PENSJON_SYSTEMID).brevreferanse(BREVREFERANSE).token(TOKEN).build();

		BrevVO brevVO = brevlagerService.hentDokumentFromBrevlagerOrJoark(brevStatusVO);

		assertThat(sha256Hex(brevVO.getBrevdata()), is("817a0c81cecd1871e5acc07ec07fa85f31482f343b72db6402359759c9fdecda"));
		assertThat(brevVO.getContentType(), is(PDF.getContentType()));
	}

	private BrevStatusVO.BrevStatusVOBuilder defaultBrevStatus() {
		return BrevStatusVO.builder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).returKoe(RETURKOE)
				.bestillerBrukerID(BESTILLER_ID).brevmal(BREVMAL).status(STATUS).format(FORMAT)
				.skrivertype(SKRIVERTYPE).skriver(SKRIVER).arkiver(ARKIVER).skuff(SKUFF);
	}

	private Brevstatus.BrevstatusBuilder defaultBrevstatusDomain() {
		return Brevstatus.builder().id(BrevreferanseSystemCompositeId.builder().brevreferanse(BREVREFERANSE).systemId(SYSTEM_ID).build()).returKoe(RETURKOE)
				.bestillerBrukerID(BRUKERID).brevmal(BREVMAL).status(STATUS).format(FORMAT).skrivertype(SKRIVERTYPE)
				.skriver(SKRIVER).arkiver(ARKIVER).skuff(SKUFF);
	}
}