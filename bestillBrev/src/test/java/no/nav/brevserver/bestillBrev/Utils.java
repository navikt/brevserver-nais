package no.nav.brevserver.bestillBrev;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.FilType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

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
		return StringUtils.rightPad(generateKvitteringHeader(FORMAT, PENSJON_SYSTEM_ID, BREVREFERANSE).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	public static String createPesysKvittering(String format){
		return StringUtils.rightPad(generateKvitteringHeader(format, PENSJON_SYSTEM_ID, BREVREFERANSE).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	//Brevserver forventer en kvitteringsheader på nøyaktig 350 chars. Pad til 350
	public static String createBisysKvittering() {
		return StringUtils.rightPad(generateKvitteringHeader(FORMAT, BISYS_SYSTEM_ID, BREVREFERANSE).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
	}

	public static String createBisysKvittering(String format) {
		return StringUtils.rightPad(generateKvitteringHeader(format, BISYS_SYSTEM_ID, BREVREFERANSE).toString(), Konstanter.MELDING_HEADER_LENGTH, ' ');
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

	public static String createInput(String fagsystem){
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		builder.append("<rtv-brev direkteutskrift=\"NEI\" format=\"ENSIDIG\" malpakke=\"BI01.BI01X01\" sysid=\"").append(fagsystem).append("\" passord=\"Bisys123\" saksbehandler=\"B100946\">");
		addText(builder);
		return builder.toString();
	}

	public static String createInput(String fagsystem, String modus){
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		builder.append("<rtv-brev direkteutskrift=\"NEI\" klientToken=\"token\" modus=\"").append(modus).append("\" malpakke=\"BI01.BI01X01\" sysid=\"").append(fagsystem).append("\" passord=\"Bisys123\" saksbehandler=\"B100946\">");
		addText(builder);
		return builder.toString();
	}

	public static StringBuilder addText(StringBuilder builder){
		builder.append("<brev brevref=\"").append(BREVREFERANSE).append("\" spraak=\"NB\" tknr=\"0814\">");
		builder.append("<brevMottaker>");
		builder.append("<navn>").append("Donald").append("</navn>");
		builder.append("<adr1>").append("Andeby 1").append("</adr1>");
		builder.append("<adr2>").append("Borte").append("</adr2>");
		builder.append("<adr3>").append("vekk").append("</adr3>");
		builder.append("<adr4/>");
		builder.append("<bidrRolle>").append("01").append("</bidrRolle>");
		builder.append("<fnr>").append("11111111111").append("</fnr>");
		builder.append("<fDato>").append("010134").append("</fDato>");
		builder.append("<postnr>").append(1234).append("</postnr>");
		builder.append("<landKd/>");
		builder.append("<spraak>").append("NB").append("</spraak>");
		builder.append("</brevMottaker>");
		builder.append("</brev>");
		builder.append("</rtv-brev>");
		return builder;
	}

	public static BrevStatusVO createDefaultBrevstatus(String systemId) {
		BrevStatusVO brevstatus = new BrevStatusVO();
		brevstatus.setBrevreferanse(BREVREFERANSE);
		brevstatus.setSystemID(systemId);
		brevstatus.setReturKoe("svarKo");
		brevstatus.setBestillerBrukerID("B100946");
		brevstatus.setBrevmal("BI01.BI01X01");
		brevstatus.setPassord(SYSTEM_PASSORD);
		return brevstatus;
	}

	//Det er noe tull med line separators om det går i en egen fil..

	public static String getHappyPathText(String fagsystem){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
				"<rtv-brev direkteutskrift=\"NEI\" format=\"ENSIDIG\" malpakke=\"BI01.BI01X01\" sysid=\""+fagsystem+"\" passord=\"Bisys123\" saksbehandler=\"B100946\"><brev brevref=\"10000000000\" spraak=\"NB\" tknr=\"0814\"><brevMottaker><navn>Donald</navn><adr1>Andeby 1</adr1><adr2>Borte</adr2><adr3>vekk</adr3><adr4/><bidrRolle>01</bidrRolle><fnr>11111111111</fnr><fDato>010134</fDato><postnr>1234</postnr><landKd/><spraak>NB</spraak></brevMottaker></brev></rtv-brev>";
	}
	public static String getBrevFinnesAlleredeString(String fagsystem){
		return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
				"<rtv-brevkvitt>\n" +
				"<brevref>10000000000</brevref>\n" +
				"<sysid>"+fagsystem+"</sysid>\n" +
				"<type>null</type>\n" +
				"<status>FEIL</status>\n" +
				"<feilkode>90000003 Brevet eksisterer allerede</feilkode>\n" +
				"</rtv-brevkvitt>";
	}
	public static String getBadPasswordString(){
		return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
				"<rtv-brevkvitt>\n" +
				"<brevref>10000000000</brevref>\n" +
				"<sysid>BI12</sysid>\n" +
				"<type>null</type>\n" +
				"<status>FEIL</status>\n" +
				"<feilkode>90000000 Ikke tilgang</feilkode>\n" +
				"</rtv-brevkvitt>";
	}
}
