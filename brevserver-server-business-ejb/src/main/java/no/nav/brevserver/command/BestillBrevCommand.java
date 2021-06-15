package no.nav.brevserver.command;

import no.nav.brevserver.converter.VoTilBrevstatusConverter;
import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.type.SystemType;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
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
 * Klassen håndterer bestilling av brev fra saksbehandlingssystemene. Bestillingene videresendes til Dialogue.
 * Klassen håndterer også tilgang til brev fra brevklient.
 *
 * @author Holger Zobel, Accenture
 * @author Morten Lileng, Cap Gemini Ernst & Young
 */
public class BestillBrevCommand extends AbstractCommand {
	BrevStatusVO brevStatusVo;
	private VoTilBrevstatusConverter converter = new VoTilBrevstatusConverter();

	public BestillBrevCommand(MessageVO message) {
		super(message);
	}

	private void validate() throws BrevTechnicalException {
		String methSig = "PEBestillBrevCommand.validate()";
		XmlLogger.logXml(SystemType.BI, messageVo);

		XMLService service = XMLServiceFactory.getInstance().createXMLService();
		StringReader reader = new StringReader(messageVo.getStringBody());

		try {
			brevStatusVo = service.marshalBrevStatus(reader);
			messageVo.setTilgangsXML(brevStatusVo != null && Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus()));
		} catch (BrevTechnicalException e) {
			log.error("BestillBrevCommand.validate()", "Ugyldig XML: " + messageVo.getStringBody());
			throw e;
		}

		brevStatusVo.setReturKoe(messageVo.getReplyQueueName());
		messageVo.setBrevreferanse(brevStatusVo.getBrevreferanse());

		if (brevStatusVo.getSystemID().startsWith(SystemType.PE.toString())) {
			String errorMessage = "Brev med feil systemID mottatt: '" + brevStatusVo.getSystemID()
					+ "', forventet ikke pensjonsbrev";
			log.error("BestillBrevCommand.validate()", errorMessage);
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, errorMessage, null);
		}

		try {
			notEmpty("Brevreferanse", brevStatusVo.getBrevreferanse(), false);
			notEmpty("Systemid", brevStatusVo.getSystemID(), false);
			notEmpty("Returkø", brevStatusVo.getReturKoe(), false);
		} catch (BrevException e) {
			log.error(methSig, "Ugyldig XML mottatt for brevreferanse " + messageVo.getBrevreferanse(), e);
			throw new BrevTechnicalException(BrevTechnicalException.FEIL_I_XML, e);
		}
	}

	/**
	 * Forespørsel lagres i databasen. Deretter sendes den originale meldingen videre på definert kø.
	 *
	 * @throws BrevException
	 * @see AbstractCommand#execute()
	 */
	public void execute() throws BrevException {
		validate();

		ArgumentValidator.isNotNull(brevStatusVo);
		String sig = "BestillBrevCommand.execute(" + brevStatusVo.getBrevreferanse() + ")";
		PerformanceLogger p = new PerformanceLogger(sig);

		MessageProducer producer = MessageProducerFactory.getInstance().createMessageProducer(SystemType.BI);

		try {
			BrevserverService brevserverService = BrevserverServiceFactory.getInstance().createBrevserverService();

			if (!brevserverService.sjekkSystemTilgang(brevStatusVo.getSystemID(), brevStatusVo.getPassord())) {
				log.warning(sig, "Feil systempassord for melding fra " + brevStatusVo.getSystemID());
				String xmlKvittering = lagFeilmelding(Konstanter.FEIL_IKKE_SYSTEM_TILGANG);
				producer.sendReturMelding(brevStatusVo.getReturKoe(), false, messageVo.getCorrelationID(), xmlKvittering);
				return;
			}

			// Hvis modus="frabrevlager" ønsker et fagsystem å gi en tilgang til brevet fra brevklient med en token
			if (brevStatusVo.getModus() != null && Konstanter.BREVMODUS_FRALAGER.equals(brevStatusVo.getModus())) {
				// Lagre token
				boolean ok = brevserverService.lagreTilgang(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse(),
						brevStatusVo.getToken());
				if (ok) {
					log.info(sig, "Tilgang gitt for systemID '" + brevStatusVo.getSystemID() + "'");
				} else {
					log.warning(
							sig,
							"Kunne ikke gi tilgang '" + brevStatusVo.getCensoredToken() + "' for systemID '"
									+ brevStatusVo.getSystemID() + "'");
				}
				// Bestille brevet fra Dialogue
			} else {
				Brevstatus tmp = brevserverService
						.hentBrevStatus(brevStatusVo.getSystemID(), brevStatusVo.getBrevreferanse());
				if (tmp != null) {
					// Brevet eksisterer fra før, returner feilmelding
					log.warning(sig, "Brevet eksisterer fra før " + brevStatusVo.getBrevreferanse());
					String xmlKvittering = lagFeilmelding(Konstanter.FEIL_BREV_EKSISTERER);
					producer.sendReturMelding(brevStatusVo.getReturKoe(), false, messageVo.getCorrelationID(), xmlKvittering);
					return;
				}

				brevStatusVo.setStatus(Konstanter.BREVSTATUS_BREVPAKKE);
				brevserverService.lagreBrevStatus(converter.convert(brevStatusVo), brevStatusVo.getToken());

				// Sende meldingen videre til Dialogue
				producer.sendToDialogue(messageVo);

				log.info(sig, "Brevet er sendt til bestilling/opprettelse i Dialogue");
			}
		} finally {
			p.stop();
		}
	}

	/**
	 * Lager feilmelding basert på feiltype.
	 *
	 * @param feilType
	 * @return Feilmeldings-XML
	 */
	private String lagFeilmelding(String feilType) {
		KvitteringVO kvittering = new KvitteringVO();
		kvittering.setSystemID(brevStatusVo.getSystemID());
		kvittering.setBrevreferanse(brevStatusVo.getBrevreferanse());
		kvittering.setFeilkode(feilType);
		brevStatusVo.setStatus(Konstanter.BREVSTATUS_FEIL);

		XMLService service = XMLServiceFactory.getInstance().createXMLService();
		String xmlKvittering = service.unmarshal(kvittering, brevStatusVo);
		return xmlKvittering;
	}

	/**
	 * @see no.nav.brevserver.command.AbstractCommand#getResult() ()
	 */
	public Object getResult() {
		return brevStatusVo;
	}
}