package uk.gov.ida.shared.dropwizard.jade;

import de.neuland.jade4j.model.JadeModel;
import io.dropwizard.views.View;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class JadeModelFactory {

    public static final String GET_METHOD_NAME_PREFIX = "get";

    public JadeModel createModel(final View view) {
        final JadeModel jadeModel = new JadeModel(new HashMap<>() {});
        addMethodsFromClassHierarchy(view, jadeModel);
        return jadeModel;
    }

    private void addMethodsFromClassHierarchy(
            final View view,
            final JadeModel jadeModel) {

        Class<?> clazz = view.getClass();
        while (!clazz.equals(View.class)) {
            addMethodsFromClass(jadeModel, view, clazz);
            clazz = clazz.getSuperclass();
        }
    }

    private void addMethodsFromClass(
            final JadeModel jadeModel,
            final View view,
            final Class<?> clazz) {

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (isGetterMethod(method)) {
                addMethod(jadeModel, view, method);
            }
        }
    }

    private void addMethod(
            final JadeModel jadeModel,
            final View view,
            final Method method) {

        try {
            final String key = WordUtils.uncapitalize(stripGetPrefix(method.getName()));
            jadeModel.put(key, method.invoke(view));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error: Failed to access public getter.");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private String stripGetPrefix(final String getterName) {
        return getterName.substring(3);
    }

    private boolean isGetterMethod(final Method method) {
        return method.getName().startsWith(GET_METHOD_NAME_PREFIX)
                && method.getParameterTypes().length == 0;
    }
}
