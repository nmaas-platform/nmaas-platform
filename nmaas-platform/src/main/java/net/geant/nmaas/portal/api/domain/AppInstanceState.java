package net.geant.nmaas.portal.api.domain;

public enum AppInstanceState {
	REQUESTED {
		@Override
		public String getUserFriendlyState(){
			return "Requested";
		}
	},
	VALIDATION{
		@Override
		public String getUserFriendlyState(){
			return "Validating request";
		}
	},
	PREPARATION{
		@Override
		public String getUserFriendlyState(){
			return "Configuring deployment environment";
		}
	},
	CONNECTING{
		@Override
		public String getUserFriendlyState(){
			return "Setting up connectivity";
		}
	},
	CONFIGURATION_AWAITING{
		@Override
		public String getUserFriendlyState(){
			return "Applying custom configuration";
		}
	},
	DEPLOYING{
		@Override
		public String getUserFriendlyState(){
			return "Deploying";
		}
	},
	RUNNING{
		@Override
		public String getUserFriendlyState(){
			return "Application instance is running";
		}
	},
	UNDEPLOYING{
		@Override
		public String getUserFriendlyState(){
			return "Undeploying";
		}
	},
	DONE{
		@Override
		public String getUserFriendlyState(){
			return "Undeployed";
		}
	},
	FAILURE{
		@Override
		public String getUserFriendlyState(){
			return "Failure";
		}
	},
	UNKNOWN{
		@Override
		public String getUserFriendlyState(){
			return "Unknown";
		}
	},
	REMOVED{
		@Override
		public String getUserFriendlyState(){
			return "Failed application removed";
		}
	};

	public abstract String getUserFriendlyState();
}
