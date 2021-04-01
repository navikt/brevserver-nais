package no.nav.brevserver.server.common.jms;

import no.nav.brevserver.server.common.cache.CacheManager;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jndi.JndiHelper;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.utility.PerformanceLogger;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Enumeration;

/**
 * JMSAccessor
 * <p/>
 * A helper class for accessing JMS objects.
 */
public final class JMSAccessor {

	public static final int NO_WAIT = -1;

	private QueueConnectionFactory connectionFactory;
	private QueueConnection connection;
	private QueueSession session;
	private Queue dest;

	private String destName;
	private String replyToJndiName = null;
	private boolean isOpen = false;
	private int priority = Message.DEFAULT_PRIORITY;

	private static final Log log = new Log(JMSAccessor.class);

	/**
	 * Oppretter en JMSAccessor basert på et kø-jndi-navn
	 *
	 * @param queueDestJndiName Jndi-navn til Queue
	 * @return JMSAccessor
	 * @throws BrevTechnicalException ved feil
	 */
	public static JMSAccessor getAccessorUsingQueueJndiName(String queueDestJndiName) throws BrevTechnicalException {

		String methSig = "JMSAccessor.getAccessorUsingQueueJndiName(" + queueDestJndiName + ")";
		PerformanceLogger p = new PerformanceLogger(methSig);

		if (queueDestJndiName == null || "".equals(queueDestJndiName)) {
			return null;
		}

		JMSAccessor result = new JMSAccessor();

		try {
			Queue newDest = JndiHelper.getInstance().lookup(Queue.class, queueDestJndiName);

			String connFactoryJndiName = findConnectionFactory(newDest.getQueueName());
			result.setQueueConnectionFactory(connFactoryJndiName);
			result.createSessionAndConnection();

			result.setQueue(newDest);
			result.destName = queueDestJndiName;

		} catch (JMSException e) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, e);

		} finally {
			p.stop();
		}

		return result;
	}

	/**
	 * Oppretter en JMSAccessor basert på et ekte kønavn (må være definert i WAS'en
	 *
	 * @param queueDestName Jndi-navn til Queue
	 * @return JMSAccessor
	 * @throws BrevTechnicalException ved feil
	 */
	public static JMSAccessor getAccessorUsingQueueName(String queueDestName) throws BrevTechnicalException {
		if (queueDestName == null || "".equals(queueDestName)) {
			return null;
		}
		String methSig = "JMSAccessor.getAccessorUsingQueueName(" + queueDestName + ")";
		JMSAccessor result = new JMSAccessor();

		PerformanceLogger p = new PerformanceLogger(methSig);

		try {
			String connFactoryJndiName = findConnectionFactory(queueDestName);

			result.setQueueConnectionFactory(connFactoryJndiName);
			result.createSessionAndConnection();
			Queue queue = result.createQueue(queueDestName);
			result.setQueue(queue);
			result.destName = queueDestName;

		} finally {
			p.stop();
		}

		return result;
	}

	private static String findConnectionFactory(String queueName) {
		if (queueName == null) {
			return null;
		}
		return ConfigManager.QCF_JNDI;
	}

	// example : queue://MRT1/QA.T343.ASDFASDF_ASDF_ASDF_SADF?CCSID=1&SG=23

	private static String stripQueueName(String queueName) {
		String result = null;

		int posQA = queueName.indexOf("/", 8) + 1;
		int posQuestionmark = queueName.indexOf("?");

		if (posQA > -1 && posQuestionmark > -1) {
			result = queueName.substring(posQA, posQuestionmark);

		} else if (posQA > -1) {
			result = queueName.substring(posQA);

		} else {
			result = queueName;
		}

		return result;
	}

	private static String stripQueueManager(String queueName) {
		String result = null;

		int posQA = queueName.indexOf("/", 8);
		if (posQA > 8) {
			result = queueName.substring(8, posQA);
		} else {
			result = queueName;
		}

		return result;
	}

	/**
	 * Hide constructor
	 */
	private JMSAccessor() {
	}

	/**
	 * Kontrollerer om køen er tilgjengelig ved å forsøke å lese fra den (browse)
	 *
	 * @return sann hvis køen er lesbar, false ellers
	 * @throws BrevTechnicalException
	 */
	public String testQueue() throws BrevTechnicalException {
		String methSig = "JMSAccessor.testQueue()";
		String queueName = "ukjent";

		QueueBrowser qBrowser = null;
		boolean logSuccessfulClose = false;

		try {
			log.debug("JMSAccessor.testQueue(" + destName + ")", "Tester kø : " + dest.getQueueName());

			queueName = dest.getQueueName();
			qBrowser = session.createBrowser(dest);
			Enumeration<?> messages = qBrowser.getEnumeration();

			if (messages.hasMoreElements()) {
				messages.nextElement();
			}

			// String result = "Kø " + destName + " fungerer! " + getQueueName() + " - Antall : " + teller;
			String result = "Kø " + destName + " (" + getQueueName() + ")" + " fungerer! ";

			return result;

		} catch (JMSException je) {
			logSuccessfulClose = true;

			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, je);

		} catch (Exception e) {
			logSuccessfulClose = true;

			throw new BrevTechnicalException("Fikk ikke testet kø! Jndi-navn : " + destName + ", kønavn : " + queueName, e);

		} finally {
			if (qBrowser != null) {
				try {
					qBrowser.close();

					if (logSuccessfulClose) {
						log.debug(methSig, "Forbindelse til kø-browser ble lukket");
					}

				} catch (JMSException je) {
					throw new BrevTechnicalException(
							"Fikk ikke lukket kø! Jndi-navn : " + destName + ", kønavn : " + queueName, je);
				}
			}
		}
	}

	/**
	 * Åpner en browser mot destinasjonen
	 *
	 * @return queueBrowser
	 * @throws BrevTechnicalException
	 */
	public QueueBrowser getBrowser() throws BrevTechnicalException {
		try {
			return session.createBrowser(dest);

		} catch (JMSException e) {

			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, e);
		}
	}

	/**
	 * Forsøker å verifisere om kølytter er oppe, men denne implementasjonen er svak og bør rettes eller tas bort
	 *
	 * @return false hvis lytter ser ut til å være nede, true ellers
	 * @throws BrevTechnicalException
	 */
	public boolean isListenerRunning() throws BrevTechnicalException {
		String methSig = "JMSAccessor.isListenerRunning()";
		String queueName = "ukjent";
		boolean result = true;
		QueueBrowser qBrowser = null;
		try {
			queueName = dest.getQueueName();

			qBrowser = session.createBrowser(dest);

			Enumeration<?> messages = qBrowser.getEnumeration();

			if (messages.hasMoreElements()) {
				Message msg = (Message) messages.nextElement();
				String id = msg.getJMSMessageID();
				String cacheId = "JMSAccessor." + queueName + ".id";
				String oldId = (String) CacheManager.getObject(cacheId);

				if (id.equals(oldId)) {
					int testingTime = ConfigManager.getInstance()
							.getInt("JMSAccessor.isListenerRunning.okAgeInMin", 3) * 60000;
					if (CacheManager.getAge(cacheId) > testingTime) {
						result = false;
					}
				} else {
					CacheManager.addObject(cacheId, id);
				}
			}

		} catch (JMSException je) {
			log.error(methSig, "Testing feilet: " + je.getLinkedException());
			throw new BrevTechnicalException("Fikk ikke testet lytter! Jndi-navn : " +
					destName + ", kønavn : " + queueName, je);
		} catch (Exception e) {
			throw new BrevTechnicalException("Fikk ikke testet lytter! Jndi-navn : " +
					destName + ", kønavn : " + queueName, e);
		} finally {

			if (qBrowser != null) {
				try {
					qBrowser.close();
				} catch (JMSException e) {
					log.warning(methSig, "Fikk ikke lukket kø-browser", e);
				}
			}

		}

		return result;
	}

	private void createSessionAndConnection() throws BrevTechnicalException {
		try {
			connection = connectionFactory.createQueueConnection();
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

		} catch (JMSException je) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, je);
		}

		isOpen = true;
	}

	private void setQueueConnectionFactory(String jndiName) throws BrevTechnicalException {
		try {
			connectionFactory = JndiHelper.getInstance().lookup(QueueConnectionFactory.class, jndiName);
		} catch (ClassCastException ce) {
			String msg = "MQ ConnectionFactory er feil satt opp. ";
			msg += "Sjekk at oppsettet i queue.properties (connectionfactory.jndi) er likt med serveroppsettet";
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, msg, ce);
		}
	}

	private void setQueue(Queue dest) {
		this.dest = dest;
	}

	public Queue getQueue() {
		return dest;
	}

	/**
	 * Henter ut kønavnet fra destinasjonsnavnet
	 *
	 * @return kønavnet
	 * @throws BrevTechnicalException
	 */
	public String getQueueName() throws BrevTechnicalException {
		if (dest == null) {
			return null;
		}

		try {
			return stripQueueName(dest.getQueueName());

		} catch (JMSException e) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, e);
		}

	}

	/**
	 * Henter ut queuemanager-navnet fra destinasjonsnavnet
	 *
	 * @return queuemanager-navnet
	 * @throws BrevTechnicalException
	 */
	public String getQueueManager() throws BrevTechnicalException {
		if (dest == null) {
			return null;
		}

		try {
			return stripQueueManager(dest.getQueueName());

		} catch (JMSException e) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, e);
		}

	}

	/**
	 * Oppretter en tilkobling til en ny kø
	 *
	 * @param queueName
	 * @return køen
	 * @throws BrevTechnicalException
	 */
	public Queue createQueue(String queueName) throws BrevTechnicalException {
		if (session == null) {
			throw new BrevTechnicalException("QueueConnectionFactory må være opprettet før køen opprettes");
		}
		try {
			// Queue result = session.createQueue(queueName);
			if (!queueName.contains("targetClient")) {
				if(queueName.contains("?")) {
					queueName = queueName + "&targetClient=1";
				} else {
					queueName = queueName + "?targetClient=1";
				}
			}
			return session.createQueue(queueName);
		} catch (JMSException je) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, je);
		}
	}

	/**
	 * Create a JMS Text message
	 *
	 * @param content
	 * @return
	 * @throws BrevTechnicalException
	 */
	public TextMessage createTextMessage(String content) throws BrevTechnicalException {
		checkAccessorOpen();
		try {
			return session.createTextMessage(content);

		} catch (JMSException je) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, je);
		}
	}

	/**
	 * Create JMS Bytes message.
	 *
	 * @return
	 * @throws BrevTechnicalException
	 */
	public BytesMessage createBytesMessage() throws BrevTechnicalException {
		checkAccessorOpen();

		try {
			return session.createBytesMessage();

		} catch (JMSException je) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, je);
		}
	}

	/**
	 * Send text message with specific string as message contents. Destination type: Topic and Queue
	 *
	 * @param content
	 * @param expiry  time in millis until message expires
	 * @throws BrevTechnicalException
	 */
	public void sendTextMessage(String content, long expiry) throws BrevTechnicalException {
		checkAccessorOpen();
		TextMessage textMessage = createTextMessage(content);
		sendMessage(textMessage, expiry);
	}

	/**
	 * Send message Destination type: Topic and Queue
	 *
	 * @param message
	 * @param expiry  time in millis until message expires
	 * @throws BrevTechnicalException if an JMSException occurs
	 */
	public void sendMessage(Message message, long expiry) throws BrevTechnicalException {

		checkAccessorOpen();

		// Set expiry
		try {
			message.setJMSExpiration(expiry);

			if (message.getJMSReplyTo() == null && replyToJndiName != null) {
				Queue replyQueue = JndiHelper.getInstance().lookup(Queue.class, replyToJndiName);
				message.setJMSReplyTo(replyQueue);
			}

			QueueSender sender = session.createSender(dest);
			sender.setTimeToLive(expiry);
			sender.setPriority(priority);
			sender.send(message);
		} catch (JMSException je) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, "Feil ved sending til kø: "
					+ destName, je);
		}
	}

	/**
	 * Check if accessor is open
	 *
	 * @throws BrevTechnicalException
	 */
	private void checkAccessorOpen() throws BrevTechnicalException {

		if (!isOpen) {
			throw new BrevTechnicalException(BrevTechnicalException.MQ_IKKE_TILGJENGELIG, "JMSAccessor er lukket!", null);
		}
	}

	/**
	 * Close all open JMS resources.
	 */
	public void close() {
		isOpen = false;
		String methSig = "JMSAccessor.close()";

		if (session != null) {
			try {
				session.close();
				session = null;
			} catch (JMSException e) {
				log.warning(methSig, "Greide ikke å lukke QueueSession", e);
			}
		}
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (JMSException e) {
				log.warning(methSig, "Greide ikke å lukke QueueConnection", e);
			}
		}
		connectionFactory = null;
		dest = null;
	}

	/**
	 * Releases the JMS resources. Might not be called.
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	/**
	 * Check if accessor is open
	 *
	 * @return True if accessor is initialized and not closed
	 */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * @param string
	 */
	public void setReplyToJndiName(String string) {
		replyToJndiName = string;
	}

	public static void close(JMSAccessor accessor) {
		if (accessor != null) {
			accessor.close();
		}
	}
}
