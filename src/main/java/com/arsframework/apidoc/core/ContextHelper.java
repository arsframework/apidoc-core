package com.arsframework.apidoc.core;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.sun.javadoc.ClassDoc;

/**
 * Context helper
 *
 * @author Woody
 */
public final class ContextHelper {
    /**
     * Class path
     */
    private static final ThreadLocal<String> CLASSPATH = new ThreadLocal<>();

    /**
     * Class loader
     */
    private static final ThreadLocal<ClassLoader> CLASS_LOADER = new ThreadLocal<>();

    /**
     * Configuration
     */
    private static final ThreadLocal<Configuration> CONFIGURATION = new ThreadLocal<>();

    /**
     * Document provider function
     */
    private static final ThreadLocal<Function<Class<?>, ClassDoc>> DOCUMENT_PROVIDER = new ThreadLocal<>();

    /**
     * Include group identities
     */
    private static final ThreadLocal<Set<String>> INCLUDE_GROUP_IDENTITIES = new ThreadLocal<>();

    private ContextHelper() {
    }

    /**
     * Get class path
     *
     * @return Class path
     */
    public static String getClasspath() {
        return Optional.ofNullable(CLASSPATH.get()).get();
    }

    /**
     * Set class path
     *
     * @param classpath Class path
     */
    public static void setClasspath(String classpath) {
        CLASSPATH.set(classpath);
    }

    /**
     * Get class loader
     *
     * @return Class loader
     */
    public static ClassLoader getClassLoader() {
        return Optional.ofNullable(CLASS_LOADER.get()).get();
    }

    /**
     * Set class loader
     *
     * @param classLoader Class loader
     */
    public static void setClassLoader(ClassLoader classLoader) {
        CLASS_LOADER.set(classLoader);
    }

    /**
     * Get configuration
     *
     * @return Configuration object
     */
    public static Configuration getConfiguration() {
        return Optional.ofNullable(CONFIGURATION.get()).get();
    }

    /**
     * Set configuration to current context
     *
     * @param configuration Configuration object
     */
    public static void setConfiguration(Configuration configuration) {
        CONFIGURATION.set(configuration);
    }

    /**
     * Set document provider to current context
     *
     * @param documentProvider Document provider object
     */
    public static void setDocumentProvider(Function<Class<?>, ClassDoc> documentProvider) {
        DOCUMENT_PROVIDER.set(documentProvider);
    }

    /**
     * Get include group identities
     *
     * @return Include group identities
     */
    public static Set<String> getIncludeGroupIdentities() {
        return Optional.ofNullable(INCLUDE_GROUP_IDENTITIES.get()).get();
    }

    /**
     * Set include group identities to current context
     *
     * @param includeGroupIdentities Group identities for include
     */
    public static void setIncludeGroupIdentities(Set<String> includeGroupIdentities) {
        INCLUDE_GROUP_IDENTITIES.set(Collections.unmodifiableSet(includeGroupIdentities));
    }

    /**
     * Get class document
     *
     * @param clazz Class object
     * @return Class document
     */
    public static ClassDoc getDocument(Class<?> clazz) {
        return clazz == null ? null : Optional.ofNullable(DOCUMENT_PROVIDER.get()).get().apply(clazz);
    }

    /**
     * Judge whether the package is active
     *
     * @param pkg Package object
     * @return true/false
     */
    public static boolean isActivePackage(Package pkg) {
        return pkg == null ? true : getIncludeGroupIdentities().stream().anyMatch(pkg.getName()::startsWith);
    }

    /**
     * Clear context
     */
    public static void clear() {
        CLASSPATH.remove();
        CLASS_LOADER.remove();
        CONFIGURATION.remove();
        DOCUMENT_PROVIDER.remove();
        INCLUDE_GROUP_IDENTITIES.remove();
    }
}
