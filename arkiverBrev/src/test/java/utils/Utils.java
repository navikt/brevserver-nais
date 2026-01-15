package utils;

import io.micrometer.core.instrument.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.brevserver.core.constants.Konstanter.BREVPAKKE_FEILNIVA_FEIL;
import static no.nav.brevserver.core.constants.Konstanter.FEIL_UKJENT;
import static no.nav.brevserver.core.constants.Konstanter.MELDING_HEADER_LENGTH;
import static no.nav.brevserver.core.vo.FilType.PDF;

public class Utils {

	public static final String BISYS_SYSTEM_ID = "BI12";
	public static final String PENSJON_SYSTEM_ID = "PE01";
	public static final String BREVREFERANSE = "10000000000";
	public static final String STATUS_LAGRET = "LAGRET";
	public static final String FILTYPE_XML = "XML";
	public static final String FORMAT = PDF.getContentType();
	public static final String TOKEN = "Token";

	public static final String CALLID = "dette-er-en-callID";
	public static final String STATUS_FERDIG = "FERDIG";
	public static final String PDF_CONTENTTYPE = PDF.getContentType();

	public static String createPesysKvittering() {
		return StringUtils.rightPad(generateKvitteringHeader(PDF_CONTENTTYPE, PENSJON_SYSTEM_ID, BREVREFERANSE).toString(), MELDING_HEADER_LENGTH, ' ');
	}

	public static String createPesysKvittering(String format) {
		return StringUtils.rightPad(generateKvitteringHeader(format, PENSJON_SYSTEM_ID, BREVREFERANSE).toString(), MELDING_HEADER_LENGTH, ' ');
	}

	//Brevserver forventer en kvitteringsheader på nøyaktig 350 chars. Pad til 350
	public static String createBisysKvittering() {
		return StringUtils.rightPad(generateKvitteringHeader(FORMAT, BISYS_SYSTEM_ID, BREVREFERANSE).toString(), MELDING_HEADER_LENGTH, ' ');
	}

	public static String createBisysKvittering(String format) {
		return StringUtils.rightPad(generateKvitteringHeader(format, BISYS_SYSTEM_ID, BREVREFERANSE).toString(), MELDING_HEADER_LENGTH, ' ');
	}

	public static String createBisysKvitteringfeilNiva() {
		return StringUtils.rightPad(generateKvitteringHeader(PDF_CONTENTTYPE, BISYS_SYSTEM_ID, BREVREFERANSE, BREVPAKKE_FEILNIVA_FEIL, FEIL_UKJENT).toString(), MELDING_HEADER_LENGTH, ' ');
	}

	public static String createPesysKvitteringFeilNiva() {
		return StringUtils.rightPad(generateKvitteringHeader(PDF_CONTENTTYPE, PENSJON_SYSTEM_ID, BREVREFERANSE, BREVPAKKE_FEILNIVA_FEIL, FEIL_UKJENT).toString(), MELDING_HEADER_LENGTH, ' ');
	}

	public static String createBadXmlKvitteringHeader(String system) {
		return generateKvitteringHeader(FORMAT, system, BREVREFERANSE).toString();
	}

	public static StringBuilder generateKvitteringHeader(String contentType, String fagsystem, String brevref) {
		return generateKvitteringHeader(contentType, fagsystem, brevref, "0", "0");
	}

	public static StringBuilder generateKvitteringHeader(String contentType, String fagsystem, String brevref, String feilniva, String feilkode) {
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

	public static String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		return IOUtils.toString(inputStream, UTF_8);
	}

}