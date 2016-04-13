package uk.gov.justice.services.example.cakeshop.command.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonObject;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CakeShopCommandControllerTest {

    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final UUID RECIPE_ID = UUID.randomUUID();

    @Mock
    JsonEnvelope jsonEnvelope;

    @Mock
    private Sender sender;

    @Mock
    JsonObject payload;

    @InjectMocks
    private CakeShopCommandController cakeShopCommandController;

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        when(jsonEnvelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RECIPE_ID.toString());
        cakeShopCommandController.makeCake(jsonEnvelope);

        verify(sender, times(1)).send(jsonEnvelope);
    }

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        when(jsonEnvelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RECIPE_ID.toString());

        cakeShopCommandController.addRecipe(jsonEnvelope);

        verify(sender, times(1)).send(jsonEnvelope);
    }

}