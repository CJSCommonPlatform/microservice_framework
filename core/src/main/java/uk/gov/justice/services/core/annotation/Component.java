package uk.gov.justice.services.core.annotation;

/**
 * Enum representing all the service components.
 */
public enum Component {

    COMMAND_API("commands", "api"), COMMAND_CONTROLLER("commands", "controller"), COMMAND_HANDLER("commands", "handler"), EVENT_LISTENER("events", "listener");

    private final String pillar;
    private final String tier;

    Component(final String pillar, final String tier) {
        this.pillar = pillar;
        this.tier = tier;
    }

    /**
     * Retrieves the Component of the provided {@link ServiceComponent}
     *
     * @param clazz The service component to be analysed.
     * @return the component from the provided {@link ServiceComponent}.
     */
    public static Component getComponentFromServiceComponent(final Class<ServiceComponent> clazz) {
        final ServiceComponent serviceComponent = clazz.getAnnotation(ServiceComponent.class);
        return serviceComponent.value();
    }

    /**
     * Retrieves the Component of the provided {@link Adapter}
     *
     * @param clazz The adapter class to be analysed.
     * @return the component from the provided {@link Adapter}.
     */
    public static Component getComponentFromAdapter(final Class<Adapter> clazz) {
        final Adapter adapter = clazz.getAnnotation(Adapter.class);
        return adapter.value();
    }

    public String pillar() {
        return pillar;
    }

    public String tier() {
        return tier;
    }

}
