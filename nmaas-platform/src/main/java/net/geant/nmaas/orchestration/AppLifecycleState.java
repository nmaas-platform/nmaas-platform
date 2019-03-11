package net.geant.nmaas.orchestration;

/**
 * Application lifecycle states as presented to the AppLifecycleManager client.
 */
public enum AppLifecycleState {

    REQUESTED{
        @Override
        public String getUserFriendlyState(){
            return "Application instance deployment requested";
        }
    },
    REQUEST_VALIDATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Request validation in progress";
        }
    },
    REQUEST_VALIDATED{
        @Override
        public String getUserFriendlyState(){
            return "Request validated";
        }
    },
    REQUEST_VALIDATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Request validation failed";
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Deployment environment preparation in progress";
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARED{
        @Override
        public String getUserFriendlyState(){
            return "Deployment environment is ready";
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Deployment environment preparation failed";
        }
    },
    MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "DCN configuration in progress";
        }
    },
    MANAGEMENT_VPN_CONFIGURED{
        @Override
        public String getUserFriendlyState(){
            return "DCN configured successfully";
        }
    },
    MANAGEMENT_VPN_CONFIGURATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "DCN configuration failed";
        }
    },
    APPLICATION_CONFIGURATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Application configuration in progress";
        }
    },
    APPLICATION_CONFIGURED{
        @Override
        public String getUserFriendlyState(){
            return "Application configured successfully";
        }
    },
    APPLICATION_CONFIGURATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Application configuration failed";
        }
    },
    APPLICATION_DEPLOYMENT_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Application deployment in progress";
        }
    },
    APPLICATION_DEPLOYED{
        @Override
        public String getUserFriendlyState(){
            return "Application deployed successfully";
        }
    },
    APPLICATION_DEPLOYMENT_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Application deployment failed";
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Deployment verification in progress";
        }
    },
    APPLICATION_DEPLOYMENT_VERIFIED{
        @Override
        public String getUserFriendlyState(){
            return "Deployment verified successfully";
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Deployment verification failed";
        }
    },
    APPLICATION_REMOVAL_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Application removal in progress";
        }
    },
    APPLICATION_REMOVED{
        @Override
        public String getUserFriendlyState(){
            return "Application removed successfully";
        }
    },
    APPLICATION_REMOVAL_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Application removal failed";
        }
    },
    APPLICATION_RESTART_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Application restart in progress";
        }
    },
    APPLICATION_RESTARTED{
        @Override
        public String getUserFriendlyState(){
            return "Application restarted successfully";
        }
    },
    APPLICATION_RESTART_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Application restart failed";
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS{
        @Override
        public String getUserFriendlyState() {
            return "Application configuration update in progress";
        }
    },
    APPLICATION_CONFIGURATION_UPDATED{
        @Override
        public String getUserFriendlyState() {
            return "Application configuration updated successfully";
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_FAILED {
        @Override
        public String getUserFriendlyState() {
            return "Application configuration update failed";
        }
    },
    UNKNOWN{
        @Override
        public String getUserFriendlyState(){
            return "Unknown state";
        }
    },
    INTERNAL_ERROR{
        @Override
        public String getUserFriendlyState(){
            return "Internal error";
        }
    };

    public abstract String getUserFriendlyState();

}
