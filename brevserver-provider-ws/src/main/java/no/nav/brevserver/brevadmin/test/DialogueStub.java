package no.nav.brevserver.brevadmin.test;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jms.JMSAccessor;
import no.nav.brevserver.server.common.jndi.JndiHelper;
import no.nav.brevserver.server.common.utility.ArgumentValidator;
import no.nav.brevserver.server.common.vo.FilType;
import no.nav.brevserver.server.common.vo.SysTilgangVO;
import no.nav.brevserver.service.brevserver.BrevserverService;
import no.nav.brevserver.service.brevserver.BrevserverServiceFactory;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 2145 $ $Author: t133126 $ $Date: 2013-07-23 12:06:48 +0200 (ti, 23 jul 2013) $
 */
public class DialogueStub {

	private static final String ENCODING = "UTF-8";

	public static String opprettBrev(DialogueStubData data) throws BrevException {
		JMSAccessor jmsAccessor = null;
		try {
			ArgumentValidator.isNotNull(data);
			ArgumentValidator.isNotNull("Dokid er ikke satt", data.brevref);

			// Read file
			String contentType = finnContentType(data);
			String status = FilType.PDF.getContentType().equals(contentType) ? "FERDIG" : "KLADD";

			byte[] filData = lesByteFil(data);

			// Create XML
			String xml = "<rtv-brevkvitt><brevref>" + data.brevref + "</brevref><sysid>BI01</sysid><status>" + status
					+ "</status><type>" + contentType + "</type><feilniva>3</feilniva></rtv-brevkvitt>";
			byte[] headerXML = xml.getBytes(ENCODING);

			// Lag korrekt mellomrom
			int headerlengde = ConfigManager.getInstance().getInt(ConfigManager.ARKIVER_HEADER_LENGDE,
					Konstanter.MELDING_HEADER_LENGTH);
			byte[] headerSpace = lagMellomrom(headerlengde - xml.length());

			// Send brev på kø
			jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(Konstanter.KONF_MOTTAK_DIALOGUE_ARKIV_BI);

			BytesMessage bytesMessage = jmsAccessor.createBytesMessage();
			bytesMessage.writeBytes(headerXML);
			bytesMessage.writeBytes(headerSpace);
			bytesMessage.writeBytes(filData);

			Queue replyQueue = JndiHelper.getInstance().lookup(Queue.class, Konstanter.KONF_DEAD_LETTER_BI);
			bytesMessage.setJMSReplyTo(replyQueue);

			jmsAccessor.sendMessage(bytesMessage, 0);

			String queueName = jmsAccessor.getQueueName();

			return queueName;

		} catch (JMSException je) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, je);

		} catch (BrevException e) {
			throw e;

		} catch (Exception e) {
			throw new BrevTechnicalException(e);

		} finally {
			JMSAccessor.close(jmsAccessor);
		}
	}

	public static String giTilgangTilBrevet(DialogueStubData data) throws BrevException {
		ArgumentValidator.isNotNull(data);
		ArgumentValidator.isNotNull("DokId må være satt", data.brevref);

		// Gi tilgang til brevet
		BrevserverService brevService = BrevserverServiceFactory.getInstance().createBrevserverService();
		SysTilgangVO tilgang = brevService.hentTilgang(data.system, true);

		JMSAccessor jmsAccessor = null;
		try {
			if (data.system.startsWith("PE")) {
				jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(Konstanter.KONF_MOTTAK_SAKSBEH_ONLINE_PE);
			} else {
				jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(Konstanter.KONF_MOTTAK_SAKSBEH_ONLINE_BI);
			}
			String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>" + "<rtv-brev sysid=\""
					+ data.system + "\" passord=\"" + tilgang.getPwd() + "\" saksbehandler=\"" + "BrevAdmin"
					+ "\" klientToken=\"" + data.token + "\" modus=\"frabrevlager\">" + "<brev brevref=\"" + data.brevref
					+ "\"/>" + "</rtv-brev>";

			String result = jmsAccessor.getQueueName();
			jmsAccessor.sendTextMessage(xml, 0);
			return result;
		} finally {
			JMSAccessor.close(jmsAccessor);
		}
	}

	public static String bestillBrev(DialogueStubData data) throws BrevException {
		String result = null;

		JMSAccessor jmsAccessor = null;
		try {
			ArgumentValidator.isNotNull(data);
			ArgumentValidator.isNotNull("Dokid er ikke satt", data.brevref);

			if (data.filnavn.indexOf(".xml") == -1) {
				throw new BrevTechnicalException("Filen må være en xml-fil");
			}

			StringBuffer filData = lesTextFil(data);

			// Send brev på kø
			jmsAccessor = JMSAccessor.getAccessorUsingQueueJndiName(Konstanter.KONF_MOTTAK_SAKSBEH_ONLINE_BI);

			TextMessage txtMessage = jmsAccessor.createTextMessage(filData.toString());

			Queue replyQueue = JndiHelper.getInstance().lookup(Queue.class, Konstanter.KONF_DEAD_LETTER_BI);
			txtMessage.setJMSReplyTo(replyQueue);

			jmsAccessor.sendMessage(txtMessage, 0);

			result = jmsAccessor.getQueueName();

		} catch (JMSException je) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, je);

		} catch (BrevException e) {
			throw e;

		} finally {
			JMSAccessor.close(jmsAccessor);
		}
		return result;
	}

	private static byte[] lagMellomrom(int l) {
		byte[] headerSpace = new byte[l];
		for (int i = 0; i < headerSpace.length; i++) {
			headerSpace[i] = 0;
		}

		return headerSpace;
	}

	private static String finnContentType(DialogueStubData data) throws BrevTechnicalException {

		if (data.filnavn.indexOf(".rtf") > 0) {
			return FilType.RTF.getContentType();

		} else if (data.filnavn.indexOf(".pdf") > 0) {
			return FilType.PDF.getContentType();
		}

		throw new BrevTechnicalException("Filen må være enten .rtf eller .pdf");
	}

	private static byte[] lesByteFil(DialogueStubData data) throws BrevException {
		byte[] result = null;

		try {

			File f = new File(data.filnavn);
			if (!f.exists()) {
				throw new BrevException("Filen " + data.filnavn + " finnes ikke");
			}

			result = new byte[(int) f.length()];
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			in.readFully(result);
			in.close();

		} catch (IOException ioe) {
			throw new BrevTechnicalException(ioe);
		}

		return result;
	}

	private static StringBuffer lesTextFil(DialogueStubData data) throws BrevException {

		StringBuffer result = null;

		try {

			File f = new File(data.filnavn);
			if (!f.exists()) {
				throw new BrevException("Filen " + data.filnavn + " finnes ikke");
			}

			BufferedReader in = new BufferedReader(new FileReader(f));
			result = new StringBuffer((int) f.length());

			String line = null;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}

			in.close();

		} catch (IOException ioe) {
			throw new BrevTechnicalException(ioe);
		}

		return result;
	}

}
