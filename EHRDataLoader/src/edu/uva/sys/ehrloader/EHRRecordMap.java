package edu.uva.sys.ehrloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class EHRRecordMap {
	
	public HashMap<String,String> codeMap=new HashMap<String,String>();
	public HashMap<String,String> nameMap=new HashMap<String,String>();
	
	public EHRRecordMap(String fpath){
		try {
			BufferedReader br=new BufferedReader(new FileReader(fpath));
			String ln=br.readLine();
			while(ln!=null){
				String cname=ln.split(" ")[0];
				nameMap.put(cname, ln.replaceAll(" ","_"));
				System.out.println(cname+"\t"+nameMap.get(cname));

				ln=br.readLine();
				String[] codes=ln.split(" ");
				for(String code:codes){
					codeMap.put(code, cname);
					if(!code.isEmpty() && !code.equals(" "))
						System.out.println("mapping: "+code+"\t"+cname);
				}
				ln=br.readLine();
				ln=br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		EHRRecordMap map = new EHRRecordMap(
				"/Users/xiongha/Box Sync/CHSN_pattern mining/Jinghe/mapping.txt");


	}

}
