package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.server.common.vo.BrevVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class VoTilBrevConverter implements Converter<BrevVO, Brev> {

	@Override
	public Brev convert(BrevVO brevVO) {
		return Brev.builder()
				.brevreferanse(brevVO.getBrevreferanse())
				.status(brevVO.getLagerStatus())
				.systemId(brevVO.getSystemID())
				.brukerId(brevVO.getBrukerID())
				.contentType(brevVO.getContentType())
				.endret(brevVO.getEndret())
				.build();
	}
}
