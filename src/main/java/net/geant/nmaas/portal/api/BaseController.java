package net.geant.nmaas.portal.api;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;

@RequiredArgsConstructor
public class BaseController {

	protected final ModelMapper modelMapper;
	protected final UserService userService;

	public User getUser(String username) {
		if (username == null) {
			throw new MissingElementException("Missing username");
		}
		return userService.findByUsername(username).orElseThrow(() -> new MissingElementException("Missing user " + username));
	}

    public User getUser(Long userId) {
		if (userId == null) {
			throw new MissingElementException("Missing user identifier");
		}
		return userService.findById(userId).orElseThrow(() -> new MissingElementException("Missing user id=" + userId));
	}

}
