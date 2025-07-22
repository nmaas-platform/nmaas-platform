package net.geant.nmaas.externalservices.kubernetes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClusterConfigView {

    private String apiVersion;
    private String kind;

    @JsonProperty("current-context")
    private String currentContext;

    private List<ClusterEntry> clusters;
    private List<ContextEntry> contexts;
    private ClusterPreferences preferences;
    private List<UserEntry> users;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class ClusterEntry {

        private Cluster cluster;
        private String name;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Cluster {

        @JsonProperty("certificate-authority-data")
        private String certificateAuthorityData;
        private String server;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class ContextEntry {

        private Context context;
        private String name;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Context {

        private String cluster;
        private String user;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ClusterPreferences {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class UserEntry {

        private String name;
        private UserToken user;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class UserToken {

        private String token;

        @JsonProperty("client-certificate-data")
        private String clientCertificateData;

        @JsonProperty("client-key-data")
        private String clientKeyData;

    }

}