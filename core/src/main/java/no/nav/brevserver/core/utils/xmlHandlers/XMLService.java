package no.nav.brevserver.core.utils.xmlHandlers;

import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.KvitteringVO;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;

public class XMLService {

	private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newDefaultInstance();

	/**
	 * Unmarshal er prosessen å generere XML fra businessobjekter
	 *
	 * @param kvittering
	 * @return String ferdig generert xml
	 */
	public static String unmarshal(KvitteringVO kvittering, BrevStatusVO status) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<rtv-brevkvitt>\n<brevref>");
		sb.append(kvittering.getBrevreferanse());
		sb.append("</brevref>\n<sysid>");
		sb.append(kvittering.getSystemID());
		sb.append("</sysid>\n<type>");
		sb.append(kvittering.getContentType());
		sb.append("</type>\n<status>");
		sb.append(status.getStatus());
		sb.append("</status>\n<feilkode>");
		sb.append(kvittering.getFeilkode());
		sb.append("</feilkode>\n");
		sb.append("</rtv-brevkvitt>");
		return sb.toString();
	}

	/**
	 * Henter ut brevstatus Marshalling er prosessen der en populerer
	 * businessobjekter fra XML
	 *
	 * @param xmlInput
	 * @return ValueObject
	 * @throws BrevTechnicalException
	 */
	public static BrevStatusVO marshalBrevStatus(StringReader xmlInput) throws BrevTechnicalException {
		XMLHandler handler = marshal(xmlInput);
		return handler.getBrevStatus();
	}

	/**
	 * Henter ut headerinformasjonen av en melding og returnerer en
	 * objektrepresentasjon av resultatet.
	 * <p>
	 * (Marshalling er prosessen der en populerer businessobjekter fra XML)
	 *
	 * @param xmlInput
	 * @return ValueObject
	 * @throws BrevTechnicalException
	 */
	public static KvitteringVO marshalHeader(java.io.InputStream xmlInput) throws BrevTechnicalException {
		XMLHandler xmlHandler = new XMLHandler();

		try {
			final SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
			InputSource source = new InputSource(xmlInput);
			saxParser.parse(source, xmlHandler);
		} catch (SAXException | ParserConfigurationException e) {
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
		} catch (IOException e) {
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
		}

		return xmlHandler.getKvittering();
	}

	private static XMLHandler marshal(StringReader xmlInput) throws BrevTechnicalException {
		XMLHandler xmlHandler = new XMLHandler();

		try {
			final SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
			InputSource source = new InputSource(xmlInput);
			saxParser.parse(source, xmlHandler);
			return xmlHandler;
		} catch (Exception e) {
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
		}
	}
}
