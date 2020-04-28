package net.geant.nmaas.orchestration.entities;

import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.User;

@Setter
@Getter
public class AppDeploymentOwner {

    private String username;

    private String sshKey;

    public static AppDeploymentOwner fromUser(User user) {
        AppDeploymentOwner owner = new AppDeploymentOwner();
        owner.setUsername(user.getUsername());
        //TODO fill in once SSH key is available in the User object
        owner.setSshKey(null);
        return owner;
    }

}
