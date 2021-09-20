package no.nav.brevserver.service.queue;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.core.constants.SystemType;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.utils.xmlHandlers.XMLService;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.core.vo.BrevVO;
import no.nav.brevserver.core.vo.KvitteringVO;
import org.springframework.stereotype.Service;

@Service
@Slf4j
//Gammel klasse for å sende til kø.
//Kan vel nå bare sende rett til route?
public class KoService {


	public KoService() {	}

	public void sendKvittering(BrevVO brev, BrevStatusVO brevstatus, SystemType systemType, String returKoe)
			throws BrevTechnicalException {
		log.info("Sender kvittering, brevStatus: " + brev.getLagerStatus());

		KvitteringVO kvittering = createKvittering(brevstatus, brev);
		String xmlKvittering = XMLService.unmarshal(kvittering, brevstatus);

		if(systemType.equals(SystemType.PE)){
			//peMessageProducer.sendReturMelding(returKoe, false, null, xmlKvittering);
		}else{
			//biMessageProducer.sendReturMelding(returKoe, false, null, xmlKvittering);
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
