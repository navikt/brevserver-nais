package no.nav.brevserver.service.brevlager.beans;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.joark.JournalClient;
import no.nav.brevserver.service.AbstractDatabaseTest;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.service.converter.VoTilBrevstatusConverter;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalpost;
import no.nav.virksomhet.gjennomforing.arkiv.journal.v2.Journalstatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BrevlagerServiceBean
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("itest")
public class BrevlagerServiceBeanTest extends AbstractDatabaseTest {

	private static final String BLANK = "";
	private static final String NO_DB2_OPTIMIZATION = BLANK;

	private static final String TOKEN = "TOKEN_123";

	private static final byte[] BREVDATA2 = "Hest er best ingen protest".getBytes();

	private static final String RETURKOE = "ReturKoe";
	private static final String BREVMAL = "NAV-01-02-03";
	private static final String STATUS = "FERDIG";
	private static final String FORMAT = FilType.PDF.getJoarkCode();
	private static final String SKRIVERTYPE = "Blekk";
	private static final String SKRIVER = "Canon";
	private static final String ARKIVER = "Ja";
	private static final String SKUFF = "0";

	@MockBean
	private VoTilBrevstatusConverter voTilBrevstatusConverter;
	@MockBean
	private BrevstatusService brevstatusServiceMock;
	@MockBean
	private BrevstatusTilVoConverter brevstatusTilVoConverter;
	@Autowired
	private BrevRepository brevRepository;
	@Autowired
	private BrevlagerService brevlagerService;


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

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
		initialBrevStatus.setToken("12345");
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
	public void shouldThrowExceptionIfBrevStatusIsFerdig() throws Exception {
		thrown.expect(BrevTechnicalException.class);
		thrown.expectMessage("Brevet har status = 'FERDIG' og kan ikke endres");

		brevlagerService.lagreBrev(defaultBrev().lagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG).build(), new BrevStatusVO());
		brevlagerService.lagreBrev(defaultBrev().build(), new BrevStatusVO());
	}

	@Test
	public void shouldFerdigstilleNyttBrevAndVerifyLagret() throws Exception {
		brevRepository.deleteAll();
		BrevStatusVO brevStatusVO = defaultBrevStatus().build();
		brevStatusVO.setSystemID("BR10");
		brevStatusVO.setToken("12345");
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
				.token("12345")
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
		BrevStatusVO brevStatusVO = BrevStatusVO.builder().systemID("BR12").brevreferanse(BREVREFERANSE).token("12345").build();
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
	public void shouldPingBrevlager() {
		brevlagerService.ping();
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