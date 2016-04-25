package com.yaming.json.internal;

import java.util.ArrayList;

import javax.lang.model.type.TypeMirror;

public class JsonTypeValid {
	private final static ArrayList<String> TYPE = new ArrayList<String>();
	
	static{
		TYPE.add("java.lang.Boolean");
		TYPE.add("java.lang.Integer");
		TYPE.add("java.lang.Long");
		TYPE.add("java.lang.Float");
		TYPE.add("java.lang.Double");
		TYPE.add("java.lang.String");
		TYPE.add("org.json.JSONArray");
		TYPE.add("org.json.JSONObject");
	}
	
	/**
	 * 
	 * @param type
	 * @param processingEnv 
	 * @return
	 */
	public static boolean valid(TypeMirror type){
		String className = Utils.getType(type);
		for(String t : TYPE){
			if(t.equals(className)){
				return false;
			}
		}
		return true;
	}
}
