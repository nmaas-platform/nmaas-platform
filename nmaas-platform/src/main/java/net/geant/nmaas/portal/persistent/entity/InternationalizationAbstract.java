package net.geant.nmaas.portal.persistent.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@MappedSuperclass
public abstract class InternationalizationAbstract {

    @Setter(AccessLevel.PROTECTED)
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String language;

    private boolean enabled;

}
