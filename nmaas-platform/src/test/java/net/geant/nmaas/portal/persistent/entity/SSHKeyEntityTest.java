package net.geant.nmaas.portal.persistent.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SSHKeyEntityTest {
    // ubuntu 18.04 LTS
    //    https://stackoverflow.com/questions/9607295/calculate-rsa-key-fingerprint
    //    ssh-keygen -t rsa -b 4096
    // ssh-rsa
    private final static String VALID_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDuQ6IUs8q207aA/q+KRswa+Ui+hx2c8yN/EoSIGCRhoadKkn1dN1GCGr6hn4te7BvWunGuRbLxtKf23IQvud3NuhWVrNCwJbHOIJ3To+45IBnGuur7u5CDBPR8tsvbkk4jde8j58K2xM+9GeGBxZhXEvgVs+uQwDqMhHeWCS9sqcf0Es0fXlQOffQCEiRnGOrd7cL1iIr7fimqGrGYmqxu3gfzhEPrMNHoXW5QArne48gK0EZvxmMoP5FWXLQx3itzDKfPaIB//uRBbBTNFUd6FWjZs2S1vsmKbV7LU0BBRu+CLfbw41eFuQUbx2/hQc+JbV0E5l31oCi04cZtfr1CKvmmA4t13UyooCPZWafS/uBi8n8eVoOT+VisEhbsFQJydulWeEeFF5bIwrMxPx4SucmvnsgZouemHSpuLvwIFanycPc6PWDL7gx6MLbLHulvNO22FVdRnuisgspGM85H1WFD51L5ARUz/bTltbYRKtcXhi3lYAETPmHjdiQCOp9pWNTTs+JHTz1mfA7LSVoceWO+5mdMEGwH3sEeZ/PgK6rUBocEV+xP7nj+i2L+KS/c+NvC49etjHiGCxUfXZozNSoma/tkSav2tvx10DWG8Yb93CAyqSyW1VdQIE/jE0PNWWwhvDzj1td4qsJw2+x8bCZVUChf50WxuEtBAFzVjw== user@vm1"; // user@vm1
    //    ssh-keygen -lf test_key.pub
    private final static String VALID_FINGERPRINT = "MmGTBFxQxk2eGpGXXF88LC2TnEV2PqaSYKk3d+lz6BM="; // octet padding check
    // https://en.wikipedia.org/wiki/Base64#Output_padding

    @Test
    public void generatedFingerprintShouldMatchWithGiven() {

        User user = new User("test");
        SSHKeyEntity test = new SSHKeyEntity(user, "name", VALID_KEY);

        assertEquals(VALID_FINGERPRINT, test.getFingerprint());
    }
}
