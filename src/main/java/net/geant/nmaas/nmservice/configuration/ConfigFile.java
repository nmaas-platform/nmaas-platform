package net.geant.nmaas.nmservice.configuration;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class ConfigFile {

    @ToString.Include
    private String fileName;
    @ToString.Include
    private String filePath;
    private String fileContent;

}
