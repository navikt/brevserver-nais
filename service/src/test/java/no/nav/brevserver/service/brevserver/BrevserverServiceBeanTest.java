package no.nav.brevserver.service.brevserver;

import no.nav.brevserver.builder.BrevStatusBuilder;
import no.nav.brevserver.core.cache.LokalCacheConfig;
import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.SysTilgangVO;
import no.nav.brevserver.service.AbstractDatabaseTest;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.brevserver.builder.BrevStatusBuilder.getBrevStatusBuilder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for BrevserverServiceBean
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("itest")
@Transactional
public class BrevserverServiceBeanTest extends AbstractDatabaseTest {



	private static final String NO_DB2_OPTIMIZATION = BLANK;

	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private BrevtilgangService brevtilgangService;
	@Autowired
	private BrevstatusService brevstatusService;
	@Autowired
	private BrevSystemTilgangRepository brevSystemTilgangRepository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldLagreTilgangAndReturnTrue() throws Exception {
		boolean lagretTilgang = brevtilgangService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
		assertThat(lagretTilgang, is(true));
	}

	@Test
	public void shouldSjekkeTilgangAndReturnFalseForNoResult() throws Exception {
		boolean lagretTilgang = brevtilgangService.sjekkTilgang(BLANK, BLANK, BLANK);
		assertThat(lagretTilgang, is(false));
	}

	@Test
	public void shouldSjekkeTilgangAndReturnTrueForLagretTilgang() throws Exception {
		brevtilgangService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
		boolean lagretTilgang = brevtilgangService.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
		assertThat(lagretTilgang, is(true));
	}


	@Test
	public void shouldLagreBrevStatusForNyttBrev() throws Exception {
		BrevStatusVO validBrevStatus = defaultBrevStatus().build();
		BrevStatusVO returnedBrevStatus = brevstatusService.lagreBrevStatus(validBrevStatus);

		assertThat(returnedBrevStatus, nullValue());
	}

	@Test
	public void shouldLagreBrevStatusForNyttBrevOgLagreTilgangMedToken() throws Exception {
		BrevStatusVO validBrevStatus = defaultBrevStatus().token(TOKEN).build();
		BrevStatusVO returnedBrevStatus = brevstatusService.lagreBrevStatus(validBrevStatus);
		boolean lagretTilgang = brevtilgangService.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);

