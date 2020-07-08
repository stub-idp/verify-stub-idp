package stubidp.saml.metadata;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.function.Predicate;

public class EntitiesDescriptorNamePredicate implements Predicate<EntityDescriptor> {

    private final EntitiesDescriptorNameCriterion criterion;

    public EntitiesDescriptorNamePredicate(EntitiesDescriptorNameCriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public boolean test(EntityDescriptor input) {
        XMLObject parent = input.getParent();
        if (!(parent instanceof EntitiesDescriptor)) {
            return false;
        }
        String entitiesName = ((EntitiesDescriptor) parent).getName();
        return entitiesName != null && entitiesName.equals(criterion.getExpectedName());
    }

}
