package stubidp.stubidp.auth;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class StubIdpBasicAuthRequiredFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(StubIdpBasicAuthRequired.class) != null ||
                resourceInfo.getResourceClass().getAnnotation(StubIdpBasicAuthRequired.class) != null) {
            context.register(StubIdpBasicAuthRequiredFilter.class);
        }
    }
}
