package net.geant.nmaas.orchestration.entities;

import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;

/**
 * Application deployment states.
 */
public enum AppDeploymentState {

    REQUESTED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.REQUESTED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case REQUEST_VERIFIED:
                    return REQUEST_VALIDATED;
                case REQUEST_VERIFICATION_FAILED:
                    return REQUEST_VALIDATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    REQUEST_VALIDATED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.REQUEST_VALIDATED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case ENVIRONMENT_PREPARED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARED;
                case ENVIRONMENT_PREPARATION_INITIATED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS;
                case ENVIRONMENT_PREPARATION_FAILED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    REQUEST_VALIDATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.REQUEST_VALIDATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case ENVIRONMENT_PREPARED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARED;
                case ENVIRONMENT_PREPARATION_FAILED:
                    return DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case CONFIGURED:
                    return APPLICATION_CONFIGURED;
                case READY_FOR_DEPLOYMENT:
                    return MANAGEMENT_VPN_CONFIGURED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    MANAGEMENT_VPN_CONFIGURED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.MANAGEMENT_VPN_CONFIGURED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case CONFIGURATION_INITIATED:
                    return APPLICATION_CONFIGURATION_IN_PROGRESS;
                case CONFIGURED:
                    return APPLICATION_CONFIGURED;
                case CONFIGURATION_FAILED:
                    return APPLICATION_CONFIGURATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_CONFIGURATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case CONFIGURED:
                    return APPLICATION_CONFIGURED;
                case CONFIGURATION_FAILED:
                    return APPLICATION_CONFIGURATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_CONFIGURED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case DEPLOYMENT_INITIATED:
                    return APPLICATION_DEPLOYMENT_IN_PROGRESS;
                case DEPLOYMENT_FAILED:
                    return APPLICATION_DEPLOYMENT_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);            }
        }
    },
    APPLICATION_CONFIGURATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_DEPLOYMENT_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case DEPLOYED:
                    return APPLICATION_DEPLOYED;
                case DEPLOYMENT_FAILED:
                    return APPLICATION_DEPLOYMENT_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_DEPLOYED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case VERIFICATION_INITIATED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case VERIFICATION_FAILED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case VERIFIED:
                    return APPLICATION_DEPLOYMENT_VERIFIED;
                case VERIFICATION_FAILED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_DEPLOYMENT_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_FAILED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_DEPLOYMENT_VERIFIED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInRunningState() {
            return true;
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_FAILED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() { return true; }
    },
    APPLICATION_RESTART_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESTART_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case RESTARTED:
                    return APPLICATION_RESTARTED;
                case RESTART_FAILED:
                    return APPLICATION_RESTART_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_RESTARTED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESTARTED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInRunningState() {
            return true;
        }
    },
    APPLICATION_RESTART_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESTART_FAILED;
        }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() { return true; }
    },
    APPLICATION_REMOVAL_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case REMOVED:
                    return APPLICATION_REMOVED;
                case REMOVAL_FAILED:
                    return APPLICATION_REMOVAL_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_REMOVED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            if  (NmServiceDeploymentState.CONFIGURATION_REMOVAL_INITIATED.equals(state)) {
                return APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS;
            }
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInEndState() {
            return true;
        }
    },
    APPLICATION_REMOVAL_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_REMOVAL_FAILED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() { return true; }
    },
    APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case CONFIGURATION_REMOVED:
                    return APPLICATION_CONFIGURATION_REMOVED;
                case CONFIGURATION_REMOVAL_FAILED:
                    return APPLICATION_CONFIGURATION_REMOVAL_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_CONFIGURATION_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_REMOVED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            if  (NmServiceDeploymentState.FAILED_APPLICATION_REMOVED.equals(state)) {
                return FAILED_APPLICATION_REMOVED;
            }
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }
    },
    APPLICATION_CONFIGURATION_REMOVAL_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_REMOVAL_FAILED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }
    },
    FAILED_APPLICATION_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() {return AppLifecycleState.FAILED_APPLICATION_REMOVED; }

        @Override
        public boolean isInEndState() {
            return true;
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case CONFIGURATION_UPDATED:
                    return APPLICATION_CONFIGURATION_UPDATED;
                case CONFIGURATION_UPDATE_FAILED:
                    return APPLICATION_CONFIGURATION_UPDATE_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_CONFIGURATION_UPDATED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_UPDATED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            switch (state) {
                case VERIFICATION_INITIATED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case VERIFICATION_FAILED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default:
                    return nextStateForNotMatchingNmServiceDeploymentState(this, state);
            }
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_FAILED {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.APPLICATION_CONFIGURATION_UPDATE_FAILED; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() { return true; }
    },
    INTERNAL_ERROR {
        @Override
        public AppLifecycleState lifecycleState() { return AppLifecycleState.INTERNAL_ERROR; }

        @Override
        public AppDeploymentState nextState(NmServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() { return true; }
    };

    public abstract AppLifecycleState lifecycleState();

    public boolean isInFailedState() {
        return false;
    }

    public boolean isInRunningState() {
        return false;
    }

    public boolean isInEndState() {
        return false;
    }

    protected AppDeploymentState nextStateForNotMatchingNmServiceDeploymentState(
            AppDeploymentState currentAppDeploymentState,
            NmServiceDeploymentState newNmServiceState) {
        if (!currentAppDeploymentState.isInEndState() && newNmServiceState.equals(NmServiceDeploymentState.REMOVAL_INITIATED)) {
            return APPLICATION_REMOVAL_IN_PROGRESS;
        }
        if(currentAppDeploymentState.isInFailedState()) {
            switch (newNmServiceState) {
                case INIT:
                    return REQUESTED;
                case FAILED_APPLICATION_REMOVED:
                    return FAILED_APPLICATION_REMOVED;
                case VERIFICATION_INITIATED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                default:
            }
        }
        if(currentAppDeploymentState.isInRunningState()) {
            switch (newNmServiceState) {
                case RESTART_INITIATED:
                    return APPLICATION_RESTART_IN_PROGRESS;
                case CONFIGURATION_UPDATE_INITIATED:
                    return APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS;
                default:
            }
        }
        throw new InvalidAppStateException(message(this, newNmServiceState));
    }

    public AppDeploymentState nextState(NmServiceDeploymentState state) {
        throw new InvalidAppStateException(message(this, state));
    }

    private static String message(AppDeploymentState currentState, NmServiceDeploymentState receivedState) {
        return "Illegal attempt to transit from " + currentState + " on " + receivedState;
    }

}
