package net.geant.nmaas.deploymentorchestration;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentState;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum AppDeploymentState {

    REQUESTED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.REQUESTED;
        }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REQUEST_VERIFIED:
                    return REQUESTED_DCN_REQUEST_VALIDATED;
                case REQUEST_VERIFICATION_FAILED:
                    return REQUEST_VALIDATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REQUEST_VERIFIED:
                    return REQUESTED_NM_SERVICE_REQUEST_VALIDATED;
                case REQUEST_VERIFICATION_FAILED:
                    return REQUEST_VALIDATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    REQUESTED_NM_SERVICE_REQUEST_VALIDATED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.REQUEST_VALIDATION_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REQUEST_VERIFIED:
                    return REQUEST_VALIDATED;
                case REQUEST_VERIFICATION_FAILED:
                    return REQUEST_VALIDATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    REQUESTED_DCN_REQUEST_VALIDATED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.REQUEST_VALIDATION_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REQUEST_VERIFIED:
                    return REQUEST_VALIDATED;
                case REQUEST_VERIFICATION_FAILED:
                    return REQUEST_VALIDATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    REQUEST_VALIDATED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.REQUEST_VALIDATED; }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case ENVIRONMENT_PREPARED:
                    return DEPLOYMENT_ENVIRONMENT_FOR_DCN_PREPARED;
                case ENVIRONMENT_PREPARATION_FAILED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case ENVIRONMENT_PREPARED:
                    return DEPLOYMENT_ENVIRONMENT_FOR_NM_SERVICE_PREPARED;
                case ENVIRONMENT_PREPARATION_FAILED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    REQUEST_VALIDATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.REQUEST_VALIDATION_FAILED; }

    },
    DEPLOYMENT_ENVIRONMENT_FOR_NM_SERVICE_PREPARED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case ENVIRONMENT_PREPARED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARED;
                case ENVIRONMENT_PREPARATION_FAILED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    DEPLOYMENT_ENVIRONMENT_FOR_DCN_PREPARED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case ENVIRONMENT_PREPARED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARED;
                case ENVIRONMENT_PREPARATION_FAILED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED; }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case DEPLOYMENT_INITIATED:
                    return MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS;
                case DEPLOYMENT_FAILED:
                    return MANAGEMENT_VPN_CONFIGURATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED; }
    },
    MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case DEPLOYED:
                    return MANAGEMENT_VPN_CONFIGURED;
                case DEPLOYMENT_FAILED:
                    return MANAGEMENT_VPN_CONFIGURATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    MANAGEMENT_VPN_CONFIGURED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.MANAGEMENT_VPN_CONFIGURED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case DEPLOYMENT_INITIATED:
                    return APPLICATION_BEING_DEPLOYED;
                case DEPLOYMENT_FAILED:
                    return APPLICATION_DEPLOYMENT_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    MANAGEMENT_VPN_CONFIGURATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.MANAGEMENT_VPN_CONFIGURATION_FAILED; }
    },
    APPLICATION_BEING_DEPLOYED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case DEPLOYED:
                    return APPLICATION_DEPLOYED;
                case DEPLOYMENT_FAILED:
                    return APPLICATION_DEPLOYMENT_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_DEPLOYED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case CONFIGURED:
                    return APPLICATION_CONFIGURED;
                case CONFIGURATION_FAILED:
                    return APPLICATION_CONFIGURATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_DEPLOYMENT_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_FAILED; }
    },
    APPLICATION_CONFIGURED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case VERIFIED:
                    return APPLICATION_DEPLOYMENT_VERIFIED;
                case VERIFICATION_FAILED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_CONFIGURATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_FAILED; }
    },
    APPLICATION_DEPLOYMENT_VERIFIED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED; }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REMOVED:
                    return APPLICATION_DCN_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REMOVED:
                    return APPLICATION_NMSERVICE_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_FAILED; }
    },
    APPLICATION_NMSERVICE_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_DCN_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_REMOVED; }
    },
    APPLICATION_REMOVAL_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_REMOVAL_FAILED; }
    },
    UNKNOWN {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.UNKNOWN; }
    };

    public abstract AppLifecycleState lifecycleState();

    public AppDeploymentState nextState(DcnDeploymentState state) throws InvalidAppStateException {
        throw new InvalidAppStateException(message(this, state));
    }

    public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
        throw new InvalidAppStateException(message(this, state));
    }

    private static String message(AppDeploymentState currentState, DcnDeploymentState receivedState) {
        return "Illegal attempt to transit from " + currentState + " on " + receivedState;
    }

    private static String message(AppDeploymentState currentState, NmServiceDeploymentState receivedState) {
        return "Illegal attempt to transit from " + currentState + " on " + receivedState;
    }

}
