package net.geant.nmaas.externalservices.inventory.gitlab.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitLabView {
    private Long id;
    private String server;
    private String token;
    private Integer port;
}
