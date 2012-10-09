package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.BooleanOperationPredicateImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryConnectedComponent.Edge;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessSet {
	private List<TreeWitness> tws = new LinkedList<TreeWitness>();
	private QueryConnectedComponent cc;
	private TreeWitnessReasonerLite reasoner;
	private PropertiesCache propertiesCache; 
	
	// working lists (may all be nulls)
	private List<TreeWitness> mergeable;
	private Queue<TreeWitness> delta;
	private Map<TreeWitness.TermCover, TreeWitness> twsCache;

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessSet.class);
	private static OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	private TreeWitnessSet(QueryConnectedComponent cc, TreeWitnessReasonerLite reasoner) {
		this.cc = cc;
		this.reasoner = reasoner;
	}
	
	public Collection<TreeWitness> getTWs(QueryConnectedComponent.Edge edge) {
		Collection<TreeWitness> m = new LinkedList<TreeWitness>();
		for (TreeWitness tw : tws)
			if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) 
				m.add(tw);

		return m;
	}
	
	public Collection<TreeWitness> getTWs() {
		return tws;
	}
	
	public static TreeWitnessSet getTreeWitnesses(QueryConnectedComponent cc, TreeWitnessReasonerLite reasoner) {		
		TreeWitnessSet treewitnesses = new TreeWitnessSet(cc, reasoner);
		
		if (!cc.isDegenerate())
			treewitnesses.computeTreeWitnesses();
				
		return treewitnesses;
	}

	private void computeTreeWitnesses() {		
		propertiesCache = new PropertiesCache(reasoner);
		QueryFolding qf = new QueryFolding(); // in-place query folding, so copying is required when creating a tree witness
		
		for (Term v : cc.getQuantifiedVariables()) {
			log.debug("QUANTIFIED VARIABLE " + v); 			
			if (qf.canBeFolded(v, cc, propertiesCache)) {
				// tws cannot contain duplicates by construction, so no caching (even negative)
				Collection<TreeWitnessGenerator> twg = getTreeWitnessGenerators(qf); 
				if (twg != null) { 
					// copy the query folding
					TreeWitness tw = new QueryFolding(qf).getTreeWitness(twg, cc.getQuantifiedVariables()); 
					tws.add(tw);
				}
			}
		}		
		
		if (tws.isEmpty())
			return;
		
		mergeable = new LinkedList<TreeWitness>();
		Queue<TreeWitness> working = new LinkedList<TreeWitness>();

		for (TreeWitness tw : tws) 
			if (tw.allRootsQuantified())  {
				working.add(tw);			
				mergeable.add(tw);
			}
		
		delta = new LinkedList<TreeWitness>();
		twsCache = new HashMap<TreeWitness.TermCover, TreeWitness>();

		while (!working.isEmpty()) {
			while (!working.isEmpty()) {
				TreeWitness tw = working.poll(); 
				saturateTreeWitnesses(new QueryFolding(tw)); 					
			}
			
			while (!delta.isEmpty()) {
				TreeWitness tw = delta.poll();
				tws.add(tw);
				if (tw.allRootsQuantified())  {
					working.add(tw);			
					mergeable.add(tw);
				}
			}
		}				

		log.debug("TREE WITNESSES FOUND: " + tws.size());
		for (TreeWitness tw : tws) 
			log.debug(" " + tw);
	}
	
	private void saturateTreeWitnesses(QueryFolding qf) { 
		boolean saturated = true; 
		
		for (QueryConnectedComponent.Edge edge : cc.getEdges()) { 
			if (qf.canBeAttachedToAnInternalRoot(edge.getTerm0(), edge.getTerm1())) {
				log.debug("EDGE " + edge + " IS ADJACENT TO THE TREE WITNESS " + qf); 

				saturated = false; 

				for (TreeWitness tw : mergeable)  
					if (tw.getRoots().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
						log.debug("    ATTACHING A TREE WITNESS " + tw);
						saturateTreeWitnesses(qf.extend(tw)); 
					} 
				
				QueryFolding qf2 = new QueryFolding(qf);
				qf2.extend(edge.getTerm1(), propertiesCache.getEdgeProperties(edge, edge.getTerm1()),  edge.getL1Atoms(), edge.getL0Atoms());
				if (qf2.isValid()) {
					log.debug("    ATTACHING A HANDLE " + edge);
					saturateTreeWitnesses(qf2);  
				}	
			} 
			else if (qf.canBeAttachedToAnInternalRoot(edge.getTerm1(),edge.getTerm0())) { 
				log.debug("EDGE " + edge + " IS ADJACENT TO THE TREE WITNESS " + qf); 
				
				saturated = false; 
				
				for (TreeWitness tw : mergeable)  
					if (tw.getRoots().contains(edge.getTerm1()) && tw.getDomain().contains(edge.getTerm0())) {
						log.debug("    ATTACHING A TREE WITNESS " + tw);
						saturateTreeWitnesses(qf.extend(tw)); 
					} 

				QueryFolding qf2 = new QueryFolding(qf);
				qf2.extend(edge.getTerm0(), propertiesCache.getEdgeProperties(edge, edge.getTerm0()),  edge.getL0Atoms(), edge.getL1Atoms());
				if (qf2.isValid()) {
					log.debug("    ATTACHING A HANDLE " + edge);
					saturateTreeWitnesses(qf2);  
				}	
			} 
		}

		if (saturated && qf.hasRoot())  {
			if (!twsCache.containsKey(qf.getTerms())) {
				Collection<TreeWitnessGenerator> twg = getTreeWitnessGenerators(qf); 
				if (twg != null) {
					TreeWitness tw = qf.getTreeWitness(twg, cc.getQuantifiedVariables()); 
					delta.add(tw);
					twsCache.put(tw.getTerms(), tw);
				}
				else
					twsCache.put(qf.getTerms(), null); // cache negative
			}
			else {
				log.debug("TWS CACHE HIT " + qf.getTerms());
				//assert (tw.getGenerators().equals(tw0.getGenerators()));
			}
		}
	}
	
	// can return null if there are no applicable generators!
	
	private Collection<TreeWitnessGenerator> getTreeWitnessGenerators(QueryFolding qf) {
		Collection<TreeWitnessGenerator> twg = null;
		
		log.debug("CHECKING WHETHER THE FOLDING " + qf + " CAN BE GENERATED: "); 
		for (TreeWitnessGenerator g : reasoner.getGenerators()) {
			log.debug("      CHECKING " + g);		
			if (qf.getProperties().contains(g.getProperty())) 
				log.debug("        PROPERTIES ARE FINE: " + qf.getProperties() + " FOR " + g.getProperty());
			else {
				log.debug("        PROPERTIES ARE TOO SPECIFIC: " + qf.getProperties() + " FOR " + g.getProperty());
				continue;
			}
			
			if (!isGenerated(g, qf.getInternalRootAtoms(), qf.getInteriorTreeWitnesses()))
				continue;
			
			if (twg == null) 
				twg = new LinkedList<TreeWitnessGenerator>();
			twg.add(g);
			log.debug("TREE WITNESS GENERATOR: " + g);
		}
		return twg;
	}
	
	private boolean isGenerated(TreeWitnessGenerator g, Set<Atom> endtype, Collection<TreeWitness> tws) {
		for (Atom a : endtype) {
			 if (a.getArity() != 1)
				 return false;        // binary predicates R(x,x) cannot be matched to the anonymous part
			 
			 Set<BasicClassDescription> subcons = reasoner.getSubConcepts(a.getPredicate());
			 if (!subcons.contains(g.getFiller()) && !subcons.contains(g.getRoleEndType())) {
				 log.debug("        ENDTYPE TOO SPECIFIC: " + a.getPredicate() + " FOR " + g.getFiller() + " AND " + g.getRoleEndType());
				 return false;
			 }
			 else
				 log.debug("        ENDTYPE IS FINE: " + a.getPredicate() + " FOR " + g.getFiller());
		}

		for (TreeWitness tw : tws) {
			boolean matched = false;
			for (BasicClassDescription con : reasoner.getMaximalBasicConcepts(tw.getGenerators())) {
				Set<BasicClassDescription> subcons = reasoner.getSubConcepts(con);
				if (!subcons.contains(g.getFiller()) && !subcons.contains(g.getRoleEndType())) {
					log.debug("        ENDTYPE TOO SPECIFIC: " + con + " FOR " + g.getFiller() + " AND " + g.getRoleEndType());
				}
				else {
					log.debug("        ENDTYPE IS FINE: " + con + " FOR " + g.getFiller());
					matched = true;
					break;
				}
			}
			if (!matched)
				return false;
		}
		return true;
	}
	
	static class PropertiesCache {
		private Map<Edge, Set<Property>> prop0 = new HashMap<Edge, Set<Property>>();
		private Map<Edge, Set<Property>> prop1 = new HashMap<Edge, Set<Property>>();

		private TreeWitnessReasonerLite reasoner;
		
		private PropertiesCache(TreeWitnessReasonerLite reasoner) {
			this.reasoner = reasoner;
		}
		
		public Set<Property> getEdgeProperties(Edge edge, Term root) {
			Map<Edge, Set<Property>> props = edge.getTerm0().equals(root) ? prop0 : prop1;
			Set<Property> properties = props.get(edge);
			
			if (properties == null) {
				properties = new HashSet<Property>();
				for (Atom a : edge.getBAtoms()) {
					log.debug("EDGE " + edge + " HAS PROPERTY " + a);
					if (a.getPredicate() instanceof BooleanOperationPredicateImpl) {
						log.debug("        NO BOOLEAN OPERATION PREDICATES ALLOWED IN PROPERTIES ");
						properties.clear();
					}
					else {
						Property p = ontFactory.createProperty(a.getPredicate(), !root.equals(a.getTerm(0)));
						if (properties.isEmpty()) // first atom
							properties.addAll(reasoner.getSubProperties(p));
						else
							properties.retainAll(reasoner.getSubProperties(p));
					}
					if (properties.isEmpty())
						break;
				}				
				props.put(edge, properties);
			}

			return properties;
		}
	}

	public Set<TreeWitnessGenerator> getGeneratorsOfDetachedCC() {		
		Set<TreeWitnessGenerator> generators = new HashSet<TreeWitnessGenerator>();
		
		if (cc.isDegenerate()) { // do not remove the curly brackets -- dangling else otherwise
			for (TreeWitnessGenerator some : reasoner.getGenerators())
				if (isGenerated(some, cc.getEdges().get(0).getL0Atoms(), Collections.EMPTY_LIST)) 
					generators.add(some);					
		} else {
			for (TreeWitness tw : tws) 
				if (tw.getDomain().containsAll(cc.getVariables())) {
					log.debug("TREE WITNESS " + tw + " COVERS THE QUERY");
					for (TreeWitnessGenerator twg : reasoner.getGenerators())
						if (isGenerated(twg, tw.getRootAtoms(), Collections.singleton(tw))) 
							generators.add(twg);
				}
		}
		
		boolean saturated = true;
		do {
			List<TreeWitnessGenerator> delta = new LinkedList<TreeWitnessGenerator>();
			for (TreeWitnessGenerator gen : generators)
				for (BasicClassDescription g : gen.getConcepts()) {
					for (TreeWitnessGenerator some : reasoner.getGenerators()) {
						if (reasoner.getSubConcepts(g).contains(some.getFiller()) || 
								reasoner.getSubConcepts(g).contains(some.getRoleEndType())) {
							saturated = false;
							delta.add(some);
						}		 		
					}
				}
			saturated = !generators.addAll(delta);
		} while (!saturated);						
		
		return generators;
	}
	
	
}
