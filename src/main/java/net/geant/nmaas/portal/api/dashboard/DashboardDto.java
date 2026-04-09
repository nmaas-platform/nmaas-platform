package net.geant.nmaas.portal.api.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class DashboardDto {

    private Long userCount;
    private Long domainsCount;
    private Long instanceCount;
    private int instanceCountInPeriod;
    private List<DashboardDeploymentsDto> instanceCountInPeriodDetails;
    private Map<String, Integer> popularApps;

    @Override
    public String toString() {
        return new StringJoiner(", ", DashboardDto.class.getSimpleName() + "[", "]")
                .add("userCount=" + userCount)
                .add("domainsCount=" + domainsCount)
                .add("instanceCount=" + instanceCount)
                .add("instanceCountInPeriod=" + instanceCountInPeriod)
                .add("popularApps=" + (popularApps != null ? popularApps.toString() : "null"))
                .toString();
    }

}
