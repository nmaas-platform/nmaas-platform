package net.geant.nmaas.portal.api.bulk;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CsvReplay {

    private Boolean successful;

    private String createdInfo;

    private Map<String, String> details;

    private BulkType type;




}

