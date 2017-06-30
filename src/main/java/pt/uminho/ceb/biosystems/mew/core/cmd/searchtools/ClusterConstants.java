package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

public final class ClusterConstants {
	
	public static final String		DELIMITER				= Delimiter.COMMA.toString();
	public static final String		DASH					= System.getProperty("file.separator");
	public static final String		NEW_LINE				= System.getProperty("line.separator");
	public static final String		JAVA_HOME				= System.getenv("JAVA_HOME");
	public static final String		LD_LIBRARY_PATH			= System.getenv("LD_LIBRARY_PATH");
	public static final String		LEGACY_MERGE_SORT		= "-Djava.util.Arrays.useLegacyMergeSort=true";
	public static final String		REMOTE_MONITOR_STRING	= "-Djava.rmi.server.hostname=192.168.1.100 -Dcom.sun.management.jmxremote.port=1100 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false";
	public static final String		JAR_FILE_NAME			= "runner.jar";
	
	public static final String		HELP_ARG				= "--help";
	public static final String		RUN_ARG					= "--run";
	public static final String		GENERATE_ARG			= "--gen";
	public static final String		LOCAL_ARG				= "--local";
	public static final String		MISSING_ARG				= "--miss";
	
	public static final String		CONFS_SUFFIX			= ".conf";
	public static final String		SCRIPT_SUFFIX			= ".sh";
	
	public static final String		RUN_ALL_PREFIX			= "submitAll";
	public static final String		RUN_ALL_MISSING			= "submitMissing";
	
	public static final String		COMMENTS_ENDING			= "";
	public static final String		DEFAULT_NAME_CONNECTOR	= "#";
	
	public static final SolverType	LP_SOLVER				= SolverType.CPLEX3;
	public static final SolverType	MILP_SOLVER				= SolverType.CPLEX3;
	public static final SolverType	QP_SOLVER				= SolverType.CPLEX3;
	
	public static final String		USAGE_HELP				= "java -jar " + JAR_FILE_NAME + " " + HELP_ARG;
	public static final String		USAGE_RUN				= "java -jar " + JAR_FILE_NAME + " " + RUN_ARG + " [configurationFile]" + " <[run_number]>";
	public static final String		USAGE_GENERATE			= "java -jar " + JAR_FILE_NAME + " " + GENERATE_ARG + " [baseConfigurationFile]" + " [numberOfRuns]";
	
	public static final String[]	UNIX_ESCAPE_CHARACTERS	= { "\\[", "\\]", "\\(", "\\)", "\\?", "\\&", "\\,", "\\<", "\\>", "\\*", "\\^", "\\|", "\\'", "\\;" };
	
	public static String replaceShellEscapeCharacters(String string) {
		for (String character : UNIX_ESCAPE_CHARACTERS)
			string = string.replaceAll(character, ("\\" + character));
		return string;
	}
}
