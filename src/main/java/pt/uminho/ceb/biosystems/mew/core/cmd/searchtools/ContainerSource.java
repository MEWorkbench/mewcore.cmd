package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.BioOptFileReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.FlatFilesReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.MetatoolReader;
import pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration.ModelConfiguration;


public enum ContainerSource {
	
	SBML {
		@Override
		public Container read(String name, Map<String, Object> files) {
			try {
//				System.out.println(files.get(ModelConfiguration.MODEL_FILE).toString());
				JSBMLReader reader = new JSBMLReader(
						files.get(ModelConfiguration.MODEL_FILE).toString(),
						name,
						false
						);
				return new Container(reader);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	},
	SPARSE_FLAT_FILES {
		@Override
		public Container read(String name, Map<String, Object> files) {
			try {
				FlatFilesReader reader = new FlatFilesReader(
						(String) files.get(ModelConfiguration.MODEL_REACTIONS), 
						(String) files.get(ModelConfiguration.MODEL_MATRIX),
						(String) files.get(ModelConfiguration.MODEL_METABOLITES),
						(String) files.get(ModelConfiguration.MODEL_GENES), 
						name
						);

				return new Container(reader);
			}
			catch(Exception e) {
				e.printStackTrace();				
			}
			return null;
		}
	},
	BIO_OPT {
		@Override
		public Container read(String name, Map<String, Object> files) {
			try {
				BioOptFileReader reader = new BioOptFileReader(
						(String) files.get(ModelConfiguration.MODEL_FILE)
						);
				return new Container(reader);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	},
	METATOOL {
		@Override
		public Container read(String name, Map<String, Object> files) {
			try {
				MetatoolReader reader = new MetatoolReader(
						(String) files.get(ModelConfiguration.MODEL_FILE)
						);
				return new Container(reader);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	};
	
	public abstract Container read(String name, Map<String,Object> files);
}
