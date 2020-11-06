package csp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * CSP: Calendar Satisfaction Problem Solver Provides a solution for scheduling
 * some n meetings in a given period of time and according to some set of unary
 * and binary constraints on the dates of each meeting.
 */
public class CSP {

	/**
	 * Public interface for the CSP solver in which the number of meetings, range of
	 * allowable dates for each meeting, and constraints on meeting times are
	 * specified.
	 * 
	 * @param nMeetings   The number of meetings that must be scheduled, indexed
	 *                    from 0 to n-1
	 * @param rangeStart  The start date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param rangeEnd    The end date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param constraints Date constraints on the meeting times (unary and binary
	 *                    for this assignment)
	 * @return A list of dates that satisfies each of the constraints for each of
	 *         the n meetings, indexed by the variable they satisfy, or null if no
	 *         solution exists.
	 */
	public static List<LocalDate> solve(int nMeetings, LocalDate rangeStart, LocalDate rangeEnd,
			Set<DateConstraint> constraints) {
		ArrayList<DateVar> meetings = new ArrayList<DateVar>(nMeetings);

		// initialize meetings
		for (int index = 0; index < nMeetings; index++) {
			DateVar d = new DateVar(new ArrayList<LocalDate>(), index);

			for (LocalDate i = rangeStart; i.compareTo(rangeEnd) <= 0; i = i.plusDays(1)) {
				d.domain.add(i);
			}

			meetings.add(d);
		}

		for (DateConstraint dc : constraints) {
			// if binary date constraint, add to binaryConstraints list in meetings in index
			// L_VAL
			if (dc.arity() == 2) {
				meetings.get(((BinaryDateConstraint) dc).L_VAL).binaryConsts.add(((BinaryDateConstraint) dc));
				meetings.get(((BinaryDateConstraint) dc).R_VAL).binaryConsts.add(((BinaryDateConstraint) dc));
			}
			// if unary date constraint, add to unaryConstraints list in meetings in
			// index L_VAL
			else {
				meetings.get(dc.L_VAL).unaryConsts.add((UnaryDateConstraint) (dc));
			}
		}

		for (DateVar dv : meetings) {
			dv.domain = (removeInconsistentNodes(dv));
			dv.domain = removeInconsistentArcs(dv, meetings);

			// if a DateVar's domain in empty, there's no solution
			if (dv.domain.size() == 0)
				return null;
		}

		// meetings are now node and arc consistent, time to backtrack
		return backtrack(new ArrayList<LocalDate>(Collections.nCopies(nMeetings, null)), meetings);
	}

	static ArrayList<LocalDate> removeInconsistentNodes(DateVar dateVar) {
		ArrayList<LocalDate> badNodes = new ArrayList<LocalDate>();
		for (UnaryDateConstraint uc : dateVar.unaryConsts) {
			for (LocalDate d : dateVar.domain) {
				if (!passesConstraint(d, uc.R_VAL, uc.OP)) {
					badNodes.add(d);
				}
			}
		}
		dateVar.domain.removeAll(badNodes);
		return dateVar.domain;
	}

	static ArrayList<LocalDate> removeInconsistentArcs(DateVar dateVar, ArrayList<DateVar> meetings) {
		ArrayList<LocalDate> badArcs = new ArrayList<LocalDate>();

		for (BinaryDateConstraint constraint : dateVar.binaryConsts) {
			for (LocalDate date : dateVar.domain) {
				DateVar tail = meetings
						.get((constraint.L_VAL == dateVar.meetingNum) ? constraint.R_VAL : constraint.L_VAL);
				boolean isConsistent = false;
				for (LocalDate d : tail.domain) {
					LocalDate head = d;
					LocalDate lval = (constraint.L_VAL == dateVar.meetingNum) ? date : head;
					LocalDate rval = (constraint.L_VAL == dateVar.meetingNum) ? head : date;

					if (passesConstraint(lval, rval, constraint.OP)) {
						isConsistent = true;
					}
				}
				if (!isConsistent) {
					badArcs.add(date);
				}
			}
		}
		dateVar.domain.removeAll(badArcs);
		return dateVar.domain;
	}

	private static List<LocalDate> backtrack(List<LocalDate> schedule, ArrayList<DateVar> meetings) {
		DateVar unscheduledMeeting = null;

		for (int i = 0; i < schedule.size(); i++) {
			if (schedule.get(i) == null) {
				unscheduledMeeting = meetings.get(i);
			}
		}

		if (unscheduledMeeting == null) {
			return schedule;
		}

		for (LocalDate date : unscheduledMeeting.domain) {

			boolean isBinaryConst = true;
			boolean isUnaryConst = true;

			for (BinaryDateConstraint bc : unscheduledMeeting.binaryConsts) {
				if (!binaryConstraintOkay(unscheduledMeeting, date, bc, schedule)) {
					isBinaryConst = false;
					break;
				}
			}

			for (UnaryDateConstraint uc : unscheduledMeeting.unaryConsts) {
				if (!passesConstraint(date, uc.R_VAL, uc.OP)) {
					isUnaryConst = false;
					break;
				}
			}

			boolean isConstistent = isUnaryConst && isBinaryConst;

			if (isConstistent) {
				schedule.set(unscheduledMeeting.meetingNum, date);

				List<LocalDate> result = backtrack(schedule, meetings);
				if (result != null) {
					return result;
				} else {
					schedule.set(unscheduledMeeting.meetingNum, null);
				}
			}
		}
		return null;
	}

	private static boolean binaryConstraintOkay(DateVar dateVar, LocalDate date, BinaryDateConstraint bc,
			List<LocalDate> assignment) {
		LocalDate lval, rval;

		if (bc.L_VAL == dateVar.meetingNum) {
			if (assignment.get(bc.R_VAL) == null) {
				return true;
			}
			rval = assignment.get(bc.R_VAL);
			lval = date;

		} else {
			if (assignment.get(bc.L_VAL) == null) {
				return true;
			}
			rval = date;
			lval = assignment.get(bc.L_VAL);

		}
		return passesConstraint(lval, rval, bc.OP);
	}

	// takes in 2 LocalDates and an operator comparing them
	private static boolean passesConstraint(LocalDate lhs, LocalDate rhs, String op) {
		switch (op) {
		case "==":
			if (lhs.compareTo(rhs) == 0) {
				return true;
			}
			break;
		case "!=":
			if (lhs.compareTo(rhs) != 0) {
				return true;
			}
			break;
		case "<=":
			if (lhs.compareTo(rhs) <= 0) {
				return true;
			}
			break;
		case ">=":
			if (lhs.compareTo(rhs) >= 0) {
				return true;
			}
			break;
		case "<":
			if (lhs.compareTo(rhs) < 0) {
				return true;
			}
			break;
		case ">":
			if (lhs.compareTo(rhs) > 0) {
				return true;
			}
			break;
		}
		return false;
	}

	private static class DateVar {
		int meetingNum;
		ArrayList<LocalDate> domain;
		ArrayList<BinaryDateConstraint> binaryConsts = new ArrayList<BinaryDateConstraint>();
		ArrayList<UnaryDateConstraint> unaryConsts = new ArrayList<UnaryDateConstraint>();

		DateVar(ArrayList<LocalDate> domain, int meetingNum) {
			this.meetingNum = meetingNum;
			this.domain = domain;
		}

	}

}
