package stubidp.stubidp.csrf;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class CSRFCheckProtectionFeature implements DynamicFeature {

    private final Class<? extends AbstractCSRFCheckProtectionFilter> clazz;

    @Inject
    public CSRFCheckProtectionFeature(Class<? extends AbstractCSRFCheckProtectionFilter> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if ((resourceInfo.getResourceMethod().getAnnotation(CSRFCheckProtection.class) != null ||
                resourceInfo.getResourceClass().getAnnotation(CSRFCheckProtection.class) != null) &&
                (resourceInfo.getResourceMethod().getAnnotation(POST.class) != null ||
                        resourceInfo.getResourceClass().getAnnotation(POST.class) != null)) {
            context.register(clazz);
        }
    }
}
