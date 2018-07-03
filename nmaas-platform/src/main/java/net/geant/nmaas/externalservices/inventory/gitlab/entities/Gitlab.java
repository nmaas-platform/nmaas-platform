package net.geant.nmaas.externalservices.inventory.gitlab.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name="gitlab")
public class Gitlab {

    @Id
    @Column(name="id")
    private Long id;

    @Column(nullable=false)
    private String server;

    @Column(nullable=false)
    private String apiVersion;

    @Column(nullable=false)
    private String token;

    @Column(nullable=false)
    private Integer port;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonIgnore
    public String getApiUrl(){
        return String.format("http://%s:%d",this.server,this.port);
    }
}
