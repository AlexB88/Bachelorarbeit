package interactiveCrawlingBA;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cc.mallet.types.Instance;
import interactiveCrawlingBA.Frontier;
import interactiveCrawlingBA.Website;

public class Website {

	// URL
	URL url;

	// Label
	String label;

	// html
	String htmlCode;

	// temp String for website content
	String content;

	// Domain
	String domain;

	// Seite besucht?
	boolean visitedURL;

	public String getHtml() {
		return htmlCode;
	}

	public void setHtml(String html) {
		this.htmlCode = html;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public boolean isVisitedURL() {
		return visitedURL;
	}

	public void setVisitedURL(boolean visitedURL) {
		this.visitedURL = visitedURL;
	}

	// get URL-Domain
	public String getDomainName(String link) throws MalformedURLException {
		URL url = new URL(link);
		String domain = url.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	// seedURLs --> seedWebites
	public void seedUrlsToWebsites(ArrayList<URL> urlList) throws IOException {
		int AMOUNT_OF_URLS = 14;
		for (int index = 0; index < urlList.size(); index++) {
			if (!(urlList.get(index) == null)) {
				Website w = new Website();
				w = getURLinformation(urlList.get(index));
				if (!(w.url == null)) {
					if (index < ((AMOUNT_OF_URLS + 1) / 2)) {
						w.label = "relevant";
					} else {
						w.label = "nicht relevant";
					}
					Frontier.seedWebsites.add(w);
				}
			} else {
				System.out.println("Eine Seed URL ist null!");
			}
		}
	}

	// TODO
	// URLs --> Websites
	public void urlsToWebsites(ArrayList<URL> urlList) throws IOException, MalformedURLException, HttpStatusException {
		if (urlList == null) {
			System.out.println("Diese Liste ist leer.");
		} else {
			Website w = new Website();
			for (URL url : urlList) {
				if (!Frontier.globalURLCorpus.contains(url)) {
					if (!(url == null)) {
						// domain mit vorheriger überprüfen
						w = w.getURLinformation(url);
						w.label = null;
						Frontier.tmpWebsites.add(w);
					}
				}
			}
		}
	}

	// URL informationextraction, getting a Website
	public Website getURLinformation(URL hyperLink) throws IOException, MalformedURLException, HttpStatusException {
		Website wInfo = new Website();
		if (!(hyperLink == null)) {
			if (wInfo.domain == getDomainName(hyperLink.toString())) {
				delayWait delay = new delayWait();
				delay.waitForIt();
			}
			try {
				Document doc = Jsoup.connect(hyperLink.toString()).timeout(8000).ignoreContentType(true)
						.validateTLSCertificates(false)
						.userAgent(
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko)Chrome/15.0.874.120 Safari/535.2")
						.get();
				wInfo.url = hyperLink;
				wInfo.content = doc.body().text();
				wInfo.htmlCode = doc.html();
				wInfo.domain = getDomainName(hyperLink.toString());
			} catch (IOException e) {
			}
		}
		return wInfo;
	}

	// extract HyperLinks
	public ArrayList<URL> extractHyperLinks(URL hyperLinks)
			throws IOException, MalformedURLException, HttpStatusException {
		ArrayList<URL> list = new ArrayList<URL>();
		if (!(hyperLinks == null)) {
			try {
				Document doc = Jsoup.connect(hyperLinks.toString()).timeout(8000).ignoreContentType(true)
						.validateTLSCertificates(false)
						.userAgent(
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko)Chrome/15.0.874.120 Safari/535.2")
						.get();
				Elements links = doc.select("a");
				if (links.size() == 0) {
					System.out.println("Diese Webseite hat keine relevanten Hyerlinks für Sie");
				} else if (links.size() > 15) {
					String relHref = "";
					for (int index = 0; index < 15; index++) {
						if (links.get(index).attr("abs:href").contains("http")
								|| links.get(index).attr("abs:href").contains("https")) {
							relHref = links.get(index).attr("abs:href");
							URL relHrefnew = new URL(relHref);
							if ((!Frontier.tmpUrlsToVisit.contains(relHrefnew))
									&& (!Frontier.globalURLCorpus.contains(relHrefnew))
									&& (!Frontier.tmpWebsites.contains(relHrefnew))) {
								Frontier.tmpUrlsToVisit.add(relHrefnew);
								list.add(relHrefnew);
							}
						}
					}
				} else {
					String relHref = "";
					for (Element element : links) {
						if ((element.attr("abs:href").contains("http"))
								|| (element.attr("abs:href").contains("https"))) {
							relHref = element.attr("abs:href");
							URL relHrefnew = new URL(relHref);
							if ((!Frontier.tmpUrlsToVisit.contains(relHrefnew))
									&& (!Frontier.globalURLCorpus.contains(relHrefnew))
									&& (!Frontier.tmpWebsites.contains(relHrefnew))) {
								Frontier.tmpUrlsToVisit.add(relHrefnew);
								list.add(relHrefnew);
							}
						}
					}
				}
			} catch (IOException e) {
			}
		}
		return list;
	}

	// Jaccard - Koeffizient
	// Filter, mit dem ähnliche Seiten von unterschiedlichen
	// Ähnlichkeit zu Frontier.relevantWebsites
	public double similarity(Website website) {
		double counterCommonWords = 0.0;
		double counterOfAllWords = 0.0;
		double counterRelevantWebsites = 0.0;
		double resultLocal = 0.0;
		double result = 0.0;

		// Website (delete multiple occurences)
		Set<String> setWebsite = new HashSet<String>();
		String tmpWebsite = website.content;
		String[] StringToArrayListFromWebsite = tmpWebsite.split(" ");
		for (int i = 0; i < (StringToArrayListFromWebsite.length); i++) {
			setWebsite.add(StringToArrayListFromWebsite[i]);
		}

		// List of Websites --> Set (delete multiple occurences)
		for (Website w : Frontier.seedWebsites) {
			Set<String> set = new HashSet<String>();
			Set<String> tmpSet = new HashSet<String>();
			String tmpWebsiteFromArray = w.content;
			String[] StringToStringArray = tmpWebsiteFromArray.split(" ");
			for (int i = 0; i < (StringToStringArray.length); i++) {
				set.add(StringToStringArray[i]);
				tmpSet.add(StringToStringArray[i]);
			}

			// Schnittmenge
			set.retainAll(setWebsite);
			// Vereinigungsmenge
			setWebsite.addAll(tmpSet);

			counterOfAllWords = setWebsite.size();
			counterCommonWords = set.size();
			// Jacard-Index
			resultLocal = counterCommonWords / counterOfAllWords;
			if (resultLocal > 0.3) {
				counterRelevantWebsites++;
				result = result + resultLocal;
			}
		}
		if ((counterRelevantWebsites / Frontier.relevantWebsites.size()) > 0.1) {
			result = result / counterRelevantWebsites;
		} else {
			result = 0.0;
		}
		return result;
	}
}
