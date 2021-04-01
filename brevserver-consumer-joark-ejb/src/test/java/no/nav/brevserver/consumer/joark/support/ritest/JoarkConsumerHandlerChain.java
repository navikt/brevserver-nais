package no.nav.brevserver.consumer.joark.support.ritest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import no.nav.brevserver.consumer.joark.handler.RequestResponseLoggingHandler;
import no.stelvio.consumer.ws.JaxWsConsumerContextHandler;

/**
 * Handler chain for web service requests to JOARK services.
 * 
 * @author Emil Urnes, Visma Sirius
 *
 */
public class JoarkConsumerHandlerChain implements HandlerResolver {

	/** {@inheritDoc} */
	@SuppressWarnings("rawtypes")
	public List<Handler> getHandlerChain(PortInfo portInfo) {
		List<Handler> handlerChain = new ArrayList<Handler>();
		handlerChain.add(new JaxWsConsumerContextHandler());
		handlerChain.add(new RequestResponseLoggingHandler());
		return handlerChain;
	}

}
