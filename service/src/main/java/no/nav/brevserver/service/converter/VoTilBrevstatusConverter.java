package no.nav.brevserver.service.converter;


import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId;
import no.nav.brevserver.core.vo.BrevStatusVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class VoTilBrevstatusConverter implements Converter<BrevStatusVO, Brevstatus> {

	@Override
	public Brevstatus convert(BrevStatusVO brevStatusVO) {
		return Brevstatus.builder()
				.id(BrevreferanseSystemCompositeId.builder()
						.brevreferanse(brevStatusVO.getBrevreferanse())
						.systemId(brevStatusVO.getSystemID())
						.build())
				.arkiver(brevStatusVO.getArkiver() != null ? brevStatusVO.getArkiver() : "JA")
				.brevmal(brevStatusVO.getBrevmal() != null ? brevStatusVO.getBrevmal() : "")
				.bestillerBrukerID(brevStatusVO.getBestillerBrukerID())
				.format(brevStatusVO.getFormat())
				.returKoe(brevStatusVO.getReturKoe() != null ? brevStatusVO.getReturKoe() : "")
				.skriver(brevStatusVO.getSkriver())
				.skrivertype(brevStatusVO.getSkrivertype())
				.skuff(brevStatusVO.getSkuff())
				.status(brevStatusVO.getStatus())
				.endret(new Timestamp(System.currentTimeMillis()))
				.build();
	}
}