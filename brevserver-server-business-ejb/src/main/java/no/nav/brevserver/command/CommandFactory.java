/**
 * Beskrivelse av klassen
 *
 * @author Dag Kristiansen
 */
package no.nav.brevserver.command;

import no.nav.brevserver.server.common.type.QueueType;
import no.nav.brevserver.server.common.vo.MessageVO;

/**
 * CommandFactory singleton responsible for creating commands based on the given command type and message.
 * 
 * @author Dag Kristiansen
 */
public final class CommandFactory {
	private static CommandFactory ourInstance = new CommandFactory();

	public static CommandFactory getInstance() {
		return ourInstance;
	}

	private CommandFactory() {
	}

	/**
	 * Creates command based on {@link javax.jms.Message}
	 * 
	 * @param queueType
	 *            The queueType of command
	 * @param messageVo
	 */
	public AbstractCommand createCommand(QueueType queueType, MessageVO messageVo) {
		if (queueType.equals(QueueType.BI_BREVSERVER_ONLINEBREV)) {
			return new BestillBrevCommand(messageVo);
		} else if (queueType.equals(QueueType.BI_BREVSERVER_MOTTAK_ARKIV)
				|| queueType.equals(QueueType.BI_BREVSERVER_MOTTAK_ONLINE)) {
			return new ArkiverBrevCommand(messageVo);
		}
		if (queueType.equals(QueueType.PE_BREVSERVER_ONLINEBREV)) {
			return new PEBestillBrevCommand(messageVo);
		}
		if (queueType.equals(QueueType.PE_BREVSERVER_MOTTAK_ONLINE) || queueType.equals(QueueType.PE_BREVSERVER_MOTTAK_ARKIV)) {
			return new PEArkiverBrevCommand(messageVo);
		} else {
			throw new RuntimeException("No command found for the given QueueType '" + queueType.toString() + "'");
		}
	}
}
