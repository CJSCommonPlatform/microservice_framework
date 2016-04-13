package uk.gov.justice.services.eventsourcing.source.core;


import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * Event stream that can be read from and appended to.
 */
public interface EventStream {

    /**
     * Get the stream of events.
     *
     * @return the stream of events
     */
    Stream<JsonEnvelope> read();

    /**
     * Get the stream of events from the given version.
     *
     * @return the stream of events
     */
    Stream<JsonEnvelope> readFrom(final Long version);

    /**
     * Store a stream of events.
     *
     * @param events the stream of events to store
     * @throws EventStreamException if an event could not be appended
     */
    void append(final Stream<JsonEnvelope> events) throws EventStreamException;

    /**
     * Store a stream of events after the given version.
     *
     * @param events  the stream of events to store
     * @param version the version to append from
     * @throws EventStreamException if an event could not be appended
     */
    void appendAfter(final Stream<JsonEnvelope> events, final Long version) throws EventStreamException;

    /**
     * Get the current (current maximum) sequence id (version number) for a stream
     *
     * @return the latest sequence id for the provided steam. 0 when stream is empty.
     */
    Long getCurrentVersion();

    /**
     * Retrieve the id of this stream.
     *
     * @return the stream id.
     */
    UUID getId();

}
