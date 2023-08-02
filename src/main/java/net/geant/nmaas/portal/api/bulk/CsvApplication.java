package net.geant.nmaas.portal.api.bulk;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CsvApplication {

    @CsvBindByName(column = "domain")
    private String domainName;

    @CsvBindByName(column = "instance")
    private String applicationInstanceName;

    @CsvBindByName(column = "version")
    private String applicationVersion;

    @CsvBindAndJoinByName(column = "param.*", elementType = String.class, mapType = HashSetValuedHashMap.class, required = false)
    private MultiValuedMap<String, String> parameters;

}
