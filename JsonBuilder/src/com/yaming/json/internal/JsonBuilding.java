package com.yaming.json.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class JsonBuilding {
	private final String key;
	private final Set<FieldBinding> fieldBindings = new LinkedHashSet<FieldBinding>();
	
	public JsonBuilding(String key){
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public Collection<FieldBinding> getFieldBindings() {
		return fieldBindings;
	}
	
	public void addFieldBinding(FieldBinding fieldBinding){
		fieldBindings.add(fieldBinding);
	}
}
