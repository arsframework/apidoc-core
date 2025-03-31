package com.arsframework.apidoc.core;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Method analyser
 *
 * @author Woody
 */
public class MethodAnalyser {
    /**
     * Api method object
     */
    protected final Method method;

    /**
     * Method document object
     */
    protected final MethodDoc document;

    public MethodAnalyser(Method method) {
        Objects.requireNonNull(method, "method not specified");
        this.method = method;
        this.document = DocumentHelper.getDocument(method);
    }

    /**
     * Get class document of method
     *
     * @return Class document object
     */
    protected ClassDoc getClassDocument() {
        return this.document == null ? null : this.document.containingClass();
    }

    /**
     * Get api key
     *
     * @return Api key
     */
    protected String getKey() {
        return DocumentHelper.getApiKey(this.method);
    }

    /**
     * Get api uri
     *
     * @return Api uri
     */
    protected String getUri() {
        return DocumentHelper.getApiUri(this.method);
    }

    /**
     * Get api name
     *
     * @return Api name
     */
    protected String getName() {
        String name = DocumentHelper.getCommentOutline(this.document);
        return name == null ? this.method.getName() : name;
    }

    /**
     * Get api tag
     *
     * @return Api tag
     */
    protected String getTag() {
        String group = DocumentHelper.getCommentOutline(this.getClassDocument());
        return group == null ? this.method.getDeclaringClass().getSimpleName() : group;
    }

    /**
     * Judge whether the api is deprecated
     *
     * @return true/false
     */
    protected boolean isDeprecated() {
        return DocumentHelper.isApiDeprecated(this.method);
    }

    /**
     * Get api request mode
     *
     * @return Request mode
     */
    protected String getApiMode() {
        return DocumentHelper.getApiMode(this.method);
    }

    /**
     * Get api description
     *
     * @return Api description
     */
    protected String getDescription() {
        return DocumentHelper.getCommentDescription(this.document);
    }

    /**
     * Get api date
     *
     * @return Api date
     */
    protected String getDate() {
        return DocumentHelper.getDateNote(this.document, this.getClassDocument());
    }

    /**
     * Get api version
     *
     * @return Api version
     */
    protected String getVersion() {
        return DocumentHelper.getVersionNote(this.document, this.getClassDocument());
    }

    /**
     * Get api authors
     *
     * @return Api authors
     */
    protected List<String> getAuthors() {
        return DocumentHelper.getAuthorNotes(this.document, this.getClassDocument());
    }

    /**
     * Get api request methods
     *
     * @return Api request methods
     */
    protected List<String> getMethods() {
        Set<RequestMethod> methods = DocumentHelper.getApiMethods(this.method);
        return methods.stream().sorted(Comparator.comparing(Enum::ordinal))
                .map(method -> method.name().toLowerCase()).collect(Collectors.toList());
    }

    /**
     * Get parameter analyser
     *
     * @return Parameter analyser
     */
    protected ParameterAnalyser getParameterAnalyser() {
        return new ParameterAnalyser(this.method);
    }

    /**
     * Parse the method to api
     *
     * @return Api object
     */
    public Api parse() {
        ParameterAnalyser parameterAnalyser = this.getParameterAnalyser();
        Objects.requireNonNull(parameterAnalyser, "ParameterAnalyser must not be null");
        return Api.builder().key(this.getKey()).name(this.getName()).tag(this.getTag()).uri(this.getUri())
                .mode(this.getApiMode()).date(this.getDate()).version(this.getVersion()).methods(this.getMethods())
                .description(this.getDescription()).deprecated(this.isDeprecated()).authors(this.getAuthors())
                .parameters(parameterAnalyser.getParameters()).returned(parameterAnalyser.getReturned()).build();
    }

    /**
     * Method analyser factory
     */
    public interface Factory {
        /**
         * Build method analyser
         *
         * @param method Method object
         * @return Method analyser
         */
        MethodAnalyser build(Method method);
    }
}
