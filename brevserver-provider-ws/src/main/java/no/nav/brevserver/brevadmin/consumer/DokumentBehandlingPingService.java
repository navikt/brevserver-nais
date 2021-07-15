package no.nav.brevserver.brevadmin.consumer;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.DokumentbehandlingPortType;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.PingRequest;

/**
 * Ping service client for DokumentBehandling.
 * 
 * @author Marius Thåring, Visma Consulting
 */
public class DokumentBehandlingPingService {

	private static final String TARGET_NAMESPACE = "http://dokumentbehandling.brevogarkiv.tjenester.nav.no/";
	private static final String TARGET_NAME = "Dokumentbehandling";

	private DokumentbehandlingPortType dokumentbehandlingPort;

	public DokumentBehandlingPingService() {
		String brevserverUrl = ConfigManager.getInstance().getString(ConfigManager.BREVSERVER_URL, null);

		QName serviceName = new QName(TARGET_NAMESPACE, TARGET_NAME);
		URL wsdlLocation = createUrl(brevserverUrl + TARGET_NAME + "?wsdl");
		dokumentbehandlingPort = Service.create(wsdlLocation, serviceName).getPort(DokumentbehandlingPortType.class);
	}

	private URL createUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void ping() {
		dokumentbehandlingPort.ping(new PingRequest());
	}
}
