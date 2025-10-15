package no.nav.brevserver.bestillBrev;

import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {

	public static final String BISYS_SYSTEM_ID = "BI12";
	public static final String PENSJON_SYSTEM_ID = "PE01";
	public static final String BREVREFERANSE = "10000000000";
	public static final String TOKEN = "Token";

	public static final String CALLID = "dette-er-en-callID";

	public static String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		return IOUtils.toString(inputStream, UTF_8);
	}

	public static String createInputFromFagsystem(String fagsystem) {
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		builder.append("<rtv-brev direkteutskrift=\"NEI\" format=\"ENSIDIG\" malpakke=\"BI01.BI01X01\" sysid=\"").append(fagsystem).append("\" passord=\"Bisys123\" saksbehandler=\"B100946\">");
		addText(builder);
		return builder.toString();
	}

	public static String createInputFromFagsystem(String fagsystem, String modus) {
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		builder.append("<rtv-brev direkteutskrift=\"NEI\" klientToken=\"token\" modus=\"").append(modus).append("\" malpakke=\"BI01.BI01X01\" sysid=\"").append(fagsystem).append("\" passord=\"Bisys123\" saksbehandler=\"B100946\">");
		addText(builder);
		return builder.toString();
	}

	public static StringBuilder addText(StringBuilder builder) {
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

	//Det er noe tull med line separators om det går i en egen fil..

	public static String getHappyPathText(String fagsystem) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
			   "<rtv-brev direkteutskrift=\"NEI\" format=\"ENSIDIG\" malpakke=\"BI01.BI01X01\" sysid=\"" + fagsystem + "\" passord=\"Bisys123\" saksbehandler=\"B100946\"><brev brevref=\"10000000000\" spraak=\"NB\" tknr=\"0814\"><brevMottaker><navn>Donald</navn><adr1>Andeby 1</adr1><adr2>Borte</adr2><adr3>vekk</adr3><adr4/><bidrRolle>01</bidrRolle><fnr>11111111111</fnr><fDato>010134</fDato><postnr>1234</postnr><landKd/><spraak>NB</spraak></brevMottaker></brev></rtv-brev>";
	}

	public static String getBrevFinnesAlleredeString(String fagsystem) {
		return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
			   "<rtv-brevkvitt>\n" +
			   "<brevref>10000000000</brevref>\n" +
			   "<sysid>" + fagsystem + "</sysid>\n" +
			   "<type>null</type>\n" +
			   "<status>FEIL</status>\n" +
			   "<feilkode>90000003 Brevet eksisterer allerede</feilkode>\n" +
			   "</rtv-brevkvitt>";
	}

}