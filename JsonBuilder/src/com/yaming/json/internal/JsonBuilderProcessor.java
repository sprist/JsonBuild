package com.yaming.json.internal;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import com.yaming.json.JsonBuilder;

@SupportedAnnotationTypes(value = { "*" })
public class JsonBuilderProcessor extends AbstractProcessor {
	public static final String SUFFIX = "$$JsonBuilder";

	private Elements elementUtils;
	private Types typeUtils;
	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();
		filer = env.getFiler();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> supportTypes = new LinkedHashSet<String>();
		supportTypes.add(JsonBuilder.class.getCanonicalName());
		return supportTypes;
	}

	@Override
	public boolean process(Set<? extends TypeElement> elements,
			RoundEnvironment env) {
		Map<TypeElement, JsonInjector> targetClassMap = findAndParseTargets(env);

		for (Map.Entry<TypeElement, JsonInjector> entry : targetClassMap
				.entrySet()) {
			TypeElement typeElement = entry.getKey();
			JsonInjector jsonInjector = entry.getValue();

			try {
				JavaFileObject jfo = filer.createSourceFile(
						jsonInjector.getFqcn(), typeElement);
				Writer writer = jfo.openWriter();
				writer.write(jsonInjector.brewJava());
				writer.flush();
				writer.close();
			} catch (IOException e) {
				error(typeElement, "Unable to write injector for type %s: %s",
						typeElement, e.getMessage());
			}
		}
		return true;
	}

	private Map<TypeElement, JsonInjector> findAndParseTargets(
			RoundEnvironment env) {
		Map<TypeElement, JsonInjector> targetClassMap = new LinkedHashMap<TypeElement, JsonInjector>();
		Set<TypeMirror> erasedTargetTypes = new LinkedHashSet<TypeMirror>();

		// Process each @InjectJson elements.
		for (Element element : env.getElementsAnnotatedWith(JsonBuilder.class)) {
			try {
				parseInjectJson(element, targetClassMap, erasedTargetTypes);
			} catch (Exception e) {
				StringWriter stackTrace = new StringWriter();
				e.printStackTrace(new PrintWriter(stackTrace));

				error(element,
						"Unable to generate json injector for @JsonBuilder.\n\n%s",
						stackTrace.toString());
			}
		}

		// TODO add find parent

		return targetClassMap;
	}

	private void parseInjectJson(Element element,
			Map<TypeElement, JsonInjector> targetClassMap,
			Set<TypeMirror> erasedTargetTypes) {
		boolean hasError = false;
		TypeElement enclosingElement = (TypeElement) element
				.getEnclosingElement();

		// Verify common generated code restrictions.
		hasError |= isValidForGeneratedCode(JsonBuilder.class, "fields",
				element);

		
		if (hasError) {
			return;
		}
		
		TypeMirror type = element.asType();
		// valid class type
		hasError = JsonTypeValid.valid(type);
		
		if(hasError){
			error(element, "@%s clasa type not support ,must be Primitive  JsonaArray or JsonObject . ",
					Utils.getType(type));
			return;
		}

		
		// Assemble information on the injection point.
		String name = element.getSimpleName().toString();
		String key = element.getAnnotation(JsonBuilder.class).value();

		JsonInjector jsonInjector = getOrCreateTargetClass(targetClassMap,
				enclosingElement);
		jsonInjector.addField(isEmpty(key) ? name : key, name, type);

		// Add the type-erased version to the valid injection targets set.
		TypeMirror erasedTargetType = typeUtils.erasure(enclosingElement
				.asType());
		erasedTargetTypes.add(erasedTargetType);
	}
	
	 /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

	private JsonInjector getOrCreateTargetClass(
			Map<TypeElement, JsonInjector> targetClassMap,
			TypeElement enclosingElement) {
		JsonInjector jsonInjector = targetClassMap.get(enclosingElement);
		if (jsonInjector == null) {
			String targetType = enclosingElement.getQualifiedName().toString();
			String classPackage = getPackageName(enclosingElement);
			String className = getClassName(enclosingElement, classPackage)
					+ SUFFIX;

			jsonInjector = new JsonInjector(classPackage, className,
					targetType);
			targetClassMap.put(enclosingElement, jsonInjector);
		}
		return jsonInjector;
	}

	private boolean isValidForGeneratedCode(
			Class<? extends Annotation> annotationClass, String targetThing,
			Element element) {
		boolean hasError = false;
		TypeElement enclosingElement = (TypeElement) element
				.getEnclosingElement();

		// Verify method modifiers.
		Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
			error(element, "@%s %s must not be private or static. (%s.%s)",
					annotationClass.getSimpleName(), targetThing,
					enclosingElement.getQualifiedName(),
					element.getSimpleName());
			hasError = true;
		}

		// Verify containing type.
		if (enclosingElement.getKind() != CLASS) {
			error(enclosingElement,
					"@%s %s may only be contained in classes. (%s.%s)",
					annotationClass.getSimpleName(), targetThing,
					enclosingElement.getQualifiedName(),
					element.getSimpleName());
			hasError = true;
		}

		// Verify containing class visibility is not private.
		if (enclosingElement.getModifiers().contains(PRIVATE)) {
			error(enclosingElement,
					"@%s %s may not be contained in private classes. (%s.%s)",
					annotationClass.getSimpleName(), targetThing,
					enclosingElement.getQualifiedName(),
					element.getSimpleName());
			hasError = true;
		}

		return hasError;
	}

	@SuppressWarnings("unused")
	private boolean isAnnotated(Element element, String annotationName) {
		if (element != null) {
			for (AnnotationMirror annotationMirror : element
					.getAnnotationMirrors()) {
				if (annotationMirror.getAnnotationType().asElement().toString()
						.equals(annotationName)) {
					return true;
				}
			}
		}
		return false;
	}


	private static String getClassName(TypeElement type, String packageName) {
		int packageLen = packageName.length() + 1;
		return type.getQualifiedName().toString().substring(packageLen)
				.replace('.', '$');
	}

	private void error(Element element, String message, Object... args) {
		processingEnv.getMessager().printMessage(ERROR,
				String.format(message, args), element);
	}

	private String getPackageName(TypeElement type) {
		return elementUtils.getPackageOf(type).getQualifiedName().toString();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
