package main_package;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;



public class Classifier {
	
	private Map<Sentiment, Double> classProbabilities;
	
	private Map<String, Map<Sentiment, Double>> smoothedLogProbs;
	
	public Classifier(Map<Path, Sentiment> trainingSet) throws IOException {
		
		this.classProbabilities = calculateClassProbabilities(trainingSet);
		
		this.smoothedLogProbs = calculateSmoothedLogProbs(trainingSet);
		
	}
	
	private static Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
		int numOfPositives = 0;
		int total = 0;
		for (Path p: trainingSet.keySet()) {
			if (trainingSet.get(p) == Sentiment.POSITIVE) {
				numOfPositives += 1;
			}
			total += 1;
		}
		
		double fraction = (double) (numOfPositives) / (double) total;
		Map<Sentiment, Double> answer = new HashMap<>();
		answer.put(Sentiment.POSITIVE, fraction);
		answer.put(Sentiment.NEGATIVE, 1.0 - fraction);
		
		return answer;
	}
	
	public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet)
			throws IOException {

		//For each word, stores the number of times it occurs in positive reviews at index 0
		//And number of times it occurs in negative reviews at index 1
		Map<String, Integer[]> wordCount = new HashMap<>();
	
		int positiveWords = 0;
		int negativeWords = 0;

		for (Path p : trainingSet.keySet()) {

			for (String word : Tokenizer.tokenize(p)) {

				if (trainingSet.get(p) == Sentiment.POSITIVE) {

					positiveWords += 1;

					if (!wordCount.containsKey(word)) {
						wordCount.put(word, new Integer[] { 1, 0 });
					} else {
						wordCount.get(word)[0] += 1;
					}

				} else {

					negativeWords += 1;
					if (wordCount.get(word) == null) {
						wordCount.put(word, new Integer[] { 0, 1 });
					} else {
						wordCount.get(word)[1] += 1;
					}

				}
			}

		}
		
		int distinctWords = wordCount.size();
		
		Map<String, Map<Sentiment, Double>> result = new HashMap<>();
		for (String word : wordCount.keySet()) {
			Map<Sentiment, Double> r = new HashMap<>();
			
			Double PWordGivenPos = (double) (wordCount.get(word)[0] + 1) / (double) (positiveWords + distinctWords);
			Double PWordGivenNeg = (double) (wordCount.get(word)[1] + 1) / (double) (negativeWords + distinctWords);
			
			r.put(Sentiment.POSITIVE, Math.log(PWordGivenPos));
			r.put(Sentiment.NEGATIVE, Math.log(PWordGivenNeg));
			
			result.put(word, r);
			
			
		}
		
		return result;
		
	}
	
	public Sentiment classify(Path pathToReview) throws IOException {
		
		Double totalForPositive = Math.log(classProbabilities.get(Sentiment.POSITIVE));
		
		Double totalForNegative = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
		
		for (String word : Tokenizer.tokenize(pathToReview)) {
			
			if (smoothedLogProbs.containsKey(word)) {
			
				totalForPositive += smoothedLogProbs.get(word).get(Sentiment.POSITIVE);
		
				totalForNegative += smoothedLogProbs.get(word).get(Sentiment.NEGATIVE);
			}
		}
		
		if (totalForPositive > totalForNegative) {
			return Sentiment.POSITIVE;
		} else {
			return Sentiment.NEGATIVE;
		}
	}
	
	public Map<Path, Sentiment> predictAll(java.util.Set<Path> paths) throws IOException {
		Map<Path, Sentiment> output = new HashMap<Path, Sentiment>();
		
		for (Path p : paths) {
			output.put(p, this.classify(p));
		}
		
		return output;
		
	}
	 
	 
}
