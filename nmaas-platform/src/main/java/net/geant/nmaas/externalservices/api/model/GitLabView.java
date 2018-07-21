package net.geant.nmaas.externalservices.api.model;

public class GitLabView {

    private Long id;
    private String server;
    private String token;
    private Integer port;
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

    public void setRepositoryAccessUsername(String repositoryAccessUsername){
        this.repositoryAccessUsername = repositoryAccessUsername;
    }

    public String getRepositoryAccessUsername(){
        return this.repositoryAccessUsername;
    }
}
