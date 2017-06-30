package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

public enum SearchQueue {
	
	SHORT{
		@Override
		public String getFlags() {
			return "-q short";
		}
	},
	BIO {
		@Override
		public String getFlags() {
			return "-q bio";
		}
	},
	BIOCNAT {
		@Override
		public String getFlags() {
			return "-q biocnat";
		}
	},
	ANY {
		@Override
		public String getFlags() {
			return "-q default";
		}
	};
	
	public abstract String getFlags();
}
	
