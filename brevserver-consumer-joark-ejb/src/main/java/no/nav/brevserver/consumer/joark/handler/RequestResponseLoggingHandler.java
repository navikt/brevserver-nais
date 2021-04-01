package no.nav.brevserver.consumer.joark.handler;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import no.nav.brevserver.server.common.log.Log;

/**
 * SOAPHandler that logs WS requests and responses.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class RequestResponseLoggingHandler implements SOAPHandler<SOAPMessageContext> {

	protected final Log log = new Log(getClass());
	
	public boolean handleMessage(SOAPMessageContext context) {
		logMessage(context);
		return true;
	}

	public boolean handleFault(SOAPMessageContext context) {
		logMessage(context);
		return true;
	}

	private void logMessage(SOAPMessageContext context) {
		if (log.isDebugEnabled()) {
			String methSig = "logMessage()"; 
			Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			if (outboundProperty.booleanValue()) {
				log.debug(methSig, "Request:");
			} else {
				log.debug(methSig, "Response:");
			}
			SOAPMessage message = context.getMessage();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				message.writeTo(baos);
				log.debug(methSig, baos.toString());
			} catch (Exception e) {
				log.debug(methSig, "Exception in handler: " + e);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set getHeaders() {
		return null;
	}
	
	public void close(MessageContext messageContext) {
	}

}
