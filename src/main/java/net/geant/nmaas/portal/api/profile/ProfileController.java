package net.geant.nmaas.portal.api.profile;

import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService users;

    private final ModelMapper modelMapper;

    @Autowired
    public ProfileController(UserService users, ModelMapper modelMapper){
        this.users = users;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/user")
    public UserView retrieveLoggedUser(@NotNull Principal principal) {
        User user = this.getUser(principal.getName());
        return this.modelMapper.map(user, UserView.class);
    }

    private User getUser(String username) {
        return users.findByUsername(username).orElseThrow(() -> new ProcessingException("User not found."));
    }
}
