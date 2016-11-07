package interactiveCrawlingBA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cc.mallet.classify.AdaBoost;
import cc.mallet.classify.AdaBoostM2;
import cc.mallet.classify.AdaBoostM2Trainer;
import cc.mallet.classify.AdaBoostTrainer;
import cc.mallet.classify.BalancedWinnowTrainer;
import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierEnsemble;
import cc.mallet.classify.ClassifierEnsembleTrainer;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.DecisionTreeTrainer;
import cc.mallet.classify.MCMaxEnt;
import cc.mallet.classify.MaxEnt;
import cc.mallet.classify.MaxEntL1Trainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesEMTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.classify.WinnowTrainer;
import cc.mallet.optimize.OptimizationException;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Label;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.Labeling;

public class Classification {

	static int RequestUserCounter = 0;
	Pipe pipe;

	public Classification() {
		pipe = buildPipe();
	}

	// generate Pipe
	public Pipe buildPipe() {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		LabelAlphabet labelAlphabet = new LabelAlphabet();
		Alphabet instanceAlphabet = new Alphabet();
		labelAlphabet.lookupLabel("relevant", true);
		labelAlphabet.lookupLabel("nicht relevant", true);

		Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));
		pipeList.add(new TokenSequenceRemoveStopwords(new File("germanST.txt"), "UTF-8", false, false, false));
		pipeList.add(new TokenSequenceLowercase());
		pipeList.add(new TokenSequence2FeatureSequence(instanceAlphabet));
		pipeList.add(new FeatureSequence2FeatureVector());
		pipeList.add(new Target2Label(labelAlphabet));

		return new SerialPipes(pipeList);
	}

	// receive Classifier from Trainingsset
	public Classifier getClassifier() throws OptimizationException {
		InstanceList instanceList = generateInstanceList(Frontier.tmpWebsites);
		Classifier classi = trainClassifier(instanceList);
		return classi;
	}

	// create InstanceList of Seed-Instances
	public InstanceList generateInstanceList(ArrayList<Website> websiteList) throws OptimizationException {
		InstanceList ilist = new InstanceList(this.pipe);
		for (Website web : websiteList) {
			// data, target, name, source
			Instance instance = new Instance(web.content, web.label, web.domain, web.htmlCode);
			ilist.addThruPipe(instance);
		}
		return ilist;
	}

	// create InstanceList of Instances
	public Instance generateInstance(Website web) throws OptimizationException {
		// data, target, name, source
		Instance instance = new Instance(web.content, null, web.url, web.htmlCode);
		return instance;
	}

	// Train Classifier
	public Classifier trainClassifier(InstanceList trainingInstances) throws OptimizationException {

		// Here we use a maximum entropy (ie polytomous logistic regression)
		// classifier. Mallet includes a wide variety of classification
		// algorithms, see the JavaDoc API for details.
		ClassifierTrainer trainer = new MaxEntL1Trainer();
		return trainer.train(trainingInstances);
	}

	// Generate Label for new data
	public boolean printLabelings(Classifier classifier, Instance instance) throws IOException, OptimizationException {

		classifier.getInstancePipe().instanceFrom(instance);
		Labeling labeling = classifier.classify(instance).getLabeling();
		for (int rank = 0; rank < labeling.numLocations(); rank++) {
			System.out.print(instance.getName() + " : " + labeling.getLabelAtRank(rank) + " : "
					+ labeling.getValueAtRank(rank) + " ");
			System.out.println();
		}

		// TODO Parameter modifizieren
		// Gebiet der Unsicherheit
		double valueMAX = 0.88;
		double valueMIN = 0.78;
		boolean bool = true;

		if (labeling.getLabelAtRank(0).toString() == "relevant") {
			if (labeling.getValueAtRank(0) < valueMAX && labeling.getValueAtRank(0) > valueMIN) {
				Frontier.relevanceValue.add(labeling.getValueAtRank(0));
				// Frontier.irrelevantURLs.add((URL) instance.getName());
				// Seite aufrufen um Nutzer bestimmen zu lassen
				UserInteraction ui = new UserInteraction();
				ui.openBrowser(instance.getName().toString());
				bool = ui.evaluateWebsite(instance);
				RequestUserCounter++;
			} else if (labeling.getValueAtRank(0) > valueMAX) {
				Frontier.relevanceValue.add(labeling.getValueAtRank(0));
				bool = true;
			} else {
				Frontier.relevanceValue.add(labeling.getValueAtRank(0));
				bool = false;
			}
		} else {
			Frontier.relevanceValue.add(labeling.getValueAtRank(0));
			bool = false;
		}
		return bool;
	}

	// TODO
	// Evaluate Classifier
	public void evaluate(Classifier classifier, Instance instance) throws IOException {

		InstanceList testInstances = new InstanceList(classifier.getInstancePipe());

		// Add all instances loaded by the iterator to
		// our instance list, passing the raw input data
		// through the classifier's original input pipe.

		testInstances.addThruPipe(instance);

		Trial trial = new Trial(classifier, testInstances);

		// The Trial class implements many standard evaluation
		// metrics. See the JavaDoc API for more details.

		System.out.println("Accuracy: " + trial.getAccuracy());

		// precision, recall, and F1 are calcuated for a specific
		// class, which can be identified by an object (usually
		// a String) or the integer ID of the class

		System.out.println("F1 for class 'good': " + trial.getF1("relevant"));

		System.out.println(
				"Precision for class '" + classifier.getLabelAlphabet().lookupLabel(1) + "': " + trial.getPrecision(1));
	}

}
