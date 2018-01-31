package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.uva.libopt.numeric.*;
import edu.uva.sys.ehrloader.ml.BalanceTTSelection;
import edu.uva.sys.ehrloader.recovery.*;
import smile.math.matrix.Matrix;
import smile.projection.PCA;
import smile.stat.distribution.MultivariateGaussianDistribution;
import smile.stat.distribution.SpikedMultivariateGaussianDistribution;
import xiong.hdstats.da.Classifier;
import xiong.hdstats.da.LDA;
import xiong.hdstats.da.PseudoInverseLDA;
import xiong.hdstats.da.OnlineLDA;
import xiong.hdstats.da.CovLDA;
import xiong.hdstats.da.mcmc.BayesLDA;
import xiong.hdstats.da.mcmc.LiklihoodBayesLDA;
import xiong.hdstats.da.mcmc.MCBayesLDA;
import xiong.hdstats.da.mcmc.MCRegularizedBayesLDA;
import xiong.hdstats.da.mcmc.RegularizedBayesLDA;
import xiong.hdstats.da.mcmc.RegularizedLikelihoodBayesLDA;
import xiong.hdstats.da.ml.AdaBoostTreeClassifier;
import xiong.hdstats.da.ml.AdaboostLRClassifier;
import xiong.hdstats.da.ml.DTreeClassifier;
import xiong.hdstats.da.ml.LRClassifier;
import xiong.hdstats.da.ml.NonlinearSVMClassifier;
import xiong.hdstats.da.ml.RandomForestClassifier;
import xiong.hdstats.da.ml.SVMClassifier;
import xiong.hdstats.da.shruken.DBSDA;
import xiong.hdstats.da.shruken.ODaehrLDA;
import xiong.hdstats.da.shruken.SDA;
import xiong.hdstats.da.shruken.ShLDA;
import xiong.hdstats.da.shruken.ShrinkageLDA;
import xiong.hdstats.da.shruken.SpikedDA;
import xiong.hdstats.da.shruken.InvalidLDA;
import xiong.hdstats.gaussian.CovarianceEstimator;

public class PsuedoRandomOnlineLDA {

	public static PrintStream ps = null;

	public static void main(String[] args) throws FileNotFoundException {

	//	_main(200, 10, 20, 1000, 5000, 4);
	//	_main(200, 10, 20, 1000, 5000, 3);
	//	_main(200, 10, 20, 1000, 5000, 2);
	//	_main(200, 10, 20, 1000, 5000, 1);

	//	_main(200, 10, 20, 500, 2000, 5);
	//	_main(200, 10, 40, 500, 2000, 5);
	//	_main(200, 10, 80, 500, 2000, 5);
	//	_main(200, 10, 160, 500, 2000, 5);
	//	_main(200, 10, 320, 500, 2000, 5);
		
	//	_main(50, 10, 20, 500, 2000, 5);
	//	_main(100, 10, 20, 500, 2000, 5);
	//	_main(400, 10, 20, 500, 2000, 5);
	//	_main(800, 10, 20, 500, 2000, 5);
		_main(1600, 10, 20, 500, 2000, 5);
	//	_main(3200, 160, 20, 500, 2000, 5);

	}

