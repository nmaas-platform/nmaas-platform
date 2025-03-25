package net.geant.nmaas.portal.api.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.api.domain.ApplicationBaseViewS;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class DashboardView {

    private Long userCount;
    private Long domainsCount;
    private Long instanceCount;
    private int instanceCountInPeriod;
    private List<DashboardDeploymentsView> instanceCountInPeriodDetails;
    private Map<String, Integer> popularApps;



    @Override
    public String toString() {
        return new StringJoiner(", ", DashboardView.class.getSimpleName() + "[", "]")
                .add("userCount=" + userCount)
                .add("domainsCount=" + domainsCount)
                .add("instanceCount=" + instanceCount)
                .add("instanceCountInPeriod=" + instanceCountInPeriod)
                .add("popularApps=" + (popularApps != null ? popularApps.toString() : "null"))
                .toString();
    }
}
