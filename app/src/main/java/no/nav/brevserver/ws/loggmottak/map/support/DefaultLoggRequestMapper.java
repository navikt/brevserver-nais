package no.nav.brevserver.ws.loggmottak.map.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.provider.map.converters.SeverityToLogSeverityCustomConverter;
import no.nav.brevserver.service.loggmottak.exception.LoggedException;
import no.nav.brevserver.service.loggmottak.to.LoggRequest;
import no.nav.brevserver.ws.loggmottak.map.LoggRequestMapper;
import no.nav.tjenester.brevogarkiv.loggmottak.BrevklientArguments;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultLoggRequestMapper implements LoggRequestMapper {

	private final SeverityToLogSeverityCustomConverter severityCustomConverter;

	public DefaultLoggRequestMapper() {
		this.severityCustomConverter = new SeverityToLogSeverityCustomConverter();
	}

	@Override
	public LoggRequest map(no.nav.tjenester.brevogarkiv.loggmottak.LoggRequest loggRequest) {
		LoggRequest loggReq = new LoggRequest();
		if(loggRequest==null) {
			log.error("Tom loggrequest mottatt");
			return loggReq;
		}
		BrevklientArguments brevklientArguments = loggRequest.getBrevklientArguments();
		loggReq.setBrevreferanse(brevklientArguments!=null?brevklientArguments.getBrevreferanse():null);
		loggReq.setInfotrygdId(brevklientArguments!=null?brevklientArguments.getInfotrygdId():null);
		loggReq.setSystemId(brevklientArguments!=null?brevklientArguments.getSystemId():null);
		loggReq.setBrukerId(loggRequest.getBrukerId());
		loggReq.setException(loggRequest.getException()!=null?new LoggedException(loggRequest.getException().getMessage(), loggRequest.getException().getStacktrace()):null);
		loggReq.setMessage(loggRequest.getMessage());
		loggReq.setKlientVersion(loggRequest.getKlientVersion());
		loggReq.setSeverity(loggRequest.getSeverity()!=null?severityCustomConverter.convertTo(loggRequest.getSeverity()):null);
		return loggReq;
	}
}
