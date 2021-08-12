package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BrevstatusTilVoConverter implements Converter<Brevstatus, BrevStatusVO> {
	@Override
	public BrevStatusVO convert(Brevstatus brevstatus) {
		if (brevstatus == null) {
			return null;
		}
		BrevStatusVO brevStatusVO = new BrevStatusVO();
		brevStatusVO.setBrevreferanse(brevstatus.getBrevreferanse());
		brevStatusVO.setStatus(brevstatus.getStatus());
		brevStatusVO.setSystemID(brevstatus.getSystemID());
		brevStatusVO.setBrevmal(brevstatus.getBrevmal());
		brevStatusVO.setArkiver(brevstatus.getArkiver());
		brevStatusVO.setBrevreferanse(brevstatus.getBrevreferanse());
		brevStatusVO.setBestillerBrukerID(brevstatus.getBestillerBrukerID());
		brevStatusVO.setFormat(brevstatus.getFormat());
		brevStatusVO.setReturKoe(brevstatus.getReturKoe());
		brevStatusVO.setSkriver(brevstatus.getSkriver());
		brevStatusVO.setSkuff(brevstatus.getSkuff());
		brevStatusVO.setSkrivertype(brevstatus.getSkrivertype());
		return brevStatusVO;
	}
}
