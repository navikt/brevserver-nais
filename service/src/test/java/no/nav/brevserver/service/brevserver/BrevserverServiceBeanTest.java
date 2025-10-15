package no.nav.brevserver.service.brevserver;

import no.nav.brevserver.core.cache.LokalCacheConfig;
import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.SysTilgangVO;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.BrevtilgangService;
import no.nav.brevserver.service.config.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import static no.nav.brevserver.core.cache.LokalCacheConfig.HENT_SYSTEM_TILGANG_CACHE;
import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.assertj.core.api.Assertions.assertThat;

public class BrevserverServiceBeanTest extends AbstractTest {

	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private BrevtilgangService brevtilgangService;
	@Autowired
	private BrevstatusService brevstatusService;

	@Test
	public void shouldLagreTilgangAndReturnTrue() throws Exception {
		boolean lagretTilgang = brevtilgangService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);

		assertThat(lagretTilgang).isTrue();
	}

	@Test
	public void shouldSjekkeTilgangAndReturnFalseForNoResult() throws Exception {
		boolean lagretTilgang = brevtilgangService.sjekkTilgang(BLANK, BLANK, BLANK);

		assertThat(lagretTilgang).isFalse();
	}

	@Test
	public void shouldSjekkeTilgangAndReturnTrueForLagretTilgang() throws Exception {
		brevtilgangService.lagreTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);
		boolean lagretTilgang = brevtilgangService.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);

		assertThat(lagretTilgang).isTrue();
	}

	@Test
	public void shouldLagreBrevStatusForNyttBrev() throws Exception {
		BrevStatusVO validBrevStatus = defaultBrevStatus();
		BrevStatusVO returnedBrevStatus = brevstatusService.lagreBrevStatus(validBrevStatus);

		assertThat(returnedBrevStatus).isNull();
	}

	@Test
	public void shouldLagreBrevStatusForNyttBrevOgLagreTilgangMedToken() throws Exception {
		BrevStatusVO validBrevStatus = defaultBrevStatus().toBuilder().token(TOKEN).build();

		BrevStatusVO returnedBrevStatus = brevstatusService.lagreBrevStatus(validBrevStatus);
		boolean lagretTilgang = brevtilgangService.sjekkTilgang(SYSTEM_ID, BREVREFERANSE, TOKEN);

		assertThat(returnedBrevStatus).isNull();
		assertThat(lagretTilgang).isTrue();
	}

	@Test
	public void shouldReturnereGammelBrevStatusForOppdateringAvBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = BrevStatusVO.builder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe("Returko").bestillerBrukerID("Brannmann").brevmal("NAV1").status("UFERDIG").format(RTF.getJoarkCode())
				.skrivertype("Laser").skriver("HP").arkiver("Nei").skuff("33").build();

		BrevStatusVO oldBrevStatus = brevstatusService.lagreBrevStatus(newBrevStatus);

		assertDefaultBrevStatusValues(oldBrevStatus);
	}

	@Test
	public void shouldOppdatereBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().toBuilder().returKoe("E18").brevmal("NAV1").build();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = BrevStatusVO.builder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe(null).bestillerBrukerID("Brannmann").brevmal(null).status("UFERDIG").format(RTF.getJoarkCode())
				.skrivertype("Laser").skriver("HP").arkiver("Nei").skuff("33").build();

		brevstatusService.lagreBrevStatus(newBrevStatus);
		BrevStatusVO actualBrevStatus = brevstatusService.hentBrevStatus(BREVREFERANSE, SYSTEM_ID);

		assertThat(actualBrevStatus.getReturKoe()).isEqualTo("E18");
		assertThat(actualBrevStatus.getBestillerBrukerID()).isEqualTo("Brannmann");
		assertThat(actualBrevStatus.getBrevmal()).isEqualTo("NAV1");
		assertThat(actualBrevStatus.getStatus()).isEqualTo("UFERDIG");
		assertThat(actualBrevStatus.getFormat()).isEqualTo("RTF");
		assertThat(actualBrevStatus.getSkrivertype()).isEqualTo("Laser");
		assertThat(actualBrevStatus.getSkriver()).isEqualTo("HP");
		assertThat(actualBrevStatus.getArkiver()).isEqualTo("Nei");
		assertThat(actualBrevStatus.getSkuff()).isEqualTo("33");
	}

	@Test
	public void shouldOppdatereBrevStatusOgFylleInnManglendeVerdierFraGammelBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus().toBuilder().returKoe(null).brevmal(null).build();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO newBrevStatus = BrevStatusVO.builder().brevreferanse(BREVREFERANSE).systemID(SYSTEM_ID)
				.returKoe(RETURKOE).brevmal(BREVMAL).status("PRINTET").build();

		brevstatusService.lagreBrevStatus(newBrevStatus);

		BrevStatusVO actualBrevStatus = brevstatusService.hentBrevStatus(BREVREFERANSE, SYSTEM_ID);

		assertThat(actualBrevStatus.getReturKoe()).isEqualTo(RETURKOE);
		assertThat(actualBrevStatus.getBestillerBrukerID()).isEqualTo(BESTILLER_ID);
		assertThat(actualBrevStatus.getBrevmal()).isEqualTo(BREVMAL);
		assertThat(actualBrevStatus.getStatus()).isEqualTo("PRINTET");
		assertThat(actualBrevStatus.getFormat()).isEqualTo(FORMAT);
		assertThat(actualBrevStatus.getSkrivertype()).isEqualTo(SKRIVERTYPE);
		assertThat(actualBrevStatus.getSkriver()).isEqualTo(SKRIVER);
		assertThat(actualBrevStatus.getArkiver()).isEqualTo(ARKIVER);
		assertThat(actualBrevStatus.getSkuff()).isEqualTo(SKUFF);
	}


	@Test
	public void shouldHenteBrevStatus() throws Exception {
		BrevStatusVO existingBrevStatus = defaultBrevStatus();
		brevstatusService.lagreBrevStatus(existingBrevStatus);

		BrevStatusVO brevStatus = brevstatusService.hentBrevStatus(BREVREFERANSE, SYSTEM_ID);

		assertDefaultBrevStatusValues(brevStatus);
	}

	@Test
	public void shouldReturnNullForNonExistingBrevStatus() throws Exception {
		BrevStatusVO brevStatus = brevstatusService.hentBrevStatus(BREVREFERANSE, SYSTEM_ID);

		assertThat(brevStatus).isNull();
	}

	@Test
	public void shouldReturnTrueForGyldigSystemTilgang() throws Exception {
		createSystemTilgang();

		boolean actual = brevtilgangService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);

		assertThat(actual).isTrue();
	}

	@Test
	public void shouldReturnTrueForGyldigCachedSystemTilgang() throws Exception {
		Cache value = cacheManager.getCache(LokalCacheConfig.SYSTEM_TILGANG_CACHE);
		SimpleKey simpleKey = new SimpleKey(SYSTEM_ID, SYSTEM_PASSORD);
		value.put(simpleKey, true);

		boolean actual = brevtilgangService.sjekkSystemTilgang(SYSTEM_ID, SYSTEM_PASSORD);

		assertThat(actual).isTrue();
	}

	@Test
	public void shouldReturnFalseForUgyldigSystemTilgang() throws Exception {
		createSystemTilgang();

		boolean actual = brevtilgangService.sjekkSystemTilgang("BI00", "HestErBest");

		assertThat(actual).isFalse();
	}

	@Test
	public void shouldHenteSystilgang() throws Exception {
		createSystemTilgang();

		SysTilgangVO sysTilgang = brevtilgangService.hentTilgangUtenCache(SYSTEM_ID);

		assertThat(sysTilgang.getSysId()).isEqualTo(SYSTEM_ID);
		assertThat(sysTilgang.getPwd()).isEqualTo(SYSTEM_PASSORD);
	}

	@Test
	public void shouldHenteSystilgangFromCache() throws BrevTechnicalException {
		Cache value = cacheManager.getCache(HENT_SYSTEM_TILGANG_CACHE);
		SysTilgangVO sysTilgangVO = new SysTilgangVO();
		sysTilgangVO.setPwd(SYSTEM_PASSORD);
		sysTilgangVO.setSysId(SYSTEM_ID);
		value.put(SYSTEM_ID, sysTilgangVO);

		SysTilgangVO sysTilgang = brevtilgangService.hentTilgangMedCache(SYSTEM_ID);

		assertThat(sysTilgang.getSysId()).isEqualTo(SYSTEM_ID);
		assertThat(sysTilgang.getPwd()).isEqualTo(SYSTEM_PASSORD);
	}

	private BrevStatusVO defaultBrevStatus() {
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
				.skuff(SKUFF)
				.build();
	}

	private void assertDefaultBrevStatusValues(BrevStatusVO brevStatus) {
		assertThat(brevStatus.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brevStatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevStatus.getReturKoe()).isEqualTo(RETURKOE);
		assertThat(brevStatus.getBestillerBrukerID()).isEqualTo(BESTILLER_ID);
		assertThat(brevStatus.getBrevmal()).isEqualTo(BREVMAL);
		assertThat(brevStatus.getStatus()).isEqualTo(STATUS);
		assertThat(brevStatus.getFormat()).isEqualTo(FORMAT);
		assertThat(brevStatus.getSkrivertype()).isEqualTo(SKRIVERTYPE);
		assertThat(brevStatus.getSkriver()).isEqualTo(SKRIVER);
		assertThat(brevStatus.getArkiver()).isEqualTo(ARKIVER);
		assertThat(brevStatus.getSkuff()).isEqualTo(SKUFF);
	}

	private void createSystemTilgang() {
		BrevSystemTilgang brevSystemTilgang = BrevSystemTilgang.builder()
				.sysId("BI12")
				.pwd("Pensjon123")
				.build();

		brevSystemTilgangRepository.save(brevSystemTilgang);
	}

}