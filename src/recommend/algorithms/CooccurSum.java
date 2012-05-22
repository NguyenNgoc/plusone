package recommend.algorithms;


import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.util.WordIndex;

import plusone.utils.PaperAbstract;


public class CooccurSum extends Algorithm {
	/*
	int[] doccount;
	HashMap<Integer,Integer>[] cooccur;
	
	public CooccurSum() {
		super( "CooccurSum" );
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		doccount = new int[WordIndex.size()];
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int word : traindoc.keySet() ) {
				doccount[word]++;
			}
		}
		
		cooccur = new HashMap[WordIndex.size()];
		
		for( int i = 0; i < cooccur.length; i++ ) {
			cooccur[i] = new HashMap<Integer,Integer>();
		}
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int word1 : traindoc.keySet() ) {
				for( int word2 : traindoc.keySet() ) {
					cooccur[word1].put( word2, cooccur[word1].containsKey( word2 ) ? 1+cooccur[word1].get( word2 ) : 1 );
				}
			}
		}
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
		double[] scores = new double[WordIndex.size()];
		
		for( int w1 : givenwords.keySet() ) {
			if( doccount[w1] < 4 ) {
				continue;
			}
			
			for( int w2 : cooccur[w1].keySet() ) {
				scores[w2] += (double)cooccur[w1].get( w2 ) / doccount[w1] * givenwords.get( w1 );
			}
		}
		
		return scores;
	}
	*/
	
	int[] doccount;
	HashMap<Integer,Integer>[] cooccur;
	private List<TrainingPaper> trainingSet;
    private Terms terms;
	
	public CooccurSum(List<TrainingPaper> trainingSet, Terms terms) {
		super( "CooccurSum" );
		this.trainingSet = trainingSet;
    	this.terms = terms;
    	
		doccount = new int[WordIndex.size()];
		
		for( TrainingPaper t : trainingSet ) {
			for( int word : t.getTrainingWords() ) {
				doccount[word]++;
			}
		}
		
		cooccur = new HashMap[WordIndex.size()];
		
		for( int i = 0; i < cooccur.length; i++ ) {
			cooccur[i] = new HashMap<Integer,Integer>();
		}
		
		for( TrainingPaper t : trainingSet ) {
			for( int word1 : t.getTrainingWords() ) {
				for( int word2 : t.getTrainingWords() ) {
					cooccur[word1].put( word2, cooccur[word1].containsKey( word2 ) ? 1+cooccur[word1].get( word2 ) : 1 );
				}
			}
		}
	}
	
	public double[] predict( int k, PredictionPaper paper ) {
				
		double[] scores = new double[WordIndex.size()];
		
		for( int w1 : ((PaperAbstract)paper).getTestingWords() ) {
			if( doccount[w1] < 4 ) {
				continue;
			}
			
			for( int w2 : cooccur[w1].keySet() ) {
				scores[w2] += (double)cooccur[w1].get( w2 ) / doccount[w1] * ((PaperAbstract)paper).getTestingTf( w1 );
			}
		}
		
		return scores;
	}

}
