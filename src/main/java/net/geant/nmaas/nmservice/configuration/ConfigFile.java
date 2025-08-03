package net.geant.nmaas.nmservice.configuration;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConfigFile {

    private String fileName;
    private String filePath;
    private String fileContent;

}