	public static void _main(int p, int nz, int initTrainSize, int testSize, int newSize, int rate)
			throws FileNotFoundException {

		ps = new PrintStream("C:/Users/xiongha/Desktop/ograph/accuracy-" + p + "-" + nz + "-" + initTrainSize + "-"
				+ ((double) rate / 1.0) + ".txt");
		double[][] cov = new double[p][p];

		for (int i = 0; i < cov.length; i++) {
			for (int j = 0; j < cov.length; j++) {
				cov[i][j] = Math.pow(0.8, Math.abs(i - j));
			}
		}
		double[] meanPositive = new double[p];
		double[] meanNegative = new double[p];
		for (int i = 0; i < meanPositive.length; i++) {
			if (i < nz)
				meanPositive[i] = 1.0;
			else
				meanPositive[i] = 0.0;
			meanNegative[i] = 0.0;
		}

		// double[][] theta_s = new Matrix(cov).inverse();
		SpikedMultivariateGaussianDistribution posD = new SpikedMultivariateGaussianDistribution(meanPositive, cov);

		SpikedMultivariateGaussianDistribution negD = new SpikedMultivariateGaussianDistribution(meanNegative, cov);

		for (int r = 0; r < 10; r++) {
			double[][] testData = new double[testSize][p];
			int[] testLabel = new int[testSize];
			int[] testLabelNN = new int[testSize];

			for (int i = 0; i < testSize; i++) {
				double[] tdat;
				if (i % 10 < rate) {
					tdat = posD.rand();
					testLabel[i] = 1;
					testLabelNN[i] = 1;
				} else {
					tdat = negD.rand();
					testLabel[i] = -1;
					testLabelNN[i] = 0;
				}
				for (int j = 0; j < cov.length; j++)
					testData[i][j] = tdat[j];
			}

			double[][] trainData = new double[initTrainSize + newSize][p];
			int[] trainLabel = new int[initTrainSize + newSize];
			int[] trainLabelNN = new int[initTrainSize + newSize];
			for (int i = 0; i < initTrainSize + newSize; i++) {
				double[] tdat;
				{
					if (i % 10 < rate) {
						tdat = posD.rand();
						trainLabel[i] = 1;
						trainLabelNN[i] = 1;
					} else {
						tdat = negD.rand();
						trainLabel[i] = -1;
						trainLabelNN[i] = 0;
					}
					for (int j = 0; j < cov.length; j++)
						trainData[i][j] = tdat[j];
				}

			}
			OnlineLDA olda = new OnlineLDA();
			olda.init(getTopKSamples(trainData, initTrainSize), getTopKLabels(trainLabel, initTrainSize),
					initTrainSize / 3);
			// System.out.println(Utils.getErrorInf(olda.means[0], new
			// double[1][p]));
			accuracy("Oline-init", 0, testData, testLabel, olda, 0, 0);

			for (int ns = 1; ns <= newSize; ns++) {
				long start = System.currentTimeMillis();
				System.out.println("online LDA updating...."+ns);
				olda.update(trainData[initTrainSize + ns-1], trainLabel[initTrainSize + ns-1]);
				System.out.println("online LDA finished updating");
				long end = System.currentTimeMillis();
				if (ns > 1 && ns % 500 == 0) {
					accuracy("OlineLDA", ns, testData, testLabel, olda, start, end);
					
					double[][] tdata = getTopKSamples(trainData, initTrainSize + ns);
					int[] tlabel = getTopKLabels(trainLabel, initTrainSize + ns);
					int[] tlabelNN = getTopKLabels(trainLabelNN, initTrainSize + ns);
					for (int i = 2; i <= 8; i *= 2) {
						long t1 = System.currentTimeMillis();
						SpikedDA oLDA = new SpikedDA(tdata, tlabel, false, i);
						long t2 = System.currentTimeMillis();
						accuracy("OfflineLDA-" + i, ns, testData, testLabel, oLDA, t1, t2);
					}
					long t1 = System.currentTimeMillis();
					SVMClassifier svm = new SVMClassifier(tdata, tlabel);
					long t2 = System.currentTimeMillis();
					accuracy("OfflineSVM", ns, testData, testLabel,  svm, t1, t2);

					t1 = System.currentTimeMillis();
					RandomForestClassifier rfc = new RandomForestClassifier(tdata, tlabelNN, 40);
					t2 = System.currentTimeMillis();
					accuracyNN("OfflineRFC-40", ns, testData, testLabelNN, rfc, t1, t2);

					t1 = System.currentTimeMillis();
					rfc = new RandomForestClassifier(tdata, tlabelNN, 10);
					t2 = System.currentTimeMillis();
					accuracyNN("OfflineRFC-10", ns, testData, testLabelNN, rfc, t1, t2);

					t1 = System.currentTimeMillis();
					rfc = new RandomForestClassifier(tdata, tlabelNN, 20);
					t2 = System.currentTimeMillis();
					accuracyNN("OfflineRFC-20", ns, testData, testLabelNN, rfc, t1, t2);
					
					t1 = System.currentTimeMillis();
					NonlinearSVMClassifier nsvm = new NonlinearSVMClassifier(tdata, tlabelNN, 15, 1);
					t2 = System.currentTimeMillis();
					accuracyNN("OfflineKernel-15.0", ns, testData, testLabelNN, nsvm, t1, t2);

					t1 = System.currentTimeMillis();
					nsvm = new NonlinearSVMClassifier(tdata, tlabelNN, 5, 1);
					t2 = System.currentTimeMillis();
					accuracyNN("OfflineKernel-5.0", ns, testData, testLabelNN, nsvm, t1, t2);
					
					t1 = System.currentTimeMillis();
					nsvm = new NonlinearSVMClassifier(tdata, tlabelNN, 10.0, 1);
					t2 = System.currentTimeMillis();
					accuracyNN("OfflineKernel-10.0", ns, testData, testLabelNN, nsvm, t1, t2);

					
				}

			}

		}
	}

	private static void accuracy(String name, int s, double[][] data, int[] labels, Classifier<double[]> classifier,
			long t1, long t2) {
		// int[] plabels=new int[labels.length];
		// System.out.println("accuracy statistics");
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < labels.length; i++) {
			int pl = classifier.predict(data[i]);
			// System.out.println(pl + "\t vs\t" + labels[i]);
			if (pl == 1 && labels[i] == 1) {
				tp++;
			} else if (pl == -1 && labels[i] == -1) {
				tn++;
			} else if (pl == 1 && labels[i] == -1) {
				fp++;
			} else {
				fn++;
			}

		}
		long train_time = t2 - t1;
		double test_time = ((double) (System.currentTimeMillis() - t2)) / ((double) labels.length);
		ps.println(
				name + "\t" + s + "\t" + tp + "\t" + tn + "\t" + fp + "\t" + fn + "\t" + train_time + "\t" + test_time);

	}
	
	
	private static void accuracyNN(String name, int s, double[][] data, int[] labels, Classifier<double[]> classifier,
			long t1, long t2) {
		// int[] plabels=new int[labels.length];
		// System.out.println("accuracy statistics");
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < labels.length; i++) {
			int pl = classifier.predict(data[i]);
			// System.out.println(pl + "\t vs\t" + labels[i]);
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
		ps.println(
				name + "\t" + s + "\t" + tp + "\t" + tn + "\t" + fp + "\t" + fn + "\t" + train_time + "\t" + test_time);

	}

	public static double[][] getTopKSamples(double[][] dat, int k) {
		double[][] gsamples = new double[k][dat[0].length];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < dat[i].length; j++) {
				gsamples[i][j] = dat[i][j];
			}
		}
		return gsamples;
	}

	public static int[] getTopKLabels(int[] lab, int k) {
		int[] gsamples = new int[k];
		for (int i = 0; i < k; i++) {
			gsamples[i] = lab[i];
		}
		return gsamples;
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
