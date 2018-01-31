package edu.uva.sys.ehrloader.ml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BalanceTTSelection implements TTSelection {
	private double[][] trainingSet;
	private int[] trainingLabels;
	private double[][] testingSet;
	private int[] testingLabels;
	private int _t_size;
	private double[][] _data;
	private int[] _labels;
	public Set<Integer> trainIndex;
	public Set<Integer> testIndex;
	public double[][] postiveSamples;
	public double[][] negativeSamples;
	private int _te_size;

	public BalanceTTSelection(double[][] data, int[] labels, int t_size, int te_size) {
		this._t_size = t_size;
		this._te_size = te_size;
		this._data = data;
		this._labels = labels;
		this.trainingSet = new double[t_size * 2][data[0].length];
		this.trainingLabels = new int[t_size * 2];
		this.testingSet = new double[te_size * 2][data[0].length];
		this.testingLabels = new int[te_size * 2];
	}

	public void select() {
		System.out.println("start selecting training/testing set");
		List<Integer> pTrain = new ArrayList<Integer>();
		List<Integer> nTrain = new ArrayList<Integer>();
		List<Integer> pTest = new ArrayList<Integer>();
		List<Integer> nTest = new ArrayList<Integer>();

		for (int i = 0; i < this._labels.length; i++) {
		//	System.out.println(this._labels[i]);
			if (this._labels[i] == 1) {
				pTrain.add(i);
			} else {
				nTrain.add(i);
			}
		}

		System.out.println("all positive patients\t" + pTrain.size());
		System.out.println("all negative patients\t" + nTrain.size());

		while (pTrain.size() > this._t_size) {
			int toRemove = (int) (Math.random() * (double) pTrain.size());
			pTest.add(pTrain.remove(Math.min(toRemove, pTrain.size() - 1)));
		}

		while (nTrain.size() > this._t_size) {
			int toRemove = (int) (Math.random() * (double) nTrain.size());
			nTest.add(nTrain.remove(Math.min(toRemove, nTrain.size() - 1)));
		}

		while (pTest.size() > this._te_size) {
			int toRemove = (int) (Math.random() * (double) pTrain.size());
			pTest.remove(Math.min(toRemove, pTest.size() - 1));
		}

		while (nTest.size() > this._te_size) {
			int toRemove = (int) (Math.random() * (double) nTrain.size());
			nTest.remove(Math.min(toRemove, nTest.size() - 1));
		}

		System.out.println("positive patients for training\t" + pTrain.size());
		System.out.println("negative patients for training\t" + nTrain.size());
		System.out.println("positive patients for testing\t" + pTest.size());
		System.out.println("negative patients for testing\t" + nTest.size());

		this.trainIndex = new HashSet<Integer>();
		this.trainIndex.addAll(pTrain);
		this.trainIndex.addAll(nTrain);
		this.testIndex = new HashSet<Integer>();
		this.testIndex.addAll(pTest);
		this.testIndex.addAll(nTest);

		System.out.println("training set\t" + this.trainIndex.size());
		System.out.println("testing set\t" + this.testIndex.size());

		this.postiveSamples = new double[pTrain.size()][_data[0].length];
		this.negativeSamples = new double[nTrain.size()][_data[0].length];
		int indexD = 0;
		for (int index : pTrain) {
			for (int i = 0; i < _data[0].length; i++) {
				this.postiveSamples[indexD][i] = _data[index][i];
			}
			indexD++;
		}

		indexD = 0;
		for (int index : nTrain) {
			for (int i = 0; i < _data[0].length; i++) {
				this.negativeSamples[indexD][i] = _data[index][i];
			}
			indexD++;
		}
		
		// int t_index=0;
		// while(trainIndex.size()<2*_t_size){
		// for(int i=0;i<_data.length&&trainIndex.size()<2*_t_size;i++){
		// if(Math.random()<(2.0*(double)_t_size/(double)_data.length)&&!trainIndex.contains(i)){
		// fromDataToTraining(i,trainIndex.size());
		// trainIndex.add(i);
		// }
		// }
		// }
		int t_index = 0;
		int te_index = 0;
		for (int i = 0; i < _data.length; i++) {
			if (trainIndex.contains(i)) {
				fromDataToTraining(i, t_index++);
			} else if (testIndex.contains(i)) {
				fromDataToTesting(i, te_index++);
			}
		}

		System.out.println("finish selecting training/testing set");

	}

	public void select(Set<Integer> si, Set<Integer> ssi) {
		this.trainIndex = new HashSet<Integer>(si);
		this.testIndex = new HashSet<Integer>(ssi);
		int t_index = 0;
		for (int i = 0; i < _data.length; i++) {
			if (trainIndex.contains(i)) {
				fromDataToTraining(i, t_index++);
			}
		}
		t_index = 0;
		for (int i = 0; i < _data.length; i++) {
			if (testIndex.contains(i)) {
				fromDataToTesting(i, t_index++);
			}
		}
	}

	private void fromDataToTraining(int i, int j) {
		for (int k = 0; k < _data[0].length; k++) {
			this.trainingSet[j][k] = this._data[i][k];
		}
		this.trainingLabels[j] = this._labels[i];
	}

	private void fromDataToTesting(int i, int j) {
		for (int k = 0; k < _data[0].length; k++) {
			this.testingSet[j][k] = this._data[i][k];
		}
		this.testingLabels[j] = this._labels[i];
	}

	@Override
	public double[][] getTrainingSet() {
		// TODO Auto-generated method stub
		return this.trainingSet;
	}

	@Override
	public int[] getTrainingLabels() {
		// TODO Auto-generated method stub
		return this.trainingLabels;
	}

	@Override
	public double[][] getTestingSet() {
		// TODO Auto-generated method stub
		return this.testingSet;
	}

	@Override
	public int[] getTestingLabels() {
		// TODO Auto-generated method stub
		return this.testingLabels;
	}

}
