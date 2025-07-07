package net.geant.nmaas.kubernetes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClusterConfigView {

    private String apiVersion;
    private String kind;

    @JsonProperty("current-context")
    private String currentContext;

    private List<ClusterEntry> clusters;
    private List<ContextEntry> contexts;
    private ClusterPreferences preferences;
    private List<UserEntry> users;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClusterConfigView {").append("\n")
                .append("  apiVersion: ").append(apiVersion).append("\n")
                .append("  kind: ").append(kind).append("\n")
                .append("  currentContext: ").append(currentContext).append("\n")
                .append("  clusters: ").append(clusters).append("\n")
                .append("  contexts: ").append(contexts).append("\n")
                .append("  preferences: ").append(preferences).append("\n")
                .append("  users: ").append(users).append("\n")
                .append("}");
        return sb.toString();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClusterEntry {
        private Cluster cluster;
        private String name;

        @Override
        public String toString() {
            return "\n    ClusterEntry { " +
                    "name: '" + name + "', " +
                    "cluster: " + cluster + " }";
        }
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Cluster {
        @JsonProperty("certificate-authority-data")
        private String certificateAuthorityData;
        private String server;

        @Override
        public String toString() {
            return "{ server: '" + server + "', certificateAuthorityData: " + certificateAuthorityData + " }";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContextEntry {
        private Context context;
        private String name;

        @Override
        public String toString() {
            return "\n    ContextEntry { name: '" + name + "', context: " + context + " }";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Context {
        private String cluster;
        private String user;

        @Override
        public String toString() {
            return "{ cluster: '" + cluster + "', user: '" + user + "' }";
        }
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
    public static class UserEntry {
        private String name;
        private UserToken user;

        @Override
        public String toString() {
            return "\n    UserEntry { name: '" + name + "', user: " + user + " }";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserToken {
        private String token;
        @JsonProperty("client-certificate-data")
        private String clientCertificateData;
        @JsonProperty("client-key-data")
        private String clientKeyData;

        @Override
        public String toString() {
            return "{ token: " + token + ", client certificate data: " + clientCertificateData + ", client key data: " + clientKeyData + " }";
        }
    }
}
