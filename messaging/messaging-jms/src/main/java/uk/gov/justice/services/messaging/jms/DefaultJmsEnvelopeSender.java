package uk.gov.justice.services.messaging.jms;

import static java.lang.String.format;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.exception.JmsEnvelopeSenderException;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An envelope producer that sends or publishes an envelope to a queue or topic respectively
 * depending on the destination type.
 */
public class DefaultJmsEnvelopeSender implements JmsEnvelopeSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJmsEnvelopeSender.class);

    @Resource(mappedName = "java:comp/DefaultJMSConnectionFactory")
    ConnectionFactory connectionFactory;

    @Inject
    EnvelopeConverter envelopeConverter;

    @Inject
    TraceLogger traceLogger;

    Context namingContext = new InitialContext();

    public DefaultJmsEnvelopeSender() throws NamingException {
    }

    /**
     * Sends envelope to the destination via JMS.
     *
     * @param envelope    envelope to be sent.
     * @param destination JMS destination for the envelope.
     */
    @Override
    public void send(final JsonEnvelope envelope, final Destination destination) {
        traceLogger.trace(LOGGER, () -> format("Sending JMS message: %s to %s", envelope,
                destination.toString()));
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(destination)) {

            producer.send(envelopeConverter.toMessage(envelope, session));

        } catch (JMSException e) {
            throw new JmsEnvelopeSenderException(format("Exception while sending envelope with name %s", envelope.metadata().name()), e);
        }
        traceLogger.trace(LOGGER, () -> format("Sent JMS message: %s to %s", envelope,
                destination.toString()));
    }

    /**
     * Sends envelope to the destination via JMS.
     *
     * @param envelope        envelope to be sent.
     * @param destinationName JNDI name of the JMS destination.
     */
    @Override
    public void send(final JsonEnvelope envelope, final String destinationName) {
        try {
            final Destination destination = (Destination) namingContext.lookup(destinationName);
            send(envelope, destination);
        } catch (NamingException e) {
            throw new JmsEnvelopeSenderException(format("Exception while looking up JMS destination name %s", destinationName), e);
        }
    }
}
