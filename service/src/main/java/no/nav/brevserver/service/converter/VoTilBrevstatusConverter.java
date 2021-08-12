package no.nav.brevserver.service.converter;


import no.nav.brevserver.core.domain.entities.Brevstatus;
import no.nav.brevserver.server.common.vo.BrevStatusVO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class VoTilBrevstatusConverter implements Converter<BrevStatusVO, Brevstatus> {


	@Override
	public Brevstatus convert(BrevStatusVO brevStatusVO) {
		return Brevstatus.builder()
				.brevreferanse(brevStatusVO.getBrevreferanse())
				.arkiver(brevStatusVO.getArkiver() != null ? brevStatusVO.getArkiver() : "JA")
				.brevmal(brevStatusVO.getBrevmal() != null ? brevStatusVO.getBrevmal() : "")
				.bestillerBrukerID(brevStatusVO.getBestillerBrukerID())
				.format(brevStatusVO.getFormat())
				.returKoe(brevStatusVO.getReturKoe() != null ? brevStatusVO.getReturKoe() : "")
				.skriver(brevStatusVO.getSkriver())
				.skrivertype(brevStatusVO.getSkrivertype())
				.skuff(brevStatusVO.getSkuff())
				.status(brevStatusVO.getStatus())
				.systemID(brevStatusVO.getSystemID())
				.brevreferanse(brevStatusVO.getBrevreferanse())
				.endret(new Timestamp(System.currentTimeMillis()))
				.build();
	}
}