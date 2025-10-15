package no.nav.brevserver.support;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import no.nav.brevserver.core.vo.FilType;
import no.nav.brevserver.nais.support.impl.DefaultLagreDokumentRequestMapper;
import no.nav.tjenester.brevogarkiv.dokumentbehandling.LagreDokumentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultLagreDokumentRequestMapperTest {

	private static final String BREVREFERANSE = "1";
	private static final String TOKEN = "123";
	private static final String SYSTEM_ID = "PE2";
	private static final String BRUKER_ID = "brukerID";
	private static final String KVITTERINGSKOE = "kvitteringsKoe";
	private static final String MALPAKKE = "malpakke";
	private static final boolean NYTT_DOKUMENT = true;
	private static final byte[] DOKUMENTDATA = "hello world".getBytes();
	private static final String CONTENT_TYPE = FilType.RTF.getContentType();

	private final DefaultLagreDokumentRequestMapper lagreBrevRequestMapper = new DefaultLagreDokumentRequestMapper();

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		no.nav.brevserver.app.dokumentbehandling.to.LagreDokumentRequest domainRequest = lagreBrevRequestMapper.map(createWsLagreDokumentRequest());

		assertThat(domainRequest.isNewDocument()).isEqualTo(NYTT_DOKUMENT);

		var brevstatus = domainRequest.getBrevStatus();
		assertThat(brevstatus.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brevstatus.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brevstatus.getToken()).isEqualTo(TOKEN);
		assertThat(brevstatus.getBrevmal()).isEqualTo(MALPAKKE);
		assertThat(brevstatus.getReturKoe()).isEqualTo(KVITTERINGSKOE);

		var brev = domainRequest.getBrev();
		assertThat(brev.getSystemID()).isEqualTo(SYSTEM_ID);
		assertThat(brev.getBrevreferanse()).isEqualTo(BREVREFERANSE);
		assertThat(brev.getContentType()).isEqualTo(CONTENT_TYPE);
		assertThat(brev.getBrevdata()).isEqualTo(DOKUMENTDATA);
		assertThat(brev.getBrukerID()).isEqualTo(BRUKER_ID);
	}

	private LagreDokumentRequest createWsLagreDokumentRequest() {
		LagreDokumentRequest lagreDokumentRequest = new LagreDokumentRequest();
		lagreDokumentRequest.setBrevreferanse(BREVREFERANSE);
		lagreDokumentRequest.setToken(TOKEN);
		lagreDokumentRequest.setSystemId(SYSTEM_ID);
		lagreDokumentRequest.setBrukerId(BRUKER_ID);
		lagreDokumentRequest.setKvitteringskoe(KVITTERINGSKOE);
		lagreDokumentRequest.setMalpakke(MALPAKKE);
		lagreDokumentRequest.setNyttDokument(NYTT_DOKUMENT);
		lagreDokumentRequest.setDokumentData(new DataHandler(new ByteArrayDataSource(DOKUMENTDATA, CONTENT_TYPE)));
		return lagreDokumentRequest;
	}

}