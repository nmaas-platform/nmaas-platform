package net.geant.nmaas.portal.persistent.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;


@Entity
@IdClass(UserLoginRegister.UserLoginRegisterId.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRegister {

    @Id
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime date;

    /**
     * single column, two values attached
     * reference: https://stackoverflow.com/questions/39185977/failed-to-convert-request-element-in-entity-with-idclass
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private UserLoginRegisterType type;
    private String remoteAddress;
    private String host;
    private String userAgent;

    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    public static class UserLoginRegisterId implements Serializable {
        private OffsetDateTime date;
        private Long userId;
    }

    public UserLoginRegister(OffsetDateTime date, User user, UserLoginRegisterType type, String remoteAddress, String host, String userAgent) {
        this.date = date;
        this.userId = user.getId();
        this.user = user;
        this.type = type;
        this.remoteAddress = remoteAddress;
        this.host = host;
        this.userAgent = userAgent;
    }
}
