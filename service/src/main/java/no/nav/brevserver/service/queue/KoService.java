package no.nav.brevserver.service.queue;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.service.queue.jms.BIMessageProducer;
import no.nav.brevserver.service.queue.jms.PEMessageProducer;
import no.nav.brevserver.service.queue.xml.XMLService;
import no.nav.brevserver.service.queue.xml.XMLServiceFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KoService {

	private final BIMessageProducer biMessageProducer;
	private final PEMessageProducer peMessageProducer;

	public KoService(BIMessageProducer biMessageProducer, PEMessageProducer peMessageProducer) {
		this.biMessageProducer = biMessageProducer;
		this.peMessageProducer = peMessageProducer;
	}

	public void sendKvittering(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe)
			throws BrevTechnicalException {
		log.info("Sender kvittering, brevStatus: " + brev.getLagerStatus());

		KvitteringVO kvittering = createKvittering(brevstatus, brev);
		XMLService xmlService = XMLServiceFactory.getInstance().createXMLService();
		String xmlKvittering = xmlService.unmarshal(kvittering, brevstatus);

		if(systemType.equals(SystemType.PE)){
			peMessageProducer.sendReturMelding(returKoe, false, null, xmlKvittering);
		}else{
			biMessageProducer.sendReturMelding(returKoe, false, null, xmlKvittering);
		}

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
