package interactiveCrawlingBA;

public class delayWait {

	public void waitForIt() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
