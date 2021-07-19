package no.nav.brevserver.dokarkiv.converter;

import no.nav.brevserver.fagarkiv.saf.hentdokument.HentDokumentResponseTo;
import no.nav.brevserver.server.common.vo.BrevVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class HentDokumentToBrevVoConverter implements Converter<HentDokumentResponseTo, BrevVO> {


	@Override
	public BrevVO convert(HentDokumentResponseTo hentDokumentResponseTo) {
		BrevVO brevVO = new BrevVO();
		brevVO.setBrevdata(hentDokumentResponseTo.getDokument());
		return brevVO;
	}
}
