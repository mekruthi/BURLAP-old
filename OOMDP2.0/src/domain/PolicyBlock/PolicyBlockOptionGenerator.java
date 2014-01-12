package domain.PolicyBlock;

import java.util.ArrayList;
import java.util.HashMap;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;

//This class is designed to read in the options generated from the policy blocks and created options based on the 
//merge, score, and subtract method described in the Pickett & Barto Paper: PolicyBlocks

public class PolicyBlockOptionGenerator {

	static PolicyBlockDomain environ;
	ArrayList<EpisodeAnalysis> episodes;
	
	//Main
	public static void main(String args[]){
		//set number of policies to merge
		int number = 6;
		PolicyBlockOptionGenerator generator = new PolicyBlockOptionGenerator();
		generator.generatePolicies(number);
		
		//generator.merge(environ.episodes.get(0), environ.episodes.get(1));
		//generator.showEpisodes();
		
		EpisodeAnalysis[] input = new EpisodeAnalysis[number];
		for(int i = 0; i < input.length; i++)
			input[i] = environ.episodes.get(i);
		
		Object[] output = unionSet(input);
		for(int i = 0; i < ((EpisodeAnalysis[])(output[0])).length; i++)
		{
			System.out.print("\n" + ((String[])(output[1]))[i] + "     Score: " + Integer.toString(((int[])(output[2]))[i]));
			visualize(((EpisodeAnalysis[])(output[0]))[i]);
		}
	}
	
	//creates a new Policy Domain Object
	public PolicyBlockOptionGenerator(){
		environ = new PolicyBlockDomain();
	}
	
	//Generates "number" iterations which contains 100 policies run via Q-Learning
	public void generatePolicies(int number){	
		environ.createEpisodes("policyBlocks", number);
	}
	
	//Displays an ASCII map of a GridWorld domain policy
	public static void visualize(EpisodeAnalysis merged){
		//initializes an empty map array
		char[][] matrix = new char[11][11];
		for(int x = 0; x < 11; x++)
		{
			for(int y = 0; y <11; y++)
			{
				matrix[x][y] = ' ';
			}
		}
		//adds walls found in the standard map
		for(int y = 0; y < 11; y++)
			matrix[5][y] = 'X';
		for(int x = 0; x < 5; x++)
			matrix[x][5] = 'X';
		for(int x = 6; x < 11; x++)
			matrix[x][4] = 'X';
		matrix[5][1] = ' ';
		matrix[5][8] = ' ';
		matrix[1][5] = ' ';
		matrix[8][4] = ' ';
		//maps path traveled by agent
		for(int i = 0; i < merged.stateSequence.size()-1; i++)
		{
			matrix[merged.stateSequence.get(i).getObservableObjectAt(1).getValues().get(0).getDiscVal()][merged.stateSequence.get(i).getObservableObjectAt(1).getValues().get(1).getDiscVal()] = (merged.actionSequence.get(i).toString().charAt(0));
		}
		//displays map
		System.out.print("\n");
		for(int col = 10; col >= 0; col--)
		{
			//left-hand numbers
			System.out.print((col + 1)%10/* + " "*/);
			for(int row = 0; row < 11; row++)
			{
				System.out.print(matrix[row][col]);
				//accounts of most fonts having spaces as half-size characters
				//if(matrix[row][col] == (' '))
					//System.out.print(" ");
				//extra space to allow reading rows of actions
				System.out.print(" ");
			}
			System.out.println();
		}
		//bottom numbers
		//System.out.print("   1  2  3  4  5  6  7  8  9  0  1 \n\n");
		System.out.print("  1 2 3 4 5 6 7 8 9 0 1 \n\n");
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
	public static EpisodeAnalysis merge(EpisodeAnalysis e0, EpisodeAnalysis e1){
		
		//new blank episode for merging of the two policies
		EpisodeAnalysis merged = new EpisodeAnalysis();
		
			for(int i = 0; i < e0.stateSequence.size()-1; i++){
				Object s = e0.stateSequence.get(i).getObservableObjectAt(1).getValues(); 			//collect the first state
				for(int j = 0; j < e1.stateSequence.size()-1; j++){
					Object p = e1.stateSequence.get(j).getObservableObjectAt(1).getValues(); 		//collect the second state
					if(s.equals(p) && e0.actionSequence.get(i).toString().equals(e1.actionSequence.get(j).toString())){					//do we have a match
						
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
		
		//System.out.println("\nMerging Done\n");
		
		//visualize it.
		environ.writeEpisode(merged, "policyBlocks/");
		//visualize(e0);
		//visualize(e1);
		//visualize(merged);
		
		return merged;
	}
	
	//returns the union set of merged policies
	public static Object[]  unionSet(EpisodeAnalysis[] set){
		EpisodeAnalysis[] result = new EpisodeAnalysis[(int)(Math.pow(2, set.length) -1)];
		String[] names = new String[(int)(Math.pow(2, set.length)-1)];
		int[] depth = new  int[(int)(Math.pow(2, set.length)-1)];
		int[] scores = new int[(int)(Math.pow(2, set.length)-1)];
		//this loop seeks to exhaustively find the union set of the policies provided
		//the variable "i" is treated as though it were in binary with each bit representing whether a specific policy is part of the merged policy or not
		//"i" is split into the leading boolean non-zero bit and the other bits; the former is used to find the proper policy and the latter is used to find the already merged other policies (within the result array)
		//n is used to identify the leading bit easily
		//for example: when "i" is 11, this can be seen as 1011; the 1 indicates the obtain the fourth policy in the provided list and the 011 indicate the merge policy 4 with policy 3 (011 in decimal) in the resulting array
		
		//variable to store the number of binary digits used to represent previous merges
		int n = 1;
		for(int i = 1; i < Math.pow(2,  set.length); i++)
		{
			if(i == Math.pow(2,  n))
				n++;
			if(i-Math.pow(2, n-1) == 0)
			{
				result[i-1] = set[n-1];
				names[i-1] = Integer.toString(n);
				depth[i-1] = 1;
				scores[i-1] = result[i-1].stateSequence.size();
			}
			else
			{
				result[i-1] = merge(set[n-1], result[(int)(i-1-Math.pow(2, n-1))]);
				names[i-1] = names[(int)(i-1-Math.pow(2, n-1))] + "+" + Integer.toString(n);
				depth[i-1] = depth[(int)(i-1-Math.pow(2, n-1))] + 1;
				scores[i-1] = result[i-1].stateSequence.size() * depth[i-1];
			}
			
		}
		Object[] output = new Object[3];
		output[0] = result;
		output[1] = names;
		output[2] = scores;
		return output;
	}
	
}
