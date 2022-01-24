package no.nav.brevserver.service.converter;

import no.nav.brevserver.core.audit.AuditTrail;
import no.nav.brevserver.core.domain.entities.Brev;
import no.nav.brevserver.core.domain.entities.BrevlagerHistorikk;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Date;
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
				.brevdata(brev.getBrevdata())
				.vasket('0')
				.endret(new Timestamp(System.currentTimeMillis()))
				.build();
	}
}
