package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;

import it.unibz.krdb.obda.ontology.Ontology;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

/**
 *  Retrieve all the connection built in our DAG 
 * 
 *
 */

public class TBoxReasonerImpl implements TBoxReasoner{

	DAGImpl dag;
	private Set<OClass> namedClasses;
	private Set<Property> property;


	public TBoxReasonerImpl(DAGImpl dag){
		this.dag=dag;
		namedClasses= dag.getClasses();
		property = dag.getRoles();


	}

	/**return the direct children starting from the given node of the dag
	 *  @param named when it's true only the children that correspond to named classes and property
	 *   are returned
	 *  @result we return a set of set of description to distinguish between different nodes and equivalent nodes. 
	 *  equivalent nodes will be in the same set of description
	 */
	@Override
	public Set<Set<Description>> getDirectChildren(Description desc, boolean named) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;

		Set<DefaultEdge> edges = dag.incomingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description source = dag.getEdgeSource(edge);
			Set<Description> equivalences =getEquivalences(source,named);

			
			if (!equivalences.isEmpty())
			result.add(equivalences);
		}
		return Collections.unmodifiableSet(result);
	}

	/**return the direct parents starting from the given node of the dag
	 *  @param named when it's true only the parents that correspond to named classes or property
	 *  are returned 
	 * */

	@Override
	public Set<Set<Description>> getDirectParents(Description desc, boolean named) {

		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Description node = dag.getReplacements().get(desc);
		if (node == null)
			node = desc;
		Set<DefaultEdge> edges = dag.outgoingEdgesOf(node);
		for (DefaultEdge edge : edges) {
			Description target = dag.getEdgeTarget(edge);
			Set<Description> equivalences =getEquivalences(target,named);
	
			if (!equivalences.isEmpty())
				result.add(equivalences);
		}
		return Collections.unmodifiableSet(result);
	}

	/**recursive function 
	return the descendants starting from the given node of the dag
	 @param named when it's true only the descendants that are named classes or property 
	 are returned
	 */
	@Override
	public Set<Set<Description>> getDescendants(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Set<Set<Description>> children;
		children= getDirectChildren(desc, false);
		if(children.isEmpty())
			return result;
		else{
			
			if(named){
				Set<Set<Description>> namedChildren;
				namedChildren= getDirectChildren(desc, true);
				if(!namedChildren.isEmpty())
					result.addAll(namedChildren);
			}
			else 
				
			result.addAll(children);
			for (Set<Description> child : children){
				Set<Set<Description>> descendants =getDescendants(child.iterator().next(), named);
				if(!descendants.isEmpty())
				result.addAll(descendants);
			}
			
			

			return Collections.unmodifiableSet(result);
		}
	}	


	/** recursive function 
	return the ancestors starting from the given node of the dag
	 @param named when it's true only the ancestors that are named classes or property 
	 are returned
	 */
	@Override
	public Set<Set<Description>> getAncestors(Description desc, boolean named) {
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		Set<Set<Description>> parents;
		parents=getDirectParents(desc, false);
		if(parents.isEmpty())
			return result;
		else{
			if(named){
				Set<Set<Description>> namedParent;
				namedParent= getDirectParents(desc, true);
				if(!namedParent.isEmpty())
					result.addAll(namedParent);
			}
			else 
				
			result.addAll(parents);
			for (Set<Description> child : parents){
				Set<Set<Description>> ancestors= getAncestors(child.iterator().next(), named);
				if(!ancestors.isEmpty())
				result.addAll(ancestors);
			}


			return Collections.unmodifiableSet(result);
		}
	}

	/**return the equivalences starting from the given node of the dag
	 *  @param named when it's true only the equivalences that are named classes or property 
	 are returned
	 */
	@Override
	public  Set<Description> getEquivalences(Description desc, boolean named) {
		Set<Description> equivalents = dag.getMapEquivalences().get(desc);
		if (equivalents == null ){
			if (named){
			if(namedClasses.contains(desc) | property.contains(desc)){
			return Collections.unmodifiableSet(Collections.singleton(desc));
			}
			else{
				Set<Description> equivalences = Collections.emptySet();
				return equivalences;
			}
			}
			return Collections.unmodifiableSet(Collections.singleton(desc));
		}
		Set<Description> equivalences = new LinkedHashSet<Description> ();
		if (named){
			for(Description vertex: equivalents){
				if(namedClasses.contains(vertex) | property.contains(vertex)){
					equivalences.add(vertex);
				}
			}
		}
		else{
			equivalences = equivalents;
		}


		return Collections.unmodifiableSet(equivalences);
	}

	public Set<Set <Description>> getNodes(){
		LinkedHashSet<Set<Description>> result = new LinkedHashSet<Set<Description>>();
		for (Description vertex: dag.vertexSet()){
			result.add(getEquivalences(vertex,false));
		}
		return result;
		
	}

	@Override
	public DAGImpl getDAG() {

		return dag;
	}



}