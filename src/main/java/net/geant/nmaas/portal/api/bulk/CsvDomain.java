package net.geant.nmaas.portal.api.bulk;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CsvDomain extends CsvBean {

    @CsvBindByName(column = "domain")
    private String domainName;

    @CsvBindByName(column = "username")
    private String adminUserName;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "networks")
    private Object DCNetworks;

    @CsvBindByName(column = "domainGroups")
    private String domainGroups;

    @Override
    public String toString() {
        return String.format("Bulk deployment for domain  %s with admin %s %s. Networks: %s, DomainGroups: %s", domainName, email, adminUserName, DCNetworks, domainGroups);
    }

}
