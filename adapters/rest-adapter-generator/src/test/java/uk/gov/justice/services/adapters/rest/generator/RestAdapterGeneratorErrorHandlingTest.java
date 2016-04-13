package uk.gov.justice.services.adapters.rest.generator;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.raml.model.ActionType;
import uk.gov.justice.raml.common.validator.RamlValidationException;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

public class RestAdapterGeneratorErrorHandlingTest {
    private RestAdapterGenerator generator = new RestAdapterGenerator();
    private static final String BASE_PACKAGE = "uk.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfNoResourcesInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No resources specified");

        generator.run(
                raml().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfNoActionsInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No actions to process");

        generator.run(
                raml()
                        .with(resource("/path"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
    }

    @Test
    public void shouldThrowExceptionIfRequestTypeNotSetForPOSTAction() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Request type not set");

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(action(POST))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
    }

    @Test
    public void shouldThrowExceptionIfIfRequestTypeDoesNotContainAValidCommand() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.people.unknown.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action(POST, "application/vnd.people.unknown.command1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfNotvalidRequestType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: nd.people.unknown.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action(POST, "nd.people.unknown.command1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfOneOfRequestTypesNotValid() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.people.commaods.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(action()
                                        .with(ActionType.POST)
                                        .withMediaType("application/vnd.people.commaods.command1+json")
                                        .withMediaType("application/vnd.people.command.command1+json")
                                ))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));

    }

    @Test
    public void shouldThrowExceptionIfResponseTypeNotSetForGETAction() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Response type not set");

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(action(GET))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder));
    }

}
