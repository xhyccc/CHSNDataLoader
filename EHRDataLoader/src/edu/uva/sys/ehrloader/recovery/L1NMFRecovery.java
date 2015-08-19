package edu.uva.sys.ehrloader.recovery;

import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import ml.clustering.Clustering;
import ml.clustering.L1NMF;
import ml.options.L1NMFOptions;

public class L1NMFRecovery implements Recovery{

	private int _latent;
	public L1NMFRecovery(int latent){
		this._latent=latent;
	}
	
	@Override
	public double[][] recover(double[][] matrix) {
		// TODO Auto-generated method stub
		Matrix m=new DenseMatrix(matrix);
		Clustering mc=new L1NMF(new L1NMFOptions(this._latent));
		mc.feedData(m);
		mc.clustering();
		return mc.getIndicatorMatrix().mtimes(mc.getCenters()).getData();
	}

}
