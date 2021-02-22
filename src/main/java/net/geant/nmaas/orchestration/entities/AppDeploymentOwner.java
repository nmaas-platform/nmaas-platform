package net.geant.nmaas.orchestration.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AppDeploymentOwner {

    private String username;

    private String name;

    private String email;

    private List<String> sshKeys;

}
