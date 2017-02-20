package net.geant.nmaas.portal.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.persistent.entity.User;

public interface UserService {
	public Optional<User> getByUsername(String username);
	public boolean register(User user);
}
