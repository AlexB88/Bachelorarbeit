package interactiveCrawlingBA;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;

public class Frontier {

	// store SeedURLS
	static ArrayList<URL> seedURLs = new ArrayList<URL>();
	
	// store seedWebsites
	static ArrayList<Website> seedWebsites = new ArrayList<Website>();
	
	static ArrayList<Website> seedTmpWebsites = new ArrayList<Website>();

	// ------------------------------------------------------------------------

	// Corpus aller schon besuchten URLs
	static Set<URL> globalURLCorpus = new HashSet<URL>();

	// TODO
	// SET --> HashSet, List --> LinkedList
	static ArrayList<Website> tmpwebsitesToVisit = new ArrayList<Website>();
	static ArrayList<Website> websitesToVisit = new ArrayList<Website>();
	static ArrayList<URL> urlsToVisit = new ArrayList<URL>();
	static ArrayList<URL> tmpUrlsToVisit = new ArrayList<URL>();

	//-------------------------------------------------------------------------
	
	// Datenstruktur für Inhalte aller relevanten, besuchten Webseiten
	static ArrayList<Website> relevantWebsites = new ArrayList<Website>();
	
	// Datenstruktur für Inhalte aller relevanten, besuchten URLs
	static ArrayList<URL> relevantURLs = new ArrayList<URL>();

	// Datenstruktur für Inhalte aller relevanten, besuchten URLs
	static ArrayList<URL> irrelevantURLs = new ArrayList<URL>();
	
	
	// TmpWebsites nach seedURLtoWebsite-Aufruf
	// Global
	static ArrayList<Website> tmpWebsites = new ArrayList<Website>();
	
	static ArrayList<Double> relevanceValue = new ArrayList<Double>();
}
