package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.fieldValue;
import static uk.gov.justice.subscription.domain.builders.EventSourceDefinitionBuilder.eventSourceDefinition;
import static uk.gov.justice.subscription.domain.builders.LocationBuilder.location;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceTransformationProducerTest {

    @Mock
    private EventStreamManagerFactory eventStreamManagerFactory;

    @Mock
    private EventRepositoryFactory eventRepositoryFactory;

    @Mock
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Mock
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @InjectMocks
    private EventSourceTransformationProducer eventSourceTransformationProducer;

    @Test
    public void shouldCreateEventSourceTransformation() throws Exception {
        final EventRepository eventRepository = mock(EventRepository.class);
        final EventStreamManager eventStreamManager = mock(EventStreamManager.class);

        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("eventsource")
                .withLocation(location()
                        .withJmsUri("")
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .withDataSource("jndi:datasource")
                        .build())
                .build();
        when(eventRepositoryFactory.eventRepository(any(EventJdbcRepository.class), any(EventStreamJdbcRepository.class))).thenReturn(eventRepository);
        when(eventStreamManagerFactory.eventStreamManager(eventRepository, eventSourceDefinition.getName())).thenReturn(eventStreamManager);
        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        final EventSourceTransformation eventSourceTransformation = eventSourceTransformationProducer.eventSourceTransformation();

        assertThat(eventSourceTransformation, is(instanceOf(DefaultEventSourceTransformation.class)));

        final DefaultEventSourceTransformation defaultEventSourceTransformation = (DefaultEventSourceTransformation) eventSourceTransformation;

        final Optional<Object> eventStreamManagerField = fieldValue(defaultEventSourceTransformation, "eventStreamManager");
        assertThat(eventStreamManagerField, is(of(eventStreamManager)));
    }
}