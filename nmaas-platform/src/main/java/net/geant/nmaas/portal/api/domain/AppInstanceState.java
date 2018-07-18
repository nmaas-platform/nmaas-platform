package net.geant.nmaas.portal.api.domain;

public enum AppInstanceState {
	SUBSCRIBED{
		@Override
		public String getUserFriendlyState(){
			return "Application subscribed";
		}
	},
	VALIDATION{
		@Override
		public String getUserFriendlyState(){
			return "Subscription validation";
		}
	},
	PREPARATION{
		@Override
		public String getUserFriendlyState(){
			return "Environment creation";
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
			return "Applying app configuration";
		}
	},
	DEPLOYING{
		@Override
		public String getUserFriendlyState(){
			return "App container deployment";
		}
	},
	RUNNING{
		@Override
		public String getUserFriendlyState(){
			return "App is running";
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
			return "Done";
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
	};

	public abstract String getUserFriendlyState();
}
