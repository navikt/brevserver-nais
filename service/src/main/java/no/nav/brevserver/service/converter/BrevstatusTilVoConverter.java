package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.vo.BrevStatusVO;
import no.nav.brevserver.service.utility.KnappStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BrevstatusTilVoConverter implements Converter<Brevstatus, BrevStatusVO> {

	@Autowired
	KnappStatusUtil knappStatusUtil;

	@Override
	public BrevStatusVO convert(Brevstatus brevstatus) {
		if (brevstatus == null) {
			return null;
		}
		BrevStatusVO brevStatusVO = new BrevStatusVO();
		brevStatusVO.setBrevreferanse(brevstatus.getId().getBrevreferanse());
		brevStatusVO.setStatus(brevstatus.getStatus());
		brevStatusVO.setSystemID(brevstatus.getId().getSystemId());
		brevStatusVO.setBrevmal(brevstatus.getBrevmal());
		brevStatusVO.setArkiver(brevstatus.getArkiver());
		brevStatusVO.setBestillerBrukerID(brevstatus.getBestillerBrukerID());
		brevStatusVO.setFormat(brevstatus.getFormat());
		brevStatusVO.setReturKoe(brevstatus.getReturKoe());
		brevStatusVO.setSkriver(brevstatus.getSkriver());
		brevStatusVO.setSkuff(brevstatus.getSkuff());
		brevStatusVO.setSkrivertype(brevstatus.getSkrivertype());
		brevStatusVO.setKnappStatus(knappStatusUtil.getKnappStatus(brevstatus.getBrevmal(), brevstatus.getId().getSystemId()));
		return brevStatusVO;
	}
}
