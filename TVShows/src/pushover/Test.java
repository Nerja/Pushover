package pushover;

public class Test {

	public static void main(String[] args)  {
		Pushover pushover;
		try {
			pushover = new Pushover();
			pushover.sendMessage("Hello World!");
		} catch (PushoverException e) {
			System.out.println(e.getMessage());
		}
	}

}
