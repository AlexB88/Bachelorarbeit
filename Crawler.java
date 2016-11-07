package interactiveCrawlingBA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.apache.derby.tools.sysinfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cc.mallet.classify.Classifier;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class Crawler {

	String domain;
	int counterIteration = 0;

	public Crawler() {
	}

	// TODO: h3, href
	// receiveSeedURLS
	public ArrayList<URL> receiveSeedURLS(String string) throws IOException {
		Document doc = Jsoup.connect(string).get();
		Elements links = doc.select("a[href]");
		String tmp;
		int index = 0;
		for (Element element : links) {
			tmp = element.attr("abs:href");
			URL url = new URL(tmp);
			Frontier.seedURLs.add(url);
			System.out.println(Frontier.seedURLs.get(index));
			index++;
		}
		return Frontier.seedURLs;
	}

	// Crawling mit möglichen (noch nicht gelabelten) Webseiten
	public void crawlWebsite(ArrayList<Website> websitesToVisit) throws IOException {
		Frontier.tmpUrlsToVisit.clear();
		Classification cl = new Classification();
		for (Website w : websitesToVisit) {
			if (w.content != null) {
				if (!(w.url == null)) {
					double jaccardIndex = w.similarity(w);
					if (jaccardIndex > 0.99) {
						System.out.println("Der JaccardIndex ist: " + jaccardIndex);
						System.out.println("Somit wird diese Webseite nicht weiter berücksichtigt!");
					} else {
						Instance instance = cl.generateInstance(w);
						boolean bool = cl.printLabelings(GetStaticClassifier.c, instance);
						if (bool == true) {
							if (!Frontier.seedWebsites.contains(w)) {
								Frontier.seedWebsites.add(w);
							}
							if (w.domain == this.domain) {
								delayWait delay = new delayWait();
								delay.waitForIt();
							}
							w.label = "relevant";
							// Frontier.tmpUrlsToVisit und urlList (lokal)
							ArrayList<URL> urlList = w.extractHyperLinks(w.url);
							System.out.println("Die Anzahl an URLs von " + w.url + " beträgt: " + urlList.size());
							System.out.println();
							// Frontier.tmpWebsites wird befüllt
							w.urlsToWebsites(urlList);
							if (!Frontier.globalURLCorpus.contains(w.url)) {
								Frontier.globalURLCorpus.add(w.url);
								if (!Frontier.relevantURLs.contains(w.url)) {
									Frontier.relevantURLs.add(w.url);
								}
							}
						} else {
							w.label = "nicht relevant";
							if (!Frontier.seedWebsites.contains(w)) {
								Frontier.seedWebsites.add(w);
							}
							if (!Frontier.irrelevantURLs.contains(w.url)) {
								Frontier.irrelevantURLs.add(w.url);
							}
							if (!Frontier.globalURLCorpus.contains(w.url)) {
								Frontier.globalURLCorpus.add(w.url);
							}
						}
					}
				}
			}
		}
		counterIteration++;

		System.out.println("Nun werden die Relevanzwerte der Reihe nach aufgelistet.");
		System.out.println(Frontier.relevanceValue.size());
		for (double d : Frontier.relevanceValue) {
			System.out.println("1. Webseite: " + d);
		}

		// Nun sind alle Webseiten der extrahierten URLs in Frontier.tmpWebsites
		System.out.println("Die " + counterIteration + ".Iteration ist nun abgearbeitet!");
		System.out.println();
		System.out.println(
				"Die Anzahl der nun zu besuchenden URLs beträgt in dieser Iteration: " + Frontier.tmpWebsites.size());
		System.out.println("----------------------------------------------------------------------");

		Frontier.websitesToVisit.clear();
		for (Website website : Frontier.tmpWebsites) {
			if (!Frontier.globalURLCorpus.contains(website.url)) {
				Frontier.websitesToVisit.add(website);
			}
		}
		Frontier.tmpWebsites.clear();

		for (int index = 0; index < Frontier.websitesToVisit.size(); index++) {
			if (Frontier.websitesToVisit.get(index).label == null) {
				Frontier.websitesToVisit.remove(index);
				// System.out.println("Wird nicht benötigt!");
			} else {
				if (Frontier.globalURLCorpus.contains(Frontier.websitesToVisit.get(index).url)
						|| Frontier.tmpUrlsToVisit.contains(Frontier.websitesToVisit.get(index).url)) {
					Frontier.websitesToVisit.remove(index);
				}
			}
		}
		// System.out.println("Anzahl der SeedWebsites danach: " +
		// Frontier.seedWebsites.size());
		// System.out.println("Danach" + Frontier.seedWebsites.size());

		for (int index = 0; index < Frontier.seedWebsites.size(); index++) {
			if (Frontier.seedWebsites.get(index).label == null) {
				Frontier.seedWebsites.remove(index);
				// System.out.println("Wird nicht benötigt!");
			} else {
				// System.out.println(Frontier.seedWebsites.get(index).url);
			}
		}

		for (Website website : Frontier.seedWebsites) {
			if (website.label != null || (!Frontier.globalURLCorpus.contains(website.url)
					&& !Frontier.tmpUrlsToVisit.contains(website.url))) {
				Frontier.seedTmpWebsites.add(website);
			}
		}

		InstanceList instances = cl.generateInstanceList(Frontier.seedTmpWebsites);
		Classifier classi = cl.trainClassifier(instances);
		GetStaticClassifier.c = classi;

		// Test
		ArrayList<String> liste1 = new ArrayList<String>();
		String temp1 = "";

		System.out.println();
		System.out.println("globale Anzahl der relevanten URLs beträgt: " + Frontier.relevantURLs.size());
		for (URL url : Frontier.relevantURLs) {
			temp1 = url.toString();
			liste1.add(temp1);
			// System.out.println(url);
		}

		// Test
		System.out.println();
		Collections.sort(liste1);
		for (String s : liste1) {
			System.out.println(s);
		}

		// Test
		ArrayList<String> liste2 = new ArrayList<String>();
		String temp2 = "";

		System.out.println();
		System.out.println("globale Anzahl der nicht relevanten URLs: " + Frontier.irrelevantURLs.size());
		for (URL url : Frontier.irrelevantURLs) {
			temp2 = url.toString();
			liste2.add(temp2);
			// System.out.println(url);
		}

		// Test
		System.out.println();
		Collections.sort(liste2);
		for (String s : liste2) {
			System.out.println(s);
		}
	}
}