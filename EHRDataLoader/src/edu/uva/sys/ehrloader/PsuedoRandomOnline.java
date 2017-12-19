package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uva.libopt.numeric.*;
import smile.math.matrix.Matrix;
import smile.stat.distribution.SpikedMultivariateGaussianDistribution;
import xiong.hdstats.gaussian.online.OnlineGraphEstimator;

public class PsuedoRandomOnline {
	public static ExecutorService ctp = Executors.newFixedThreadPool(4);

	public static void main(String[] args) {
		int[] pa = { 200, };
		double[] is = { 1.0, 2.0 };
		double[] od = { 0.01, 0.02, 0.03, };
		for (final int p : pa) {
			for (double o : od) {
				for (double i : is) {
					final int init_size = (int) (i * p);
					final int original_density = (int) (o * p * p);
					try {
						final PrintStream pps = new PrintStream("C:/Users/xiongha/Desktop/sgraph/results-" + init_size
								+ "-" + original_density + "-" + p + ".txt");
						Runnable thread = new Runnable() {
							public void run() {
								_main(init_size, p, original_density, pps);
							}
						};
						ctp.execute(thread);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		ctp.shutdown();
	}

	public static void _main(int init_size, int p, int origin_density, PrintStream ps) {
		int[] est_density = new int[4];
		for (int i = 0; i < 4; i++) {
			est_density[i] = origin_density / 2 * (i + 1);
		}

		double[][] cov = new double[p][p];
		for (int i = 0; i < cov.length; i++) {
			for (int j = 0; j < cov.length; j++) {
				cov[i][j] = Math.pow(0.8, Math.abs(i - j));
			}
		}
		double[] mean = new double[p];
		double[][] theta_s = new Matrix(cov).inverse();

		double[][] sparseICov = new Matrix(theta_s).inverse();
		double density = Utils.getLxNorm(theta_s, Utils.L0);
		SpikedMultivariateGaussianDistribution gaussian = new SpikedMultivariateGaussianDistribution(mean, sparseICov);
		double[][] init = new double[init_size][p];
		for (int i = 0; i < init.length; i++) {
			double[] randv = gaussian.rand();
			for (int j = 0; j < mean.length; j++) {
				init[i][j] = randv[j];
			}
		}
		HashMap<Integer, OnlineGraphEstimator> algorithms = new HashMap<Integer, OnlineGraphEstimator>();
		for (int ed : est_density) {
			OnlineGraphEstimator alg = new OnlineGraphEstimator();
			alg.init(init);
			algorithms.put(ed, alg);
		}
		OnlineGraphEstimator og = new OnlineGraphEstimator();

		og.init(init);

		// for(int i=0;i<osg.graph.length;i++){
		// for(int j=0;j<osg.graph.length;j++){
		// System.out.print(osg.graph[i][j]+"\t");
		// }
		// System.out.println();
		// }
		// System.out.println("sep");
		// for(int i=0;i<osg.graph.length;i++){
		// for(int j=0;j<osg.graph.length;j++){
		// System.out.print(theta_s[i][j]+"\t");
		// }
		// System.out.println();
		// }

		for (int ed : est_density) {
			OnlineGraphEstimator osg = algorithms.get(ed);
			double overlaps = overlap(osg.graph, theta_s);
			double sprecision = overlaps / osg.getL0Norm();
			double srecall = overlaps / density;
			double f1s = 2 * sprecision * srecall / (sprecision + srecall);
			ps.println("OSG-" + ed + "\t" + 0 + "\t" + sprecision + "\t" + srecall + "\t" + f1s + "\t"
					+ Utils.getErrorInf(osg.graph, theta_s));
		}
		double overlapg = overlap(og.graph, theta_s);
		double dprecision = overlapg / og.getL0Norm();
		double drecall = overlapg / density;
		double f1d = 2 * dprecision * drecall / (dprecision + drecall);
		ps.println("OI\t" + 0 + "\t" + dprecision + "\t" + drecall + "\t" + f1d + "\t"
				+ Utils.getErrorInf(og.graph, theta_s));

		for (int t = 1; t <= 200; t++) {
			int index = init_size + t;
			double[] randov = gaussian.rand();

			for (int ed : est_density) {
				OnlineGraphEstimator osg = algorithms.get(ed);
				osg.update(index, randov);
				double overlaps = overlap(osg.graph, theta_s);
				double sprecision = overlaps / osg.getL0Norm();
				double srecall = overlaps / density;
				double f1s = 2 * sprecision * srecall / (sprecision + srecall);
				ps.println("OSG-" + ed + "\t" + t + "\t" + sprecision + "\t" + srecall + "\t" + f1s + "\t"
						+ Utils.getErrorInf(osg.graph, theta_s));
			}
			og.update(index, randov);
			overlapg = overlap(og.graph, theta_s);
			dprecision = overlapg / og.getL0Norm();
			drecall = overlapg / density;
			f1d = 2 * dprecision * drecall / (dprecision + drecall);
			ps.println("OI\t" + t + "\t" + dprecision + "\t" + drecall + "\t" + f1d + "\t"
					+ Utils.getErrorInf(og.graph, theta_s));
		}
		ps.close();
	}

	public static int overlap(double[][] m1, double[][] m2) {
		int overlap = 0;
		for (int i = 0; i < m1.length; i++) {
			for (int j = 0; j < m1.length; j++) {
				if (m1[i][j] != 0 && m2[i][j] != 0)
					overlap++;
			}
		}
		return overlap;
	}

}
