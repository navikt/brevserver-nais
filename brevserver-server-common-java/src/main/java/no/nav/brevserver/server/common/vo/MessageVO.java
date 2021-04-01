package no.nav.brevserver.server.common.vo;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;

import no.nav.brevserver.server.common.config.Konstanter;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;

public class MessageVO implements Serializable {
	private static final long serialVersionUID = 8684875760609707790L;

	private String replyQueueName;
	private String correlationID;
	private byte[] byteBody;
	private String stringBody;

	private String brevreferanse = null;
	private boolean tilgangsXML = false;
	
	public MessageVO(Message msg) throws BrevTechnicalException {
		try {
			if (msg instanceof BytesMessage) {
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				int returnValue = 0;
				while ((returnValue = ((BytesMessage) msg).readBytes(buffer)) != -1) {
					bos.write(buffer, 0, returnValue);
				}
				byteBody = bos.toByteArray();
			} else if (msg instanceof TextMessage) {
				TextMessage tmsg = (javax.jms.TextMessage) msg;
				stringBody = tmsg.getText();
			}

			if (msg.getJMSReplyTo() != null) {
				setReplyQueueName(((Queue) msg.getJMSReplyTo()).getQueueName());
			}
			setCorrelationID(msg.getJMSCorrelationID());

		} catch (JMSException e) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, e);
		}
	}

	public String getBodySample() {
		if (byteBody != null) {
			return new String(byteBody, 0, 20);
		} else if (stringBody != null) {
			return stringBody.substring(0, 20);
		}
		return "";
	}

	public String getReplyQueueName() {
		return replyQueueName;
	}

	public boolean hasStringBody() {
		return stringBody != null;
	}

	public boolean hasByteBody() {
		return byteBody != null;
	}

	public void setReplyQueueName(String replyQueueName) {
		this.replyQueueName = replyQueueName;
	}

	public String getCorrelationID() {
		return correlationID;
	}

	public void setCorrelationID(String correlationID) {
		this.correlationID = correlationID;
	}

	public byte[] getByteBody() throws BrevTechnicalException {
		if (byteBody != null) {
			return byteBody;
		} else if (stringBody != null) {
			try {
				return stringBody.getBytes(Konstanter.TEGNSETT_ISO);
			} catch (UnsupportedEncodingException e) {
				throw new BrevTechnicalException(Konstanter.TEGNSETT_ISO + " er ikke et gyldig tegnsett", e);
			}
		} else {
			return null;
		}
	}

	public void setByteBody(byte[] byteBody) {
		this.byteBody = byteBody;
	}

	public long getMessageLength() {
		if (byteBody != null) {
			return byteBody.length;
		} else if (stringBody != null) {
			return stringBody.length();
		}
		return 0;
	}

	public String getStringBody() {
		return stringBody;
	}

	public void setStringBody(String stringBody) {
		this.stringBody = stringBody;
	}

	public String getBrevreferanse() {
		return brevreferanse;
	}

	public void setBrevreferanse(String brevreferanse) {
		this.brevreferanse = brevreferanse;
	}

	public boolean isTilgangsXML() {
		return tilgangsXML;
	}

	public void setTilgangsXML(boolean isTilgangsXML) {
		this.tilgangsXML = isTilgangsXML;
	}

}
