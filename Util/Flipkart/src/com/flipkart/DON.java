package com.flipkart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.flipkart.error.DOMError;
import com.flipkart.validtype.DOMValidType;


public class DON {

	static HashMap<String, HashMap<String,String>> classMap=new HashMap<String, HashMap<String,String>>();
	
	static HashMap<String, String> objMap=new HashMap<String, String>();
	
	static List<String> curObjMap=new ArrayList<String>();
	
	static List<DOMError> domErrors=new ArrayList<DOMError>();
	
	public static void processDOM(List<String> classDef)
	{
		
		for (Iterator iterator = classDef.iterator(); iterator.hasNext();) {
			int count=0;
			String string = (String) iterator.next();
			String []clas=string.split("=");
			String []classDetail=clas[1].split(",");
			HashMap<String, String> map=new HashMap<String, String>();
			for (int i = 0; i < classDetail.length; i++) {
				String []s=classDetail[i].split(":");
				if(s[1].trim().equals(DOMValidType.INT.getTypeName()) || s[1].trim().equals(DOMValidType.STRING.getTypeName()))
				{
					map.put(s[0].trim(), s[1].trim());
					if(s[0].trim().contains("?"))
						count++;
				}
				else
				{
					if(s[0].trim().contains("?"))
						count++;
					map.put(s[0].trim(), s[1].trim());
				}
					
			}
			map.put("OPTIONALNUM", String.valueOf(count));
			classMap.put(clas[0].trim(), map);
		}
		
		
	}
	
	 
	private static void processError(List<String> obj) {
		
		for (Iterator iterator = obj.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			String []ob=string.split("=");
			String []obDetail=ob[1].split(";");
			
			String clasname=obDetail[0].trim();
			HashMap<String, String> map=null;
			if(classMap.containsKey(clasname))
			{
				map=classMap.get(clasname);
				objMap.put(ob[0].trim(),clasname);
			}
			else
			{
				DOMError.MISSING_FIELD.setErrorMessage(clasname);
				domErrors.add(DOMError.MISSING_FIELD);
				return;
			}
			
			String []objt=obDetail[1].split(",");
			
			int size=map.size()-1-Integer.parseInt(map.get("OPTIONALNUM"));
			
			if(objt.length>size)
			{
				DOMError.EXTRA_FIELD.setErrorMessage(ob[0].trim());
				domErrors.add(DOMError.EXTRA_FIELD);
				return;
			}
			if(objt.length<size)
			{
				DOMError.MISSING_FIELD.setErrorMessage(ob[0].trim());
				domErrors.add(DOMError.MISSING_FIELD);
			}
			
			for (int i = 0; i < objt.length; i++) {
				String []s=objt[i].split(":");
				
				if(map.containsKey(s[0].trim())){
				
				if(map.get(s[0].trim()).equals(DOMValidType.INT.getTypeName()) || map.get(s[0].trim()).equals(DOMValidType.STRING.getTypeName()))
				{
					
				}
				else
				{
					curObjMap.add(s[1].trim());
				}
				}
				else
				{
					DOMError.EXTRA_FIELD.setErrorMessage(s[0].trim());
					domErrors.add(DOMError.EXTRA_FIELD);
				}
					
			}
			
		}
		for (Iterator iterator = curObjMap.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if(!objMap.containsKey(string))
			{
				DOMError.MISSING_FIELD.setErrorMessage(string);
				domErrors.add(DOMError.MISSING_FIELD);
			}
			
		}
		
		
	}


	public static void main(String[] args) {
		Scanner scanner=new Scanner(System.in);
		String str=null;
		
		List<String> list=new ArrayList<String>();
		
		do{
		str=scanner.nextLine();
		if(!str.equals("#"))
		list.add(str.trim());
		}while(!str.equals("#"));
		
		List<String> obj=new ArrayList<String>();
		processDOM(list);
		while (true) {
			str=scanner.nextLine();
			if(str.equalsIgnoreCase("stop"))
				break;
			obj.add(str.trim());
			
		}
		processError(obj);
 		printError();
		
	}


	private static void printError() {
		
		if(domErrors.size()==0)
			System.out.println("No errors");
		
		for (Iterator iterator = domErrors.iterator(); iterator.hasNext();) {
			
			DOMError domError=(DOMError)iterator.next();
			System.out.println(domError.getErrorMessage());
		}
		
	}
}
