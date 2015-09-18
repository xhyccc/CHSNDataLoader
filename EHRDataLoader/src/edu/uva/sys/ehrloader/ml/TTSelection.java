package edu.uva.sys.ehrloader.ml;

public interface TTSelection {
	public double[][] getTrainingSet();
	public int[] getTrainingLabels();
	
	public double[][] getTestingSet();
	public int[] getTestingLabels();
	
	public void select();

}
