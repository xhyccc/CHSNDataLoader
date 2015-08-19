package edu.uva.sys.ehrloader.recovery;

import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import ml.recovery.MatrixCompletion;
import ml.recovery.RobustPCA;

public class PCARecovery implements Recovery{

	@Override
	public double[][] recover(double[][] matrix) {
		// TODO Auto-generated method stub
		Matrix m=new DenseMatrix(matrix);
		RobustPCA mc=new RobustPCA(0.5);
		mc.feedData(m);
		mc.run();
		double[][] error=mc.GetErrorMatrix().getData();
		double err=0;
		for(int i=0;i<error.length;i++){
			for(int j=0;j<error[i].length;j++){
				err+=Math.abs(error[i][j]);
			}
		}
		System.out.println("error:"+err);
		return mc.GetLowRankEstimation().getData();
	}

}
