package bg.smoc.model;

import java.util.Vector;

public class TaskType {
	
	public static Vector<TaskType> allTaskTypes = null;
	private int numeric;
	private String string;

	public TaskType(int numeric, String string) {
		this.numeric = numeric;
		this.string = string;
	}

	public static Vector<TaskType> getAllTaskTypes() {
		if (allTaskTypes == null) {
			allTaskTypes = new Vector<TaskType>();
			allTaskTypes.add(new TaskType(Task.PROBLEM_TYPE_STANDARD, "Standard Input, Standard Output"));
			allTaskTypes.add(new TaskType(Task.PROBLEM_TYPE_OUTPUT, "Non-excutable result file"));
			allTaskTypes.add(new TaskType(Task.PROBLEM_TYPE_MODULE, "Module communication"));
		}
		return allTaskTypes;
	}

	public int getNumeric() {
		return numeric;
	}

	public String getString() {
		return string;
	}
}
