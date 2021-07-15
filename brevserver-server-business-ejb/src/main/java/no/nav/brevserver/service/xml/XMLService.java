package no.nav.brevserver.service.xml;

import java.io.InputStream;
import java.io.StringReader;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;

public interface XMLService {
	/**
	 * Unmarshal er prosessenågenerere XML fra businessobjekter
	 * 
	 * @param kvittering
	 *            kvittering
	 */
	String unmarshal(KvitteringVO kvittering, BrevStatusVO status);

	/**
	 * Metode foråpopulere mq-header Marshalling er prosessen der en populerer businessobjekter fra XML
	 * 
	 * @param xmlInput
	 *            xmlInput
	 * @throws BrevTechnicalException
	 */
	KvitteringVO marshalHeader(InputStream xmlInput) throws BrevTechnicalException;

	/**
	 * Metode foråpopulere brevstatus objekt. Marshalling er prosessen der en populerer businessobjekter fra XML
	 * 
	 * @param xmlInput
	 *            xmlInput
	 * @throws BrevTechnicalException
	 */
	BrevStatusVO marshalBrevStatus(StringReader xmlInput) throws BrevTechnicalException;
}
