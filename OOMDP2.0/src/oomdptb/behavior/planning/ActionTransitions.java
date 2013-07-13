package oomdptb.behavior.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oomdptb.behavior.planning.statehashing.StateHashFactory;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TransitionProbability;

public class ActionTransitions {

	public GroundedAction ga;
	public List <HashedTransitionProbability> transitions;
	
	public ActionTransitions(GroundedAction ga, List <TransitionProbability> transitions, StateHashFactory hashingFactory){
		this.ga = ga;
		this.transitions = this.getHashedTransitions(transitions, hashingFactory);
	}
	
	public ActionTransitions(State s, GroundedAction ga, StateHashFactory hashingFactory){
		this.ga = ga;
		this.transitions = this.getHashedTransitions(ga.action.getTransitions(s, ga.params), hashingFactory);
	}
	
	public boolean matchingTransitions(GroundedAction oga){
		return ga.equals(oga);
	}
	
	private List <HashedTransitionProbability> getHashedTransitions(List <TransitionProbability> tps, StateHashFactory hashingFactory){
		List <HashedTransitionProbability> htps = new ArrayList<HashedTransitionProbability>();
		for(TransitionProbability tp : tps){
			htps.add(new HashedTransitionProbability(tp, hashingFactory));
		}
		return htps;
	}
	
}