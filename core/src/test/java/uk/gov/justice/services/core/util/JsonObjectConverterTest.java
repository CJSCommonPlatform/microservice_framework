package uk.gov.justice.services.core.util;

import com.google.common.io.Resources;
import org.junit.Test;
import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JsonObjectConverterTest {

    private static final String ID = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME = "court-names.commands.create-court-name";
    private static final String CLIENT = "d51597dc-2526-4c71-bd08-5031c79f11e1";
    private static final String SESSION = "45b0c3fe-afe6-4652-882f-7882d79eadd9";
    private static final String USER = "72251abb-5872-46e3-9045-950ac5bae399";
    private static final String CAUSATION_1 = "cd68037b-2fcf-4534-b83d-a9f08072f2ca";
    private static final String CAUSATION_2 = "43464b22-04c1-4d99-8359-82dc1934d763";

    private static final String PAYLOAD_ID = "c3f7182b-bd20-4678-ba8b-e7e5ea8629c3";
    private static final Long PAYLOAD_VERSION = 0L;
    private static final String PAYLOAD_NAME = "Name of the Payload";

    @Test
    public void fromString() throws Exception {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        JsonObject joEnvelope = jsonObjectConverter.fromString(Resources.toString(Resources.getResource("json/envelope.json"),
                Charset.defaultCharset()));

        assertThat(joEnvelope, notNullValue());
        JsonObject joMetadata = joEnvelope.getJsonObject(JsonObjectConverter.METADATA);
        JsonObject joPayload = jsonObjectConverter.extractPayloadFromEnvelope(joEnvelope);
        assertThat(joMetadata, notNullValue());
        assertThat(joPayload, notNullValue());

        assertThat(joMetadata.getString("id"), equalTo(ID));
        assertThat(joMetadata.getString("name"), equalTo(NAME));
        JsonObject correlation = joMetadata.getJsonObject("correlation");
        assertThat(correlation, notNullValue());
        assertThat(correlation.getString("client"), equalTo(CLIENT));

        JsonObject context = joMetadata.getJsonObject("context");
        assertThat(context, notNullValue());
        assertThat(context.getString("session"), equalTo(SESSION));
        assertThat(context.getString("user"), equalTo(USER));

        JsonArray causation = joMetadata.getJsonArray(Metadata.CAUSATION);
        assertThat(causation, notNullValue());
        assertThat(causation.size(), equalTo(2));
        assertThat(causation.get(0).toString().replaceAll("\"", ""), equalTo(CAUSATION_1));
        assertThat(causation.get(1).toString().replaceAll("\"", ""), equalTo(CAUSATION_2));

        assertThat(joEnvelope.getString("payloadId"), equalTo(PAYLOAD_ID));
        assertThat(new Long(joEnvelope.getInt("payloadVersion")), equalTo(PAYLOAD_VERSION));
        assertThat(joEnvelope.getString("payloadName"), equalTo(PAYLOAD_NAME));

    }

    @Test(expected = IllegalArgumentException.class)
    public void asMetadata_MissingId() throws Exception {
        final JsonObject joEnvelope = new JsonObjectConverter().fromString(Resources.toString(Resources.getResource("json/envelope-missing-id.json"),
                Charset.defaultCharset()));
        JsonObjectMetadata.metadataFrom(joEnvelope.getJsonObject(JsonObjectConverter.METADATA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void asMetadata_MissingName() throws Exception {
        final JsonObject joEnvelope = new JsonObjectConverter().fromString(Resources.toString(Resources.getResource("json/envelope-missing-name.json"),
                Charset.defaultCharset()));
        JsonObjectMetadata.metadataFrom(joEnvelope.getJsonObject(JsonObjectConverter.METADATA));
    }

    @Test
    public void asEnvelope() throws Exception {
        final JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        Envelope envelope = jsonObjectConverter.asEnvelope(jsonObjectConverter.fromString(Resources.toString(Resources.getResource("json/envelope.json"),
                Charset.defaultCharset())));

        assertThat(envelope, notNullValue());
        Metadata metadata = envelope.metadata();
        JsonObject payload = envelope.payload();
        assertThat(metadata, notNullValue());
        assertThat(payload, notNullValue());
        assertThat(metadata.id().toString(), equalTo(ID));
        assertThat(metadata.name(), equalTo(NAME));
        Optional<String> clientCorrelationId = metadata.clientCorrelationId();
        assertThat(clientCorrelationId.get(), equalTo(CLIENT));

        assertThat(metadata.sessionId().get(), equalTo(SESSION));
        assertThat(metadata.userId().get(), equalTo(USER));

        List causation = metadata.causation();
        assertThat(causation, notNullValue());
        assertThat(causation.size(), equalTo(2));
        assertThat(causation.get(0).toString(), equalTo(CAUSATION_1));
        assertThat(causation.get(1).toString(), equalTo(CAUSATION_2));

    }

    @Test
    public void fromEnvelope() throws IOException {
        final JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        final JsonObject expectedEnvelope = jsonObjectConverter.fromString(Resources.toString(Resources.getResource("json/envelope.json"),
                Charset.defaultCharset()));
        final Metadata metadata = JsonObjectMetadata.metadataFrom(expectedEnvelope.getJsonObject(JsonObjectConverter.METADATA));
        final JsonObject payload = jsonObjectConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final Envelope envelope = DefaultEnvelope.envelopeFrom(metadata, payload);
        assertThat(jsonObjectConverter.fromEnvelope(envelope), equalTo(expectedEnvelope));
    }

}