package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

public enum Walltime {
	
	SHORT {
		public String getFlags() {
			return "-l walltime=20:00:00 ";
		}
	},
	
	SHORT_MEDIUM {
		public String getFlags() {
			return "-l walltime=25:00:00 ";
		}
	},
	
	MEDIUM {
		public String getFlags() {
			return "-l walltime=50:00:00 ";
		}
	},
	
	MEDIUM_LONG {
		public String getFlags() {
			return "-l walltime=100:00:00 ";
		}
	},
	
	LONG {
		public String getFlags() {
			return "-l walltime=300:00:00 ";
		}
	};
	
	public abstract String getFlags();
	
}
