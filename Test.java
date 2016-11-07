package interactiveCrawlingBA;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import org.jsoup.HttpStatusException;

import cc.mallet.classify.Classifier;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class Test {

	public static void main(String[] args) throws IOException, URISyntaxException, HttpStatusException {

		Website w = new Website();
		Crawler cr = new Crawler();
		Classification cl = new Classification();

		// create TrainingSet ("relevant")
		URL url1 = new URL("http://haartransplantation.svenson.de/?gclid=CKfh5p2m1c0CFbQV0wodDzUP8A");
		URL url2 = new URL("http://www.bc-klinik.de/haare?gclid=CMaYup6m1c0CFRFmGwodJBIJdg");
		URL url3 = new URL(
				"http://lp.medical-one.de/hair/haartransplantation/?adwords_id=080002&gclid=CJ-2yJ-m1c0CFQbgGwodiMME1w");
		URL url4 = new URL(
				"http://www.koe-hair.de/kontakt/beratungszentren/koe-hair-beratungszentrum-in-stuttgart.html?AdWords=1&gclid=CJGhu6Gm1c0CFWQq0wod-DcD5A");
		// URL url5 = new
		// URL("https://de.wikipedia.org/wiki/Haartransplantation");
		URL url6 = new URL("http://haartransplantation-kosten.info/");
		URL url7 = new URL("https://www.moser-kliniken.de/haartransplantation");
		URL url8 = new URL("http://www.lexerklinik.de/de/behandlungen-haut-haare/haartransplantation");

		// create TrainingSet ("nicht relevant")
		URL url1i = new URL("https://www.abenteuer-regenwald.de/wissen/folgen");
		// URL url2i = new
		// URL("https://www.abenteuer-regenwald.de/wissen/abholzung");
		URL url3i = new URL("http://www.faszination-regenwald.de/info-center/zerstoerung/flaechenverluste.htm");
		URL url4i = new URL(
				"https://www.regenwald.org/news/3286/regenwaldrodung-mehr-als-ein-halbes-fussballfeld-pro-sekunde");
		URL url5i = new URL("http://www.pro-regenwald.de/");
		URL url6i = new URL("https://www.orangutan.de/der-tropische-regenwald");
		URL url7i = new URL("http://www.faszination-regenwald.de/info-center/allgemein/geografie.htm");
		URL url8i = new URL("https://www.abenteuer-regenwald.de/wissen");

		Frontier.seedURLs.add(url1);
		Frontier.seedURLs.add(url2);
		Frontier.seedURLs.add(url3);
		Frontier.seedURLs.add(url4);
		// Frontier.seedURLs.add(url5);
		Frontier.seedURLs.add(url6);
		Frontier.seedURLs.add(url7);
		Frontier.seedURLs.add(url8);
		Frontier.seedURLs.add(url1i);
		// Frontier.seedURLs.add(url2i);
		Frontier.seedURLs.add(url3i);
		Frontier.seedURLs.add(url4i);
		Frontier.seedURLs.add(url5i);
		Frontier.seedURLs.add(url6i);
		Frontier.seedURLs.add(url7i);
		Frontier.seedURLs.add(url8i);

		// store seedURLs in: Frontier.seedWebsites
		// init: "relevant" and "nicht relevant"
		w.seedUrlsToWebsites(Frontier.seedURLs);

		// create Classifier
		InstanceList instances = cl.generateInstanceList(Frontier.seedWebsites);
		Classifier classi = cl.trainClassifier(instances);
		// static, in order to update the model
		GetStaticClassifier.c = classi;

		// extract URLs from relevant Seeds
		for (int i = 0; i < Frontier.seedWebsites.size(); i++) {
			if (Frontier.seedWebsites.get(i).label == "relevant") {
				Frontier.globalURLCorpus.add(Frontier.seedWebsites.get(i).url);
				Frontier.relevantURLs.add(Frontier.seedWebsites.get(i).url);
				Frontier.relevantWebsites.add(Frontier.seedWebsites.get(i));
				// extracted URLs store in: Frontier.tmpUrlsToVisit
				w.extractHyperLinks(Frontier.seedWebsites.get(i).url);
			} else {
				Frontier.globalURLCorpus.add(Frontier.seedWebsites.get(i).url);
				Frontier.irrelevantURLs.add(Frontier.seedWebsites.get(i).url);
			}
		}
		Frontier.urlsToVisit = Frontier.tmpUrlsToVisit;

		System.out.println("Es wurden nun " + Frontier.tmpUrlsToVisit.size() + " URLs aus den SeedURLs extrahiert.");
		System.out.println();

		for (URL url : Frontier.urlsToVisit) {
			w = w.getURLinformation(url);
			// aus den zu betrachteten URLs werden Webseiten erstellt
			Frontier.websitesToVisit.add(w);
		}

		while (Frontier.globalURLCorpus.size() < 100) {
			cr.crawlWebsite(Frontier.websitesToVisit);
			System.out.println();
			System.out.println("Es sind somit " + Frontier.relevantURLs.size()
					+ " relevante URLs für den Nutzer gefunden worden.");
			System.out.println();
		}

		System.out.println("Der Nutzer wurde " + Classification.RequestUserCounter + "-mal gefragt");
		System.out.println();
		System.out.println("Die Anzahl der ingesammt besuchten Webseiten beträgt: " + Frontier.seedWebsites.size());
		System.out.println();
		System.out.println("Die Anzahl der relevanten Webseiten beträgt: " + Frontier.relevantURLs.size());
		System.out.println();
		System.out.println("Die Anzahl der nicht relevanten Webseiten beträgt: " + Frontier.irrelevantURLs.size());
	}
}