package pathfinder.informed;

import java.util.*;
import java.lang.Math;

/**
 * Maze Pathfinding algorithm that implements a basic, uninformed, breadth-first tree search.
 */
public class Pathfinder {
    
    /**
     * Given a MazeProblem, which specifies the actions and transitions available in the
     * search, returns a solution to the problem as a sequence of actions that leads from
     * the initial to a goal state.
     * 
     * @param problem A MazeProblem that specifies the maze, actions, transitions.
     * @return An ArrayList of Strings representing actions that lead from the initial to
     * the goal state, of the format: ["R", "R", "L", ...]
     */
	
    public static ArrayList<String> solve(MazeProblem problem) {
    	boolean foundKey = false;
        SearchTreeNode root = new SearchTreeNode(foundKey, problem, problem.INITIAL_STATE, "initial", null);
        ArrayList<String> keyPath = searchMaze(foundKey, root, problem);
        
        if(keyPath != null) {
        	foundKey = true;
        	SearchTreeNode keyNode = new SearchTreeNode(foundKey, problem, problem.KEY_STATE, "initial", null);
        	ArrayList<String> keyToGoal = searchMaze(foundKey, keyNode, problem);
        	
        	if(keyToGoal != null) {        		
        		ArrayList<String> path = new ArrayList<String>();
        		path.addAll(keyPath);
        		path.addAll(keyToGoal);
        		
        		return path;
        	}
        }
        return null;
    }
    
    public static ArrayList<String> searchMaze(boolean foundKey, SearchTreeNode current, MazeProblem problem) {
    	Set<MazeState> visitedSet = new HashSet<MazeState>();
        PriorityQueue<SearchTreeNode> frontier = new PriorityQueue<SearchTreeNode>();
        frontier.add(current);
        
        while(!frontier.isEmpty()) {
        	
        	for(Map.Entry<String, MazeState> action : problem.getTransitions(current.m_state).entrySet()) {
        		
        		if(!visitedSet.contains(action.getValue())) {
        			
        			frontier.add(new SearchTreeNode(foundKey, problem, action.getValue(), action.getKey(), current));
        			
        			if(!foundKey && action.getValue().equals(problem.KEY_STATE)) {
        				return getPath(new SearchTreeNode(foundKey, problem, action.getValue(), action.getKey(), current));
        			}
        			else if(foundKey && problem.isGoal(action.getValue())) {
        				return getPath(new SearchTreeNode(foundKey, problem, action.getValue(), action.getKey(), current));
        			}
        		}
        	}
        	visitedSet.add(current.m_state);
        	current = frontier.remove();
        }
        
    	return null;
    }
    
    public static ArrayList<String> getPath(SearchTreeNode node) {
    	ArrayList<String> path = new ArrayList<String>();
    	while(node.m_action != "initial") {
    		path.add(node.m_action);
    		node = node.m_parent;
    	}
    	Collections.reverse(path);
    	return path;
    }

/**
 * SearchTreeNode that is used in the Search algorithm to construct the Search
 * tree.
 * [!] NOTE: Feel free to change this however you see fit to adapt your solution 
 *     for A* (including any fields, changes to constructor, additional methods)
 */
    static class SearchTreeNode implements Comparable<SearchTreeNode> {
	
	////////////////// private members //////////////////
	
	private int m_guaranteeCost;
	private int m_heuristicCost;
	
    private int findGuaranteeCost(MazeProblem problem) {
    	if(m_parent == null) return 0;
    	else return problem.getCost(m_state) + m_parent.getGuaranteeCost();
    }
   
    private int findHeuristicCost(boolean foundKey, MazeProblem problem) {
	   if(!foundKey) {
		   return (Math.abs(m_state.row - problem.KEY_STATE.row) + Math.abs(m_state.col - problem.KEY_STATE.col));
	   }
	   
	   int minDistance = -1;
	   for(MazeState goal: problem.GOAL_STATES) {
	   		int distance = Math.abs(m_state.row - goal.row) + Math.abs(m_state.col - goal.col);
	   		
	   		if(distance < minDistance || minDistance < 0)
	   			minDistance = distance;
	   	}
	   	return minDistance;
   }
   
   private int getEvalCost(int guaranteeCost, int heuristicCost) {
	   return guaranteeCost + heuristicCost;
   }
   
   ///////////////////// public members //////////////////////

   int m_evaluationCost;
    MazeState m_state;
    String m_action;
    SearchTreeNode m_parent;
    
    /**
     * Constructs a new SearchTreeNode to be used in the Search Tree.
     * 
     * @param state The MazeState (row, col) that this node represents.
     * @param action The action that *led to* this state / node.
     * @param parent Reference to parent SearchTreeNode in the Search Tree.
     */
    
    int getGuaranteeCost() {return m_guaranteeCost;}
    
    SearchTreeNode (boolean foundKey, MazeProblem problem, MazeState state, String action, SearchTreeNode parent) {
        m_state = state;
        m_action = action;
        m_parent = parent;
        
        m_guaranteeCost =  findGuaranteeCost(problem);
        m_heuristicCost = findHeuristicCost(foundKey, problem);
        
        m_evaluationCost = getEvalCost(m_guaranteeCost, m_heuristicCost);
    }
    
    boolean equals(SearchTreeNode other) {
    	return (m_state.equals(other.m_state)) ? true : false;
    }
    
    @Override
    public int compareTo(SearchTreeNode other) {
    	return Integer.compare(m_evaluationCost, other.m_evaluationCost);
    }
  }
}