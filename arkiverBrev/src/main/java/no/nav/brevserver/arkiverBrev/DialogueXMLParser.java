package no.nav.brevserver.arkiverBrev;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.KvitteringVO;
import org.springframework.stereotype.Component;

/**
 * Beskrivelse av klassen
 * 
 * @author Dag Kristiansen
 */
@Component
public class DialogueXMLParser {

	private static XMLService xmlService;
	public DialogueXMLParser(XMLService xmlService){
		this.xmlService = xmlService;
	}

	/**
	 * Metode for å skille ut meldingsheaderen og brevdata og putte disse inn i et kvitteringsobjekt.
	 * 
	 * @param bytesMelding
	 *            Meldingen
	 * @return KvitteringVO med meldingskropp og vedlegg
	 * @throws BrevTechnicalException
	 */
	public static KvitteringVO lagKvitteringVOFraDialogueMelding(byte[] bytesMelding) throws BrevTechnicalException {
		int meldingslengde = bytesMelding.length;

		// Ta ut header
		int headerLengde = finnHeaderlengde(bytesMelding);
		byte[] header = new byte[headerLengde];
		System.arraycopy(bytesMelding, 0, header, 0, headerLengde);

		StringBuffer strHeader = new StringBuffer();

		for (int i = 0; i < header.length; i++) {
			char ch = header[i] == 0 ? ' ' : (char) header[i];
			strHeader.append(ch);
		}

		// kopierer ut brevdelen (resterende del av meldingen)
		byte[] brevData;
		if (meldingslengde <= headerLengde) {
			brevData = new byte[0];
		} else {
			brevData = new byte[(meldingslengde - headerLengde)];
			System.arraycopy(bytesMelding, headerLengde, brevData, 0, brevData.length);
		}

		// parserer XML fra headeren
		for (int i = 0; i < header.length; i++) {
			// xml-parseren sliter med 0 så vi simulerer et mellomrom
			if (header[i] == 0) {
				header[i] = ' ';
			}
			if (header[i] == 20) {
				header[i] = ' ';
			}
		}

		java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream(header);

		KvitteringVO kvittering = xmlService.marshalHeader(is);
		kvittering.setBrevdata(brevData);
		return kvittering;
	}

	private static int finnHeaderlengde(byte[] melding) {
		int headerLengde = ConfigManager.getInstance().getInt(ConfigManager.ARKIVER_HEADER_LENGDE,
				Konstanter.MELDING_HEADER_LENGTH);
		int tegnIStarten = ConfigManager.getInstance().getInt(ConfigManager.ARKIVER_HEADER_LENGDE_TEGN_I_STARTEN, 5);

		// Hente ut headerlengde fra de første tegnene i meldingen
		if (tegnIStarten > 0 && tegnIStarten < 10) {
			if (melding.length > tegnIStarten) {
				String strLengde = "";
				for (int i = 0; i < tegnIStarten; i++) {
					char ch = (melding[i] == 0) ? '0' : (char) melding[i];
					strLengde += ch;
				}

				headerLengde = Integer.parseInt(strLengde);

				// Hvis vellykket : Fjerne lengden slik at parsing av XML fungerer
				for (int i = 0; i < tegnIStarten; i++) {
					melding[i] = ' ';
				}
			}
		}

		return headerLengde;
	}
}
