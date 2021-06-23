package no.nav.brevserver.converter;


import no.nav.brevserver.core.domain.entities.Brevstatus;
import org.springframework.core.convert.converter.Converter;
import no.nav.brevserver.server.common.vo.BrevStatusVO;

public class VoTilBrevstatusConverter implements Converter<BrevStatusVO, Brevstatus> {


	@Override
	public Brevstatus convert(BrevStatusVO brevStatusVO) {
		return Brevstatus.builder()
				.brevreferanse(brevStatusVO.getBrevreferanse())
				.arkiver(brevStatusVO.getArkiver())
				.brevmal(brevStatusVO.getBrevmal())
				.bestillerBrukerID(brevStatusVO.getBestillerBrukerID())
				.format(brevStatusVO.getFormat())
				.returKoe(brevStatusVO.getReturKoe())
				.skriver(brevStatusVO.getSkriver())
				.skrivertype(brevStatusVO.getSkrivertype())
				.skuff(brevStatusVO.getSkuff())
				.status(brevStatusVO.getStatus())
				.systemID(brevStatusVO.getSystemID())
				.brevreferanse(brevStatusVO.getBrevreferanse())
				.build();
	}
}