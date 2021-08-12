package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.arkiverBrev.DialogueXMLParser;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.BrevlagerService;
import no.nav.brevserver.service.BrevstatusService;
import no.nav.brevserver.service.converter.BrevstatusTilVoConverter;
import no.nav.brevserver.service.queue.jms.MessageProducer;
import no.nav.brevserver.service.queue.jms.MessageProducerFactory;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

/**
 * Håndterer meldinger fra Dialogue. Lagrer brev og setter status og sender kvittering til saksbehandlingsystemet.
 *
 * @author Holger Zobel, Accenture
 */
@Slf4j
@Component
public class ArkiverBrevService {

	private final BrevstatusService brevstatusService;
	private final BrevlagerService brevlagerService;
	private final BrevstatusTilVoConverter converter;

	public ArkiverBrevService(BrevstatusService brevstatusService, BrevlagerService brevlagerService, BrevstatusTilVoConverter converter) {
		this.brevstatusService = brevstatusService;
		this.brevlagerService = brevlagerService;
		this.converter = converter;
	}

	private KvitteringVO generateKvittering(MessageVO messageVo) throws BrevTechnicalException {
		KvitteringVO kvittering;

		try {
			kvittering = DialogueXMLParser.lagKvitteringVOFraDialogueMelding(messageVo.getByteBody());
		} catch (BrevTechnicalException e) {
			log.error("Ugyldig XML: ", e);
			throw e;
		}

		if (kvittering.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + kvittering.getSystemID()
					+ "', forventet ikke pensjonsbrev";
			log.error(errorMessage);
			throw new BrevTechnicalException(errorMessage);
		}

		return kvittering;
	}

	/**
	 * Forespørsel lagres i databasen. Deretter sendes den originale meldingen videre på definert kø.
	 *
	 */
	//TODO: fix exceptions
	@Handler
	public BrevStatusVO execute(MessageVO messageVo) throws BrevException {
		KvitteringVO kvittering = generateKvittering(messageVo);

		ArgumentValidator.isNotNull(kvittering);

		messageVo.setBrevreferanse(kvittering.getBrevreferanse());

		BrevStatusVO brevStatusVo = brevstatusService.hentBrevStatus(kvittering.getSystemID(), kvittering.getBrevreferanse());
		// Hvis ingen status så opprett en basert på det man vet
		if (brevStatusVo == null) {
			brevStatusVo = new BrevStatusVO();
		}
		if (brevStatusVo.getSystemID() == null) {
			brevStatusVo.setSystemID(kvittering.getSystemID());
		}
		if (brevStatusVo.getBrevreferanse() == null) {
			brevStatusVo.setBrevreferanse(kvittering.getBrevreferanse());
		}
		if (brevStatusVo.getBrevmal() == null) {
			brevStatusVo.setBrevmal(kvittering.getTmpMalpakke());
		}
		if (brevStatusVo.getReturKoe() == null) {
			brevStatusVo.setReturKoe(messageVo.getReplyQueueName());
		}

		// Hvis feilnivå er 0x så endre til x
		if (kvittering.getFeilniva() != null && kvittering.getFeilniva().length() > 1
				&& kvittering.getFeilniva().charAt(0) == '0') {
			kvittering.setFeilniva(kvittering.getFeilniva().substring(1));
		}

		// Hvis brevet eksisterer allerede så gi feilmelding
		if (Konstanter.BREVSTATUS_FERDIG.equals(brevStatusVo.getStatus())) {
			kvittering.setFeilkode(Konstanter.FEIL_BREV_EKSISTERER);
			brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

			// Ved feilmelding fra dialogue så gi feilmelding
		} else if (kvittering.getFeilniva() == null ||
				kvittering.getFeilniva().equals(Konstanter.BREVPAKKE_FEILNIVA_FEIL)) {
			brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

			if (brevStatusVo.getBrevreferanse() != null && brevStatusVo.getSystemID() != null) {
				brevstatusService.lagreBrevStatus(brevStatusVo);
			}

			// Alt gikk bra
		} else {
			if (FilType.PDF.getContentType().equals(kvittering.getContentType())) {
				kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_FERDIG);
				brevStatusVo.setStatus(Konstanter.BREVSTATUS_FERDIG);
			} else {
				kvittering.setLagerStatus(Konstanter.BREVLAGER_STATUS_KLADD);
				brevStatusVo.setStatus(Konstanter.BREVSTATUS_LAGRET_KLADD);
			}
			// Lagre i Brevlageret
			brevlagerService.lagreBrev(kvittering, brevStatusVo);

			log.info("Brevet er arkivert i Brevlageret");
		}

		//TODO: Dette burde vel inn i routen
		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.BI);
		producer.sendKvittering(brevStatusVo, messageVo, kvittering);
		return brevStatusVo;

	}
}

