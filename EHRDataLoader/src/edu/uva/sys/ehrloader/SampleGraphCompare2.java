package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uva.libopt.numeric.*;
import edu.uva.sys.ehrloader.ml.BalanceTTSelection;
import edu.uva.sys.ehrloader.ml.RandomSampleSelection;
import edu.uva.sys.ehrloader.recovery.*;
import smile.math.matrix.Matrix;
import xiong.hdstats.da.Classifier;
import xiong.hdstats.da.LDA;
import xiong.hdstats.da.PseudoInverseLDA;
import xiong.hdstats.da.CovLDA;
import xiong.hdstats.da.PseudoInverse;
import xiong.hdstats.da.ml.AdaboostLRClassifier;
import xiong.hdstats.da.ml.LRClassifier;
import xiong.hdstats.da.ml.SVMClassifier;
import xiong.hdstats.da.shruken.DaehrLDA;
import xiong.hdstats.da.shruken.ODaehrLDA;
import xiong.hdstats.da.shruken.ShLDA;
import xiong.hdstats.da.shruken.ShrinkageLDA;
import xiong.hdstats.da.shruken.mDaehrLDA;
import xiong.hdstats.gaussian.CovarianceEstimator;
import xiong.hdstats.graph.DGLassoGraph;
import xiong.hdstats.graph.GLassoGraph;
import xiong.hdstats.graph.GraphEva;
import xiong.hdstats.graph.SampleGraph;
import xiong.hdstats.graph.ens.SampleWishartGraph;
import xiong.hdstats.graph.ens.DGLassoWishartGraph;

public class SampleGraphCompare2 {

	public static PrintStream ps = null;
	public static int t_size = 400;
	public static int te_size = 100;
	public static int days = 30;

