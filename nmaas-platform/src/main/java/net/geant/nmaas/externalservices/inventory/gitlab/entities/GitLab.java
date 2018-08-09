package net.geant.nmaas.externalservices.inventory.gitlab.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gitlab")
public class GitLab {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String server;

    @Column(nullable = false)
    private String sshServer;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    private String repositoryAccessUsername;

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

    public String getSshServer() {
        return sshServer;
    }

    public void setSshServer(String sshServer) {
        this.sshServer = sshServer;
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

    public String getRepositoryAccessUsername() {
        return repositoryAccessUsername;
    }

    public void setRepositoryAccessUsername(String repositoryAccessUsername) {
        this.repositoryAccessUsername = repositoryAccessUsername;
    }

    @JsonIgnore
    public String getApiUrl(){
        return String.format("http://%s:%d", this.server, this.port);
    }

}
