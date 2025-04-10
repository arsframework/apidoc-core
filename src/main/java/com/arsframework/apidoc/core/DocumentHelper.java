package com.arsframework.apidoc.core;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Document helper
 *
 * @author Woody
 */
public final class DocumentHelper {
    /**
     * Enter definition name
     */
    private static final String ENTER_DEFINITION_NAME = "\n";

    /**
     * Date definition name
     */
    private static final String DATE_DEFINITION_NAME = "@date";

    /**
     * Author definition name
     */
    private static final String AUTHOR_DEFINITION_NAME = "@author";

    /**
     * Version definition name
     */
    private static final String SINCE_DEFINITION_NAME = "@since";

    /**
     * Version definition name
     */
    private static final String VERSION_DEFINITION_NAME = "@version";

    /**
     * Return definition name
     */
    private static final String RETURN_DEFINITION_NAME = "@return";

    /**
     * Example definition name
     */
    private static final String EXAMPLE_DEFINITION_NAME = "@example";

    private DocumentHelper() {
    }

    /**
     * Whether the class is api class
     *
     * @param clazz Class object
     * @return true/false
     */
    public static boolean isApiClass(Class<?> clazz) {
        return clazz != null
                && (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class));
    }

    /**
     * Whether the method is api method
     *
     * @param method Method object
     * @return true/false
     */
    public static boolean isApiMethod(Method method) {
        return method != null && (method.isAnnotationPresent(RequestMapping.class)
                || method.isAnnotationPresent(PostMapping.class) || method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PutMapping.class) || method.isAnnotationPresent(DeleteMapping.class)
                || method.isAnnotationPresent(PatchMapping.class));
    }

    /**
     * Whether the method is deprecated
     *
     * @param method Method object
     * @return true/false
     */
    public static boolean isApiDeprecated(Method method) {
        return method != null && (method.isAnnotationPresent(Deprecated.class)
                || method.getDeclaringClass().isAnnotationPresent(Deprecated.class));
    }

    /**
     * Get active mapping for request url
     *
     * @param mappings Request url array
     * @return Request mapping url
     */
    private static String getActiveMapping(String[]... mappings) {
        if (mappings == null || mappings.length == 0) {
            return null;
        }
        for (String[] values : mappings) {
            if (values == null || values.length == 0) {
                continue;
            }
            for (String value : values) {
                if (value != null && !(value = value.trim()).isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Get url mapping of class
     *
     * @param clazz Class object
     * @return URL mapping
     */
    private static String getClassMapping(Class<?> clazz) {
        String mapping;
        Annotation annotation;
        if ((annotation = clazz.getAnnotation(Controller.class)) != null
                && !(mapping = ((Controller) annotation).value().trim()).isEmpty()) {
            return mapping;
        }
        if ((annotation = clazz.getAnnotation(RestController.class)) != null
                && !(mapping = ((RestController) annotation).value().trim()).isEmpty()) {
            return mapping;
        }
        if ((annotation = clazz.getAnnotation(RequestMapping.class)) != null
                && (mapping = getActiveMapping(((RequestMapping) annotation).value(),
                ((RequestMapping) annotation).path())) != null) {
            return mapping;
        }
        return null;
    }

    /**
     * Get request mapping of method
     *
     * @param method Method object
     * @return Request mapping
     */
    private static String getMethodMapping(Method method) {
        String mapping;
        Annotation annotation;
        if ((annotation = method.getAnnotation(RequestMapping.class)) != null
                && (mapping = getActiveMapping(((RequestMapping) annotation).value(),
                ((RequestMapping) annotation).path())) != null) {
            return mapping;
        }
        if ((annotation = method.getAnnotation(PostMapping.class)) != null
                && (mapping = getActiveMapping(((PostMapping) annotation).value(),
                ((PostMapping) annotation).path())) != null) {
            return mapping;
        }
        if ((annotation = method.getAnnotation(GetMapping.class)) != null
                && (mapping = getActiveMapping(((GetMapping) annotation).value(),
                ((GetMapping) annotation).path())) != null) {
            return mapping;
        }
        if ((annotation = method.getAnnotation(PutMapping.class)) != null
                && (mapping = getActiveMapping(((PutMapping) annotation).value(),
                ((PutMapping) annotation).path())) != null) {
            return mapping;
        }
        if ((annotation = method.getAnnotation(DeleteMapping.class)) != null
                && (mapping = getActiveMapping(((DeleteMapping) annotation).value(),
                ((DeleteMapping) annotation).path())) != null) {
            return mapping;
        }
        if ((annotation = method.getAnnotation(PatchMapping.class)) != null
                && (mapping = getActiveMapping(((PatchMapping) annotation).value(),
                ((PatchMapping) annotation).path())) != null) {
            return mapping;
        }
        return null;
    }

    /**
     * Get api key of method
     *
     * @param method Method object
     * @return Api key
     */
    public static String getApiKey(Method method) {
        Objects.requireNonNull(method, "method not specified");
        return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
    }

    /**
     * Get api uri of method
     *
     * @param method Method object
     * @return Api uri
     */
    public static String getApiUri(Method method) {
        Objects.requireNonNull(method, "method not specified");
        StringBuilder api = new StringBuilder();
        String prefix = getClassMapping(method.getDeclaringClass());
        String suffix = getMethodMapping(method);
        if (prefix != null) {
            api.append("/").append(prefix);
        }
        if (suffix != null) {
            api.append("/").append(suffix);
        }
        return api.toString().replace("//", "/");
    }

    /**
     * Get api request mode
     *
     * @param method Method object
     * @return Request mode
     */
    public static String getApiMode(Method method) {
        Objects.requireNonNull(method, "method not specified");

        // @RequestBody
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestBody) {
                    return "application/json";
                }
            }
        }

        // File upload
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> type : parameterTypes) {
            if (MultipartFile.class.isAssignableFrom(type)) {
                return "multipart/form-data";
            } else if (!ClassHelper.isMetaClass(type)) {
                for (Field field : type.getDeclaredFields()) {
                    if (!field.isSynthetic() && MultipartFile.class.isAssignableFrom(field.getType())) {
                        return "multipart/form-data";
                    }
                }
            }
        }

        // Empty parameter
        if (parameterTypes.length == 0 && !getApiMethods(method).contains(RequestMethod.GET)) {
            return "application/json";
        }

        // Default
        return "application/x-www-form-urlencoded";
    }

    /**
     * Get api request method
     *
     * @param method Method object
     * @return Request method list
     */
    public static Set<RequestMethod> getApiMethods(Method method) {
        Objects.requireNonNull(method, "method not specified");
        Annotation annotation;
        RequestMethod[] defaults = RequestMethod.values();
        Set<RequestMethod> methods = new HashSet<>(defaults.length);
        if ((annotation = method.getAnnotation(RequestMapping.class)) != null
                && getActiveMapping(((RequestMapping) annotation).value(),
                ((RequestMapping) annotation).path()) != null) {
            methods.addAll(Arrays.asList(((RequestMapping) annotation).method()));
        }
        if ((annotation = method.getAnnotation(PostMapping.class)) != null
                && getActiveMapping(((PostMapping) annotation).value(),
                ((PostMapping) annotation).path()) != null) {
            methods.add(RequestMethod.POST);
        }
        if ((annotation = method.getAnnotation(GetMapping.class)) != null
                && getActiveMapping(((GetMapping) annotation).value(),
                ((GetMapping) annotation).path()) != null) {
            methods.add(RequestMethod.GET);
        }
        if ((annotation = method.getAnnotation(PutMapping.class)) != null
                && getActiveMapping(((PutMapping) annotation).value(),
                ((PutMapping) annotation).path()) != null) {
            methods.add(RequestMethod.PUT);
        }
        if ((annotation = method.getAnnotation(DeleteMapping.class)) != null
                && getActiveMapping(((DeleteMapping) annotation).value(),
                ((DeleteMapping) annotation).path()) != null) {
            methods.add(RequestMethod.DELETE);
        }
        if ((annotation = method.getAnnotation(PatchMapping.class)) != null
                && getActiveMapping(((PatchMapping) annotation).value(),
                ((PatchMapping) annotation).path()) != null) {
            methods.add(RequestMethod.PATCH);
        }
        if (methods.isEmpty()) {
            methods.addAll(Arrays.asList(defaults));
        }
        return methods;
    }

    /**
     * Get field document
     *
     * @param field Field object
     * @return Field document
     */
    public static FieldDoc getDocument(Field field) {
        Objects.requireNonNull(field, "field not specified");
        ClassDoc classDocument = ContextHelper.getDocument(field.getDeclaringClass());
        if (classDocument != null) {
            for (FieldDoc fieldDocument : classDocument.fields(false)) {
                if (fieldDocument.name().equals(field.getName())) {
                    return fieldDocument;
                }
            }
        }
        return null;
    }

    /**
     * Get document of method
     *
     * @param method Method object
     * @return Method document object
     */
    public static MethodDoc getDocument(Method method) {
        Objects.requireNonNull(method, "method not specified");
        ClassDoc classDocument = ContextHelper.getDocument(method.getDeclaringClass());
        if (classDocument != null) {
            for (MethodDoc methodDocument : classDocument.methods(false)) {
                if (methodDocument.name().equals(method.getName())) {
                    return methodDocument;
                }
            }
        }
        return null;
    }

    /**
     * Get comment lines
     *
     * @param document Document object
     * @return Comment lines
     */
    public static List<String> getCommentLines(Doc document) {
        if (document == null) {
            return new ArrayList<>(0);
        }
        String[] splits = document.commentText().split(ENTER_DEFINITION_NAME);
        List<String> lines = new ArrayList<>(splits.length);
        for (String line : splits) {
            if (!(line = line.trim()).isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Get comment outline
     *
     * @param document Document object
     * @return Comment outline
     */
    public static String getCommentOutline(Doc document) {
        List<String> lines = getCommentLines(document);
        return lines.isEmpty() ? null : lines.get(0);
    }

    /**
     * Get comment description
     *
     * @param document Document object
     * @return Comment description
     */
    public static String getCommentDescription(Doc document) {
        List<String> lines = getCommentLines(document);
        return lines.size() < 2 ? null : String.join(ENTER_DEFINITION_NAME, lines.subList(1, lines.size()));
    }

    /**
     * Get annotation note
     *
     * @param name      Annotation name
     * @param documents Document object array
     * @return Annotation note
     */
    private static String getAnnotationNote(String name, Doc... documents) {
        return getAnnotationNote(name, note -> false, documents);
    }

    /**
     * Get annotation note
     *
     * @param name      Annotation name
     * @param iterable  The function of note is iterable
     * @param documents Document object array
     * @return Annotation note
     */
    private static String getAnnotationNote(String name, Function<String, Boolean> iterable, Doc... documents) {
        Objects.requireNonNull(name, "name not specified");
        Objects.requireNonNull(iterable, "iterable not specified");
        if (documents == null || documents.length == 0) {
            return null;
        }
        String note;
        for (Doc document : documents) {
            if (document == null) {
                continue;
            }
            for (String line : document.getRawCommentText().trim().split(ENTER_DEFINITION_NAME)) {
                if (!(line = line.trim()).isEmpty() && line.startsWith(name)
                        && !(note = line.substring(name.length()).trim()).isEmpty() && !iterable.apply(note)) {
                    return note;
                }
            }
        }
        return null;
    }

    /**
     * Get date note from document
     *
     * @param documents Document object array
     * @return Date for string
     */
    public static String getDateNote(Doc... documents) {
        return getAnnotationNote(DATE_DEFINITION_NAME, documents);
    }

    /**
     * Get authors note from document
     *
     * @param documents Document object array
     * @return Api authors
     */
    public static List<String> getAuthorNotes(Doc... documents) {
        List<String> authors = new LinkedList<>();
        if (documents != null && documents.length > 0) {
            for (Doc document : documents) {
                if (document == null) {
                    continue;
                }
                getAnnotationNote(AUTHOR_DEFINITION_NAME, author -> {
                    authors.add(author);
                    return true;
                }, document);
                if (!authors.isEmpty()) {
                    break;
                }
            }
        }
        return authors;
    }

    /**
     * Get since note from document
     *
     * @param documents Document object array
     * @return Api since
     */
    public static String getSinceNote(Doc... documents) {
        return getAnnotationNote(SINCE_DEFINITION_NAME, documents);
    }

    /**
     * Get version note from document
     *
     * @param documents Document object array
     * @return Api version
     */
    public static String getVersionNote(Doc... documents) {
        String version = getAnnotationNote(VERSION_DEFINITION_NAME, documents);
        return version == null ? getSinceNote(documents) : version;
    }

    /**
     * Get parameter note
     *
     * @param name      Parameter name
     * @param documents Document object array
     * @return Parameter note
     */
    public static String getParameterNote(String name, Doc... documents) {
        Objects.requireNonNull(name, "name not specified");
        return getAnnotationNote(String.format("@param %s ", name), documents);
    }

    /**
     * Get return note from document
     *
     * @param documents Document object array
     * @return Api return
     */
    public static String getReturnNote(Doc... documents) {
        return getAnnotationNote(RETURN_DEFINITION_NAME, documents);
    }

    /**
     * Get example note from document
     *
     * @param documents Document object array
     * @return Api parameter example
     */
    public static String getExampleNote(Doc... documents) {
        return getAnnotationNote(EXAMPLE_DEFINITION_NAME, documents);
    }

    /**
     * Get files of directory
     *
     * @param directory File directory
     * @return File list
     */
    public static List<File> listDirectoryFiles(File directory) {
        File[] files;
        if (directory != null && directory.exists() && directory.isDirectory()
                && (files = directory.listFiles()) != null) {
            return Arrays.asList(files);
        }
        return Collections.emptyList();
    }

    /**
     * Copy file directory
     *
     * @param source Source directory
     * @param target Target directory
     * @throws IOException IO exception
     */
    public static void copyDirectory(File source, File target) throws IOException {
        if (source == null || target == null) {
            return;
        } else if (!target.exists()) {
            target.mkdirs();
        }
        for (File file : listDirectoryFiles(source)) {
            File to = new File(target, file.getName());
            if (file.isFile()) {
                Files.copy(file.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                copyDirectory(file, to);
            }
        }
    }

    /**
     * Remove file directory
     *
     * @param directory File directory
     */
    public static void removeDirectory(File directory) {
        if (directory != null && directory.exists()) {
            for (File file : listDirectoryFiles(directory)) {
                removeDirectory(file);
            }
            directory.delete();
        }
    }
}
