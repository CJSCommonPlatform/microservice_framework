package uk.gov.justice.services.core.configuration;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceContextNameProvider {

    @Resource(lookup = "java:app/AppName")
    String appName;

    public String getServiceContextName() {
        return appName;
    }
}
