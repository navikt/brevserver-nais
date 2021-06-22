package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.server.common.vo.BrevVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BrevTilVoConverter implements Converter<Brev, BrevVO> {

	@Override
	public BrevVO convert(Brev brev) {
		BrevVO brevVO = new BrevVO();
		brevVO.setBrevdata(brev.getBrevdata()!=null?brev.getBrevdata():null);
		brevVO.setBrevreferanse(brev.getBrevreferanse());
		brevVO.setBrukerID(brev.getBrukerId());
		brevVO.setContentType(brev.getContentType());
		brevVO.setEndret(brev.getEndret());
		brevVO.setLagerStatus(brev.getStatus());
		brevVO.setSystemID(brev.getSystemId());
		return brevVO;
	}
}
