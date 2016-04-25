package com.yaming.json.internal;

import javax.lang.model.type.TypeMirror;

public class Utils {
	public static String getType(TypeMirror type) {
		if (type.getKind().isPrimitive()) {
			// Get wrapper for primitive types
			switch (type.getKind()) {
			case BOOLEAN:
				return "java.lang.Boolean";
			case INT:
				return "java.lang.Integer";
			case LONG:
				return "java.lang.Long";
			case FLOAT:
				return "java.lang.Float";
			case DOUBLE:
				return "java.lang.Double";
			default:
				// Shouldn't happen
				throw new RuntimeException();
			}
		} else {
			return type.toString();
		}
	}
}
