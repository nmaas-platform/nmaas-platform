package net.geant.nmaas.portal.api.info;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/info")
@PropertySource("classpath:git.properties")
public class GeneralInformationController {

    @Value("classpath:changelog.json")
    private Resource changelogPath;

    @GetMapping(value = "/changelog", produces = "application/json")
    public FileSystemResource getChangelog() throws IOException {
        File tempJsonFile = File.createTempFile("changelog",".json");
        try(InputStream inputStream = changelogPath.getInputStream()) {
            FileUtils.copyInputStreamToFile(inputStream, tempJsonFile);
        }
        return new FileSystemResource(tempJsonFile);
    }

    @GetMapping(value = "/git")
    public Map<String, String> getGitInfo(@Value("${git.commit.time:none}") String buildTime, @Value("${git.commit.id.abbrev}") String commitName,
                                          @Value("${git.build.version}") String buildVersion, @Value("${git.branch}") String branchName){
        return ImmutableMap.of("buildTime",buildTime, "commitName",commitName, "buildVersion", buildVersion, "branchName", branchName);
    }

}
