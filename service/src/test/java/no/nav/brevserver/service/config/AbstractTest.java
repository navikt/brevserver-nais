package no.nav.brevserver.service.config;

import no.nav.brevserver.ApplicationTestConfig;
import no.nav.brevserver.core.repository.BrevRepository;
import no.nav.brevserver.core.repository.BrevSystemTilgangRepository;
import no.nav.brevserver.core.repository.BrevlagerHistorikkRepository;
import no.nav.brevserver.core.repository.BrevstatusRepository;
import no.nav.brevserver.core.repository.BrevtilgangRepository;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.FilType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.brevserver.core.constants.Konstanter.BREVLAGER_STATUS_KLADD;
import static no.nav.brevserver.core.vo.FilType.RTF;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@ActiveProfiles("itest")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractTest {

	protected static final String SYSTEM_ID = "BI12";
	protected static final String BREVREFERANSE = "10000000000";
	protected static final String BRUKERID = "b111111";
	protected static final byte[] BREVDATA = "Hest er best".getBytes();
	protected static final String BLANK = "";
	protected static final String SYSTEM_PASSORD = "Pensjon123";
	protected static final String BESTILLER_ID = "b1111";
	protected static final String RETURKOE = "ReturKoe";
	protected static final String BREVMAL = "NAV-01-02-03";
	protected static final String STATUS = "FERDIG";
	protected static final String FORMAT = FilType.PDF.getJoarkCode();
	protected static final String SKRIVERTYPE = "Blekk";
	protected static final String SKRIVER = "Canon";
	protected static final String ARKIVER = "Ja";
	protected static final String SKUFF = "0";
	protected static final String TOKEN = "Token";

	@Autowired
	protected BrevtilgangRepository brevtilgangRepository;

	@Autowired
	protected BrevSystemTilgangRepository brevSystemTilgangRepository;

	@Autowired
	protected BrevstatusRepository brevstatusRepository;

	@Autowired
	protected BrevlagerHistorikkRepository brevlagerHistorikkRepository;

	@Autowired
	protected BrevRepository brevRepository;

	protected BrevVO.BrevVOBuilder defaultBrev() {
		return BrevVO.builder()
				.brevreferanse(BREVREFERANSE)
				.systemID(SYSTEM_ID)
				.contentType(RTF.getContentType())
				.lagerStatus(BREVLAGER_STATUS_KLADD)
				.brukerID(BRUKERID)
				.brevdata(BREVDATA);
	}

}