		assertThat(returnedBrevStatus, nullValue());
		assertThat(lagretTilgang, is(true));
	}

	@Test
	public void shouldReturnereGammelBrevStatusForOppdateringAvBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().build();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe("Returko").bestillerBrukerID("Brannmann").brevmal("NAV1").status("UFERDIG").format(FilType.RTF.getJoarkCode())
				.skrivertype("Laser").skriver("HP").arkiver("Nei").skuff("33").build();

		BrevStatusVO oldBrevStatus = brevstatusService.lagreBrevStatus(newBrevStatus);

		assertDefaultBrevStatusValues(oldBrevStatus);
	}

	@Test
	public void shouldOppdatereBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().returKoe("E18").brevmal("NAV1").build();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe(null).bestillerBrukerID("Brannmann").brevmal(null).status("UFERDIG").format(FilType.RTF.getJoarkCode())
				.skrivertype("Laser").skriver("HP").arkiver("Nei").skuff("33").build();
		brevstatusService.lagreBrevStatus(newBrevStatus);

		BrevStatusVO actualBrevStatus = brevstatusService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertThat(actualBrevStatus.getReturKoe(), is("E18"));
		assertThat(actualBrevStatus.getBestillerBrukerID(), is("Brannmann"));
		assertThat(actualBrevStatus.getBrevmal(), is("NAV1"));
		assertThat(actualBrevStatus.getStatus(), is("UFERDIG"));
		assertThat(actualBrevStatus.getFormat(), is("RTF"));
		assertThat(actualBrevStatus.getSkrivertype(), is("Laser"));
		assertThat(actualBrevStatus.getSkriver(), is("HP"));
		assertThat(actualBrevStatus.getArkiver(), is("Nei"));
		assertThat(actualBrevStatus.getSkuff(), is("33"));
	}

	@Test
	public void shouldOppdatereBrevStatusOgFylleInnManglendeVerdierFraGammelBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().returKoe(null).brevmal(null).build();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe(RETURKOE).brevmal(BREVMAL).status("PRINTET").build();
		brevstatusService.lagreBrevStatus(newBrevStatus);

		BrevStatusVO actualBrevStatus = brevstatusService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertThat(actualBrevStatus.getReturKoe(), is(RETURKOE));
		assertThat(actualBrevStatus.getBestillerBrukerID(), is(BESTILLER_ID));
		assertThat(actualBrevStatus.getBrevmal(), is(BREVMAL));
		assertThat(actualBrevStatus.getStatus(), is("PRINTET"));
		assertThat(actualBrevStatus.getFormat(), is(FORMAT));
		assertThat(actualBrevStatus.getSkrivertype(), is(SKRIVERTYPE));
		assertThat(actualBrevStatus.getSkriver(), is(SKRIVER));
		assertThat(actualBrevStatus.getArkiver(), is(ARKIVER));
		assertThat(actualBrevStatus.getSkuff(), is(SKUFF));
	}


	@Test
	public void shouldHenteBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().build();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO brevStatus = brevstatusService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertDefaultBrevStatusValues(brevStatus);
	}

	@Test
	public void shouldReturnNullForNonExistingBrevStatus() throws Exception {
		BrevStatusVO brevStatus = brevstatusService.hentBrevStatus(SYSTEM_ID, BREVREFERANSE);

		assertThat(brevStatus, nullValue());
	}



	@Test
	public void shouldReturnTrueForGyldigSystemTilgang() throws Exception {
		createSystemTilgang();

		boolean actual = brevtilgangService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);

		assertThat(actual, is(true));
	}

	@Test
	public void shouldReturnTrueForGyldigCachedSystemTilgang() throws Exception {
		Cache value = cacheManager.getCache(LokalCacheConfig.SYSTEM_TILGANG_CACHE);
		SimpleKey simpleKey = new SimpleKey(SYSTEM_ID, SYSTEM_PASSORD);
		value.put(simpleKey, true);
		boolean actual = brevtilgangService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);
		assertThat(actual, is(true));
	}

	@Test
	public void shouldReturnFalseForUgyldigSystemTilgang() throws Exception {
		createSystemTilgang();

		boolean actual = brevtilgangService.sjekkSystemTilgang("BI00", "HestErBest");

		assertThat(actual, is(false));
	}

	@Test
	public void shouldHenteSystilgang() throws Exception {
		createSystemTilgang();

		SysTilgangVO sysTilgang = brevtilgangService.hentTilgangUtenCache(SYSTEM_ID);

		assertThat(sysTilgang.getSysId(), is(SYSTEM_ID));
		assertThat(sysTilgang.getPwd(), is(SYSTEM_PASSORD));
	}

	@Test
	public void shouldHenteSystilgangFromCache() throws BrevTechnicalException {
		Cache value = cacheManager.getCache(LokalCacheConfig.HENT_SYSTEM_TILGANG_CACHE);
		SysTilgangVO sysTilgangVO = new SysTilgangVO();
		sysTilgangVO.setPwd(SYSTEM_PASSORD);
		sysTilgangVO.setSysId(SYSTEM_ID);
		value.put(SYSTEM_ID, sysTilgangVO);
		SysTilgangVO sysTilgang = brevtilgangService.hentTilgangMedCache(SYSTEM_ID);

		assertThat(sysTilgang.getSysId(), is(SYSTEM_ID));
		assertThat(sysTilgang.getPwd(), is(SYSTEM_PASSORD));
	}

	private BrevStatusBuilder defaultBrevStatus() {
		return getBrevStatusBuilder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID).returKoe(RETURKOE)
				.bestillerBrukerID(BESTILLER_ID).brevmal(BREVMAL).status(STATUS).format(FORMAT)
				.skrivertype(SKRIVERTYPE).skriver(SKRIVER).arkiver(ARKIVER).skuff(SKUFF);
	}

	private void assertDefaultBrevStatusValues(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getReturKoe(), is(RETURKOE));
		assertThat(brevStatus.getBestillerBrukerID(), is(BESTILLER_ID));
		assertThat(brevStatus.getBrevmal(), is(BREVMAL));
		assertThat(brevStatus.getStatus(), is(STATUS));
		assertThat(brevStatus.getFormat(), is(FORMAT));
		assertThat(brevStatus.getSkrivertype(), is(SKRIVERTYPE));
		assertThat(brevStatus.getSkriver(), is(SKRIVER));
		assertThat(brevStatus.getArkiver(), is(ARKIVER));
		assertThat(brevStatus.getSkuff(), is(SKUFF));
	}


	private void createSystemTilgang() {
		BrevSystemTilgang brevSystemTilgang = BrevSystemTilgang.builder().sysId("PE00").pwd("Pensjon123").build();
		brevSystemTilgangRepository.save(brevSystemTilgang);
	}


}
