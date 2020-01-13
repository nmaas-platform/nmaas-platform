package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistent.entity.UserLoginRegisterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLoginRegisterRepository extends JpaRepository<UserLoginRegister, UserLoginRegister.UserLoginRegisterId> {

    Optional<UserLoginRegister> findFirstByUserOrderByDateDesc(User user);
    Optional<UserLoginRegister> findFirstByUserAndTypeOrderByDateDesc(User user, UserLoginRegisterType type);

    List<UserLoginRegister> findAllByUserOrderByDateDesc(User user);
}
