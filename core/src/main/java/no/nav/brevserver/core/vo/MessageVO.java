package no.nav.brevserver.core.vo;

import no.nav.brevserver.core.constants.Konstanter;
import no.nav.brevserver.core.exception.BrevTechnicalException;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class MessageVO implements Serializable {
	private static final long serialVersionUID = 8684875760609707790L;

	private String replyQueueName;
	private byte[] byteBody;
	private String stringBody;

	private String brevreferanse = null;
	private boolean tilgangsXML = false;

	public MessageVO(byte[] bytes) {
			byteBody = bytes;
	}

	public String getReplyQueueName() {
		return replyQueueName;
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

	public void setReplyQueueName(String replyQueueName){ this.replyQueueName = replyQueueName; }

}
