package net.geant.nmaas.orchestration.entities;

import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;

/**
 * Application deployment states.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum AppDeploymentState {

    REQUESTED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.REQUESTED;
        }

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
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case ENVIRONMENT_PREPARED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARED;
                case ENVIRONMENT_PREPARATION_INITIATED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS;
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

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            if(state.equals(NmServiceDeploymentState.INIT))
                return REQUESTED;
            throw new InvalidAppStateException(message(this, state));
        }

    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS {
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
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case CONFIGURED:
                    return APPLICATION_CONFIGURED;
                case READY_FOR_DEPLOYMENT:
                    return MANAGEMENT_VPN_CONFIGURED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case INIT:
                    return REQUESTED;
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
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
                case CONFIGURATION_INITIATED:
                    return APPLICATION_CONFIGURATION_IN_PROGRESS;
                case CONFIGURED:
                    return APPLICATION_CONFIGURED;
                case CONFIGURATION_FAILED:
                    return APPLICATION_CONFIGURATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_CONFIGURATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case CONFIGURED:
                    return APPLICATION_CONFIGURED;
                case DEPLOYMENT_FAILED:
                    return APPLICATION_DEPLOYMENT_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_CONFIGURED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case DEPLOYMENT_INITIATED:
                    return APPLICATION_DEPLOYMENT_IN_PROGRESS;
                case DEPLOYMENT_FAILED:
                    return APPLICATION_DEPLOYMENT_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_CONFIGURATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_FAILED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case INIT:
                    return REQUESTED;
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_DEPLOYMENT_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS; }

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
                case VERIFICATION_INITIATED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case VERIFIED:
                    return APPLICATION_DEPLOYMENT_VERIFIED;
                case VERIFICATION_FAILED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS; }

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
    APPLICATION_DEPLOYMENT_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_FAILED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case INIT:
                    return REQUESTED;
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_DEPLOYMENT_VERIFIED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case RESTART_INITIATED:
                    return APPLICATION_RESTART_IN_PROGRESS;
                case RESTARTED:
                    return APPLICATION_DEPLOYED;
                case RESTART_FAILED:
                    return APPLICATION_RESTART_FAILED;
                case REMOVED:
                    return APPLICATION_REMOVED;
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

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case INIT:
                    return REQUESTED;
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_RESTART_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case RESTARTED:
                    return APPLICATION_DEPLOYED;
                case RESTART_FAILED:
                    return APPLICATION_RESTART_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    },
    APPLICATION_RESTART_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_FAILED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case INIT:
                    return REQUESTED;
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

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            if(state.equals(NmServiceDeploymentState.INIT))
                return REQUESTED;
            throw new InvalidAppStateException(message(this, state));
        }
    },
    UNKNOWN {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.UNKNOWN; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            if(state.equals(NmServiceDeploymentState.INIT))
                return REQUESTED;
            throw new InvalidAppStateException(message(this, state));
        }
    },
    INTERNAL_ERROR {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.INTERNAL_ERROR; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            switch (state) {
                case INIT:
                    return REQUESTED;
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    throw new InvalidAppStateException(message(this, state));
            }
        }
    }, GENERIC_ERROR {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.GENERIC_ERROR; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
            if(state.equals(NmServiceDeploymentState.INIT))
                return REQUESTED;
            throw new InvalidAppStateException(message(this, state));
        }
    };

    public abstract AppLifecycleState lifecycleState();

    public AppDeploymentState nextState(NmServiceDeploymentState state) throws InvalidAppStateException {
        throw new InvalidAppStateException(message(this, state));
    }

    private static String message(AppDeploymentState currentState, NmServiceDeploymentState receivedState) {
        return "Illegal attempt to transit from " + currentState + " on " + receivedState;
    }

}
