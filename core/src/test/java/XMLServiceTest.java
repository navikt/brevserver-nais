import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.core.vo.KvitteringVO;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import static no.nav.brevserver.core.utils.xmlHandlers.XMLService.marshalBrevStatus;
import static no.nav.brevserver.core.utils.xmlHandlers.XMLService.marshalHeader;
import static no.nav.brevserver.core.utils.xmlHandlers.XMLService.unmarshal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XMLServiceTest {
	private final String BREVREFERANSE = "12345";
	private final String SYSTEM_ID = "PE00";
	private final String FEILKODE = "0";
	private final String TOKEN = "TOKEN";
	private final String BRUKERID = "b11111";
	private final String BREVMAL = "PE00.01";
	private final String MODUS = "MODUS";
	private final String FORMAT = "FORMAT";
	private final String SKRIVER_TYPE = "CANON";
	private final String SKRIVER = "LOKAL";
	private final String ARKIVER = "JA";
	private final String SKUFF = "01";

	@Test
	public void shouldUnmarshalKvitteringAndBrevstatus() {
		String result = unmarshal(createKvittering(), createBrevstatus());

		assertThat(result, is(xmlKvittering()));
	}

	@Test
	public void shouldMarshalHeader() throws Exception {
		KvitteringVO kvittering = marshalHeader(new ByteArrayInputStream(xmlKvittering().getBytes()));

		assertThat(kvittering.getBrevreferanse(), is(BREVREFERANSE));
		assertThat(kvittering.getSystemID(), is(SYSTEM_ID));
		assertThat(kvittering.getContentType(), is(FilType.PDF.getContentType()));
		assertThat(kvittering.getFeilkode(), is(FEILKODE));
	}

	@Test
	public void shouldMarshalBrevstatus() throws Exception {
		BrevStatusVO brevStatus = marshalBrevStatus(new StringReader(xmlBrevstatus()));

		assertThat(brevStatus.getToken(), is(TOKEN));
		assertThat(brevStatus.getBestillerBrukerID(), is(BRUKERID));
		assertThat(brevStatus.getBrevmal(), is(BREVMAL));
		assertThat(brevStatus.getSystemID(), is(SYSTEM_ID));
		assertThat(brevStatus.getModus(), is(MODUS));
		assertThat(brevStatus.getFormat(), is(FORMAT));
		assertThat(brevStatus.getSkrivertype(), is(SKRIVER_TYPE));
		assertThat(brevStatus.getSkriver(), is(SKRIVER));
		assertThat(brevStatus.getArkiver(), is(ARKIVER));
		assertThat(brevStatus.getSkuff(), is(SKUFF));
	}

	private KvitteringVO createKvittering() {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setBrevreferanse(BREVREFERANSE);
		kvittering.setSystemID(SYSTEM_ID);
		kvittering.setContentType(FilType.PDF.getContentType());
		kvittering.setFeilkode(FEILKODE);
		return kvittering;
	}

	private BrevStatusVO createBrevstatus() {
		BrevStatusVO brevStatus = new BrevStatusVO();
		brevStatus.setToken(TOKEN);
		brevStatus.setBestillerBrukerID(BRUKERID);
		brevStatus.setBrevmal(BREVMAL);
		brevStatus.setSystemID(SYSTEM_ID);
		brevStatus.setModus(MODUS);
		brevStatus.setFormat(FORMAT);
		brevStatus.setSkrivertype(SKRIVER_TYPE);
		brevStatus.setSkriver(SKRIVER);
		brevStatus.setArkiver(ARKIVER);
		brevStatus.setSkuff(SKUFF);
		brevStatus.setStatus(Konstanter.BREVSTATUS_FERDIG);
		return brevStatus;
	}

	private String xmlKvittering() {
		return """
				<?xml version="1.0" encoding="ISO-8859-1" ?>
				<rtv-brevkvitt>
				<brevref>12345</brevref>
				<sysid>PE00</sysid>
				<type>application/pdf</type>
				<status>FERDIG</status>
				<feilkode>0</feilkode>
				</rtv-brevkvitt>""";
	}

	private String xmlBrevstatus() {
		return """
				<rtv-brev klientToken="TOKEN" saksbehandler="b11111" malpakke="PE00.01" sysid="PE00" modus="MODUS" format="FORMAT" skrivertype="CANON" skriver="LOKAL" arkiver="JA" skuff="01"></rtv-brev>
				""";
	}
}
