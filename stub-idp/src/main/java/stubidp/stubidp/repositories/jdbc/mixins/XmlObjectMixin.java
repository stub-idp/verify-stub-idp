package stubidp.stubidp.repositories.jdbc.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.schema.XSBooleanValue;

public abstract class XmlObjectMixin {
	@JsonIgnore
	public abstract void setNil(@Nullable final XSBooleanValue newNil);
}
