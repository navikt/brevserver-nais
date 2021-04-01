package no.nav.brevserver.consumer.joark.stub;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.vo.BrevVO;
import no.nav.brevserver.server.common.vo.FilType;

/**
 * Beskrivelse av klassen
 *
 * @author Dag Kristiansen
 */
public class JoarkServiceStub implements JoarkServiceBi {
	private Log log;

	public JoarkServiceStub() {
		log = new Log(this.getClass());
	}

	@Override
	public BrevVO hentDokument(String brevreferanse) throws BrevTechnicalException {
		log.debug("JoarkServiceStub:hentDokument(" + brevreferanse + ")", "Henter dokument");
		BrevVO brevVo = new BrevVO();
		brevVo.setBrevreferanse(brevreferanse);
		brevVo.setContentType(FilType.RTF.getContentType());
		brevVo.setBrevdata(getMockRtf());
		brevVo.setLagerStatus("D");
		return brevVo;
	}

	@Override
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevData) throws BrevTechnicalException {
		log.debug("JoarkServiceStub:lagreDokument(" + brevreferanse + ")", "Mellomlagret dokument. Størrelse på rtf: "
				+ brevData.length + " bytes");
	}

	@Override
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO) throws BrevTechnicalException {
		log.debug("JoarkServiceStub:lagreFerdigstiltDokument(" + brevreferanse + ")",
				"Lagret ferdigstilt dokument. Størrelse på rtf:" + redBrevVO.getBrevdata().length + ", pdf:" + pdfBrevVO.getBrevdata().length
						+ " bytes");
	}

	@Override
	public boolean isJournalpost(String brevreferanse) throws BrevTechnicalException {
		return true;
	}

	private byte[] getMockRtf() {
		return "{\\rtf1 Stubbing av Joark er aktivert!}".getBytes();
	}
}
