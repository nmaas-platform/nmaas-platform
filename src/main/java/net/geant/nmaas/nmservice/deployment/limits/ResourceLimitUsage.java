package net.geant.nmaas.nmservice.deployment.limits;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResourceLimitUsage {

    private Integer memoryUsed;
    private Integer memoryLimit;

    private Integer cpuUsed;
    private Integer cpuLimit;

    private Integer instancesNoUsed;
    private Integer instancesNoLimit;

    private Integer containersNoUsed;
    private Integer containersNoLimit;

    private boolean globalLimit;

    @Override
    public String toString() {
        return String.format("ResourceLimitUsage -> memory = %d/%d, cpuUsed = %d/%d, instancesNo = %d/%d, containersNoUsed = %d/%d, globalLimit=%b",
                memoryUsed, memoryLimit, cpuUsed, cpuLimit, instancesNoUsed, instancesNoLimit, containersNoUsed, containersNoLimit, globalLimit);
    }
}