	public static void main(String[] args) {

		if (args.length >= 1)
			t_size = Integer.parseInt(args[0]);

		if (args.length >= 2)
			te_size = Integer.parseInt(args[1]);

		if (args.length >= 3)
			days = Integer.parseInt(args[2]);

		ps = System.out;

		EHRRecordMap map = new EHRRecordMap("/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/mapping.txt");

		EHRecordBase base = ICDLineReader.load(map, "/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/non-mh_icd.csv",
				"x_icdcode", 500000);

		EHRecordBase base_2 = ICDLineReader.load(map, "/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/icd_MD.csv",
				"y_icdcode", 500000);

		// base_2.removeVisitsAfter(MHCode.codes);

		// base.removePatientLessNVisit(5);
		// base_2.removePatientLessNVisit(5);

		// base.setLabelsForAllPatients(0);
		// base_2.setLabelsForAllPatients(1);
		base.insertRecords(base_2);
		base.setPositiveLabel(MHCode.codes);
		base.removeVisitsAfter(MHCode.codes, days);
		base.removePatientLessNVisit(3);

		ps.println("patients: " + base.getPatients().size());
		ps.println("codes: " + base.getCodes().size());
		ps.println("dates: " + base.getDates().size());

		double[][] fm = base.getFrequencyMatrix();
		// HashMap<Integer, Set<Integer>> missingcodes = new HashMap<Integer,
		// Set<Integer>>();

		System.out.println("matrix " + fm.length + " x " + fm[0].length);
		// ps.println("using NMF 10");
		// double[][] recoveredData = dataRecovery(new NMFRecovery(10), fm, fm,
		// missingcodes, 0);

		String path = "/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/";

		// for (int r = 0; r < 50; r++) {
		BalanceTTSelection s1 = new BalanceTTSelection(fm, base.getLabels(), 5000, 1);
		s1.select();
		// RandomSampleSelection rss = new
		// RandomSampleSelection(s1.getTrainingSet(), s1.getTrainingLabels(), 0,
		// 10000);
		double[][] negLarge = s1.negativeSamples;
		System.out.println("Negative Samples OUT");

		// RandomSampleSelection rsss = new
		// RandomSampleSelection(s1.getTrainingSet(), s1.getTrainingLabels(), 1,
		// 10000);
		double[][] posLarge = s1.postiveSamples;
		System.out.println("Positive Samples OUT");

		// DGLassoGraph negTruth = new DGLassoGraph(negLarge, 0.1);
		SampleGraph posTruth = new SampleGraph(posLarge);
		// double[] thrs={4.62,4.72,4.91};
		// double[] thrs={0,};
		// double[] thrs={4.62,};
		double[] thrs = { 4.72, };
		// double[] thrs = { 4.91 };
		for (double thrr : thrs) {
			// int[][] diffTruth = posTruth.adaptiveThresholdingDiff(thrr /
			// Math.sqrt(5000), negTruth);
			try {
				ps = new PrintStream(
						"/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/graph-ehr-threshold-" + thrr + ".txt");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int[] ts = { 250, 300 };
			for (int t : ts) {
				for (int r = 0; r < 5; r++) {
				//	RandomSampleSelection neg = new RandomSampleSelection(negLarge, t);
					RandomSampleSelection pos = new RandomSampleSelection(posLarge, t);
				//	double[][] negSmall = neg.getDataSet();
					double[][] posSmall = pos.getDataSet();
					double thr = thrr / Math.sqrt(t);
					// SampleGraph negSample = new SampleGraph(negSmall);
					// new GraphEva(negTruth.adaptiveThresholding(thrr /
					// Math.sqrt(20000)),
					// negSample.adaptiveThresholding(thr)).print("sample-neg\t"
					// + t, ps);
					SampleGraph posSample = new SampleGraph(posSmall);
					new GraphEva(posTruth.adaptiveThresholding(thrr / Math.sqrt(5000)),
							posSample.adaptiveThresholding(thr)).print("sample-pos\t" + t, ps);
					// int[][] diffSample =
					// posSample.adaptiveThresholdingDiff(thr, negSample);
					// new GraphEva(diffTruth, diffSample).print("sample-diff\t"
					// + t, ps);

					for (double lbd = 0.1; lbd <= 10; lbd *= 10) {
						// GLassoGraph negGlasso = new GLassoGraph(negSmall,
						// lbd);
						// new GraphEva(negTruth.adaptiveThresholding(thrr /
						// Math.sqrt(20000)),
						// negGlasso.adaptiveThresholding(thr)).print("glasso" +
						// lbd + "-neg\t" + t, ps);
						GLassoGraph posGlasso = new GLassoGraph(posSmall, lbd);
						new GraphEva(posTruth.adaptiveThresholding(thrr / Math.sqrt(5000)),
								posGlasso.adaptiveThresholding(thr)).print("glasso" + lbd + "-0.1-pos\t" + t, ps);
						// int[][] diffGlasso =
						// posGlasso.adaptiveThresholdingDiff(thr, negGlasso);
						// new GraphEva(diffTruth, diffGlasso).print("glasso-" +
						// lbd + "-diff\t" + t, ps);

						// DGLassoGraph negDGLasso = new DGLassoGraph(negSmall,
						// lbd);
						// new GraphEva(negTruth.adaptiveThresholding(thrr /
						// Math.sqrt(20000)),
						// negDGLasso.adaptiveThresholding(thr)).print("dglasso-"
						// + lbd + "-neg\t" + t, ps);
						DGLassoGraph posDGLasso = new DGLassoGraph(posSmall, lbd);
						new GraphEva(posTruth.adaptiveThresholding(thrr / Math.sqrt(5000)),
								posDGLasso.adaptiveThresholding(thr)).print("dglasso-" + lbd + "-pos\t" + t, ps);
						// int[][] diffDGLasso =
						// posDGLasso.adaptiveThresholdingDiff(thr, negDGLasso);
						// new GraphEva(diffTruth, diffDGLasso).print("dglasso-"
						// + lbd + "-diff\t" + t, ps);
					}

					for (int sampling = 100; sampling <= 200; sampling += 100) {
						// DGLassoWishartGraph negWishart = new
						// DGLassoWishartGraph(negSmall, lbd, sampling);
						for (double lbd = 1; lbd <= 100; lbd *= 10) {
							SampleWishartGraph posWishart = new SampleWishartGraph(posSmall, sampling);
							for (int selected = 1000; selected <= 5000; selected += 1000) {
								// new
								// GraphEva(negTruth.adaptiveThresholding(thrr /
								// Math.sqrt(20000)),
								// negWishart.adaptiveThresholding(thr,
								// selected))
								// .print("wishart-" + sampling + "-" + selected
								// + "-neg\t" + t, ps);
								new GraphEva(posTruth.adaptiveThresholding(thrr / Math.sqrt(5000)),
										posWishart.adaptiveThresholding(thr, selected,0))
												.print("wishart-" + sampling + "-" + selected + "-pos\t" + t, ps);

							}
						}
						// for (int overlap = 10; overlap <= 30; overlap += 10)
						// {
						// int[][] diffWishart =
						// posWishart.adaptiveThresholdingDiff(thr, overlap,
						// negWishart);
						// new GraphEva(diffTruth, diffWishart)
						// .print("wishart-" + sampling + "-" + overlap +
						// "-diff\t" + t, ps);
						// }

					}
				}
			}
		}
		// }
	}
	// }

	// }

	private static void saveMatrxInFile(String path, double[][] matrx, List<String> codes, Map<String, String> map) {
		try {
			PrintStream ps = new PrintStream(path + ".txt");
			for (int i = 0; i < matrx.length; i++) {
				for (int j = 0; j < matrx[i].length; j++) {
					ps.println(map.get(codes.get(i)) + "\t" + map.get(codes.get(j)) + "\t" + matrx[i][j]);
				}
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void accuracy(String name, double[][] data, int[] labels, Classifier<double[]> classifier, long t1,
			long t2) {
		// int[] plabels=new int[labels.length];
		System.out.println("accuracy statistics");
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < labels.length; i++) {
			int pl = classifier.predict(data[i]);
			if (pl == 1 && labels[i] == 1) {
				tp++;
			} else if (pl == 0 && labels[i] == 0) {
				tn++;
			} else if (pl == 1 && labels[i] == 0) {
				fp++;
			} else {
				fn++;
			}

		}
		long train_time = t2 - t1;
		double test_time = ((double) (System.currentTimeMillis() - t2)) / ((double) labels.length);
		ps.println(name + "\t" + tp + "\t" + tn + "\t" + fp + "\t" + fn + "\t" + train_time + "\t" + test_time);

	}

	private static double[][] dataRecovery(Recovery r, double[][] fm, double[][] fm2,
			HashMap<Integer, Set<Integer>> missingcodes, int sum_miss_codes) {
		// Recovery r = new NMFRecovery(5);

		double[][] fm3 = r.recover(fm2);
		// int sum_recover_codes = 0;

		// ps.println("original matrix " + fm.length + " x " + fm[0].length);
		// ps.println("missing matrix " + fm2.length + " x " + fm2[0].length);
		// ps.println("recovered matrix " + fm3.length + " x " + fm3[0].length);
		//
		// ps.print("threshold,");
		// ps.print("missing,");
		// ps.print("recover," );
		// ps.print("intersect,");
		// ps.print("f1 score,");
		// ps.print("precision score,");
		// ps.print("recall score,");
		// ps.print("normalized l1 error,");
		// ps.println("normalized l2 error");
		//
		//
		//
		// // plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.0);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.1);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.2);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.3);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.4);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.5);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.6);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.7);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.8);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,0.9);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.0);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.1);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.2);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.3);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.4);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.5);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.6);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.7);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.8);
		// plotAccuracy(fm, fm2, missingcodes, sum_miss_codes, fm3,
		// sum_recover_codes,1.9);

		RecoveryUtil.maxMerge(fm3, fm);
		return fm3;
	}

	private static void plotAccuracy(String name, double[][] fm, double[][] fm2) {

		double err_l1 = Utils.normalizedErrorL1((fm2), (fm), 0);
		double err_l2 = Utils.normalizedErrorL2((fm2), (fm), 0);

		ps.print(name + "\t" + err_l1);
		ps.println("\t" + err_l2);

	}

	public static double F1(Set<String> miss, Set<String> recover) {
		int intersect = 0;
		for (String i : miss)
			if (recover.contains(i))
				intersect++;
		return 2.0 * (double) intersect / (double) (miss.size() + recover.size());
	}

	public static int intersection(HashMap<Integer, Set<Integer>> miss, HashMap<Integer, Set<Integer>> recover) {
		int intersect = 0;
		for (int p : miss.keySet()) {
			if (recover.containsKey(p)) {
				for (int code : miss.get(p)) {
					if (recover.get(p).contains(code)) {
						intersect++;
					}
				}
			}
		}

		return intersect;
	}

}
