package net.geant.nmaas.portal.api.bulk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;

import java.time.OffsetDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BulkDeploymentViewS {

    public static final String BULK_DETAIL_KEY_APP_INSTANCE_NO = "appInstanceNo";
    public static final String BULK_DETAIL_KEY_APP_NAME = "appName";
    public static final String BULK_DETAIL_KEY_APP_ID = "appId";

    private Long id;
    private UserViewMinimal creator;
    private OffsetDateTime creationDate;
    private BulkDeploymentState state;
    private BulkType type;
    private Map<String, String> details;

}
