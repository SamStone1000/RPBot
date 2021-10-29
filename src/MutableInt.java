
public class MutableInt  {
		int value = 1;
		
		public MutableInt(int value) {
			this.value = value;
		}
		
		public static MutableInt parseInt(String string) {
			return new MutableInt(Integer.parseInt(string));
		}

		public void increment() {this.value++;}
		public void decrement() {this.value--;}
		
		public void add(int value) {this.value += value;}
		
		public String toString() {
			return Integer.toString(value);
		}
		
	}