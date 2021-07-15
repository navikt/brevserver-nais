package no.nav.brevserver.service.xml.beans;

import java.io.IOException;
import java.io.StringReader;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.service.xml.XMLService;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLServiceBean implements XMLService {
    /**
     * Unmarshal er prosessenågenerere XML fra businessobjekter
     * 
     * @param kvittering
     * @return String ferdig generert xml
     */
    public String unmarshal(KvitteringVO kvittering, BrevStatusVO status) {
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
     * Henter ut headerinformasjonen av en melding og returnerer en
     * objektrepresentasjon av resultatet.
     * 
     * (Marshalling er prosessen der en populerer businessobjekter fra XML)
     * 
     * @param xmlInput
     * @return ValueObject
     * @throws BrevTechnicalException
     */
    public KvitteringVO marshalHeader(java.io.InputStream xmlInput) throws BrevTechnicalException {
        XMLHandler handler;
        handler = marshal(xmlInput);
        return handler.getKvittering();
    }

    /**
     * Henter ut brevstatus Marshalling er prosessen der en populerer
     * businessobjekter fra XML
     * 
     * @param xmlInput
     * @return ValueObject
     * @throws BrevTechnicalException
     */
    public BrevStatusVO marshalBrevStatus(StringReader xmlInput) throws BrevTechnicalException {
        XMLHandler handler = marshal(xmlInput);
        return handler.getBrevStatus();
    }

    private XMLHandler marshal(java.io.InputStream xmlInput) throws BrevTechnicalException {
        XMLHandler handler = XMLHandlerFactory.getInstance().createXmlHandler();

        try {
            SAXParser saxParser = new SAXParser();

            saxParser.setContentHandler(handler);
            saxParser.setErrorHandler(handler);

            InputSource source = new InputSource(xmlInput);
            saxParser.parse(source);
        } catch (SAXException e) {
            throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
        } catch (IOException e) {
            throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
        }

        return handler;
    }

    private XMLHandler marshal(StringReader xmlInput) throws BrevTechnicalException {
        XMLHandler handler = XMLHandlerFactory.getInstance().createXmlHandler();

        try {
            SAXParser saxParser = new SAXParser();

            saxParser.setContentHandler(handler);
            saxParser.setErrorHandler(handler);

            InputSource source = new InputSource(xmlInput);
            saxParser.parse(source);

        } catch (Exception e) {
            throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
        }

        return handler;
    }
}
