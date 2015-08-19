package edu.uva.sys.ehrloader.recovery;

import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import ml.clustering.Clustering;
import ml.clustering.L1NMF;
import ml.clustering.NMF;
import ml.options.L1NMFOptions;
import ml.options.NMFOptions;

public class NMFRecovery implements Recovery{

	private int _latent;
	public NMFRecovery(int latent){
		this._latent=latent;
	}
	
	@Override
	public double[][] recover(double[][] matrix) {
		// TODO Auto-generated method stub
		Matrix m=new DenseMatrix(matrix);
		Clustering mc=new NMF(new NMFOptions(this._latent));
		mc.feedData(m);
		mc.clustering();
		return mc.getIndicatorMatrix().mtimes(mc.getCenters()).getData();
	}

}
