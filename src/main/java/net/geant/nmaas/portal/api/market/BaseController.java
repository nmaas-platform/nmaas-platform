package net.geant.nmaas.portal.api.market;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;

@RequiredArgsConstructor
public class BaseController {

	protected final ModelMapper modelMapper;
	protected final UserService userService;

	protected User getUser(String username) {
		if (username == null) {
			throw new MissingElementException("Missing username.");
		}
		return userService.findByUsername(username).orElseThrow(() -> new MissingElementException("Missing user " + username));
	}

	protected User getUser(Long userId) {
		if (userId == null) {
			throw new MissingElementException("Missing username.");
		}
		return userService.findById(userId).orElseThrow(() -> new MissingElementException("Missing user id=" + userId));
	}

}