package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.domains.DomainRequest;
import net.geant.nmaas.kubernetes.DummyKubernetesApiClientServiceConfig;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserListEntry;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static net.geant.nmaas.portal.persistence.entity.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(DummyKubernetesApiClientServiceConfig.class)
public class UserServiceIntTest {

    private final DomainRepository domainRepository;

    private final DomainService domainService;

    private final UserRepository userRepository;

    private final UserService userService;

    public UserServiceIntTest(@Autowired DomainRepository domainRepository, @Autowired DomainService domainService,
                              @Autowired UserRepository userRepository, @Autowired UserService userService) {
        this.domainRepository = domainRepository;
        this.domainService = domainService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @BeforeEach
    void addDomains() {
        userRepository.findAll().stream().filter(u -> !Objects.equals(u.getUsername(), "admin")).forEach(
                u -> userRepository.delete(u)
        );
        domainRepository.findAll().stream().filter(d -> !Objects.equals(d.getName(), "GLOBAL")).forEach(
                d -> domainRepository.deleteById(d.getId())
        );

        domainService.createGlobalDomain();
        domainService.createDomain(new DomainRequest("DOMAIN", "domain", true));
        domainService.createDomain(new DomainRequest("DOMAIN2", "domain2", true));

        User userStub = new User("userEntity", true, "userEntity",
                domainService.findDomain("DOMAIN").get(), Collections.singletonList(ROLE_USER));
        userStub.setFirstname("Test1");
        userStub.setLastname("Test1");
        userStub.setEmail("test1@gmail.com");
        userRepository.save(userStub);

        User userStub2 = new User("userEntity2", true, "userEntity2",
                domainService.findDomain("DOMAIN2").get(), Collections.singletonList(ROLE_USER));
        userStub2.setFirstname("Test2");
        userStub2.setLastname("Test2");
        userStub2.setEmail("test2@gmail.com");
        userRepository.save(userStub2);

        User userStub3 = new User("userEntity3", true, "userEntity3",
                domainService.findDomain("DOMAIN2").get(), Collections.singletonList(ROLE_USER));
        userStub3.setFirstname("Test3");
        userStub3.setLastname("Test3");
        userStub3.setEmail("test3@gmail.com");
        userRepository.save(userStub3);
    }

//    @AfterEach
//    void removeDomains() {
//        userRepository.findAll().stream().filter(u -> !Objects.equals(u.getUsername(), "admin")).forEach(
//                u -> userRepository.delete(u)
//        );
//        domainRepository.findAll().stream().filter(d -> !Objects.equals(d.getName(), "GLOBAL")).forEach(
//                d -> domainRepository.deleteById(d.getId())
//        );
//    }

    @Test
    void shouldGetAllUsers() {
        Page<UserListEntry> users = userService.findAllListEntry(PageRequest.of(0, 10), "");
        assertThat(users.getContent().size()).isEqualTo(4);

        users = userService.findAllListEntry(PageRequest.of(0, 2), "");
        assertThat(users.getContent().size()).isEqualTo(2);

        users = userService.findAllListEntry(PageRequest.of(0, 10), "ad");
        assertThat(users.getContent().size()).isEqualTo(1);
        assertThat(users.getContent().getFirst().getUsername()).isEqualTo("admin");

        users = userService.findAllListEntry(PageRequest.of(0, 6), "Test2");
        assertThat(users.getContent().size()).isEqualTo(1);
        assertThat(users.getContent().getFirst().getUsername()).isEqualTo("userEntity2");

        users = userService.findAllListEntry(PageRequest.of(0, 20), "gmail");
        assertThat(users.getContent().size()).isEqualTo(3);
        assertThat(users.getContent().getFirst().getUsername()).contains("userEntity");
    }

    @Test
    void shouldGetAllUsersInDomain() {
        Domain domain2 = domainService.findDomain("DOMAIN2").get();

        Page<UserListEntry> users = userService.findAllInDomainListEntry(domain2.getId(), PageRequest.of(0, 10), "");
        assertThat(users.getContent().size()).isEqualTo(2);
        assertThat(users.getContent().getFirst().getUsername()).isEqualTo("userEntity2");

        users = userService.findAllInDomainListEntry(domain2.getId(), PageRequest.of(0, 10), "Test2");
        assertThat(users.getContent().size()).isEqualTo(1);
        assertThat(users.getContent().getFirst().getUsername()).isEqualTo("userEntity2");
    }

    @Test
    void shouldGetAllUsersSorted() {
        Page<UserListEntry> users = userService.findAllListEntry(
                PageRequest.of(0, 10, Sort.Direction.ASC, "username"), "");
        assertThat(users.getContent().size()).isEqualTo(4);
        assertThat(users.getContent().stream().map(UserListEntry::getUsername).toList())
                .isEqualTo(List.of("admin", "userEntity", "userEntity2", "userEntity3"));

        users = userService.findAllListEntry(
                PageRequest.of(0, 2, Sort.Direction.DESC, "firstname"), "");
        assertThat(users.getContent().size()).isEqualTo(2);
        assertThat(users.getContent().stream().map(UserListEntry::getUsername).toList()).isEqualTo(List.of("userEntity3", "userEntity2"));

        users = userService.findAllListEntry(
                PageRequest.of(1, 2, Sort.Direction.DESC, "firstname"), "");
        assertThat(users.getContent().size()).isEqualTo(2);
        assertThat(users.getContent().stream().map(UserListEntry::getUsername).toList()).isEqualTo(List.of("userEntity", "admin"));
    }

    @Test
    void shouldGetAllUsersInDomainSorted() {
        Domain domain2 = domainService.findDomain("DOMAIN2").get();

        Page<UserListEntry> users = userService.findAllInDomainListEntry(domain2.getId(),
                PageRequest.of(0, 10, Sort.Direction.ASC, "lastname"), "");
        assertThat(users.getContent().size()).isEqualTo(2);
        assertThat(users.getContent().stream().map(UserListEntry::getUsername).toList()).isEqualTo(List.of("userEntity2", "userEntity3"));

        users = userService.findAllInDomainListEntry(domain2.getId(),
                PageRequest.of(0, 1, Sort.Direction.DESC, "firstname"), "");
        assertThat(users.getContent().size()).isEqualTo(1);
        assertThat(users.getContent().stream().map(UserListEntry::getUsername).toList()).isEqualTo(List.of("userEntity3"));
    }

}
