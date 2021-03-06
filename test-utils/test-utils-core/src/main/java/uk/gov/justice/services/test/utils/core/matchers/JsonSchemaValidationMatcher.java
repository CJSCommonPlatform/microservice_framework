package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static uk.gov.justice.schema.catalog.test.utils.SchemaCatalogResolver.schemaCatalogResolver;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsNot;
import org.json.JSONObject;


public class JsonSchemaValidationMatcher {

    private static final Random random = new Random();
    private static final String JSON_SCHEMA_TEMPLATE = "json/schema/%s.json";
    private static final String RAML_JSON_SCHEMA_TEMPLATE = "raml/json/schema/%s.json";
    private static final String YAML_JSON_SCHEMA_TEMPLATE = "yaml/json/schema/%s.json";
    private static final String VALIDATION_ERROR = " to fail validation against schema ";
    private static final String VALIDTAE_SCHEMA = " to validate against schema ";
    private static final String JSON_FILE = "json file ";
    private static final String VALIDATION_SUCCESS = "validation passed";
    private static final List<String> LOCATION_TEMPLATES = asList(JSON_SCHEMA_TEMPLATE, RAML_JSON_SCHEMA_TEMPLATE, YAML_JSON_SCHEMA_TEMPLATE);


    public static Matcher<String> isValidForSchema(final String pathToJsonSchema) {
        return new TypeSafeMatcher<String>() {

            private String pathToJsonFile;
            private ValidationException exception = null;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;

                try {
                    getJsonSchemaFor(pathToJsonSchema).validate(getJsonObjectFor(pathToJsonFile));
                    return true;
                } catch (ValidationException var3) {
                    this.exception = var3;
                    return false;
                } catch (IOException var4) {
                    throw new IllegalArgumentException(var4);
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText(JSON_FILE).appendValue(this.pathToJsonFile).appendText(VALIDTAE_SCHEMA).appendValue(pathToJsonSchema);
            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                mismatchDescription.appendText("validation failed with message ").appendValue(this.exception.toJSON());
            }
        };
    }

    public static Matcher<JsonEnvelope> isValidJsonEnvelopeForSchema() {
        return new TypeSafeDiagnosingMatcher<JsonEnvelope>() {
            private ValidationException validationException = null;

            @Override
            protected boolean matchesSafely(final JsonEnvelope jsonEnvelope, final Description description) {
                if (null == this.validationException) {

                    final String name = jsonEnvelope.metadata().name();
                    final String payload = jsonEnvelope.payloadAsJsonObject().toString();

                    for (int index = 0; index < LOCATION_TEMPLATES.size(); index++) {
                        try {

                            if (validateSchemaAt(format(LOCATION_TEMPLATES.get(index), name), payload)) {
                                return true;
                            } else {
                                final boolean isLastTemplate = index == LOCATION_TEMPLATES.size() - 1;

                                if (isLastTemplate) {

                                    final String locations = LOCATION_TEMPLATES.stream()
                                            .map(locationTemplate -> format(locationTemplate, name))
                                            .collect(Collectors.joining(", "));

                                    throw new IllegalArgumentException("Failed to find schema at any of the following locations : " + locations);
                                }
                            }

                        } catch (final ValidationException validationException) {
                            this.validationException = validationException;
                            return false;
                        }
                    }

                    return true;
                } else {
                    description.appendText("Schema validation failed with message: ").appendValue(this.validationException.getMessage());
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("JsonEnvelope validated against schema found on classpath at \'raml/json/schema/\' ");
            }
        };
    }

    public static Matcher<String> isNotValidForSchema(final String pathToJsonSchema) {
        return IsNot.not(isValidForSchema(pathToJsonSchema));
    }

    public static Matcher<String> failsValidationWithMessage(final String pathToJsonSchema, final String errorMessage) {
        return new TypeSafeMatcher<String>() {
            private String pathToJsonFile;
            private ValidationException exception = null;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;

                try {
                    getJsonSchemaFor(pathToJsonSchema).validate(getJsonObjectFor(pathToJsonFile));
                    return false;
                } catch (final ValidationException validationException) {
                    this.exception = validationException;
                    return validationException.getMessage().equals(errorMessage);
                } catch (final IOException ioException) {
                    throw new IllegalArgumentException(ioException);
                }
            }

            @Override
            public void describeTo(final Description description) {
                if (this.exception == null) {
                    description.appendText(JSON_FILE).appendValue(this.pathToJsonFile).appendText(VALIDATION_ERROR).appendValue(pathToJsonSchema);
                } else {
                    description.appendText(JSON_FILE).appendValue(this.pathToJsonFile).appendText(VALIDATION_ERROR).appendValue(pathToJsonSchema).appendText(" with message ").appendValue(errorMessage);
                }

            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                if (this.exception == null) {
                    mismatchDescription.appendText(VALIDATION_SUCCESS);
                } else {
                    mismatchDescription.appendText("validation failed with message ").appendValue(this.exception.toJSON());
                }

            }
        };
    }

