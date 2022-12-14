package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistent.results.UserLoginDate;

import java.util.List;
import java.util.Optional;

public interface UserLoginRegisterService {

    UserLoginRegister registerNewSuccessfulLogin(User user, String host, String userAgent, String remoteAddress);
    UserLoginRegister registerNewFailedLogin(User user, String host, String userAgent, String remoteAddress);

    Optional<UserLoginRegister> getLastLogin(User user);
    Optional<UserLoginRegister> getLastSuccessfulLogin(User user);
    Optional<UserLoginRegister> getLastFailedLogin(User user);
    Optional<UserLoginRegister> getFirsLogin(User user);

    List<UserLoginRegister> getAllLoginDetails();
    List<UserLoginRegister> getAllLoginDetails(User user);

    Optional<UserLoginDate> getUserFirstAndLastSuccessfulLoginDate(User user);
    List<UserLoginDate> getAllFirstAndLastSuccessfulLoginDate();
}
