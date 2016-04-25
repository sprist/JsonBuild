package com.yaming.json.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

final class JsonInjection {
  private final String key;
  private final Set<FieldBinding> fieldBindings = new LinkedHashSet<FieldBinding>();

  JsonInjection(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public Collection<FieldBinding> getFieldBindings() {
    return fieldBindings;
  }

  public void addFieldBinding(FieldBinding fieldBinding) {
    fieldBindings.add(fieldBinding);
  }
}