package utils;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.FilType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.brevserver.core.constants.Konstanter.BREVPAKKE_FEILNIVA_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_UKJENT;

public class Utils {


	public static final String BISYS_SYSTEM_ID = "BI12";
	public static final String PENSJON_SYSTEM_ID = "PE01";
	public static final String BREVREFERANSE = "10000000000";
	public static final String BREVREFERANSE2 = "10000000006";
	public static final String BRUKERID = "b111111";
	public static final byte[] BREVDATA = "".getBytes();
	public static final String BLANK = "";
	public static final String SYSTEM_PASSORD = "Bisys123";
	public static final String BESTILLER_ID = "b1111";
	public static final String RETURKOE = "ReturKoe";
	public static final String BREVMAL = "NAV-01-02-03";
	public static final String STATUS_KLADD = "KLADD";
	public static final String STATUS_LAGRET = "LAGRET";
	public static final String FILTYPE_XML = "XML";
	public static final String FILTYPE_PDF = "PDF";
	public static final String FORMAT = FilType.PDF.getJoarkCode();
	public static final String SKRIVERTYPE = "Blekk";
	public static final String SKRIVER = "Canon";
	public static final String ARKIVER = "Ja";
	public static final String SKUFF = "0";
	public static final String TOKEN = "Token";

	public static final String CALLID = "dette-er-en-callID";
	public static final String STATUS_FERDIG = "FERDIG";
	public static final String PDF_CONTENTTYPE = FilType.PDF.getContentType();

	public static BrevStatusVO createBrevstatus(String systemId, String brevref){
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setSystemID(systemId);
		brevstatus.setBrevreferanse(brevref);
		brevstatus.setReturKoe(RETURKOE);
		brevstatus.setBrevmal(BREVMAL);
		brevstatus.setStatus(STATUS_KLADD);
		brevstatus.setFormat(FORMAT);
		brevstatus.setToken(TOKEN);
		brevstatus.setBestillerBrukerID(BRUKERID);
		brevstatus.setSkrivertype(SKRIVERTYPE);
		brevstatus.setSkriver(SKRIVER);
		brevstatus.setArkiver(ARKIVER);
		brevstatus.setSkuff(SKUFF);
		return brevstatus;
	}

	public static String createPesysKvittering(){
		return StringUtils.rightPad(generateKvitteringHeader(PDF_CONTENTTYPE, PENSJON_SYSTEM_ID, BREVREFERANSE).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	//Brevserver forventer en kvitteringsheader på nøyaktig 350 chars. Pad til 350
	public static String createBisysKvittering() {
		return StringUtils.rightPad(generateKvitteringHeader(FORMAT, BISYS_SYSTEM_ID, BREVREFERANSE).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	public static String createBisysKvittering(String format) {
		return StringUtils.rightPad(generateKvitteringHeader(format, BISYS_SYSTEM_ID, BREVREFERANSE).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	public static String createBisysKvitteringfeilNiva() {
		return StringUtils.rightPad(generateKvitteringHeader(PDF_CONTENTTYPE, BISYS_SYSTEM_ID, BREVREFERANSE, BREVPAKKE_FEILNIVA_FEIL, FEIL_UKJENT).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	public static String createBadXmlKvitteringHeader(){
		return generateKvitteringHeader(FORMAT, BISYS_SYSTEM_ID, BREVREFERANSE).toString();
	}

	public static String createBisysKvittering2(){
		return StringUtils.rightPad(generateKvitteringHeader(FORMAT, BISYS_SYSTEM_ID, BREVREFERANSE2).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	public static  StringBuilder generateKvitteringHeader(String contentType, String fagsystem, String brevref){
		return generateKvitteringHeader(contentType, fagsystem, brevref, "0", "0");

	}

	public static  StringBuilder generateKvitteringHeader(String contentType, String fagsystem, String brevref, String feilniva, String feilkode){
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
		builder.append("<rtv-brevkvitt>");
		builder.append("<brevref>").append(brevref).append("</brevref>");
		builder.append("<sysid>").append(fagsystem).append("</sysid>");
		builder.append("<type>").append(contentType).append("</type>");
		builder.append("<feilniva>").append(feilniva).append("</feilniva>");
		builder.append("<feilkode>").append(feilkode).append("</feilkode>");
		builder.append("</rtv-brevkvitt>");
		return builder;
	}

	public static  String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		return IOUtils.toString(inputStream, UTF_8);
	}
}
