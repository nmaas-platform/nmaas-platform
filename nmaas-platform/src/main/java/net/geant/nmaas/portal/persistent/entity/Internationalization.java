package net.geant.nmaas.portal.persistent.entity;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Internationalization {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String language;

    private boolean enabled;

    @Lob
    @Type(type= "text")
    @Column
    private String content;
}