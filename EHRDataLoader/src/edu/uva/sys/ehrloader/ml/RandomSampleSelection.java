package edu.uva.sys.ehrloader.ml;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomSampleSelection {
	private double[][] dataset;
	public Set<Integer> selIndex=new HashSet<Integer>();

	
	public RandomSampleSelection(double[][] data, int size){
		this.dataset=new double[size][data[0].length];
		Random r=new Random(System.currentTimeMillis());
		while(selIndex.size()<size){
			int offset=r.nextInt()%data.length;
			if(!selIndex.contains(offset)){
				for(int i=0;i<data[0].length;i++){
					dataset[selIndex.size()][i]=data[offset][i];
				}
				this.selIndex.add(offset);
			}
		}
	}
	public double[][] getDataSet(){
		return this.dataset;
	}
	
}
