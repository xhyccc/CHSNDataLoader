package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.uva.libopt.numeric.*;
import edu.uva.sys.ehrloader.ml.BalanceTTSelection;
import edu.uva.sys.ehrloader.recovery.*;
import smile.math.matrix.Matrix;
import smile.projection.PCA;
import smile.stat.distribution.GLassoMultivariateGaussianDistribution;
import xiong.hdstats.Estimator;
import xiong.hdstats.da.AdaBoostTreeClassifier;
import xiong.hdstats.da.AdaboostLRClassifier;
import xiong.hdstats.da.BayesLDA;
import xiong.hdstats.da.Classifier;
import xiong.hdstats.da.DTreeClassifier;
import xiong.hdstats.da.SDA;
import xiong.hdstats.da.LDA;
import xiong.hdstats.da.LRClassifier;
import xiong.hdstats.da.LiklihoodBayesLDA;
import xiong.hdstats.da.MCBayesLDA;
import xiong.hdstats.da.MCRegularizedBayesLDA;
import xiong.hdstats.da.DBSDA;
import xiong.hdstats.da.NonlinearSVMClassifier;
import xiong.hdstats.da.ODaehrLDA;
import xiong.hdstats.da.OLDA;
import xiong.hdstats.da.OrgLDA;
import xiong.hdstats.da.PDLassoLDA;
import xiong.hdstats.da.RandomForestClassifier;
import xiong.hdstats.da.RegularizedBayesLDA;
import xiong.hdstats.da.RegularizedLikelihoodBayesLDA;
import xiong.hdstats.da.SVMClassifier;
import xiong.hdstats.da.ShLDA;
import xiong.hdstats.da.ShrinkageLDA;
import xiong.hdstats.da.mDaehrLDA;

public class PsuedoRandomSimulation {

	public static PrintStream ps = null;
	public static int t_size = 400;
	public static int te_size = 100;
	public static int days = 30;

	public static String path = "/Users/xiongha/Dropbox/technical-reports/report-1/libsvm-data/";
	public static String[] datasets = { "web1", "web2", "web3" };
	public static String[][] datafiles = { { "w1a.txt", "w1b.txt" }, { "w2a.txt", "w2b.txt" },
			{ "w3a.txt", "w3b.txt" } };

	public static void main(String[] args) {
		for (int i = 0; i < datasets.length; i++)
			_main(datasets[i], datafiles[i]);
	}

	public static void _main(String dataset, String[] datafile) {

		double[][] cov = new double[200][200];

		for (int i = 0; i < cov.length; i++) {
			for (int j = 0; j < cov.length; j++) {
				cov[i][j] = Math.pow(0.8, Math.abs(i - j));
			}
		}
		double[] meanPositive = new double[200];
		double[] meanNegative = new double[200];
		for (int i = 0; i < meanPositive.length; i++) {
			if (i < 10)
				meanPositive[i] = 1.5;
			else
				meanPositive[i] = 0.5;
			meanNegative[i] = 0.5;
		}

		double[][] theta_s = new Matrix(cov).inverse();
		GLassoMultivariateGaussianDistribution posD = new GLassoMultivariateGaussianDistribution(meanPositive, cov);

		GLassoMultivariateGaussianDistribution negD = new GLassoMultivariateGaussianDistribution(meanPositive, cov);
		double[][] testData = new double[500][200];
		int[] testLabel = new int[500];
		for (int i = 0; i < 500; i++) {
			double[] tdat;
			if (i % 2 == 0) {
				tdat = posD.rand();
				testLabel[i] = 1;
			} else {
				tdat = negD.rand();
				testLabel[i] = 0;
			}
			for (int j = 0; j < cov.length; j++)
				testData[i][j] = tdat[j];
		}

		for (int t = 2; t < Math.pow(2, 8); t *= 2) {
			try {
				ps = new PrintStream("/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/simulation-" + t + ".txt");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int r = 0; r < 20; r++) {
				double[][] trainData = new double[t][200];
				int[] trainLabel = new int[t];
				for (int i = 0; i < t; i++) {
					double[] tdat;
					{
						if (i % 2 == 0) {
							tdat = posD.rand();
							trainLabel[i] = 1;
						} else {
							tdat = negD.rand();
							trainLabel[i] = 0;
						}
						for (int j = 0; j < cov.length; j++)
							trainData[i][j] = tdat[j];
					}

				}
				for (double lambda = 0.5; lambda <= 100; lambda *= 2) {
					Estimator.lambda = lambda;
					SDA oLDA = new SDA(trainData, trainLabel, false);
					accuracy("SDA-" + Estimator.lambda, testData, testLabel, oLDA, 0, 0);
				}

				for (double lambda = 0.5; lambda <= 100; lambda *= 2) {
					Estimator.lambda = lambda;
					DBSDA oLDA = new DBSDA(trainData, trainLabel, false);
					accuracy("\\TheName{}-" + Estimator.lambda, testData, testLabel, oLDA, 0, 0);
				}

				OLDA LDA = new OLDA(trainData, trainLabel, false);
				accuracy("LDA", testData, testLabel, LDA, 0, 0);

			}
		}

	}

	private static void accuracy(String name, double[][] data, int[] labels, Classifier<double[]> classifier, long t1,
			long t2) {
		// int[] plabels=new int[labels.length];
		System.out.println("accuracy statistics");
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < labels.length; i++) {
			int pl = classifier.predict(data[i]);
			System.out.println(pl + "\t vs\t" + labels[i]);
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

	private static void plotAccuracy(double[][] fm, double[][] fm2, HashMap<Integer, Set<Integer>> missingcodes,
			int sum_miss_codes, double[][] fm3, int sum_recover_codes, double threshold) {
		HashMap<Integer, Set<Integer>> recoverycodes = new HashMap<Integer, Set<Integer>>();

		for (int i = 0; i < fm.length; i++) {
			for (int j = 0; j < fm[i].length; j++) {
				if (fm2[i][j] == 0 && fm3[i][j] > threshold) {
					sum_recover_codes++;
					if (!recoverycodes.containsKey(i))
						recoverycodes.put(i, new HashSet<Integer>());
					recoverycodes.get(i).add(j);

				}
			}
		}

		int intersection = intersection(missingcodes, recoverycodes);
		double precision = (double) intersection / (double) sum_recover_codes;
		double recall = (double) intersection / (double) sum_miss_codes;
		double f1 = 2 * precision * recall / (precision + recall);
		double err_l1 = Utils.normalizedErrorL1Recovery(fm, fm2, fm3, threshold);
		double err_l2 = Utils.normalizedErrorL2Recovery(fm, fm2, fm3, threshold);

		ps.print(threshold);
		ps.print("," + sum_miss_codes);
		ps.print("," + sum_recover_codes);
		ps.print("," + intersection);
		ps.print("," + f1);
		ps.print("," + precision);
		ps.print("," + recall);
		ps.print("," + err_l1);
		ps.println("," + err_l2);

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
