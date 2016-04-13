package uk.gov.justice.services.event.enveloper;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Event;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.event.enveloper.exception.InvalidEventException;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

/**
 * Enveloper of PoJo classes to the equivalent event envelopes using the event map registry built from {@link Event} annotated classes.
 */
@ApplicationScoped
public class Enveloper {

    @Inject
    ObjectToJsonValueConverter objectToJsonValueConverter;

    private ConcurrentHashMap<Class, String> eventMap = new ConcurrentHashMap<>();

    /**
     * Register method, invoked automatically to register all event classes into the eventMap.
     *
     * @param event identified by the framework to be registered into the event map.
     */
    void register(@Observes final EventFoundEvent event) {
        eventMap.putIfAbsent(event.getClazz(), event.getEventName());
    }

    /**
     * Provides a function that wraps the provided object into a new {@link JsonEnvelope} using metadata from the
     * given jsonEnvelope.
     *
     * @param jsonEnvelope - the jsonEnvelope containing source metadata.
     * @return a function that wraps objects into an jsonEnvelope.
     */
    public Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope jsonEnvelope) {
        return x -> DefaultJsonEnvelope.envelopeFrom(buildMetaData(x, jsonEnvelope.metadata()), objectToJsonValueConverter.convert(x));
    }

    /**
     * Provides a function that wraps the provided object into a new {@link JsonEnvelope} using metadata from the
     * given jsonEnvelope, except the name.
     *
     * @param jsonEnvelope - the jsonEnvelope containing source metadata.
     * @param name         - name of the payload.
     * @return a function that wraps objects into an jsonEnvelope.
     */
    public Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope jsonEnvelope, final String name) {
        return x -> DefaultJsonEnvelope.envelopeFrom(buildMetaData(jsonEnvelope.metadata(), name), objectToJsonValueConverter.convert(x));
    }

    private Metadata buildMetaData(final Object eventObject, final Metadata metadata) {
        if (!eventMap.containsKey(eventObject.getClass())) {
            throw new InvalidEventException(format("Failed to map event. No event registered for %s", eventObject.getClass()));
        }

        return buildMetaData(metadata, eventMap.get(eventObject.getClass()));
    }

    private Metadata buildMetaData(final Metadata metadata, final String name) {

        JsonObjectBuilder metadataBuilder = JsonObjects.createObjectBuilderWithFilter(metadata.asJsonObject(),
                x -> !Arrays.asList(ID, NAME, CAUSATION).contains(x));

        final JsonObject jsonObject = metadataBuilder
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, name)
                .add(CAUSATION, createCausation(metadata))
                .build();

        return metadataFrom(jsonObject);
    }

    private JsonArray createCausation(final Metadata metadata) {
        JsonArrayBuilder causation = Json.createArrayBuilder();
        if (metadata.asJsonObject().containsKey(CAUSATION)) {
            metadata.asJsonObject().getJsonArray(CAUSATION).stream().forEach(causation::add);
        }
        causation.add(metadata.id().toString());

        return causation.build();
    }

}