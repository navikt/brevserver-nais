package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.domain.entities.BrevSystemTilgang;
import no.nav.brevserver.server.common.vo.SysTilgangVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SystemtilgangTilVoConverter implements Converter<BrevSystemTilgang, SysTilgangVO> {


	@Override
	public SysTilgangVO convert(BrevSystemTilgang brevSystemTilgang) {
		if(brevSystemTilgang==null){
			return null;
		}
		SysTilgangVO sysTilgangVO = new SysTilgangVO();
		sysTilgangVO.setSysId(brevSystemTilgang.getSysId());
		sysTilgangVO.setPwd(brevSystemTilgang.getPwd());
		return sysTilgangVO;
	}
}
