package com.yaming.json.internal;

import javax.lang.model.type.TypeMirror;

public final class FieldBinding implements Binding {
	private String name;
	private TypeMirror type;

	public FieldBinding(String name, TypeMirror type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public TypeMirror getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(TypeMirror type) {
		this.type = type;
	}

	@Override
	public String getDescription() {
		return "field '" + name + "'";
	}

}
