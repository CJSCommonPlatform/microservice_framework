package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.EventSourceBuilder.eventsource;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.Location;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceRegistryProducerTest {

    @Mock
    private YamlFileFinder yamlFileFinder;

    @Mock
    private EventSourcesParser eventSourcesParser;

    @InjectMocks
    private EventSourceRegistryProducer eventSourceRegistryProducer;

    @Test
    public void shouldCreateARegistryOfAllEventSourcesFromTheClasspath() throws Exception {

        final String event_source_name_1 = "event_source_name_1";
        final String event_source_name_2 = "event_source_name_2";

        final URL url_1 = new URL("file:/test");
        final URL url_2 = new URL("file:/test");

        final EventSource eventSource1 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_1).build();

        final EventSource eventSource2 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_2).build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getEventSourcesPaths()).thenReturn(pathList);
        when(eventSourcesParser.eventSourcesFrom(pathList)).thenReturn(Stream.of(eventSource1, eventSource2));

        final EventSourceRegistry eventSourceRegistry = eventSourceRegistryProducer.getEventSourceRegistry();

        assertThat(eventSourceRegistry, is(notNullValue()));

        assertThat(eventSourceRegistry.getEventSourceFor(event_source_name_1), is(of(eventSource1)));
        assertThat(eventSourceRegistry.getEventSourceFor(event_source_name_2), is(of(eventSource2)));
    }

    @Test
    public void shouldCreateSingleRegistryAndReturnSameInstance() throws Exception {

        final String event_source_name_1 = "event_source_name_1";
        final String event_source_name_2 = "event_source_name_2";

        final URL url_1 = new URL("file:/test");
        final URL url_2 = new URL("file:/test");

        final EventSource eventSource1 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_1).build();

        final EventSource eventSource2 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_2).build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getEventSourcesPaths()).thenReturn(pathList);
        when(eventSourcesParser.eventSourcesFrom(pathList)).thenReturn(Stream.of(eventSource1, eventSource2));

        final EventSourceRegistry eventSourceRegistry_1 = eventSourceRegistryProducer.getEventSourceRegistry();
        final EventSourceRegistry eventSourceRegistry_2 = eventSourceRegistryProducer.getEventSourceRegistry();

        assertThat(eventSourceRegistry_1, is(sameInstance(eventSourceRegistry_2)));
    }

    @Test
    public void shouldThrowExceptionIfIOExceptionOccursWhenFindingEventSourcesOnTheClasspath() throws Exception {

        when(yamlFileFinder.getEventSourcesPaths()).thenThrow(new IOException());

        try {
            eventSourceRegistryProducer.getEventSourceRegistry();
            fail();
        } catch (final Exception e) {
            assertThat(e, is(instanceOf(RegistryException.class)));
            assertThat(e.getMessage(), is("Failed to find yaml/event-sources.yaml resources on the classpath"));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
        }
    }
}