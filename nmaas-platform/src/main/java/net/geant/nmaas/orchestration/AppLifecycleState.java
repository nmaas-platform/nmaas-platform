package net.geant.nmaas.orchestration;

/**
 * Application lifecycle states as presented to the AppLifecycleManager client.
 */
public enum AppLifecycleState {

    REQUESTED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DEPLOY_REQUESTED";
        }
    },
    REQUEST_VALIDATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.VALIDATION_REQUEST";
        }
    },
    REQUEST_VALIDATED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.REQUEST_VALIDATE";
        }
    },
    REQUEST_VALIDATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.VALIDATE_FAILED";
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DEPLOY_IN_PROGRESS";
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.ENV_READY";
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.ENV_FAILED";
        }
    },
    MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DCN_PROGRESS";
        }
    },
    MANAGEMENT_VPN_CONFIGURED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DCN_SUCCESS";
        }
    },
    MANAGEMENT_VPN_CONFIGURATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DCN_FAILED";
        }
    },
    APPLICATION_CONFIGURATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_CONFIG_PROGRESS";
        }
    },
    APPLICATION_CONFIGURED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_CONFIG_SUCCESS";
        }
    },
    APPLICATION_CONFIGURATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_CONFIG_FAILED";
        }
    },
    APPLICATION_DEPLOYMENT_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_DEPLOY_PROGRESS";
        }
    },
    APPLICATION_DEPLOYED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_DEPLOY_SUCCESS";
        }
    },
    APPLICATION_DEPLOYMENT_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_DEPLOY_FAILED";
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DEPLOY_VER_PROGRESS";
        }
    },
    APPLICATION_DEPLOYMENT_VERIFIED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DEPLOY_VER_SUCCESS";
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.DEPLOY_VER_FAILED";
        }
    },
    APPLICATION_REMOVAL_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_REMOVE_PROGRESS";
        }
    },
    APPLICATION_REMOVED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_REMOVE_SUCCESS";
        }
    },
    APPLICATION_REMOVAL_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_REMOVE_FAILED";
        }
    },
    APPLICATION_RESTART_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_RESTART_PROGRESS";
        }
    },
    APPLICATION_RESTARTED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_RESTART_SUCCESS";
        }
    },
    APPLICATION_RESTART_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.APP_RESTART_FAILED";
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS{
        @Override
        public String getUserFriendlyState() {
            return "APP_INSTANCE.PROGRESS.APP_UPDATE_PROGRESS";
        }
    },
    APPLICATION_CONFIGURATION_UPDATED{
        @Override
        public String getUserFriendlyState() {
            return "APP_INSTANCE.PROGRESS.APP_UPDATE_SUCCESS";
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_FAILED {
        @Override
        public String getUserFriendlyState() {
            return "APP_INSTANCE.PROGRESS.APP_UPDATE_FAILED";
        }
    },
    UNKNOWN{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.UNKNOWN";
        }
    },
    INTERNAL_ERROR{
        @Override
        public String getUserFriendlyState(){
            return "APP_INSTANCE.PROGRESS.ERROR";
        }
    },
    FAILED_APPLICATION_REMOVED{
        @Override
        public String getUserFriendlyState() { return "APP_INSTANCE.PROGRESS.FAILED_APPLICATION_REMOVED"; }
    };

    public abstract String getUserFriendlyState();

}
