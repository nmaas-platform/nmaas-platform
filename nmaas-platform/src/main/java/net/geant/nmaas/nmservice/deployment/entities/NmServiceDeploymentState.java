package net.geant.nmaas.nmservice.deployment.entities;

/**
 * NM service deployment states.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public enum NmServiceDeploymentState {

    INIT {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    REQUEST_VERIFIED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    REQUEST_VERIFICATION_FAILED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    ENVIRONMENT_PREPARATION_INITIATED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    ENVIRONMENT_PREPARED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    ENVIRONMENT_PREPARATION_FAILED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    READY_FOR_DEPLOYMENT {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    CONFIGURATION_INITIATED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    CONFIGURED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    CONFIGURATION_FAILED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    DEPLOYMENT_INITIATED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    DEPLOYED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    DEPLOYMENT_FAILED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    VERIFICATION_INITIATED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    VERIFIED {
        @Override
        public boolean isRunning() {
            return true;
        }
    },
    VERIFICATION_FAILED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    REMOVED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    REMOVAL_FAILED {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    UNKNOWN {
        @Override
        public boolean isRunning() {
            return false;
        }
    },
    ERROR {
        @Override
        public boolean isRunning() {
            return false;
        }
    };

    public abstract boolean isRunning();

}
