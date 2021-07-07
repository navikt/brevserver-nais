package no.nav.brevserver.command;

import no.nav.brevserver.converter.VoTilBrevstatusConverter;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.PerformanceLogger;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import no.nav.brevserver.server.common.vo.MessageVO;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;
import no.nav.brevserver.service.jms.MessageProducer;
import no.nav.brevserver.service.jms.MessageProducerFactory;
import no.nav.brevserver.service.xml.XMLService;
import no.nav.brevserver.service.xml.XMLServiceFactory;

import java.io.StringReader;

/**
 * Kommando som kalles n�r det kommer en bestilling av brev fra Pensjon p� brevbestillingsk�en.
 *
 * @author Dag Kristiansen
 */
public class PEBestillBrevCommand extends AbstractCommand {
	private BrevStatusVO brevStatusVo;
	private VoTilBrevstatusConverter voTilBrevstatus = new VoTilBrevstatusConverter();

	public PEBestillBrevCommand(MessageVO messageVO) {
		super(messageVO);
	}

	/**
	 * Validerer meldingen f�r bestilling gj�res. 'Validering' betyr � lese XML uten feil, samt hente ut brevreferanse og
	 * returk� uten feil.
	 *
	 * @throws BrevTechnicalException
	 */
	private void validate() throws BrevTechnicalException {
		String methSig = "PEBestillBrevCommand.validate()";
		XmlLogger.logXml(SystemType.PE, messageVo);

		brevStatusVo = new BrevStatusVO();
		XMLService service = XMLServiceFactory.getInstance().createXMLService();
		StringReader reader = new StringReader(messageVo.getStringBody());
		brevStatusVo = service.marshalBrevStatus(reader);
		messageVo.setBrevreferanse(brevStatusVo.getBrevreferanse());
		brevStatusVo.setReturKoe(messageVo.getReplyQueueName());
		messageVo.setTilgangsXML(Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus()));

		if (!brevStatusVo.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + brevStatusVo.getSystemID() + "', forventet prefix: '"
					+ SystemType.PE + "'";
			log.error(methSig, errorMessage);
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, errorMessage, null);
		}

		try {
			notEmpty("Brevreferanse", brevStatusVo.getBrevreferanse(), true);
			notEmpty("Systemid", brevStatusVo.getSystemID(), false);
			notEmpty("Returk�", brevStatusVo.getReturKoe(), false);
		} catch (BrevException e) {
			log.error(methSig, "Ugyldig XML mottatt for brevreferanse " + messageVo.getBrevreferanse(), e);
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
		}
	}

	/**
	 * Bestill pensjonsbrev i Dialogue basert p� melding mottatt p� BREVSERVER_ONLINEBREV_PE
	 *
	 * @throws BrevException
	 */
	public void execute() throws BrevException {
		validate();

		String brevReferanse = brevStatusVo.getBrevreferanse();
		String methSig = "PEBestillBrevCommand.execute(" + brevReferanse + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);
		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.PE);

		try {
			BrevserverService brevserverService = BrevserverServiceFactory.getInstance().createBrevserverService();
			if (messageVo.isTilgangsXML()) {
				giTilgang(methSig, brevserverService);
			} else {
				bestillBrev(brevReferanse, methSig, producer, brevserverService);
			}
		} finally {
			p.stop();
		}
	}

	private void bestillBrev(String brevReferanse, String methSig, MessageProducer producer,
							 BrevserverService brevserverService)
			throws BrevTechnicalException {
		Brevstatus brevEksisterer = brevserverService.hentBrevStatus(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse());

		if (brevEksisterer != null) {
			log.warning(methSig, "Brevet eksisterer fra f�r " + brevReferanse);
			String feilmelding = lagFeilmelding(Konstanter.FEIL_BREV_EKSISTERER);
			producer.sendReturMelding(brevStatusVo.getReturKoe(), false, messageVo.getCorrelationID(), feilmelding);
		} else {
			brevStatusVo.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
			brevserverService.lagreBrevStatus(voTilBrevstatus.convert(brevStatusVo), brevStatusVo.getToken());
			producer.sendToDialogue(messageVo);
			log.info(methSig, "Brevet er sendt til bestilling/opprettelse i Dialogue");
		}
	}

	private void giTilgang(String methSig, BrevserverService brevserverService) throws BrevTechnicalException {
		boolean ok = brevserverService.lagreTilgang(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse(),
				brevStatusVo.getToken());
		if (ok) {
			log.debug(methSig, "Token '" + brevStatusVo.getCensoredToken() + "' er satt for systemID '" + brevStatusVo.getSystemID()
					+ "' for brevreferanse '" + brevStatusVo.getBrevreferanse() + "'");
			log.info(methSig, "Tilgang gitt for systemID '" + brevStatusVo.getSystemID() + "'");
		} else {
			log.warning(methSig,
					"Kunne ikke gi tilgang '" + brevStatusVo.getCensoredToken() +
							"' for systemID '" + brevStatusVo.getSystemID() + "'");
		}
	}

	/**
	 * Lag feilmelding som skal legges p� returk� dersom noe galt skjer.
	 *
	 * @param feilType
	 * @return
	 */
	private String lagFeilmelding(String feilType) {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setSystemID(brevStatusVo.getSystemID());
		kvittering.setBrevreferanse(brevStatusVo.getBrevreferanse());
		kvittering.setFeilkode(feilType);
		brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

		XMLService service = XMLServiceFactory.getInstance().createXMLService();
		return service.unmarshal(kvittering, brevStatusVo);
	}

	public Object getResult() {
		return brevStatusVo;
	}
}
