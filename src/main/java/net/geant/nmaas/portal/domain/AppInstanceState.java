package net.geant.nmaas.portal.domain;

public enum AppInstanceState {
    REQUESTED {
        @Override
        public String getUserFriendlyState() {
            return "Requested";
        }
    },
    VALIDATION {
        @Override
        public String getUserFriendlyState() {
            return "Validating request";
        }
    },
    PREPARATION {
        @Override
        public String getUserFriendlyState() {
            return "Configuring deployment environment";
        }
    },
    CONNECTING {
        @Override
        public String getUserFriendlyState() {
            return "Setting up connectivity";
        }
    },
    CONFIGURATION_AWAITING {
        @Override
        public String getUserFriendlyState() {
            return "Applying custom configuration";
        }
    },
    DEPLOYING {
        @Override
        public String getUserFriendlyState() {
            return "Deploying";
        }
    },
    RUNNING {
        @Override
        public String getUserFriendlyState() {
            return "Application instance is running";
        }
    },
    UNDEPLOYING {
        @Override
        public String getUserFriendlyState() {
            return "Undeploying";
        }
    },
    PAUSED {
        @Override
        public String getUserFriendlyState() {
            return "Paused";
        }
    },
    DONE {
        @Override
        public String getUserFriendlyState() {
            return "Undeployed";
        }
    },
    FAILURE {
        @Override
        public String getUserFriendlyState() {
            return "Failure";
        }
    },
    UNKNOWN {
        @Override
        public String getUserFriendlyState() {
            return "Unknown";
        }
    },
    REMOVED {
        @Override
        public String getUserFriendlyState() {
            return "Failed application removed";
        }
    },
    VALIDATION_FAILED {
        @Override
        public String getUserFriendlyState() {
            return "Deployment blocked due to validation issue";
        }
    };

    public abstract String getUserFriendlyState();
}
