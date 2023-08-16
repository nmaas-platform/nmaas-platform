package net.geant.nmaas.portal.api.bulk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BulkDeploymentEntryView {

    public static final String BULK_ENTRY_DETAIL_KEY_DOMAIN_ID = "domainId";
    public static final String BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME = "domainName";
    public static final String BULK_ENTRY_DETAIL_KEY_DOMAIN_CODENAME = "domainCodename";

    public static final String BULK_ENTRY_DETAIL_KEY_USER_ID = "userId";
    public static final String BULK_ENTRY_DETAIL_KEY_USER_NAME = "userName";
    public static final String BULK_ENTRY_DETAIL_KEY_USER_EMAIL = "email";

    public static final String BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_ID = "appInstanceId";
    public static final String BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_NAME = "appInstanceName";

    public static final String BULK_ENTRY_DETAIL_KEY_STATUS = "status";
    public static final String BULK_ENTRY_DETAIL_KEY_ERROR_MESSAGE = "errorMessage";
    public static final String BULK_ENTRY_DETAIL_KEY_APP_INSTANCE_NO = "appInstanceNo";
    public static final String BULK_ENTRY_DETAIL_KEY_APP_NAME = "appName";
    public static final String BULK_ENTRY_DETAIL_KEY_APP_ID = "appId";

    private BulkType type;
    private BulkDeploymentState state;
    private Boolean created;
    private Map<String, String> details;

}
