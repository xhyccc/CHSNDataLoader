package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.FileReader;

public class ResultAnalysis {

	public static HashMap<String, String> nmap = new HashMap<String, String>();

	public static String fix3D(double n){
		BigDecimal   b   =   new   BigDecimal(n);  
		double   f1   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
		String sf=""+f1;
		if(sf.length()==4)
			return sf+"0";
		if(sf.length()==3)
			return sf+"00";
		else
			return sf;
	}
	
	public static void main(String[] args) {
		nmap.put("Daehr-5.000000000000001E-7", "daehr");
		// nmap.put("DDaehr-5.000000000000001E-7", "rec+d");
		nmap.put("LDA", "LDA&");
		nmap.put("Shrinkage-0.0", "DIAG&");
		nmap.put("Shrinkage-0.25", "Shrinkage ($\\beta=0.25$)&");
		nmap.put("Shrinkage-0.5", "Shrinkage ($\\beta=0.5$)&");
		nmap.put("Shrinkage-0.75", "Shrinkage ($\\beta=0.75$)&");
		nmap.put("Daehr-0.005", "Daehr ($\\lambda=0.005$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,1)), "Daehr ($\\lambda=0.005*0.5^1$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,2)), "Daehr ($\\lambda=0.005*0.5^2$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,3)), "Daehr ($\\lambda=0.005*0.5^3$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,4)), "Daehr ($\\lambda=0.005*0.5^4$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,5)), "Daehr ($\\lambda=0.005*0.5^5$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,6)), "Daehr ($\\lambda=0.005*0.5^6$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,7)), "Daehr ($\\lambda=0.005*0.5^7$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,8)), "Daehr ($\\lambda=0.005*0.5^8$)&");
		nmap.put("Daehr-"+(0.005*Math.pow(0.5,9)), "Daehr ($\\lambda=0.005*0.5^9$)&");

		// nmap.put("recoveredLDA", "recover");
		String[] names = { "LDA", "Shrinkage-0.0", 
							"Shrinkage-0.25",
							"Shrinkage-0.5",
							"Shrinkage-0.75",
							"Daehr-0.005",
							"Daehr-"+(0.005*0.5),
							"Daehr-"+(0.005*Math.pow(0.5,2)),
							"Daehr-"+(0.005*Math.pow(0.5,3)),
							"Daehr-"+(0.005*Math.pow(0.5,4)),
							"Daehr-"+(0.005*Math.pow(0.5,5)),
							"Daehr-"+(0.005*Math.pow(0.5,6)),
							"Daehr-"+(0.005*Math.pow(0.5,7)),
							"Daehr-"+(0.005*Math.pow(0.5,8)),
							"Daehr-"+(0.005*Math.pow(0.5,9)),

};	
		for(int te_size:new int[]{200,1000})
		for (int t = 50; t <= 350; t += 50) {
	//		System.out.println("\\begin{table}\n\\caption{Performance Comparison with Training Set:"+t+"$\\times$ 2, Testing Set: "+te_size+"$\\times$2}");
	//		System.out.println("\\footnotesize\n\\centering\n\\begin{tabular}{*{5}{l}}\n\\toprule");	
	//		System.out.println(" & Accuracy & F1-Score & Sensitivity & Specificity\\\\");

			
			for (int days = 30; days <= 90; days += 30) {
				int t_size = t;
				try {

					HashMap<String, Double> acc = new HashMap<String, Double>();
					HashMap<String, Double> f1 = new HashMap<String, Double>();
					HashMap<String, Double> sen = new HashMap<String, Double>();
					HashMap<String, Double> spe = new HashMap<String, Double>();
					HashMap<String, Integer> counts = new HashMap<String, Integer>();

					BufferedReader br = new BufferedReader(
							new FileReader("/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/accuracy-oLDA-"
									+ t_size + "-" + te_size + "-" + days + ".txt"));
					String ln = br.readLine();
					while (ln != null) {
						String[] lns = ln.split("\t");
						if (lns.length > 1) {
							if (!acc.containsKey(lns[0])) {
								acc.put(lns[0], 0.0);
								f1.put(lns[0], 0.0);
								sen.put(lns[0], 0.0);
								spe.put(lns[0], 0.0);
								counts.put(lns[0], 0);
							}
							double _tp=Integer.parseInt(lns[1]);
							double _tn=Integer.parseInt(lns[2]);
							double _fp=Integer.parseInt(lns[3]);
							double _fn=Integer.parseInt(lns[4]);
							acc.put(lns[0], acc.get(lns[0])+((_tp+_tn)/(_tp+_tn+_fp+_fn)));
							f1.put(lns[0], f1.get(lns[0])+((2*_tp)/(2*_tp+_fp+_fn)));
							sen.put(lns[0], sen.get(lns[0])+((_tp)/(_tp+_fn)));
							spe.put(lns[0], spe.get(lns[0])+((_tn)/(_tn+_fp)));
							counts.put(lns[0], counts.get(lns[0])+1);
						}
						ln = br.readLine();
					}
					br.close();

					
					HashMap<String, Double> acc_std = new HashMap<String, Double>();
					HashMap<String, Double> f1_std = new HashMap<String, Double>();
					HashMap<String, Double> sen_std = new HashMap<String, Double>();
					HashMap<String, Double> spe_std = new HashMap<String, Double>();
					
					br = new BufferedReader(
							new FileReader("/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/accuracy-oLDA-"
									+ t_size + "-" + te_size + "-" + days + ".txt"));
					ln = br.readLine();
					while (ln != null) {
						String[] lns = ln.split("\t");
						if (lns.length > 1) {
							if (!acc_std.containsKey(lns[0])) {
								acc_std.put(lns[0], 0.0);
								f1_std.put(lns[0], 0.0);
								sen_std.put(lns[0], 0.0);
								spe_std.put(lns[0], 0.0);
							}
							double _tp=Integer.parseInt(lns[1]);
							double _tn=Integer.parseInt(lns[2]);
							double _fp=Integer.parseInt(lns[3]);
							double _fn=Integer.parseInt(lns[4]);
							acc_std.put(lns[0], acc_std.get(lns[0])+Math.pow((acc.get(lns[0])/counts.get(lns[0])-((_tp+_tn)/(_tp+_tn+_fp+_fn))),2));
							f1_std.put(lns[0], f1_std.get(lns[0])+Math.pow((f1.get(lns[0])/counts.get(lns[0])-((2*_tp)/(2*_tp+_fp+_fn))),2));
							sen_std.put(lns[0], sen_std.get(lns[0])+Math.pow((sen.get(lns[0])/counts.get(lns[0])-((_tp)/(_tp+_fn))),2));
							spe_std.put(lns[0], spe_std.get(lns[0])+Math.pow((spe.get(lns[0])/counts.get(lns[0])-((_tn)/(_tn+_fp))),2));
						}
						ln = br.readLine();
					}
					br.close();

					
				//	System.out.println("\\hline\\multicolumn{5}{c}{  Days in Advance: "+days+"}\\\\\\hline");
					for (String n : names) {
				//	System.out.println(nmap.get(n) + 
				//			fix3D(acc.get(n)/counts.get(n))+" $\\pm$ "+fix3D(Math.sqrt(acc_std.get(n)/counts.get(n))) + "&"+ 
				//			fix3D(f1.get(n)/counts.get(n)) +" $\\pm$ "+fix3D(Math.sqrt(f1_std.get(n)/counts.get(n))) +  "&" + 
				//			fix3D(sen.get(n)/counts.get(n)) +" $\\pm$ "+fix3D(Math.sqrt(sen_std.get(n)/counts.get(n))) +  "&" +
				//			fix3D(spe.get(n)/counts.get(n)) +" $\\pm$ "+fix3D(Math.sqrt(spe_std.get(n)/counts.get(n)))+"\\\\");
						
						System.out.println(nmap.get(n) + "\t"+
								fix3D(sen.get(n)/counts.get(n)) +"\t"+
								fix3D(spe.get(n)/counts.get(n)));

					}
					

				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}

			}
			
		//	System.out.println("\\bottomrule\n\\end{tabular}\n\\end{table}");	
		//	System.out.println();

		}
	}
}
