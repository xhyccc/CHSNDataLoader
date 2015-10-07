package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.FileReader;

public class ResultAnalysis {
	
	public static HashMap<String,String> nmap=new HashMap<String,String>();

	public static void main(String[] args) {
		nmap.put("Daehr-5.000000000000001E-7", "daehr");
	//	nmap.put("DDaehr-5.000000000000001E-7", "rec+d");
		nmap.put("LDA", "LDA");
	//	nmap.put("recoveredLDA", "recover");
		String[] names={"LDA","recoveredLDA","Daehr-5.000000000000001E-7","DDaehr-5.000000000000001E-7"};
		for (int t = 200; t <= 3000; t += 200) {
				for (int days = 30; days <= 90; days += 30) {
					int t_size = t;
					int te_size = 200;
					try {
						BufferedReader br = new BufferedReader(
								new FileReader("/Users/bertrandx/Box Sync/CHSN_pattern mining/Jinghe/accuracy-LDA-"
										+ t_size + "-" + te_size + "-" + days + ".txt"));
						HashMap<String,Integer> tp=new HashMap<String,Integer>();
						HashMap<String,Integer> tn=new HashMap<String,Integer>();
						HashMap<String,Integer> fp=new HashMap<String,Integer>();
						HashMap<String,Integer> fn=new HashMap<String,Integer>();

						String ln=br.readLine();
						while(ln!=null){
							String[] lns=ln.split("\t");
							if(!tp.containsKey(lns[0])){
								tp.put(lns[0], 0);
								tn.put(lns[0], 0);
								fp.put(lns[0], 0);
								fn.put(lns[0], 0);
							}
							tp.put(lns[0],tp.get(lns[0])+Integer.parseInt(lns[1]));
							tn.put(lns[0],tn.get(lns[0])+Integer.parseInt(lns[2]));
							fp.put(lns[0],fp.get(lns[0])+Integer.parseInt(lns[3]));
							fn.put(lns[0],fn.get(lns[0])+Integer.parseInt(lns[4]));

							ln=br.readLine();
						}


					System.out.println("training set:"+t+"\t testing set:"+200+"\t days:"+days);
					Set<String> ns=fp.keySet();
					for(String n:ns){
						double pf1=(double)(2.0*tp.get(n)/(double)(2.0*tp.get(n)+fp.get(n)+fn.get(n)));
						double nf1=(double)(2.0*tn.get(n)/(double)(2.0*tn.get(n)+fp.get(n)+fn.get(n)));
						double acc=((double)(tp.get(n)+tn.get(n))/(double)(tp.get(n)+tn.get(n)+fp.get(n)+fn.get(n)));
						double sens=((double)(tp.get(n))/(double)(tp.get(n)+fn.get(n)));
						double spec=((double)(tn.get(n))/(double)(tn.get(n)+fp.get(n)));

						System.out.println(n+"\t"+acc+"\t"+pf1+"\t"+nf1+"\t"+(pf1+nf1)+"\t"+sens+"\t"+spec);
					}
					System.out.println();

					br.close();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					}

				}
			}
		}
	}

