package uk.gov.justice.services.messaging.jms;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.exception.JmsEnvelopeSenderException;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * An envelope producer that sends or publishes an envelope to a queue or topic respectively depending on the
 * destination type.
 */
public class JmsEnvelopeSender {

    @Resource(mappedName = "java:comp/DefaultJMSConnectionFactory")
    ConnectionFactory connectionFactory;

    @Inject
    EnvelopeConverter envelopeConverter;

    /**
     * Sends jsonEnvelope to the destination via JMS.
     *
     * @param jsonEnvelope    jsonEnvelope to be sent.
     * @param destination JMS destination for the jsonEnvelope.
     */
    public void send(final JsonEnvelope jsonEnvelope, final Destination destination) {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(destination)) {

            producer.send(envelopeConverter.toMessage(jsonEnvelope, session));
        } catch (JMSException e) {
            throw new JmsEnvelopeSenderException(String.format("Exception while sending jsonEnvelope with name %s", jsonEnvelope.metadata().name()), e);
        }
    }

}
