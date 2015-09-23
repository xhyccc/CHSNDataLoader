package edu.uva.sys.ehrloader.ml;

import java.util.HashSet;
import java.util.Set;

public class BalanceTTSelection implements TTSelection{
	private double[][] trainingSet;
	private int[] trainingLabels;
	private double[][] testingSet;
	private int[] testingLabels;
	private int _t_size;
	private double[][] _data;
	private int[] _labels;
	public Set<Integer> trainIndex;
	public Set<Integer> testIndex;

	private int _te_size;
	
	public BalanceTTSelection(double[][] data, int[] labels, int t_size, int te_size){
		this._t_size=t_size;
		this._te_size=te_size;
		this._data=data;
		this._labels=labels;
		this.trainingSet=new double[t_size*2][data[0].length];
		this.trainingLabels=new int[t_size*2];
		this.testingSet=new double[data.length-te_size*2][data[0].length];
		this.testingLabels=new int[labels.length-te_size*2];
	}
	
	public void select(){
		Set<Integer> pTrain=new HashSet<Integer>(); 
		Set<Integer> nTrain=new HashSet<Integer>(); 
		Set<Integer> pTest=new HashSet<Integer>(); 
		Set<Integer> nTest=new HashSet<Integer>(); 
		while(pTrain.size()<_t_size){
			for(int i=0;i<_data.length&&pTrain.size()<_t_size;i++){
				if(Math.random()<((double)_t_size/(double)_data.length)&&!pTrain.contains(i)
						&&this._labels[i]==1){
					pTrain.add(i);
				}
			}
		}
		
		while(nTrain.size()<_t_size){
			for(int i=0;i<_data.length&&nTrain.size()<_t_size;i++){
				if(Math.random()<((double)_t_size/(double)_data.length)&&!nTrain.contains(i)
						&&this._labels[i]==0){
					nTrain.add(i);
				}
			}
		}
		
		while(pTest.size()<_te_size){
			for(int i=0;i<_data.length&&pTrain.size()<_te_size;i++){
				if(Math.random()<((double)_te_size/(double)_data.length)&&!pTrain.contains(i)
						&&!pTest.contains(i)&&this._labels[i]==1){
					pTest.add(i);
				}
			}
		}
		
		while(nTest.size()<_te_size){
			for(int i=0;i<_data.length&&pTrain.size()<_te_size;i++){
				if(Math.random()<((double)_te_size/(double)_data.length)&&!nTrain.contains(i)
						&&!nTest.contains(i)&&this._labels[i]==0){
					nTest.add(i);
				}
			}
		}



		
		this.trainIndex=new HashSet<Integer>();
		this.trainIndex.addAll(pTrain);
		this.trainIndex.addAll(nTrain);
		this.testIndex=new HashSet<Integer>();
		this.testIndex.addAll(pTest);
		this.testIndex.addAll(nTest);
		
	//	int t_index=0;
//		while(trainIndex.size()<2*_t_size){
//			for(int i=0;i<_data.length&&trainIndex.size()<2*_t_size;i++){
//				if(Math.random()<(2.0*(double)_t_size/(double)_data.length)&&!trainIndex.contains(i)){
//					fromDataToTraining(i,trainIndex.size());
//					trainIndex.add(i);
//				}
//			}
//		}
		int t_index=0;
		int te_index=0;
		for(int i=0;i<_data.length;i++){
			if(trainIndex.contains(i)){
				fromDataToTraining(i,t_index++);
			}else if(testIndex.contains(i)){
				fromDataToTesting(i,te_index);
			}
		}
	}
	
	public void select(Set<Integer> si,Set<Integer> ssi){
		this.trainIndex=new HashSet<Integer>(si);
		this.testIndex=new HashSet<Integer>(ssi);
		int t_index=0;
		for(int i=0;i<_data.length;i++){
			if(trainIndex.contains(i)){
				fromDataToTraining(i,t_index++);
			}
		}
		t_index=0;
		for(int i=0;i<_data.length;i++){
			if(testIndex.contains(i)){
				fromDataToTesting(i,t_index++);
			}
		}
	}
	
	
	private void fromDataToTraining(int i, int j){
		for(int k=0;k<_data[0].length;k++){
			this.trainingSet[j][k]=this._data[i][k];
		}
		this.trainingLabels[j]=this._labels[i];
	}
	
	private void fromDataToTesting(int i, int j){
		for(int k=0;k<_data[0].length;k++){
			this.testingSet[j][k]=this._data[i][k];
		}
		this.testingLabels[j]=this._labels[i];
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
