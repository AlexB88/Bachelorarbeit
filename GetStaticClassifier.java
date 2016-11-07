package interactiveCrawlingBA;

import cc.mallet.classify.Classifier;

public class GetStaticClassifier {

	static Classifier c;
	
	public void getClassifier() {
		Classification cl = new Classification();
		Classifier c = cl.getClassifier();
		GetStaticClassifier.c = c;
	}	
}
