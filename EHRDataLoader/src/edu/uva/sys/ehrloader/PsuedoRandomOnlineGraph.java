package edu.uva.sys.ehrloader;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uva.libopt.numeric.*;
import smile.math.matrix.Matrix;
import smile.stat.distribution.SpikedMultivariateGaussianDistribution;
import xiong.hdstats.gaussian.GLassoEstimator;
import xiong.hdstats.gaussian.SampleCovarianceEstimator;
import xiong.hdstats.gaussian.SpikedUpperEstimator;
import xiong.hdstats.gaussian.online.GLassoIntialOnlineGraphEstimator;
import xiong.hdstats.gaussian.online.OnlineGraphEstimator;
import xiong.hdstats.gaussian.online.SampleInitialOnlineGraphEstimator;
import xiong.hdstats.gaussian.online.SpikedInitialOnlineGraphEstimator;
import xiong.hdstats.mat.TruncatedSVD;

public class PsuedoRandomOnlineGraph {
	public static ExecutorService ctp = Executors.newFixedThreadPool(4);

	public static void main(String[] args) {
		int[] pa = { 200, 400, 800, 1600 };
		int[] inits = { 20, 40, 80, 160, 320 };
		int[] sps = { 10, 20, 40, 80, 160 };
		for (final int p : pa) {
			for (final int init_size : inits) {
				for (final int sp : sps) {
					try {
						final PrintStream pps = new PrintStream(
								"C:/Users/xiongha/Desktop/sgraph/results-" + init_size + "-" + p + "-" + sp + ".txt");
						Runnable thread = new Runnable() {
							public void run() {
								_main(init_size, p, sp, pps);
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

	public static void _main(int init_size, int p, int s, PrintStream ps) {
		int[] est_density = new int[4];

		double[][] cov = new double[p][p];
		for (int i = 0; i < cov.length; i++) {
			for (int j = 0; j < cov.length; j++) {
				cov[i][j] = Math.pow(0.8, Math.abs(i - j));
			}
		}
		double[] mean = new double[p];
		double[][] theta_s = new Matrix(cov).inverse();

		double[][] ocov = TruncatedSVD.eigenTruncate(cov, s);
		double[][] icov = TruncatedSVD.eigenTruncatedInverse(cov, s);

		SpikedMultivariateGaussianDistribution gaussian = new SpikedMultivariateGaussianDistribution(mean, ocov);
		double[][] init = new double[init_size][p];
		double[][] allsamples = new double[init_size + 5000][p];
		for (int i = 0; i < init.length + 5000; i++) {
			double[] randv = gaussian.rand();
			for (int j = 0; j < mean.length; j++) {
				if (i < init.length)
					init[i][j] = randv[j];
				allsamples[i][j] = randv[j];
			}
		}
		SampleInitialOnlineGraphEstimator ogel = new SampleInitialOnlineGraphEstimator();
		SpikedInitialOnlineGraphEstimator osgel10 = new SpikedInitialOnlineGraphEstimator(10);
		SpikedInitialOnlineGraphEstimator osgel20 = new SpikedInitialOnlineGraphEstimator(20);
		SpikedInitialOnlineGraphEstimator osgel50 = new SpikedInitialOnlineGraphEstimator(50);
		SpikedInitialOnlineGraphEstimator osgel100 = new SpikedInitialOnlineGraphEstimator(100);
		GLassoIntialOnlineGraphEstimator oggel01 = new GLassoIntialOnlineGraphEstimator(0.1);
		GLassoIntialOnlineGraphEstimator oggel1 = new GLassoIntialOnlineGraphEstimator(1);
		GLassoIntialOnlineGraphEstimator oggel2 = new GLassoIntialOnlineGraphEstimator(2);
		GLassoIntialOnlineGraphEstimator oggel5 = new GLassoIntialOnlineGraphEstimator(5);
		GLassoIntialOnlineGraphEstimator oggel10 = new GLassoIntialOnlineGraphEstimator(10);
		HashMap<String, OnlineGraphEstimator> oges = new HashMap<String, OnlineGraphEstimator>();
		oges.put("SampleOnline", ogel);
		oges.put("SpikedOnline-10", osgel10);
		oges.put("SpikedOnline-20", osgel20);
		oges.put("SpikedOnline-50", osgel50);
		oges.put("SpikedOnline-100", osgel100);
		oges.put("GLassoOnline-0.1", oggel01);
		oges.put("GLassoOnline-1.0", oggel1);
		oges.put("GLassoOnline-2.0", oggel2);
		oges.put("GLassoOnline-5.0", oggel5);
		oges.put("GLassoOnline-10.0", oggel10);

		for (String name : oges.keySet()) {
			long start = System.nanoTime();
			oges.get(name).init(init);
			long end = System.nanoTime();
			ps.println(name + "\t initialization\t" + Utils.getErrorL2(icov, oges.get(name).getGraph()) + "\t"
					+ Utils.getErrorInf(icov, oges.get(name).getGraph()) + "\t" + (end - start));

		}

		for (int t = 1; t <= 10000; t++) {
			int index = init_size + t;
			double[] randov = allsamples[index - 1];
			for (String name : oges.keySet()) {
				long start = System.nanoTime();
				oges.get(name).update(index, randov);
				long end = System.nanoTime();
				if (t % 500 == 0)
					ps.println(name+"\t"+t + "\t updating\t" + Utils.getErrorL2(icov, oges.get(name).getGraph()) + "\t"
							+ Utils.getErrorInf(icov, oges.get(name).getGraph()) + "\t" + (end - start));
			}
			if (t % 500 == 0) {
				double[][] dat = new double[init_size + t][p];
				for (int i = 0; i < dat.length; i++) {
					for (int j = 0; j < dat[i].length; j++) {
						dat[i][j] = allsamples[i][j];
					}
				}
				long start = System.nanoTime();
				double[][] GLassoGraph01 = new GLassoEstimator(0.1).inverseCovariance(dat);
				long end = System.nanoTime();
				ps.println("GLasso-Off-0.1"+"\t"+t+" \t updating\t" + Utils.getErrorL2(icov, GLassoGraph01) + "\t"
						+ Utils.getErrorInf(icov, GLassoGraph01) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] GLassoGraph1 = new GLassoEstimator(1).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("GLasso-Off-1.0"+"\t"+t+" \t updating\t" + Utils.getErrorL2(icov, GLassoGraph1) + "\t"
						+ Utils.getErrorInf(icov, GLassoGraph1) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] GLassoGraph2 = new GLassoEstimator(2).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("GLasso-Off-2.0"+"\t"+t+" \t updating\t" + Utils.getErrorL2(icov, GLassoGraph2) + "\t"
						+ Utils.getErrorInf(icov, GLassoGraph2) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] GLassoGraph5 = new GLassoEstimator(5).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("GLasso-Off-5.0"+"\t"+t+" \t updating\t" +  Utils.getErrorL2(icov, GLassoGraph5) + "\t"
						+ Utils.getErrorInf(icov, GLassoGraph5) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] GLassoGraph10 = new GLassoEstimator(10).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("GLasso-Off-10.0"+"\t"+t+" \t updating\t" +  Utils.getErrorL2(icov, GLassoGraph10) + "\t"
						+ Utils.getErrorInf(icov, GLassoGraph10) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] SampleGraph = new SampleCovarianceEstimator().inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("Sample-off"+"\t"+t+" \t updating\t" +  Utils.getErrorL2(icov, SampleGraph) + "\t"
						+ Utils.getErrorInf(icov, SampleGraph) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] SpikedGraph10 = new SpikedUpperEstimator(10).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("Spiked-off-10"+"\t"+t+" \t updating\t" +  Utils.getErrorL2(icov, SpikedGraph10) + "\t"
						+ Utils.getErrorInf(icov, SpikedGraph10) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] SpikedGraph20 = new SpikedUpperEstimator(20).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("Spiked-off-20"+"\t"+t+" \t updating\t" +  Utils.getErrorL2(icov, SpikedGraph20) + "\t"
						+ Utils.getErrorInf(icov, SpikedGraph20) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] SpikedGraph50 = new SpikedUpperEstimator(50).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("Spiked-off-50"+"\t"+t+" \t updating\t" +  Utils.getErrorL2(icov, SpikedGraph50) + "\t"
						+ Utils.getErrorInf(icov, SpikedGraph50) + "\t" + (end - start));

				start = System.nanoTime();
				double[][] SpikedGraph100 = new SpikedUpperEstimator(100).inverseCovariance(dat);
				end = System.nanoTime();
				ps.println("Spiked-off-100"+"\t"+t+" \t updating\t" +  Utils.getErrorL2(icov, SpikedGraph100) + "\t"
						+ Utils.getErrorInf(icov, SpikedGraph100) + "\t" + (end - start));

			}
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
