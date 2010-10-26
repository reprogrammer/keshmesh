package p;

public class Test {

	int i;

	public static void main(String args[]) {
		Test test = new Test();
		C c = test.new C();
		test.increment(c);
	}

	public Test() {
		i = 5;
	}

	private void increment(C c) {
		Class l = c.getClass();
		synchronized (l) {
			i++;
		}
		;
	};

	class C {

	}
}
