package no.nav.brevserver.service.xml;

import java.io.InputStream;
import java.io.StringReader;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;

public interface XMLService {
	/**
	 * Unmarshal er prosessen å generere XML fra businessobjekter
	 * 
	 * @param kvittering
	 *            kvittering
	 */
	String unmarshal(KvitteringVO kvittering, BrevStatusVO status);

	/**
	 * Metode for å populere mq-header Marshalling er prosessen der en populerer businessobjekter fra XML
	 * 
	 * @param xmlInput
	 *            xmlInput
	 * @throws BrevTechnicalException
	 */
	KvitteringVO marshalHeader(InputStream xmlInput) throws BrevTechnicalException;

	/**
	 * Metode for å populere brevstatus objekt. Marshalling er prosessen der en populerer businessobjekter fra XML
	 * 
	 * @param xmlInput
	 *            xmlInput
	 * @throws BrevTechnicalException
	 */
	BrevStatusVO marshalBrevStatus(StringReader xmlInput) throws BrevTechnicalException;
}
