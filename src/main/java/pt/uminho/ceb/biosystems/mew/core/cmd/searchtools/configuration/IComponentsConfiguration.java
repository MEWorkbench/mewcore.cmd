package pt.uminho.ceb.biosystems.mew.core.cmd.searchtools.configuration;

import java.util.List;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.tree.generictree.TreeNode;

public interface IComponentsConfiguration {
	
	List<TreeNode<Pair<String, String>>> getPossibleStatesPaths();
}
