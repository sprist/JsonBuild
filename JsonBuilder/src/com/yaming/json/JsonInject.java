package com.yaming.json;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

import com.yaming.json.internal.JsonBuilderProcessor;

public class JsonInject {
	static final Map<Class<?>, Method> INJECTORS = new LinkedHashMap<Class<?>, Method>();
	static final Method NO_OP = null;
	private static final String TAG = "JsonInject";
	private static boolean debug = false;

	private JsonInject() {
		// No instances.
	}

	/** Control whether debug logging is enabled. */
	public static void setDebug(boolean debug) {
		JsonInject.debug = debug;
	}
	/**
	   * Inject fields annotated with {@link com.yaming.json.JsonBuilder} in the specified {@code
	   * target} using the {@code source} {@link org.json.JSONObject} as the source.
	   *
	   * @param target Target class for field injection.
	   * @param source Bundle source on which extras will be looked up.
	   * @throws com.yaming.json.JsonInject.UnableToInjectException if injection could not be
	   * performed.
	   */
	public static void inject(Object target, JSONObject source) {
		inject(target, source, Finder.JSONOBJECT);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T opt(JSONObject obj, String key){
		if(obj == null || key == null)return null;
		final Object o = obj.opt(key);
		if(o == null) return null;
		return (T) o;
	}

	static void inject(Object target, Object source, Finder finder) {
		Class<?> targetClass = target.getClass();
		try {
			if (debug)
				Log.d(TAG,
						"Looking up json injector for "
								+ targetClass.getName());
			Method inject = findInjectorForClass(targetClass);
			if (inject != null) {
				inject.invoke(null, finder, target, source);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new UnableToInjectException("Unable to inject json for "
					+ target, e);
		}
	}

	private static Method findInjectorForClass(Class<?> cls)
			throws NoSuchMethodException {
		Method inject = INJECTORS.get(cls);
		if (inject != null) {
			if (debug)
				Log.d(TAG, "HIT: Cached in injector map.");
			return inject;
		}
		String clsName = cls.getName();
		if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
			if (debug)
				Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
			return NO_OP;
		}
		try {
			Class<?> injector = Class.forName(clsName
					+ JsonBuilderProcessor.SUFFIX);
			inject = injector.getMethod("inject", Finder.class, cls,
					JSONObject.class);
			if (debug)
				Log.d(TAG, "HIT: Class loaded injection class.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			if (debug)
				Log.d(TAG, "Not found. Trying superclass "
						+ cls.getSuperclass().getName());
			inject = findInjectorForClass(cls.getSuperclass());
		}
		INJECTORS.put(cls, inject);
		return inject;
	}

	public enum Finder {
		JSONOBJECT {
			@Override
			public Object opt(JSONObject source, String key) {
				return source == null ? null : source.opt(key);
			}
		};

		public abstract Object opt(JSONObject source, String key);
	}
	
	
	@SuppressWarnings("serial")
	public static class UnableToInjectException extends RuntimeException {

		UnableToInjectException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}