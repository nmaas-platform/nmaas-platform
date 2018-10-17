package net.geant.nmaas.portal.persistent.entity;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name="internationalization")
public class Internationalization {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String language;
    @Lob
    @Type(type= "text")
    @Column
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
}