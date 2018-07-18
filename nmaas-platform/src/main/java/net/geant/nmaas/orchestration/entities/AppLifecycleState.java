package net.geant.nmaas.orchestration.entities;

import net.geant.nmaas.orchestration.AppLifecycleManager;

/**
 * Application lifecycle states as presented to the {@link AppLifecycleManager} client.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum AppLifecycleState {

    REQUESTED{
        @Override
        public String getUserFriendlyState(){
            return "Requested";
        }
    },
    REQUEST_VALIDATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Validation in progress";
        }
    },
    REQUEST_VALIDATED{
        @Override
        public String getUserFriendlyState(){
            return "Successful validation";
        }
    },
    REQUEST_VALIDATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "Request validation has failed";
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "Preparing deployment environment";
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
            return "Preparation of deployment environment has failed";
        }
    },
    MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS{
        @Override
        public String getUserFriendlyState(){
            return "VPN configuration in progress";
        }
    },
    MANAGEMENT_VPN_CONFIGURED{
        @Override
        public String getUserFriendlyState(){
            return "VPN configured successfully";
        }
    },
    MANAGEMENT_VPN_CONFIGURATION_FAILED{
        @Override
        public String getUserFriendlyState(){
            return "VPN configuration has failed";
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
            return "Configuration of application has failed";
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
            return "Deployment of application has failed";
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
            return "Deployment verification has failed";
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
            return "Application removal has failed";
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
    },
    GENERIC_ERROR{
        @Override
        public String getUserFriendlyState(){
            return "Generic error";
        }
    };

    public abstract String getUserFriendlyState();

}
