package net.geant.nmaas.portal.api.csv;

import com.opencsv.bean.CsvBindByName;

public class CsvUser extends CsvBean {

    @CsvBindByName(column = "domain")
    private String domainName;
}
