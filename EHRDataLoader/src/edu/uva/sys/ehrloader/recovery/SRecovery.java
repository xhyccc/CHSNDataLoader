package edu.uva.sys.ehrloader.recovery;

public class SRecovery implements Recovery{
	private Recovery _r;
	
	public SRecovery(Recovery r){
		this._r=r;
	}
	
	@Override
	public double[][] recover(double[][] matrix) {
		// TODO Auto-generated method stub
		return RecoveryUtil.maxMerge(_r.recover(matrix), matrix);
	}

}
