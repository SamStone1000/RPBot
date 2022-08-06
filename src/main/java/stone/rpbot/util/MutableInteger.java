package stone.rpbot.util;

public class MutableInteger extends Number {
	/**
	*
	*/
	private static final long serialVersionUID = 7386729089718430939L;
	int value = 0;

	public MutableInteger(int value) { this.value = value; }

	public static MutableInteger parseInt(String string) { return new MutableInteger(Integer.parseInt(string)); }

	public void increment() { this.value++; }

	public void decrement() { this.value--; }

	public void add(int value) { this.value += value; }

	@Override
	public String toString() { return Integer.toString(value); }

	@Override
	public int intValue() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public long longValue() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public float floatValue() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public double doubleValue() {
		// TODO Auto-generated method stub
		return value;
	}

}