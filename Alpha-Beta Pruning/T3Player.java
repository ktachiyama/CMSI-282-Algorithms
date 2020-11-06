/**
 * @author Krysten Tachiyama
 * CMSI 282 Spring 2020
 *
 */

package t3;

import java.util.Map;

/**
 * Artificial Intelligence responsible for playing the game of T3! Implements
 * the alpha-beta-pruning mini-max search algorithm
 */

public class T3Player {
	/**
	 * Workhorse of an AI T3Player's choice mechanics that, given a game state,
	 * makes the optimal choice from that state as defined by the mechanics of the
	 * game of Tic-Tac-Total. Note: In the event that multiple moves have
	 * equivalently maximal minimax scores, ties are broken by move col, then row,
	 * then move number in ascending order (see spec and unit tests for more info).
	 * The agent will also always take an immediately winning move over a delayed
	 * one (e.g., 2 moves in the future).
	 * 
	 * @param state The state from which the T3Player is making a move decision.
	 * @return The T3Player's optimal action.
	 */
	public T3Action choose(T3State state) {
		Pair secureTheBag = choose_aux(state, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
		return secureTheBag.action;
	}

	/**
	 * alpha-beta pruning recursive helper that returns a Pair containing a T3Action
	 * of the player's most optimal action and a minimax score.
	 *
	 */
	Pair choose_aux(T3State state, int alpha, int beta, boolean isMaximizingPlayer) {
		if (state.isTie()) {
			return new Pair(null, 0);
		}
		if (state.isWin()) {
			if (isMaximizingPlayer)
				return new Pair(null, -1);
			else
				return new Pair(null, 1);
		}

		if (isMaximizingPlayer) {
			Pair theBag = new Pair(null, Integer.MIN_VALUE);

			for (Map.Entry<T3Action, T3State> transition : state.getTransitions().entrySet()) {
				if (transition.getValue().isWin()) {
					return new Pair(transition.getKey(), 1);
				}
				Pair child = choose_aux(transition.getValue(), alpha, beta, false);
				if (child.val > theBag.val) {
					theBag.action = transition.getKey();
					theBag.val = child.val;
				}
				alpha = (alpha > theBag.val) ? alpha : theBag.val;

				if (beta <= alpha)
					break;
			}
			return theBag;
		}

		else {
			Pair theBag = new Pair(null, Integer.MAX_VALUE);

			for (Map.Entry<T3Action, T3State> transition : state.getTransitions().entrySet()) {
				if (transition.getValue().isWin()) {
					return new Pair(transition.getKey(), -1);
				}
				Pair child = choose_aux(transition.getValue(), alpha, beta, true);
				if (child.val < theBag.val) {
					theBag.action = transition.getKey();
					theBag.val = child.val;
				}
				beta = (beta < theBag.val) ? beta : theBag.val;

				if (beta <= alpha)
					break;
			}
			return theBag;
		}
	}

	/**
	 * Pair class that holds a T3Action and a minimax score
	 *
	 */
	class Pair {
		T3Action action;
		Integer val;

		/**
		 * @param act T3Action representing an action to a state
		 * @param val Corresponding minimax score
		 *
		 */
		Pair(T3Action act, Integer v) {
			action = act;
			val = v;
		}
	}
}