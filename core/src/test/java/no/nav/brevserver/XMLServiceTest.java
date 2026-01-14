package no.nav.brevserver;

import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.KvitteringVO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import static no.nav.brevserver.core.utils.xmlHandlers.XMLService.marshalBrevStatus;
import static no.nav.brevserver.core.utils.xmlHandlers.XMLService.marshalHeader;
import static no.nav.brevserver.core.utils.xmlHandlers.XMLService.unmarshal;
import static no.nav.brevserver.core.vo.FilType.PDF;
import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(result).isEqualTo(xmlKvittering());
	}

	@Test
	public void shouldMarshalHeader() throws Exception {
		KvitteringVO kvittering = marshalHeader(new ByteArrayInputStream(xmlKvittering().getBytes()));

		assertThat(kvittering.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(kvittering.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(kvittering.getContentType()).isEqualTo(PDF.getContentType());
		assertThat(kvittering.getFeilkode()).isEqualTo(FEILKODE);
	}

	@Test
	public void shouldMarshalBrevstatus() throws Exception {
		BrevStatusVO brevStatus = marshalBrevStatus(new StringReader(xmlBrevstatus()));

		assertThat(brevStatus.getToken()).isEqualTo(TOKEN);
		assertThat(brevStatus.getBestillerBrukerID()).isEqualTo(BRUKERID);
		assertThat(brevStatus.getBrevmal()).isEqualTo(BREVMAL);
		assertThat(brevStatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevStatus.getModus()).isEqualTo(MODUS);
		assertThat(brevStatus.getFormat()).isEqualTo(FORMAT);
		assertThat(brevStatus.getSkrivertype()).isEqualTo(SKRIVER_TYPE);
		assertThat(brevStatus.getSkriver()).isEqualTo(SKRIVER);
		assertThat(brevStatus.getArkiver()).isEqualTo(ARKIVER);
		assertThat(brevStatus.getSkuff()).isEqualTo(SKUFF);
	}

	@Test
	public void shouldMarshalBrevstatusBisysExample() throws BrevTechnicalException {
		BrevStatusVO brevStatus = marshalBrevStatus(new StringReader(xmlBrevstatusBisysExample()));

		assertThat(brevStatus.getBrevreferanse()).isEqualTo("BIF100000061");
		assertThat(brevStatus.getToken()).isNull();
		assertThat(brevStatus.getPassord()).isEqualTo("test");
		assertThat(brevStatus.getBestillerBrukerID()).isEqualTo("Z994977");
		assertThat(brevStatus.getBrevmal()).isEqualTo("BI01.BI01S02");
		assertThat(brevStatus.getSystemID()).isEqualTo("BI12");
		assertThat(brevStatus.getModus()).isNull();
		assertThat(brevStatus.getFormat()).isEqualTo("ENSIDIG");
		assertThat(brevStatus.getSkrivertype()).isEqualTo("LOKAL");
		assertThat(brevStatus.getSkriver()).isEqualTo("");
		assertThat(brevStatus.getArkiver()).isEqualTo("JA");
		assertThat(brevStatus.getSkuff()).isEqualTo("");
	}

	@Test
	public void shouldMarshalBrevstatusPesysExample() throws BrevTechnicalException {
		BrevStatusVO brevStatus = marshalBrevStatus(new StringReader(xmlBrevstatusPesysExample()));

		assertThat(brevStatus.getBrevreferanse()).isEqualTo("453836537");
		assertThat(brevStatus.getToken()).isNull();
		assertThat(brevStatus.getPassord()).isNull();
		assertThat(brevStatus.getBestillerBrukerID()).isEqualTo("F_Z990541 E_Z990541");
		assertThat(brevStatus.getBrevmal()).isEqualTo("PE_IY_05_007");
		assertThat(brevStatus.getSystemID()).isEqualTo("PE2");
		assertThat(brevStatus.getModus()).isNull();
		assertThat(brevStatus.getFormat()).isNull();
		assertThat(brevStatus.getSkrivertype()).isNull();
		assertThat(brevStatus.getSkriver()).isNull();
		assertThat(brevStatus.getArkiver()).isNull();
		assertThat(brevStatus.getSkuff()).isNull();
	}

	private KvitteringVO createKvittering() {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setBrevreferanse(BREVREFERANSE);
		kvittering.setSystemID(SYSTEM_ID);
		kvittering.setContentType(PDF.getContentType());
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

	// Q2 eksempel syntetisk data
	private String xmlBrevstatusBisysExample() {
		return """
				<?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>
				<rtv-brev sysid="BI12" arkiver="JA" direkteutskrift="NEI" format="ENSIDIG" skriver="" skrivertype="LOKAL" skuff=""
						  malpakke="BI01.BI01S02" passord="test" saksbehandler="Z994977">
					<brev tknr="4806" spraak="NB" brevref="BIF100000061">
						<brevMottaker>
							<navn>BRA KLANG</navn>
							<adr1>Ulrik Olsens vei 25</adr1>
							<adr2/>
							<adr3>6511 KRISTIANSUND N</adr3>
							<adr4/>
							<boligNr/>
							<bidrRolle>02</bidrRolle>
							<fnr>44874800500</fnr>
							<fdato>040748</fdato>
							<postnr>6511</postnr>
							<landKd/>
							<spraak>NB</spraak>
						</brevMottaker>
						<parter>
							<bpfnr/>
							<bpNavn/>
							<bpfDato/>
							<bpKravFremAv/>
							<bpbelopGebyr/>
							<bpLandKd/>
							<bpDatoDod/>
							<bmfnr>44874800500</bmfnr>
							<bmNavn>BRA KLANG</bmNavn>
							<bmfDato>040748</bmfDato>
							<bmKravFremAv/>
							<bmbelopGebyr/>
							<bmLandKd/>
							<bmDatoDod/>
						</parter>
						<soknBost>
							<saksnr>2300269</saksnr>
							<BBFogd/>
							<BPFogd/>
							<sakstype>X</sakstype>
							<jounalkode/>
							<indexRegDato/>
							<indexRegPro/>
							<hgKode/>
							<ugKode/>
							<datoSakReg>20230524</datoSakReg>
							<resKode/>
							<datoVtak/>
							<rmISak>N</rmISak>
							<forskUtBet>N</forskUtBet>
							<sendtDato>20230524</sendtDato>
							<gebyrsats>01243.0</gebyrsats>
							<innkrSamtid>N</innkrSamtid>
							<mottDato/>
							<virknDato/>
							<myndighet/>
							<ffuRefNr/>
							<konv/>
							<soknGrKode/>
							<soknFraKode/>
							<soknType/>
							<b4Kode/>
							<b4Belop/>
						</soknBost>
						<Kontaktinfo>
							<NavnAvsender>
								<NavnAvsEnh>NAV Familie- og pensjonsytelser Drammen</NavnAvsEnh>
							</NavnAvsender>
							<TelfAvsender>
								<TelfAvsEnh>55553333</TelfAvsEnh>
							</TelfAvsender>
							<Returadr>
								<NavEnhId>4806</NavEnhId>
								<NavEnhNavn>NAV Familie- og pensjonsytelser Drammen</NavEnhNavn>
								<Telefon/>
								<AdrLinje1>Postboks 1583</AdrLinje1>
								<AdrLinje2/>
								<PostNr>3007</PostNr>
								<PostSted>Drammen</PostSted>
								<Land/>
							</Returadr>
							<Postadr>
								<NavEnhId>4806</NavEnhId>
								<NavEnhNavn>NAV Familie- og pensjonsytelser Drammen</NavEnhNavn>
								<Telefon/>
								<AdrLinje1>Postboks 1583</AdrLinje1>
								<AdrLinje2/>
								<PostNr>3007</PostNr>
								<PostSted>Drammen</PostSted>
								<Land/>
							</Postadr>
						</Kontaktinfo>
						<Saksbehandl>
							<saksbNavn>F_Z994977 E_Z994977</saksbNavn>
						</Saksbehandl>
					</brev>
				</rtv-brev>
				""";
	}

	// Q2 eksempel syntetisk data
	private String xmlBrevstatusPesysExample() {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<rtv-brev direkteutskrift="nei" malpakke="PE_IY_05_007" saksbehandler="F_Z990541 E_Z990541" saksbehandlerId="Z990541"
						  sysid="PE2">
					<brev brevref="453836537" spraak="nb" tknr="0437">
						<BrevMottaker>
							<BMfnr_tssid>02496734672</BMfnr_tssid>
							<BMNavn>Klokke Stabil</BMNavn>
							<BMpostnr>2500</BMpostnr>
							<BMadr1>SKARDVANGVEIEN 14</BMadr1>
							<BMadr4>2500 TYNSET</BMadr4>
						</BrevMottaker>
						<PersonSak>
							<spesen>4815</spesen>
							<PSfnr_tssid>02496734672</PSfnr_tssid>
							<PSNavn>Klokke Stabil</PSNavn>
							<PSadr1>SKARDVANGVEIEN 14</PSadr1>
							<PSadr4>2500 TYNSET</PSadr4>
							<PSpostnr>2500</PSpostnr>
							<PSsivilstand>GIFT</PSsivilstand>
							<PSstatsborger>NORGE</PSstatsborger>
							<PSkontonummer/>
							<Fodselsdato>02091967</Fodselsdato>
							<AdresseType>BOAD</AdresseType>
							<PSFornavn>Stabil</PSFornavn>
							<PSMellomnavn/>
							<PSEtternavn>Klokke</PSEtternavn>
						</PersonSak>
						<SaksData>
							<saksreferanse>22972604</saksreferanse>
							<Sakstype>omsorg</Sakstype>
						</SaksData>
						<EktefellePartner>
							<EPfodselsnummer>14486518333</EPfodselsnummer>
							<EPnavn>Hette Internasjonal</EPnavn>
						</EktefellePartner>
						<Samboer/>
						<GenerelleSatser>
							<grunnbelop>0000111477</grunnbelop>
							<minstepensjon_ep>0000222960</minstepensjon_ep>
						</GenerelleSatser>
						<Kontaktinformasjon>
							<NavnAvsenderEnhet>NAV Pensjon</NavnAvsenderEnhet>
							<TelefonAvsenderEnhet>55 55 33 34</TelefonAvsenderEnhet>
							<Returadresse>
								<NavEnhetsId>4815</NavEnhetsId>
								<NavEnhetsNavn>AV FAMILIE- OG PENSJONSYTELSER</NavEnhetsNavn>
								<Adresselinje1>Postboks 6600 Etterstad</Adresselinje1>
								<PostNr>0607</PostNr>
								<Poststed>OSLO</Poststed>
								<Land>NORGE</Land>
							</Returadresse>
							<Postadresse>
								<NavEnhetsId>4815</NavEnhetsId>
								<NavEnhetsNavn>AV FAMILIE- OG PENSJONSYTELSER</NavEnhetsNavn>
								<Adresselinje1>Postboks 6600 Etterstad</Adresselinje1>
								<PostNr>0607</PostNr>
								<Poststed>OSLO</Poststed>
								<Land>NORGE</Land>
							</Postadresse>
							<Besoksadresse>
								<NavEnhetsId>0437</NavEnhetsId>
								<NavEnhetsNavn>NAV NORD-ØSTERDAL</NavEnhetsNavn>
								<Adresselinje1>Rådhuset</Adresselinje1>
								<Adresselinje2>Torvgata 1</Adresselinje2>
								<PostNr>2500</PostNr>
								<Poststed>TYNSET</Poststed>
								<Land>NORGE</Land>
								<KommuneNr>3427</KommuneNr>
							</Besoksadresse>
						</Kontaktinformasjon>
					</brev>
				</rtv-brev>
				""";
	}
}
