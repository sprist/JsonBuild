package com.yaming.json.internal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.type.TypeMirror;

public class JsonInjector {
	private final Map<String, JsonInjection> injectionMap = new LinkedHashMap<String, JsonInjection>();
	private final String classPackage;
	private final String className;
	private final String targetClass;

	JsonInjector(String classPackage, String className, String targetClass) {
		this.classPackage = classPackage;
		this.className = className;
		this.targetClass = targetClass;
	}

	static void emitCast(StringBuilder builder, TypeMirror fieldType) {
		if(Utils.getType(fieldType).equals("java.lang.Boolean")){
			builder.append("ContertUtils.toBoolean");
		}else if(Utils.getType(fieldType).equals("java.lang.Integer")){
			builder.append("ContertUtils.toInteger");
		}else if(Utils.getType(fieldType).equals("java.lang.Long")){
			builder.append("ContertUtils.toLong");
		}else if(Utils.getType(fieldType).equals("java.lang.Float")){
			builder.append("ContertUtils.toFloat");
		}else if(Utils.getType(fieldType).equals("java.lang.Double")){
			builder.append("ContertUtils.toDouble");
		}else if(Utils.getType(fieldType).equals("java.lang.String")){
			builder.append("ContertUtils.toString");
		}else{
			builder.append('(').append(Utils.getType(fieldType)).append(") ");
		}
	}

	

	void addField(String key, String name, TypeMirror type) {
		getOrCreateJsonBinding(key).addFieldBinding(
				new FieldBinding(name, type));
	}

	private JsonInjection getOrCreateJsonBinding(String key) {
		JsonInjection extraInjection = injectionMap.get(key);
		if (extraInjection == null) {
			extraInjection = new JsonInjection(key);
			injectionMap.put(key, extraInjection);
		}
		return extraInjection;
	}

	String getFqcn() {
		return classPackage + "." + className;
	}

	String brewJava() {
		StringBuilder builder = new StringBuilder();
		builder.append("// Generated code from JsonInject. Do not modify!\n");
		builder.append("package ").append(classPackage).append(";\n\n");
		builder.append("import com.yaming.json.JsonInject.Finder;\n\n");
		builder.append("import com.yaming.json.ContertUtils;\n\n");
		builder.append("import org.json.JSONObject;\n\n");
		builder.append("public class ").append(className).append(" {\n");
		emitInject(builder);
		builder.append("}\n");
		return builder.toString();
	}

	private void emitInject(StringBuilder builder) {
		builder.append("  public static void inject(Finder finder, final ")
				.append(targetClass).append(" target, JSONObject source) {\n");


		// Local variable in which all extras will be temporarily stored.
		builder.append("    Object object;\n");

		// Loop over each extras injection and emit it.
		for (JsonInjection injection : injectionMap.values()) {
			emitExtraInjection(builder, injection);
		}

		builder.append("  }\n");
	}

	private void emitExtraInjection(StringBuilder builder,
			JsonInjection injection) {
		builder.append("    object = finder.opt(source, \"")
				.append(injection.getKey()).append("\");\n");

		// an optional extra, wrap it in a check to keep original value, if
		// any
		builder.append("    if (object != null) {\n");
		// todo better indentation for this code
		emitFieldBindings(builder, injection);
		builder.append("    }\n");
	}

	private void emitFieldBindings(StringBuilder builder,
			JsonInjection injection) {
		Collection<FieldBinding> fieldBindings = injection.getFieldBindings();
		if (fieldBindings.isEmpty()) {
			return;
		}

		for (FieldBinding fieldBinding : fieldBindings) {
			builder.append("    target.").append(fieldBinding.getName())
					.append(" = ");

			emitCast(builder, fieldBinding.getType());
			builder.append("(object);\n");
		}
	}

}
