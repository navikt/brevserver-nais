package no.nav.brevserver.service.queue;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.service.queue.jms.MessageProducer;
import no.nav.brevserver.service.queue.jms.MessageProducerFactory;
import no.nav.brevserver.service.queue.xml.XMLService;
import no.nav.brevserver.service.queue.xml.XMLServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KoService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void sendKvittering(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe)
			throws BrevTechnicalException {
		String methSig = "sendKvittering(" + brev.getBrevreferanse() + ")";
		logger.info(methSig, "Sender kvittering, brevStatus: " + brev.getLagerStatus());

		KvitteringVO kvittering = createKvittering(brevstatus, brev);
		XMLService xmlService = XMLServiceFactory.getInstance().createXMLService();
		String xmlKvittering = xmlService.unmarshal(kvittering, brevstatus);

		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(systemType);
		producer.sendReturMelding(returKoe, false, null, xmlKvittering);
	}

	private KvitteringVO createKvittering(BrevStatusVO brevstatus, BrevVO brev) {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setBrevreferanse(brevstatus.getBrevreferanse());
		kvittering.setSystemID(brevstatus.getSystemID());
		kvittering.setLagerStatus(brevstatus.getStatus());
		kvittering.setContentType(brev.getContentType());
		return kvittering;
	}

}
