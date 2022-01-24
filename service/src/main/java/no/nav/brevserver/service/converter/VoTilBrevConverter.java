package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.audit.AuditTrail;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.vo.BrevVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;

@Component
public class VoTilBrevConverter implements Converter<BrevVO, Brev> {

	@Override
	public Brev convert(BrevVO brevVO) {
		return Brev.builder()
				.id(BrevreferanseSystemCompositeId.builder().brevreferanse(brevVO.getBrevreferanse()).systemId(brevVO.getSystemID()).build())
				.status(brevVO.getLagerStatus())
				.brukerId(brevVO.getBrukerID())
				.contentType(brevVO.getContentType())
				.brevdata(brevVO.getBrevdata())
				.endret(new Timestamp(System.currentTimeMillis()))
				.build();
	}
}
