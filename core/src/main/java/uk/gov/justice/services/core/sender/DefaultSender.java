package uk.gov.justice.services.core.sender;


import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.core.util.CoreUtil;
import uk.gov.justice.services.messaging.Envelope;

import javax.enterprise.inject.Alternative;
import java.util.Objects;

/**
 * Sends an action to the next layer using JMS Sender.
 */

@Alternative
public class DefaultSender implements Sender {

    private final JmsSender jmsSender;
    private final Component destinationComponent;
    private final JmsEndpoints jmsEndpoints;

    DefaultSender(final JmsSender jmsSender, final Component destinationComponent, final JmsEndpoints jmsEndpoints) {
        this.jmsSender = jmsSender;
        this.destinationComponent = destinationComponent;
        this.jmsEndpoints = jmsEndpoints;
    }

    @Override
    public void send(final Envelope envelope) {
        final String contextName = CoreUtil.extractContextNameFromActionOrEventName(envelope.metadata().name());
        jmsSender.send(jmsEndpoints.getEndpoint(destinationComponent, contextName), envelope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultSender that = (DefaultSender) o;
        return Objects.equals(jmsSender, that.jmsSender) &&
                destinationComponent == that.destinationComponent &&
                Objects.equals(jmsEndpoints, that.jmsEndpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jmsSender, destinationComponent, jmsEndpoints);
    }
}
