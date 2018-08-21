package net.geant.nmaas.portal.persistent.entity;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="content")
public class Content {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    private String name;
    @Lob
    @Column
    private String content;
    private String title;
    //private Date lastUpdate;

    public Content(){

    }

    public Content(String name){
        this.name = name;
        this.content = "";
    }

    public Content(String name, String title, String content){
        this.name = name;
        this.title = title;
        this.content = content;
    }

    public void setTitle(String title){ this.title = title;}

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName(){return this.name;}

    public String getContent(){return this.content;}

    public String getTitle(){return this.title;}

    public Long getId(){return this.id;}
}
