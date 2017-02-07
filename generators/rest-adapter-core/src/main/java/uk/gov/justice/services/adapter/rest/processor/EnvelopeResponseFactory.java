package uk.gov.justice.services.adapter.rest.processor;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Create {@link Response} with OK status, with resulting JsonEnvelope as the returned entity.
 */
public class EnvelopeResponseFactory implements ResponseFactory {

    @Inject
    ResponseFactoryHelper responseFactoryHelper;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    /**
     * Uses the {@link ResponseFactoryHelper} to process the result JsonEnvelope into the
     * appropriate {@link Response}.  On OK status response, creates {@link Response} with a status
     * of OK and adds the result JsonEnvelope as the response entity.
     *
     * @param action the action being processed
     * @param result the resulting JsonEnvelope
     * @return the {@link Response} to return from the REST call
     */
    @Override
    public Response responseFor(final String action, final Optional<JsonEnvelope> result) {
        return responseFactoryHelper.responseFor(action, result,
                jsonEnvelope -> status(OK)
                        .entity(jsonObjectEnvelopeConverter.fromEnvelope(jsonEnvelope))
                        .build());
    }
}