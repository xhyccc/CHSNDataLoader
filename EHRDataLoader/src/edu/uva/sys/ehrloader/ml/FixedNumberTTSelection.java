package edu.uva.sys.ehrloader.ml;

import java.util.HashSet;
import java.util.Set;

public class FixedNumberTTSelection implements TTSelection{
	private double[][] trainingSet;
	private int[] trainingLabels;
	private double[][] testingSet;
	private int[] testingLabels;
	private int _t_size;
	private double[][] _data;
	private int[] _labels;
	
	public FixedNumberTTSelection(double[][] data, int[] labels, int t_size){
		this._t_size=t_size;
		this._data=data;
		this._labels=labels;
		this.trainingSet=new double[t_size*2][data[0].length];
		this.trainingLabels=new int[t_size*2];
		this.testingSet=new double[data.length-t_size*2][data[0].length];
		this.testingLabels=new int[labels.length-t_size*2];
	}
	
	public void select(){
		Set<Integer> trainIndex=new HashSet<Integer>();
	//	int t_index=0;
		while(trainIndex.size()<2*_t_size){
			for(int i=0;i<_data.length&&trainIndex.size()<2*_t_size;i++){
				if(Math.random()<(2.0*(double)_t_size/(double)_data.length)&&!trainIndex.contains(i)){
					fromDataToTraining(i,trainIndex.size());
					trainIndex.add(i);
				}
			}
		}
		int t_index=0;
		for(int i=0;i<_data.length;i++){
			if(!trainIndex.contains(i)){
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
