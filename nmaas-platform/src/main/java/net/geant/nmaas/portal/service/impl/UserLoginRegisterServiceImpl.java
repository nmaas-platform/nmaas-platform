package net.geant.nmaas.portal.service.impl;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistent.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistent.repositories.UserLoginRegisterRepository;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@NoArgsConstructor
public class UserLoginRegisterServiceImpl implements UserLoginRegisterService {

    private UserLoginRegisterRepository repository;

    @Autowired
    public UserLoginRegisterServiceImpl(UserLoginRegisterRepository registerRepository) {
        this.repository = registerRepository;
    }

    @Override
    public UserLoginRegister registerNewSuccessfulLogin(User user, String host, String userAgent, String remoteAddress) {
        UserLoginRegister ulr = new UserLoginRegister(OffsetDateTime.now(), user, UserLoginRegisterType.SUCCESS, remoteAddress, host, userAgent);
        log.info("Store");
        UserLoginRegister temp = repository.save(ulr);
        log.info(temp.getDate().toString());
        log.info("Login Successful");
        return temp;
    }

    @Override
    public UserLoginRegister registerNewFailedLogin(User user, String host, String userAgent, String remoteAddress) {
        UserLoginRegister ulr = new UserLoginRegister(OffsetDateTime.now(), user, UserLoginRegisterType.FAILURE, remoteAddress, host, userAgent);
        return repository.save(ulr);
    }

    @Override
    public Optional<UserLoginRegister> getLastLogin(User user) {
        return repository.findFirstByUserOrderByDateDesc(user);
    }

    @Override
    public Optional<UserLoginRegister> getLastSuccessfulLogin(User user) {
        return repository.findFirstByUserAndTypeOrderByDateDesc(user, UserLoginRegisterType.SUCCESS);
    }

    @Override
    public Optional<UserLoginRegister> getLastFailedLogin(User user) {
        return repository.findFirstByUserAndTypeOrderByDateDesc(user, UserLoginRegisterType.FAILURE);
    }

    @Override
    public List<UserLoginRegister> getAllLoginDetails() {
        return repository.findAll();
    }

    @Override
    public List<UserLoginRegister> getAllLoginDetails(User user) {
        return repository.findAllByUserOrderByDateDesc(user);
    }
}
