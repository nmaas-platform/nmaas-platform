package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.SSHKeyRequest;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SSHKeyControllerIntTest extends BaseControllerTestSetup {

    private final static String VALID_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDuQ6IUs8q207aA/q+KRswa+Ui+hx2c8yN/EoSIGCRhoadKkn1dN1GCGr6hn4te7BvWunGuRbLxtKf23IQvud3NuhWVrNCwJbHOIJ3To+45IBnGuur7u5CDBPR8tsvbkk4jde8j58K2xM+9GeGBxZhXEvgVs+uQwDqMhHeWCS9sqcf0Es0fXlQOffQCEiRnGOrd7cL1iIr7fimqGrGYmqxu3gfzhEPrMNHoXW5QArne48gK0EZvxmMoP5FWXLQx3itzDKfPaIB//uRBbBTNFUd6FWjZs2S1vsmKbV7LU0BBRu+CLfbw41eFuQUbx2/hQc+JbV0E5l31oCi04cZtfr1CKvmmA4t13UyooCPZWafS/uBi8n8eVoOT+VisEhbsFQJydulWeEeFF5bIwrMxPx4SucmvnsgZouemHSpuLvwIFanycPc6PWDL7gx6MLbLHulvNO22FVdRnuisgspGM85H1WFD51L5ARUz/bTltbYRKtcXhi3lYAETPmHjdiQCOp9pWNTTs+JHTz1mfA7LSVoceWO+5mdMEGwH3sEeZ/PgK6rUBocEV+xP7nj+i2L+KS/c+NvC49etjHiGCxUfXZozNSoma/tkSav2tvx10DWG8Yb93CAyqSyW1VdQIE/jE0PNWWwhvDzj1td4qsJw2+x8bCZVUChf50WxuEtBAFzVjw== user@vm1"; // user@vm1

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup(){
        this.mvc = createMVC();
    }

    @Test
    public void shouldAddValidKey() {
        SSHKeyRequest req = new SSHKeyRequest("longName", VALID_KEY);
        assertDoesNotThrow(() -> {
            mvc.perform(put("/api/user/keys")
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        });
    }

    @Test
    public void shouldReturnErrorWhenKeyIsNotValid() {
        SSHKeyRequest req = new SSHKeyRequest("longName", "some random text which by definition is not valid ssh key");
        assertDoesNotThrow(() -> {
            mvc.perform(put("/api/user/keys")
                    .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        });
    }
}
