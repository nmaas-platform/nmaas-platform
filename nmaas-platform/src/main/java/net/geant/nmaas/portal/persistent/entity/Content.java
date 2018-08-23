package net.geant.nmaas.portal.persistent.entity;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="content")
public class Content {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    private String name;
    @Lob
    @Column
    private String content;
    private String title;

    public Content(String name, String title, String content){
        this.name = name;
        this.title = title;
        this.content = content;
    }

}
