package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Component;

import javax.enterprise.inject.spi.Bean;

public class ServiceComponentFoundEvent extends BaseServiceComponentFoundEvent {

    public ServiceComponentFoundEvent(final Component component, final Bean<Object> commandHandlerBean) {
        super(commandHandlerBean, component);
    }
}
