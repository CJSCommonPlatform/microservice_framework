package uk.gov.justice.services.jmx;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultShutteringMBeanTest {

    @Mock
    private UtcClock clock;

    @Mock
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Mock
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @InjectMocks
    private DefaultShutteringMBean defaultShutteringMBean;

    @Test
    public void shouldFireShutteringRequestedEvent() {

        final ZonedDateTime requestedAt = new UtcClock().now();

        when(clock.now()).thenReturn(requestedAt);

        defaultShutteringMBean.doShutteringRequested();

        verify(shutteringRequestedEventFirer).fire(new ShutteringRequestedEvent(defaultShutteringMBean, requestedAt));
    }

    @Test
    public void shouldFireUnshutteringRequestedEvent() {

        final ZonedDateTime requestedAt = new UtcClock().now();

        when(clock.now()).thenReturn(requestedAt);

        defaultShutteringMBean.doUnshutteringRequested();
        
        verify(unshutteringRequestedEventFirer).fire(new UnshutteringRequestedEvent(defaultShutteringMBean, requestedAt));
    }
}
