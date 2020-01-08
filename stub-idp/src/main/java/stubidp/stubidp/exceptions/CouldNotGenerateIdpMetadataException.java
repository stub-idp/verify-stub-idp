package stubidp.stubidp.exceptions;

public class CouldNotGenerateIdpMetadataException  extends RuntimeException {
    public CouldNotGenerateIdpMetadataException(Exception e) {
        super(e);
    }
}
