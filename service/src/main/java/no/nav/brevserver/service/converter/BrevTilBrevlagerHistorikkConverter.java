package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.BrevlagerHistorikk;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class BrevTilBrevlagerHistorikkConverter implements Converter<Brev, BrevlagerHistorikk> {

	@Override
	public BrevlagerHistorikk convert(Brev brev) {
		return BrevlagerHistorikk.builder()
				.brevreferanse(brev.getId().getBrevreferanse())
				.systemId(brev.getId().getSystemId())
				.brukerId(brev.getBrukerId())
				.status(brev.getStatus())
				.contentType(brev.getContentType())
				.timestamp(brev.getEndret()!=null?brev.getEndret():new Timestamp(System.currentTimeMillis()))
				.brevdata(brev.getBrevdata())
				.vasket("0")
				.build();
	}
}
