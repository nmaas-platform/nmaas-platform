package net.geant.nmaas.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerApiClient;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DockerApiClientMockInit {

    public static void mockMethods(DockerApiClient dockerApiClient) throws DockerException, InterruptedException {
        when(dockerApiClient.createNetwork(any(), any())).thenReturn("networkId");
        when(dockerApiClient.createContainer(any(), any(), any())).thenReturn("containerId");
        when(dockerApiClient.execCreate(any(), any(), any(), any())).thenReturn(new ExecCreation() {
            @Override
            public String id() {
                return "execCreationId";
            }

            @Override
            public ImmutableList<String> warnings() {
                return null;
            }
        });
        when(dockerApiClient.inspectContainer(any(), any())).thenReturn(new ContainerInfo() {
            @Override
            public String id() {
                return "containerId";
            }

            @Override
            public Date created() {
                return null;
            }

            @Override
            public String path() {
                return null;
            }

            @Override
            public ImmutableList<String> args() {
                return null;
            }

            @Override
            public ContainerConfig config() {
                return null;
            }

            @Override
            public HostConfig hostConfig() {
                return null;
            }

            @Override
            public ContainerState state() {
                return new ContainerState() {
                    @Override
                    public String status() {
                        return "running";
                    }

                    @Override
                    public Boolean running() {
                        return null;
                    }

                    @Override
                    public Boolean paused() {
                        return null;
                    }

                    @Override
                    public Boolean restarting() {
                        return null;
                    }

                    @Override
                    public Integer pid() {
                        return null;
                    }

                    @Override
                    public Integer exitCode() {
                        return null;
                    }

                    @Override
                    public Date startedAt() {
                        return null;
                    }

                    @Override
                    public Date finishedAt() {
                        return null;
                    }

                    @Override
                    public String error() {
                        return null;
                    }

                    @Override
                    public Boolean oomKilled() {
                        return null;
                    }

                    @Override
                    public Health health() {
                        return null;
                    }
                };
            }

            @Override
            public String image() {
                return null;
            }

            @Override
            public NetworkSettings networkSettings() {
                return null;
            }

            @Override
            public String resolvConfPath() {
                return null;
            }

            @Override
            public String hostnamePath() {
                return null;
            }

            @Override
            public String hostsPath() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String driver() {
                return null;
            }

            @Override
            public String execDriver() {
                return null;
            }

            @Override
            public String processLabel() {
                return null;
            }

            @Override
            public String mountLabel() {
                return null;
            }

            @Override
            public ImmutableMap<String, String> volumes() {
                return null;
            }

            @Override
            public ImmutableMap<String, Boolean> volumesRw() {
                return null;
            }

            @Override
            public String appArmorProfile() {
                return null;
            }

            @Override
            public ImmutableList<String> execIds() {
                return null;
            }

            @Override
            public String logPath() {
                return null;
            }

            @Override
            public Long restartCount() {
                return null;
            }

            @Override
            public ImmutableList<ContainerMount> mounts() {
                return null;
            }

            @Override
            public Node node() {
                return null;
            }
        });
        when(dockerApiClient.countContainersInNetwork(any(), any())).thenReturn(1);
    }

}
