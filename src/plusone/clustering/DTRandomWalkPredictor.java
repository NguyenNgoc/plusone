package plusone.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import plusone.Main;
import plusone.utils.PaperIF;
import plusone.utils.PredictionPaper;
import plusone.utils.SampleDist;
import plusone.utils.SparseVec;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

/** Does a random walk on the document-topic graph to find words.
 */
public class DTRandomWalkPredictor extends ClusteringTest {

    protected final List<TrainingPaper> trainingSet;
    protected final int walkLength;
    protected final boolean stochastic;
    protected final int nSampleWalks;
    protected final Terms terms;
    protected final boolean finalIdf;

    protected final Map<Integer, SparseVec> docsByWord;
    protected final Map<Integer, SampleDist<Integer>> docDistByWord;
    protected final Map<Integer, SampleDist<Integer>> wordDistByDoc;

    public DTRandomWalkPredictor(List<TrainingPaper> trainingSet,
				 Terms terms, int walkLength, 
				 boolean stochastic, int nSampleWalks, boolean finalIdf) {
	super("dtrw-" + Integer.toString(walkLength) +
		(stochastic ? "-s" + nSampleWalks : ""));
        this.trainingSet = trainingSet;
	this.terms = terms;
        this.walkLength = walkLength;
        this.stochastic = stochastic;
        this.nSampleWalks = nSampleWalks;
        this.finalIdf = finalIdf;
        
        docDistByWord = new HashMap<Integer, SampleDist<Integer>>();
        for (int i = 0; i < this.trainingSet.size(); ++i) {
            docDistByWord.put(i, SampleDist.samplePaperWords(trainingSet.get(i)));
        }
        docsByWord = makeDocsByWord(trainingSet);
        wordDistByDoc = new HashMap<Integer, SampleDist<Integer>>();
        for (Map.Entry<Integer, SparseVec> docsForThisWord : docsByWord.entrySet()) {
            docDistByWord.put(docsForThisWord.getKey(), SampleDist.sampleVecCoords(docsForThisWord.getValue()));
        }
    }

    public DTRandomWalkPredictor(List<TrainingPaper> trainingSet,
                                 int walkLength, Terms terms, boolean finalIdf) {
	this(trainingSet, terms, walkLength, false, 0, finalIdf);
    }
    
    protected Map<Integer, SparseVec> makeDocsByWord(List<TrainingPaper> trainingSet) {
	Map<Integer, SparseVec> ret = new HashMap<Integer, SparseVec>();
	for (int i = 0; i < trainingSet.size(); ++i) {
	    TrainingPaper paper = trainingSet.get(i);
	    for (Integer word : paper.getTrainingWords()) {
		if (!ret.containsKey(word)) ret.put(word, new SparseVec());
		ret.get(word).addSingle(i, (double)paper.getTrainingTf(word));
	    }
	}
	return ret;
    }

    public Integer[] predict(int k, PredictionPaper paper) {
	SparseVec words = stochastic ? stochWalkMany(paper) : detWalk(paper);
        SparseVec w;
        if (finalIdf) {
            SparseVec wordsIdf = new SparseVec();
            for (Map.Entry<Integer, Double> pair : words.pairs()) {
                wordsIdf.addSingle(pair.getKey(), pair.getValue() * terms.get(pair.getKey()).trainingIdf(trainingSet.size()));
            }
            w = wordsIdf;
        } else
            w = words;
	return firstKExcluding(w.descending(), k, paper.getTrainingWords());
    }

    Integer[] firstKExcluding(Integer[] l, Integer k, Set<Integer> excl) {
	List<Integer> ret = new ArrayList<Integer>();
	for (int i = 0; i < l.length && ret.size() < k; ++i) {
	    Integer word = l[i];
	    if (excl == null || !excl.contains(word)) {
		ret.add(word);
	    }
	}
	return ret.toArray(new Integer[0]);
    }

    protected SparseVec detWalk(PredictionPaper start) {
	SparseVec words = new SparseVec(start);
        int nDocs = trainingSet.size();
	for (int i = 0; i < walkLength; ++i) {
	    /* Walk from words to docs. */
	    SparseVec docs = new SparseVec();
	    for (Map.Entry<Integer, Double> pair : words.pairs()) {
                Terms.Term term = terms.get(pair.getKey());
                SparseVec docsForThisWord = docsByWord.get(pair.getKey());
                if (null != docsForThisWord)
                    docs.plusEqualsWithCoef(docsForThisWord, pair.getValue() / term.totalCount);
	    }
	    /* Walk from docs to words. */
	    words = new SparseVec();
	    for (Map.Entry<Integer, Double> pair : docs.pairs()) {
		SparseVec wordsForThisDoc = makeTrainingWordVec(trainingSet.get(pair.getKey()), true, nDocs);
		wordsForThisDoc.dotEquals(pair.getValue() / wordsForThisDoc.coordSum());
		words.plusEquals(wordsForThisDoc);
	    }
	}
	return words;
    }
    
    protected int stochWalkOnce(PaperIF start) {
	Random r = new Random();
	int word = SampleDist.samplePaperWords(start).sample(r);
	for (int i = 0; i < walkLength; ++i) {
	    int doc = docDistByWord.get(word).sample(r);
	    word = wordDistByDoc.get(doc).sample(r);
	}
	return word;
    }
    
    protected SparseVec stochWalkMany(PaperIF start) {
	SparseVec v = new SparseVec();
	for (int i = 0; i < nSampleWalks; ++i)
	    v.addSingle(stochWalkOnce(start), 1.0);
	return v;
    }

    public SparseVec makeTrainingWordVec(TrainingPaper paper, 
					 boolean useFreqs, int nDocs) {
        SparseVec ret = new SparseVec();
        for (Integer word: paper.getTrainingWords())
            ret.addSingle(word, 
			  (useFreqs ? paper.getTrainingTf(word) : 1.0));
        return ret;
    }
}
