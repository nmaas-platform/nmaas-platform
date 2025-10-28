package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistence.results.UserLoginDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserLoginRegisterRepository extends JpaRepository<UserLoginRegister, UserLoginRegister.UserLoginRegisterId> {

    Optional<UserLoginRegister> findFirstByUserOrderByDateDesc(User user);
    Optional<UserLoginRegister> findFirstByUserAndTypeOrderByDateDesc(User user, UserLoginRegisterType type);

    Optional<UserLoginRegister> findFirstByUserOrderByDateAsc(User user);

    List<UserLoginRegister> findAllByUserOrderByDateDesc(User user);

    @Query("SELECT new net.geant.nmaas.portal.persistence.results.UserLoginDate(u.userId, MIN(u.date), MAX(u.date)) FROM UserLoginRegister u group by u.userId")
    List<UserLoginDate> findAllFirstAndLastLogin();

    @Query("SELECT new net.geant.nmaas.portal.persistence.results.UserLoginDate(u.userId, MIN(u.date), MAX(u.date)) FROM UserLoginRegister u group by u.userId, u.type having u.type = :type")
    List<UserLoginDate> findAllFirstAndLastLoginByType(UserLoginRegisterType type);

    @Query("SELECT new net.geant.nmaas.portal.persistence.results.UserLoginDate(u.userId, MIN(u.date), MAX(u.date)) FROM UserLoginRegister u group by u.userId, u.type having u.type = :type and u.userId = :userId")
    Optional<UserLoginDate> findFirstAndLastLoginByUserAndType(Long userId, UserLoginRegisterType type);

}
