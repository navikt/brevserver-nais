package no.nav.brevserver.mdb.bi;

import javax.annotation.PostConstruct;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;

import no.nav.brevserver.mdb.BiAbstractMdb;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.type.QueueType;

@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@MessageDriven
public class BiOnlineMdb extends BiAbstractMdb implements MessageListener {

	@PostConstruct
	public void initLog() {
		log = new Log(this.getClass());
	}

	@Override
	public void onMessage(Message message) {
		String methSig = "BiOnlineMdb.onMessage()";
		onMessage(methSig, message, QueueType.BI_BREVSERVER_MOTTAK_ONLINE);
	}
}