package interactiveCrawlingBA;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import cc.mallet.types.Instance;

public class UserInteraction {

	String topic;

	// users' Website-relevance
	public boolean evaluateWebsite(Instance instance) throws IOException {
		// openBrowser(string);
		System.out.println("Ist diese Website für Sie relevant? Bestätigen mit: y oder n");
		Scanner sc = new Scanner(System.in);
		String input = sc.next();
		if (input.equals("y")) {
			return true;
		}
		else if (input.equals("n")) {
			return false;
		} else {
			boolean bool = true;
			System.out.println("Bitte mit y oder n bestätigen!");
			Scanner scAgain = new Scanner(System.in);
			String inputAgain = scAgain.next();
			while (!(inputAgain.equals("y") || inputAgain.equals("n"))) {
				System.out.println("Nur y oder n eingeben!");
				scAgain = new Scanner(System.in);
				inputAgain = scAgain.next();
				if (inputAgain.equals("y")) {
					Frontier.relevantURLs.add((URL) instance.getName());
					Frontier.globalURLCorpus.add((URL) instance.getName());
					bool = true;
				}
				if (inputAgain.equals("n")) {
					Frontier.irrelevantURLs.add((URL) instance.getName());
					Frontier.globalURLCorpus.add((URL) instance.getName());
					bool = false;
				}
			}
			return bool;
		}
	}

	// TODO
	// ask User for topic
	public void inputRequestUser() throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.print("Welches Thema interessiert Sie: ");
		System.out.println();
		String input = sc.next();
		UserInteraction ui = new UserInteraction();
		// add delay: 1000ms
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String topic = "http://www.google.de/#q=" + input;
		this.topic = topic;
		ui.openBrowser(topic);
	}

	// open Webbrowser for User with specific topic
	public void openBrowser(String stringToEvaluate) throws IOException {
		new ProcessBuilder(new String[] { "cmd", "/c", "start", stringToEvaluate }).start();
	}

}
