package stubidp.stubidp.resources;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.stubidp.resources.idp.GeneratePasswordResource;
import stubidp.stubidp.services.GeneratePasswordService;
import stubidp.stubidp.views.GeneratePasswordView;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratePasswordResourceTest {

    @Test
    public void saltedPasswordsShouldVerifyWithBCrypt() throws Exception {
        GeneratePasswordResource generatePasswordResource = new GeneratePasswordResource(new GeneratePasswordService());

        GeneratePasswordView passwordPage = generatePasswordResource.getPasswordPage();

        assertThat(BCrypt.checkpw(passwordPage.getPassword(), passwordPage.getPasswordHash())).isTrue();
    }

}
