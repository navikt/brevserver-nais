package no.nav.brevserver.joark;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.consumer.DokarkivConsumer;
import no.nav.brevserver.consumer.SettBrevdataResponse;
import no.nav.brevserver.core.exception.BrevTechnicalException;
import no.nav.brevserver.core.vo.BrevVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Qualifier("dokarkivService")
@Service
@Slf4j
public class DokarkivServiceImpl implements JoarkService {

	private final DokarkivConsumer dokarkivConsumer;

	public DokarkivServiceImpl(DokarkivConsumer dokarkivConsumer) {
		this.dokarkivConsumer = dokarkivConsumer;
	}

	@Override
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevdata) throws BrevTechnicalException {
		settBrevdata(brevreferanse, contentType, brevdata);
	}

	@Override
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO) {
		settBrevdata(brevreferanse, redBrevVO.getContentType(), redBrevVO.getBrevdata());
		settBrevdata(brevreferanse, pdfBrevVO.getContentType(), pdfBrevVO.getBrevdata());
	}

	private void settBrevdata(String brevreferanse, String contentType, byte[] brevdata) {
		SettBrevdataResponse settBrevdataResponse = dokarkivConsumer.settBrevdata(Long.parseLong(brevreferanse), contentType, brevdata);
		log.info("Lagret dokument på brevreferanse={}. filUuid={}, contentType={}, filstørrelse={}",
				brevreferanse, settBrevdataResponse.filUuid(), contentType, settBrevdataResponse.filstoerrelse());
	}

	@Override
	public BrevVO hentDokument(String brevreferanse) throws BrevTechnicalException {
		throw new UnsupportedOperationException("hentDokument støttes ikke");
	}
}
