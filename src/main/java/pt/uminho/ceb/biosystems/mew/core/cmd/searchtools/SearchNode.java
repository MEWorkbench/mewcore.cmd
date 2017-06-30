package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

public enum SearchNode {
	
	HEX {
		@Override
		public String getFlags() {
			return "hex";
		}
	},
	R601 {
		@Override
		public String getFlags() {
			return "r601";
		}
	},
	NEHALEM {
		@Override
		public String getFlags() {
			return "nehalem";
		}
	},
	F266 {
		@Override
		public String getFlags() {
			return "f2.66";
		}
	},
	MYRI {
		@Override
		public String getFlags() {
			return "myri";
		}
	};
	
	public abstract String getFlags();
	
	
}
