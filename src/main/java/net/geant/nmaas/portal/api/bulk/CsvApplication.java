package net.geant.nmaas.portal.api.bulk;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@Getter
@Setter
public class CsvApplication extends CsvBean{

    @CsvBindByName(column = "domain")
    private String domainName;
    @CsvBindByName(column = "app")
    private String applicationName;
    @CsvBindByName(column = "instance")
    private String applicationInstanceName;
    @CsvBindByName(column = "version")
    private String applicationVersion;
    @CsvBindAndJoinByName(column = "param.*", elementType = String.class, mapType = HashSetValuedHashMap.class, required = false)
    private MultiValuedMap<String, String> parameters;


    @Override
    public String toString() {
        return String.format("Deployment of %s in Domain %s with name %s - version %s Additional parameters: %s", applicationName, domainName, applicationInstanceName, applicationVersion, parameters);
    }
}
