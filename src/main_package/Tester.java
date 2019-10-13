package main_package;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;



import java.util.List;

public class Tester {

	public static void main(String[] args) throws IOException {
		Map<Path, Sentiment> data = getData();
		
		Pair<Map<Path, Sentiment>, Map<Path, Sentiment>> dataSplit = getTrainAndTestData(data, 0.8);
		
		Map<Path, Sentiment> trainingData = dataSplit.getItem1();
		
		Map<Path, Sentiment> testingData = dataSplit.getItem2();
		
		Classifier naiveBayesClassifier = new Classifier(trainingData);
		
		Map<Path, Sentiment> trainingPredictions = naiveBayesClassifier.predictAll(trainingData.keySet());
		
		System.out.println("Training accuracy: " + calculateAccuracy(trainingData, trainingPredictions));
		
		
		Map<Path, Sentiment> testingPredictions = naiveBayesClassifier.predictAll(testingData.keySet());
		
		System.out.println("Testing accuracy: " + calculateAccuracy(testingData, testingPredictions));
		
		
	}
	
	private static Map<Path, Sentiment> getData() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("data/review_sentiment"));
		String line = "";
		
		Map<Path, Sentiment> output = new HashMap<Path, Sentiment>();
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split("\\s+");
			Path pathToReview = Paths.get("data", "reviews", arr[0]);
			
			Sentiment sentiment = null;
			
			if (arr[1].equals("NEG")) {
				sentiment = Sentiment.NEGATIVE;
			} else {
				sentiment = Sentiment.POSITIVE;
			}
			
			output.put(pathToReview, sentiment);
			
		}
		
		
		return output;
		
	}
	
	private static Pair<Map<Path, Sentiment>, Map<Path, Sentiment>> getTrainAndTestData(Map<Path, Sentiment> data, double trainSetProportion) {
		
		int trainSize = (int) Math.floor(trainSetProportion * data.size());
		
		Map<Path, Sentiment> trainingSet = new HashMap<Path, Sentiment>();
		Map<Path, Sentiment> testingSet = new HashMap<Path, Sentiment>();
		
		
		List<Entry<Path, Sentiment>> dataList = new ArrayList<>(data.entrySet());
		Collections.shuffle(dataList);
		
		for (int i = 0; i < dataList.size(); i++) {
			
			Entry<Path, Sentiment> entry = dataList.get(i);
			
			if (i < trainSize) {
				trainingSet.put(entry.getKey(), entry.getValue());
			} else {
				testingSet.put(entry.getKey(), entry.getValue());
			}
			
		}
		
		return new Pair<Map<Path, Sentiment>, Map<Path, Sentiment>> (trainingSet, testingSet);
		
	}
	
	private static double calculateAccuracy(Map<Path, Sentiment> dataSet, Map<Path, Sentiment> predictions) {
		
		int totalCorrect = 0;
		int total = 0;
		
		for (Path p : dataSet.keySet()) {
			
			if (dataSet.get(p) == predictions.get(p) ) {
				totalCorrect += 1;
			}
			
			total += 1;
		}
		
		return (double) (totalCorrect) / (double) (total) ;
	}

}

class Pair<T, V> {
	
	private T item1;
	private V item2;
	
	public Pair(T item1, V item2) {
		super();
		this.item1 = item1;
		this.item2 = item2;
	}

	public T getItem1() {
		return item1;
	}


	public V getItem2() {
		return item2;
	}

}
