package net.geant.nmaas.portal.api.profile;

import net.geant.nmaas.portal.api.domain.User;
import net.geant.nmaas.portal.api.exception.ProcessingException;
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

    private UserService users;

    private ModelMapper modelMapper;

    @Autowired
    public ProfileController(UserService users, ModelMapper modelMapper){
        this.users = users;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/user")
    public User retrieveLoggedUser(@NotNull Principal principal) throws ProcessingException{
        net.geant.nmaas.portal.persistent.entity.User user = this.getUser(principal.getName());
        return this.modelMapper.map(user, User.class);
    }

    private net.geant.nmaas.portal.persistent.entity.User getUser(String username) throws ProcessingException{
        return users.findByUsername(username).orElseThrow(() -> new ProcessingException("User not found."));
    }
}
