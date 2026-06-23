package net.geant.nmaas.orchestration.entities;

import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
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
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case REQUEST_VERIFIED -> REQUEST_VALIDATED;
                case REQUEST_VERIFICATION_FAILED -> REQUEST_VALIDATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    REQUEST_VALIDATED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.REQUEST_VALIDATED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case ENVIRONMENT_PREPARED -> DEPLOYMENT_ENVIRONMENT_PREPARED;
                case ENVIRONMENT_PREPARATION_INITIATED -> DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS;
                case ENVIRONMENT_PREPARATION_FAILED -> DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    REQUEST_VALIDATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.REQUEST_VALIDATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
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
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case ENVIRONMENT_PREPARED -> DEPLOYMENT_ENVIRONMENT_PREPARED;
                case ENVIRONMENT_PREPARATION_FAILED -> DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case ENVIRONMENT_PREPARATION_INITIATED -> DEPLOYMENT_ENVIRONMENT_PREPARED;
                case CONFIGURED -> APPLICATION_CONFIGURED;
                case READY_FOR_DEPLOYMENT -> MANAGEMENT_VPN_CONFIGURED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
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
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case CONFIGURATION_INITIATED -> APPLICATION_CONFIGURATION_IN_PROGRESS;
                case CONFIGURED -> APPLICATION_CONFIGURED;
                case CONFIGURATION_FAILED -> APPLICATION_CONFIGURATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_CONFIGURATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case CONFIGURED -> APPLICATION_CONFIGURED;
                case CONFIGURATION_FAILED -> APPLICATION_CONFIGURATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_CONFIGURED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case DEPLOYMENT_INITIATED -> APPLICATION_DEPLOYMENT_IN_PROGRESS;
                case DEPLOYMENT_FAILED -> APPLICATION_DEPLOYMENT_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_CONFIGURATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
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
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case DEPLOYED -> APPLICATION_DEPLOYED;
                case DEPLOYMENT_FAILED -> APPLICATION_DEPLOYMENT_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_DEPLOYED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case VERIFICATION_INITIATED -> APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case VERIFICATION_FAILED -> APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case VERIFIED -> APPLICATION_DEPLOYMENT_VERIFIED;
                case VERIFICATION_FAILED -> APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_DEPLOYMENT_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
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
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            if (ServiceDeploymentState.UPGRADE_INITIATED.equals(state)) {
                return APPLICATION_UPGRADE_IN_PROGRESS;
            }
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInRunningState() {
            return true;
        }
    },
    APPLICATION_DEPLOYMENT_VERIFICATION_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_RESTART_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESTART_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case RESTARTED -> APPLICATION_RESTARTED;
                case RESTART_FAILED -> APPLICATION_RESTART_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_RESTARTED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESTARTED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
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
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_PAUSE_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_PAUSE_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case PAUSED -> APPLICATION_PAUSED;
                case PAUSE_FAILED -> APPLICATION_PAUSE_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_PAUSED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_PAUSED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case RESUME_INITIATED -> APPLICATION_RESUME_IN_PROGRESS;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }

        @Override
        public boolean isInRunningState() {
            return true;
        }
    },
    APPLICATION_PAUSE_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_PAUSE_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_RESUME_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESUME_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case RESUMED -> APPLICATION_RESUMED;
                case RESUME_FAILED -> APPLICATION_RESUME_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_RESUMED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESUMED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case VERIFICATION_INITIATED -> APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case VERIFICATION_FAILED -> APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_RESUME_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_RESUME_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_UPGRADE_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_UPGRADE_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case UPGRADED -> APPLICATION_UPGRADED;
                case UPGRADE_FAILED -> APPLICATION_UPGRADE_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_UPGRADED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_UPGRADED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case VERIFICATION_INITIATED -> APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case VERIFICATION_FAILED -> APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }

        @Override
        public boolean isInRunningState() {
            return true;
        }
    },
    APPLICATION_UPGRADE_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_UPGRADE_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_REMOVAL_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_REMOVAL_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case REMOVED -> APPLICATION_REMOVED;
                case REMOVAL_FAILED -> APPLICATION_REMOVAL_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_REMOVED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case DEPLOYMENT_FAILED -> APPLICATION_DEPLOYMENT_FAILED;
                case CONFIGURATION_REMOVAL_INITIATED -> APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };

        }

        @Override
        public boolean isInEndState() {
            return true;
        }
    },
    APPLICATION_REMOVAL_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_REMOVAL_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case CONFIGURATION_REMOVED -> APPLICATION_CONFIGURATION_REMOVED;
                case CONFIGURATION_REMOVAL_FAILED -> APPLICATION_CONFIGURATION_REMOVAL_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_CONFIGURATION_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_REMOVED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            if (ServiceDeploymentState.FAILED_APPLICATION_REMOVED.equals(state)) {
                return FAILED_APPLICATION_REMOVED;
            }
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }
    },
    APPLICATION_CONFIGURATION_REMOVAL_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_REMOVAL_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }
    },
    FAILED_APPLICATION_REMOVED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.FAILED_APPLICATION_REMOVED;
        }

        @Override
        public boolean isInEndState() {
            return true;
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case CONFIGURATION_UPDATED -> APPLICATION_CONFIGURATION_UPDATED;
                case CONFIGURATION_UPDATE_FAILED -> APPLICATION_CONFIGURATION_UPDATE_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_CONFIGURATION_UPDATED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_UPDATED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return switch (state) {
                case VERIFICATION_INITIATED -> APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case VERIFICATION_FAILED -> APPLICATION_DEPLOYMENT_VERIFICATION_FAILED;
                default -> nextStateForNotMatchingNmServiceDeploymentState(this, state);
            };
        }
    },
    APPLICATION_CONFIGURATION_UPDATE_FAILED {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.APPLICATION_CONFIGURATION_UPDATE_FAILED;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
    },
    INTERNAL_ERROR {
        @Override
        public AppLifecycleState lifecycleState() {
            return AppLifecycleState.INTERNAL_ERROR;
        }

        @Override
        public AppDeploymentState nextState(ServiceDeploymentState state) {
            return nextStateForNotMatchingNmServiceDeploymentState(this, state);
        }

        @Override
        public boolean isInFailedState() {
            return true;
        }
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
            ServiceDeploymentState newNmServiceState) {
        if (!currentAppDeploymentState.isInEndState() && newNmServiceState.equals(ServiceDeploymentState.REMOVAL_INITIATED)) {
            return APPLICATION_REMOVAL_IN_PROGRESS;
        }
        if (currentAppDeploymentState.isInFailedState()) {
            switch (newNmServiceState) {
                case INIT:
                    return REQUESTED;
                case FAILED_APPLICATION_REMOVED:
                    return FAILED_APPLICATION_REMOVED;
                case VERIFICATION_INITIATED:
                    return APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
                case CONFIGURATION_UPDATE_INITIATED:
                    return APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS;
                default:
            }
        }
        if (currentAppDeploymentState.isInRunningState()) {
            switch (newNmServiceState) {
                case RESTART_INITIATED:
                    return APPLICATION_RESTART_IN_PROGRESS;
                case PAUSE_INITIATED:
                    return APPLICATION_PAUSE_IN_PROGRESS;
                case CONFIGURATION_UPDATE_INITIATED:
                    return APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS;
                default:
            }
        }
        throw new InvalidAppStateException(message(this, newNmServiceState));
    }

    public AppDeploymentState nextState(ServiceDeploymentState state) {
        throw new InvalidAppStateException(message(this, state));
    }

    private static String message(AppDeploymentState currentState, ServiceDeploymentState receivedState) {
        return "Illegal attempt to transit from " + currentState + " on " + receivedState;
    }

}
