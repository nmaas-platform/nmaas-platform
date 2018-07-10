package net.geant.nmaas.portal.api.info;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/info")
@PropertySource("classpath:git.properties")
public class GeneralInformationController {

    @Value("classpath:changelog.json")
    private Resource changelogPath;

    @GetMapping(value = "/changelog", produces = "application/json")
    public FileSystemResource getChangelog() throws IOException {
        return new FileSystemResource(changelogPath.getFile());
    }

    @GetMapping(value = "/git")
    public Map<String, String> getGitInfo(@Value("${git.build.time}")String buildTime, @Value("${git.commit.id.abbrev}")String commitName,
                                          @Value("${git.build.version}")String buildVersion, @Value("${git.branch}") String branchName){
        return ImmutableMap.of("buildTime",buildTime, "commitName",commitName, "buildVersion", buildVersion, "branchName", branchName);
    }

}
