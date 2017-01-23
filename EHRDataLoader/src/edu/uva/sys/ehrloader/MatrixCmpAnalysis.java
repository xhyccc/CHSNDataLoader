package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.FileReader;

public class MatrixCmpAnalysis {

	public static HashMap<String, String> nmap = new HashMap<String, String>();

	public static String fix3D(double n) {
		BigDecimal b = new BigDecimal(n);
		double f1 = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
		String sf = "" + f1;
		if (sf.length() == 4)
			return sf + "0";
		if (sf.length() == 3)
			return sf + "00";
		else
			return sf;
	}

	public static void main(String[] args) {
		// for(int te_size:new int[]{200,1000})
		boolean headPrinted = false;
		for (int t = 50; t <= 250; t += 50) {
			// System.out.println("\\begin{table}\n\\caption{Performance
			// Comparison with Training Set:"+t+"$\\times$ 2, Testing Set:
			// "+te_size+"$\\times$2}");
			// System.out.println("\\footnotesize\n\\centering\n\\begin{tabular}{*{5}{l}}\n\\toprule");
			// System.out.println(" & Accuracy & F1-Score & Sensitivity &
			// Specificity\\\\");

			// for (int days = 30; days <= 90; days += 30)
			{
				// int t_size = t;
				try {

					HashMap<String, List<Double>> l1error = new HashMap<String, List<Double>>();
					HashMap<String, List<Double>> fnerror = new HashMap<String, List<Double>>();

					BufferedReader br = new BufferedReader(new FileReader(
							"/Users/xiongha/Dropbox/technical-reports/report-3/error-est3/error-precision-matrix－" + t
									+ ".txt"));
					String ln = br.readLine();
					while (ln != null) {
						String[] lns = ln.split("\t");
						if (lns.length > 1) {
							if (!l1error.containsKey(lns[0])) {
								l1error.put(lns[0], new ArrayList<Double>());
								fnerror.put(lns[0], new ArrayList<Double>());
							}
							double _e1 = Double.parseDouble(lns[1]);
							double _ef = Double.parseDouble(lns[2]);
							l1error.get(lns[0]).add(_e1);
							fnerror.get(lns[0]).add((_ef));
						}
						ln = br.readLine();
					}
					br.close();
					List<String> names = new ArrayList<String>(l1error.keySet());
					Collections.sort(names);
					if (headPrinted == false){
						for (String name : names)
							if (!name.startsWith("sample"))
								System.out.print("\t" + name);
						System.out.println();	
					}
					headPrinted = true;
					System.out.print(t);

					for (String key1 : names) {
						for (String key2 : names) {
							int size = l1error.get(key1).size();
							double l1e = 0;
							double fne = 0;
							for (int i = 0; i < size; i++) {
								l1e += (l1error.get(key1).get(i) - l1error.get(key2).get(i));///l1error.get(key1).get(i);
								fne += (fnerror.get(key1).get(i) - fnerror.get(key2).get(i));///fnerror.get(key1).get(i);
							//	l1e += l1error.get(key2).get(i);///l1error.get(key1).get(i);
							//	fne += fnerror.get(key2).get(i);///fnerror.get(key1).get(i);
							}
							
							double t1e = 0;
							double tne = 0;
							for (int i = 0; i < size; i++) {
								t1e += l1error.get(key1).get(i);///l1error.get(key1).get(i);
								tne += fnerror.get(key1).get(i) ;///fnerror.get(key1).get(i);
							//	l1e += l1error.get(key2).get(i);///l1error.get(key1).get(i);
							//	fne += fnerror.get(key2).get(i);///fnerror.get(key1).get(i);
							}

							l1e = (l1e) / size;
							fne = (fne) / size;
							t1e = (t1e) / size;
							tne = (tne) / size;
							if (key1.startsWith("sample") && !key2.startsWith("sample"))
								System.out.print("\t" + fne);
						}
					}
					System.out.println();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}

			}
//&&key1.replaceAll("glasso", "").equals(key2.replaceAll("nonsparse", ""))
			// System.out.println("\\bottomrule\n\\end{tabular}\n\\end{table}");
			// System.out.println();

		}
	}
}
