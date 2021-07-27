package no.nav.brevserver.arkiverBrev.ArkiverBrevDefault;

import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.vo.MessageVO;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import javax.jms.Message;

@Component
public class MessageVoMapper {


	//TODO: Tviler på at denne vil funke. Må ryddes i
	@Handler
	public MessageVO mapMessageVo(Message msg) throws BrevTechnicalException {
		return new MessageVO(msg);
	}
}
