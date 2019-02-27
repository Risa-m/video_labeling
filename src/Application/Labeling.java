package Application;

public class Labeling {
	public int labelNumber;
	
	public Labeling() {
		labelNumber = LabelNum.getLabelNumber(LabelNum.NON_LABELING);
	}
	public void setLabeling(LabelNum labelnum) {
		labelNumber = LabelNum.getLabelNumber(labelnum);
	}
	public int getLabeling() {
		return labelNumber;
	}
	
	public enum LabelNum{
		LEARNING	(0),
		STOPPING (1),
		BORERING (2),
		NON_LABELING (3),
		ROBOT_TALKING (4);
		
		public  int labelNumber;

		private LabelNum(int number) {
			labelNumber = number;
		}

		public static LabelNum getType(int number){
			if (number == 0)
				return LabelNum.LEARNING;
			else if(number == 1)
				return LabelNum.STOPPING;
			else if(number == 2)
				return LabelNum.BORERING;
			else if(number == 3)
				return LabelNum.NON_LABELING;
			else
				return LabelNum.ROBOT_TALKING;
		}

		public static int getLabelNumber(LabelNum label){
			return label.labelNumber;
		}
		
		public int getLabelNumber() {
			return this.labelNumber;
		}
		
	}
}
