package no.nav.brevserver.server.comptest.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;

/**
 * Simple message handler class using {@link JmsTemplate}.
 * 
 * @author Stian Landsnes, Visma Sirius
 * 
 */
public class MessageHandler {

	private int timeout = 10000;

	private JmsTemplate jmsTemplate;

	public MessageHandler(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Sends a byte message to given queue.
	 * 
	 * @param queue
	 *            The receiving queue.
	 * @param message
	 *            The byte message.
	 */
	public void sendByteMessage(String queue, final byte[] message) {
		MessageCreator messageCreator = new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				BytesMessage byteMessage = session.createBytesMessage();
				byteMessage.setStringProperty("JMS_IBM_Format", "MQSTR"); // Sender med riktig format skal at meldingen ikke feiler.
				byteMessage.writeBytes(message);
				return byteMessage;
			}
		};
		jmsTemplate.send(queue, messageCreator);
	}

	/**
	 * Sends a {@link Message} instance to given queue.
	 * 
	 * @param queue
	 *            The receiving queue.
	 * @param message
	 *            The message.
	 */
	public void sendMessage(String queue, final Message message) {
		MessageCreator messageCreator = new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return message;
			}
		};
		jmsTemplate.send(queue, messageCreator);
	}
	
	/**
	 * Sends a text message to given queue.
	 * 
	 * @param queue
	 *            The receiving queue.
	 * @param message
	 *            The text message.
	 */
	public void sendTextMessage(String queue, final String message) {
		MessageCreator messageCreator = new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				TextMessage textMessage = session.createTextMessage(message);
				return textMessage;
			}
		};
		jmsTemplate.send(queue, messageCreator);
	}

	/**
	 * Browse messages on given queue. Messages will NOT be deleted from queue.
	 * 
	 * @param queue
	 *            The queue to browse for messages.
	 * @return Messages on queue.
	 */
	public List<Message> browseMessages(String queue) {
		return jmsTemplate.browse(queue, new BrowserCallback<List<Message>>() {
			public List<Message> doInJms(Session session, QueueBrowser browser) throws JMSException {
				List<Message> messages = new ArrayList<Message>();
				Enumeration<?> enumeration = browser.getEnumeration();
				while (enumeration.hasMoreElements()) {
					messages.add((Message) enumeration.nextElement());
				}
				return messages;
			}
		});
	}

	/**
	 * Consumes messages on given queue. Message will be deleted from queue.
	 * 
	 * @param queue
	 *            The queue to consume messages from.
	 * @return The consumed messages.
	 */
	public List<Message> consumeMessages(final String queue) {
		return jmsTemplate.execute(new SessionCallback<List<Message>>() {
			public List<Message> doInJms(Session session) throws JMSException {
				List<Message> messages = new ArrayList<Message>();
				MessageConsumer consumer = session.createConsumer(session.createQueue(queue));
				Message message = null;
				while ((message = consumer.receiveNoWait()) != null) {
					messages.add(message);
				}
				return messages;
			}
		}, true);
	}

	/**
	 * Consumes messages on given queue. Message will be deleted from queue. Method waits 10 seconds to receive each message.
	 * 
	 * @param queue
	 *            The queue to consume messages from.
	 * @param numberOfMessages
	 *            Number of messages to consume.
	 * @return The consumed messages.
	 */
	public List<Message> consumeMessages(final String queue, final int numberOfMessages) {
		return jmsTemplate.execute(new SessionCallback<List<Message>>() {
			public List<Message> doInJms(Session session) throws JMSException {
				List<Message> messages = new ArrayList<Message>();
				MessageConsumer consumer = session.createConsumer(session.createQueue(queue));
				Message message = null;
				while ((message = consumer.receive(timeout)) != null && messages.size() < numberOfMessages) {
					messages.add(message);
				}
				return messages;
			}
		}, true);
	}

}