    public static Matcher<String> failsValidationForAnyMissingField(final String pathToJsonSchema) {
        return failsValidationForAnyMissingField(pathToJsonSchema, (String) null);
    }

    public static Matcher<String> failsValidationForAnyMissingField(final String pathToJsonSchema, final String xpathToParent) {
        return new TypeSafeMatcher<String>() {

            private String pathToJsonFile;

            @Override
            protected boolean matchesSafely(final String pathToJsonFile) {
                this.pathToJsonFile = pathToJsonFile;

                try {
                    getJsonSchemaFor(pathToJsonSchema).validate(getJsonObjectWithAMissingField(pathToJsonFile, xpathToParent));
                    return false;
                } catch (ValidationException var3) {
                    return true;
                } catch (IOException var4) {
                    throw new IllegalArgumentException(var4);
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText(JSON_FILE).appendValue(this.pathToJsonFile).appendText(VALIDATION_ERROR).appendValue(pathToJsonSchema);
            }

            @Override
            protected void describeMismatchSafely(final String pathToJsonFile, final Description mismatchDescription) {
                mismatchDescription.appendText(VALIDATION_SUCCESS);
            }
        };
    }

    private static boolean validateSchemaAt(final String location, final String payload) {

        try {
            getJsonSchemaFor(location).validate(new JSONObject(payload));
        } catch (final IOException | IllegalArgumentException e) {
            return false;
        }

        //Validation completed
        return true;
    }

    private static JSONObject getJsonObjectFor(final String pathToJsonFile) throws IOException {
        return new JSONObject(getJsonContentFrom(pathToJsonFile));
    }

    private static Schema getJsonSchemaFor(final String pathToJsonSchema) throws IOException {
        return schemaCatalogResolver().loadSchema(getJsonContentFrom(pathToJsonSchema));
    }

    private static String getJsonContentFrom(final String pathToJsonSchema) throws IOException {
        String jsonContent;
        if (Paths.get(pathToJsonSchema, "").isAbsolute()) {
            jsonContent = Files.toString(new File(pathToJsonSchema), Charsets.UTF_8);
        } else {
            jsonContent = Resources.toString(Resources.getResource(pathToJsonSchema), Charsets.UTF_8);
        }

        return jsonContent;
    }

    private static JSONObject getJsonObjectWithAMissingField(final String pathToJsonFile, final String xpathToParent) throws IOException {
        final JSONObject jsonObject = getJsonObjectFor(pathToJsonFile);
        JSONObject parentJsonObject = jsonObject;
        String key;
        if (!Strings.isNullOrEmpty(xpathToParent)) {
            for (Iterator keysAsArray = Splitter.onPattern("/").omitEmptyStrings().split(xpathToParent).iterator(); keysAsArray.hasNext(); parentJsonObject = (JSONObject) parentJsonObject.get(key)) {
                key = (String) keysAsArray.next();
            }
        }

        final ArrayList keysAsArray1 = Lists.newArrayList(parentJsonObject.keySet());
        parentJsonObject.remove((String) keysAsArray1.get(random.nextInt(keysAsArray1.size())));
        return jsonObject;
    }
}
