package domain.PolicyBlock;

import java.util.ArrayList;
import java.util.HashMap;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;

//This class is designed to read in the options generated from the policy blocks and created options based on the 
//merge, score, and subtract method described in the Pickett & Barto Paper: PolicyBlocks

public class PolicyBlockOptionGenerator {

	PolicyBlockDomain environ;
	ArrayList<EpisodeAnalysis> episodes;
	
	//Main
	public static void main(String args[]){
		PolicyBlockOptionGenerator generator = new PolicyBlockOptionGenerator();
		generator.generatePolicies();
		generator.merge();
		generator.showEpisodes();
		
	}
	
	//creates a new Policy Domain Object
	public PolicyBlockOptionGenerator(){
		environ = new PolicyBlockDomain();
	}
	
	//Generates 5 iterations which contains 100 policies run via Q-Learning
	public void generatePolicies(){	
		environ.createEpisodes("policyBlocks");
	}
	
	//pushes the generated episodes to the GUI
	public void showEpisodes(){
		environ.visualize("policyBlocks");
	}
	
	/*
	 * Merge() - Merges two policies
	 * This is the merge function. Currently all it does is collect the first two merged
	 * generated by Q-Learning. Then it iterates state by state, checking to see if the agent visits the 
	 * same state in both policies. 
	 * 
	 * If so, it takes the state-action-reward set and writes it to the episode analysis object.
	*/
	public void merge(){
		
		//collects the episodes from PolicyBlockDomain
		this.episodes = environ.episodes;
		EpisodeAnalysis e0 = episodes.get(0);
		EpisodeAnalysis e1 = episodes.get(1);
		
		//new blank episode for merging of the two policies
		EpisodeAnalysis merged = new EpisodeAnalysis();
		
		/*
		 * Note: I had a small error (now fixed) here. You need to determine which is the smaller EA obj 
		 * to know how long to iterate for, as well as when to add. It's a 2x search algorithm.
		 * Also the lenghts for StateSequence, ActionSequence, and RewardSequence don't match up 
		 * because no action is taken after the agent reaches the goal. 
		 */
		if(episodes.get(0).actionSequence.size() <= episodes.get(1).actionSequence.size()){
			for(int i = 0; i < e0.stateSequence.size(); i++){
				State s = e0.stateSequence.get(i); 			//collect the first state
				
				for(int j = 0; j < e0.stateSequence.size(); i++){
					State p = e1.stateSequence.get(j); 		//collect the second state
					
					if(s.equals(p)){ 						//do we have a match
						
						//if you can figure out a better way to do this, by all means!!!
						if(e0.actionSequence.size() <= i){
							break;
						}else{								//push the state-action-reward set into merged
						merged.stateSequence.add(e0.stateSequence.get(i));
						merged.actionSequence.add(e0.actionSequence.get(i));
						merged.rewardSequence.add(e0.rewardSequence.get(i));
						}
					}
				}
			}
		}else{
			for(int i = 0; i < e1.stateSequence.size(); i++){
				State s = e1.stateSequence.get(i);
				
				for(int j = 0; j < e1.stateSequence.size(); i++){
					State p = e0.stateSequence.get(j);
					
					if(s.equals(p)){
						if(e1.actionSequence.size() <= i){
							break;
						}else{
						merged.stateSequence.add(e1.stateSequence.get(i));
						merged.actionSequence.add(e1.actionSequence.get(i));
						merged.rewardSequence.add(e1.rewardSequence.get(i));
						}
					}
				}
			}
			
		}
		
		System.out.println("\nMerging Done\n");
		
		 //prints out the total State length of the merged set
		for(int i = 0; i < merged.actionSequence.size(); i++){
			System.out.println("\tMerged Size: " + merged.actionSequence.get(i));
		}
		
		
		//visualize it.
		environ.writeEpisode(merged, "policyBlocks/");
		
	}
	
}
