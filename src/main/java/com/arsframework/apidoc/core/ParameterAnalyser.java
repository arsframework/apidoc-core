package com.arsframework.apidoc.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.arsframework.spring.web.utils.param.Rename;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import org.springframework.core.io.InputStreamSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.multipart.MultipartFile;

/**
 * Parameter analyser
 *
 * @author Woody
 */
public class ParameterAnalyser {
    /**
     * Api method object
     */
    protected final Method method;

    public ParameterAnalyser(Method method) {
        Objects.requireNonNull(method, "method not specified");
        this.method = method;
    }

    /**
     * Judge whether the class is reiterated
     *
     * @param stack Class stack
     * @param clazz Target class object
     * @return true/false
     */
    protected boolean isRecursion(LinkedList<Class<?>> stack, Class<?> clazz) {
        if (stack != null && clazz != null) {
            int count = 0;
            for (Class<?> c : stack) {
                if (c == clazz && ++count > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Judge whether the field is active parameter
     *
     * @param field Field object
     * @return true/false
     */
    protected boolean isActiveParameter(Field field) {
        return field != null && !field.isSynthetic() && !Modifier.isStatic(field.getModifiers())
                && !field.isAnnotationPresent(JsonIgnore.class);
    }

    /**
     * Judge whether the parameter is be used for request
     *
     * @param parameter Parameter object
     * @param type      Parameter type
     * @return true/false
     */
    protected boolean isRequestParameter(java.lang.reflect.Parameter parameter, Class<?> type) {
        if (parameter == null || type == null || parameter.isAnnotationPresent(SessionAttribute.class)) {
            return false;
        }
        return ClassHelper.isMetaClass(type) || ContextHelper.isActivePackage(type.getPackage());
    }

    /**
     * Get parameter type by class
     *
     * @param clazz Class object
     * @return Parameter type class
     */
    protected Class<?> getType(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz not specified");
        if (clazz == byte.class || clazz == Byte.class) {
            return Byte.class;
        } else if (clazz == char.class || clazz == Character.class) {
            return Character.class;
        } else if (clazz == int.class || clazz == Integer.class) {
            return Integer.class;
        } else if (clazz == short.class || clazz == Short.class) {
            return Short.class;
        } else if (clazz == long.class || clazz == Long.class || BigInteger.class.isAssignableFrom(clazz)) {
            return Long.class;
        } else if (clazz == float.class || clazz == Float.class) {
            return Float.class;
        } else if (clazz == double.class || clazz == Double.class || BigDecimal.class.isAssignableFrom(clazz)) {
            return Double.class;
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return Boolean.class;
        } else if (clazz == Locale.class || TimeZone.class.isAssignableFrom(clazz) ||
                Enum.class.isAssignableFrom(clazz) || CharSequence.class.isAssignableFrom(clazz)) {
            return String.class;
        } else if (ClassHelper.isDateClass(clazz)) {
            return Date.class;
        } else if (File.class.isAssignableFrom(clazz) || MultipartFile.class.isAssignableFrom(clazz)) {
            return File.class;
        } else if (Reader.class.isAssignableFrom(clazz) || InputStream.class.isAssignableFrom(clazz)
                || InputStreamSource.class.isAssignableFrom(clazz)) {
            return Reader.class;
        } else if (Writer.class.isAssignableFrom(clazz) || OutputStream.class.isAssignableFrom(clazz)) {
            return Writer.class;
        }
        return Object.class;
    }

    /**
     * Get field naming strategy
     *
     * @param field Filed object
     * @return Property naming strategy
     */
    protected PropertyNamingStrategy getPropertyNamingStrategy(Field field) {
        Objects.requireNonNull(field, "field not specified");

        JsonNaming naming = field.getAnnotation(JsonNaming.class);
        if (naming == null) {
            return ContextHelper.getConfiguration().isEnableSnakeUnderlineConversion() ?
                    PropertyNamingStrategy.SNAKE_CASE : null;
        }
        Class<? extends PropertyNamingStrategy> clazz = naming.value();
        if (clazz == PropertyNamingStrategy.SnakeCaseStrategy.class) {
            return PropertyNamingStrategy.SNAKE_CASE;
        } else if (clazz == PropertyNamingStrategy.UpperCamelCaseStrategy.class) {
            return PropertyNamingStrategy.UPPER_CAMEL_CASE;
        } else if (clazz == PropertyNamingStrategy.LowerCaseStrategy.class) {
            return PropertyNamingStrategy.LOWER_CASE;
        } else if (clazz == PropertyNamingStrategy.KebabCaseStrategy.class) {
            return PropertyNamingStrategy.KEBAB_CASE;
        }
        return PropertyNamingStrategy.LOWER_CAMEL_CASE;
    }

    /**
     * Get field name
     *
     * @param field Field object
     * @return Field name
     */
    protected String getName(Field field) {
        Objects.requireNonNull(field, "field not specified");

        // Form parameter conversion
        Rename rename = field.getAnnotation(Rename.class);
        String name = rename == null ? null : rename.value().trim();
        if (name != null && !name.isEmpty()) {
            return name;
        }

        // Json parameter conversion
        JsonProperty property = field.getAnnotation(JsonProperty.class);
        if (property != null && !(name = property.value().trim()).isEmpty()) {
            return name;
        }

        // Property strategy conversion
        name = field.getName();
        PropertyNamingStrategy strategy = this.getPropertyNamingStrategy(field);
        if (strategy instanceof PropertyNamingStrategy.PropertyNamingStrategyBase) {
            return ((PropertyNamingStrategy.PropertyNamingStrategyBase) strategy).translate(name);
        }
        return name;
    }

    /**
     * Get parameter name
     *
     * @param parameter Parameter object
     * @return Parameter name
     */
    protected String getName(java.lang.reflect.Parameter parameter) {
        Objects.requireNonNull(parameter, "parameter not specified");

        String name;
        PathVariable path = parameter.getAnnotation(PathVariable.class);
        if (path != null && (!(name = path.value().trim()).isEmpty() || !(name = path.name().trim()).isEmpty())) {
            return name;
        }

        RequestParam query = parameter.getAnnotation(RequestParam.class);
        if (query != null && (!(name = query.value().trim()).isEmpty() || !(name = query.name().trim()).isEmpty())) {
            return name;
        }

        RequestHeader header = parameter.getAnnotation(RequestHeader.class);
        if (header != null && (!(name = header.value().trim()).isEmpty() || !(name = header.name().trim()).isEmpty())) {
            return name;
        }

        CookieValue cookie = parameter.getAnnotation(CookieValue.class);
        if (cookie != null && (!(name = cookie.value().trim()).isEmpty() || !(name = cookie.name().trim()).isEmpty())) {
            return name;
        }

        return parameter.getName();
    }

    /**
     * Get parameter size
     *
     * @param element Annotated element
     * @return Parameter size object
     */
    protected Parameter.Size getSize(AnnotatedElement element) {
        Objects.requireNonNull(element, "element not specified");
        Size size = element.getAnnotation(Size.class);
        if (size != null) {
            return Parameter.Size.builder().min((double) size.min()).max((double) size.max()).build();
        }

        Min min = element.getAnnotation(Min.class);
        Max max = element.getAnnotation(Max.class);
        if (min != null && max != null) {
            return Parameter.Size.builder().min((double) min.value()).max((double) max.value()).build();
        } else if (min != null) {
            return Parameter.Size.builder().min((double) min.value()).build();
        } else if (max != null) {
            return Parameter.Size.builder().max((double) max.value()).build();
        }

        DecimalMin decimalMin = element.getAnnotation(DecimalMin.class);
        DecimalMax decimalMax = element.getAnnotation(DecimalMax.class);
        if (decimalMin != null && decimalMax != null) {
            return Parameter.Size.builder().min(Double.parseDouble(decimalMin.value()))
                    .max(Double.parseDouble(decimalMax.value())).build();
        } else if (decimalMin != null) {
            return Parameter.Size.builder().min(Double.parseDouble(decimalMin.value())).build();
        } else if (decimalMax != null) {
            return Parameter.Size.builder().max(Double.parseDouble(decimalMax.value())).build();
        }
        return null;
    }

    /**
     * Get parameter entry
     *
     * @param parameter Parameter object
     * @return Parameter entry
     */
    protected String getEntry(java.lang.reflect.Parameter parameter) {
        PathVariable path = parameter.getAnnotation(PathVariable.class);
        if (path != null) {
            return "path";
        }

        RequestParam query = parameter.getAnnotation(RequestParam.class);
        if (query != null) {
            return "query";
        }

        RequestHeader header = parameter.getAnnotation(RequestHeader.class);
        if (header != null) {
            return "header";
        }

        CookieValue cookie = parameter.getAnnotation(CookieValue.class);
        if (cookie != null) {
            return "cookie";
        }

        return null;
    }

    /**
     * Get parameter format
     *
     * @param element Annotated element
     * @return Parameter format
     */
    protected String getFormat(AnnotatedElement element) {
        Objects.requireNonNull(element, "element not specified");
        String format;
        Annotation annotation;
        if ((annotation = element.getAnnotation(DateTimeFormat.class)) != null
                && !(format = ((DateTimeFormat) annotation).pattern()).isEmpty()) {
            return format;
        }
        if ((annotation = element.getAnnotation(JsonFormat.class)) != null
                && !(format = ((JsonFormat) annotation).pattern()).isEmpty()) {
            return format;
        }
        if ((annotation = element.getAnnotation(Pattern.class)) != null
                && !(format = ((Pattern) annotation).regexp()).isEmpty()) {
            return format;
        }
        return null;
    }

    /**
     * Judge whether the element is required
     *
     * @param element Annotated element
     * @return true/false
     */
    protected boolean isRequired(AnnotatedElement element) {
        return element != null && (element.isAnnotationPresent(NotNull.class)
                || element.isAnnotationPresent(NotBlank.class) || element.isAnnotationPresent(NotEmpty.class)
                || (element.isAnnotationPresent(Size.class) && element.getAnnotation(Size.class).min() > 0)
                || (element.isAnnotationPresent(PathVariable.class) && element.getAnnotation(PathVariable.class).required())
                || (element.isAnnotationPresent(RequestParam.class) && element.getAnnotation(RequestParam.class).required())
                || (element.isAnnotationPresent(RequestHeader.class) && element.getAnnotation(RequestHeader.class).required())
                || (element.isAnnotationPresent(CookieValue.class) && element.getAnnotation(CookieValue.class).required()));
    }

    /**
     * Judge whether the element is deprecated
     *
     * @param element Annotated element
     * @return true/false
     */
    protected boolean isDeprecated(AnnotatedElement element) {
        return element != null && element.isAnnotationPresent(Deprecated.class);
    }

    /**
     * Get parameter example
     *
     * @param field Field object
     * @return Example value
     */
    protected String getExample(Field field) {
        Objects.requireNonNull(field, "field not specified");
        return DocumentHelper.getExampleNote(DocumentHelper.getDocument(field));
    }

    /**
     * Get parameter default value
     *
     * @param instance Class instance
     * @param field    Field object
     * @return Parameter default value
     */
    protected Object getDefaultValue(Object instance, Field field) {
        Objects.requireNonNull(field, "field not specified");
        if (instance != null) {
            try {
                // Get default value of field
                field.setAccessible(true);
                Object defaultValue = field.get(instance);
                if (defaultValue != null
                        && (!(defaultValue instanceof CharSequence) || ((CharSequence) defaultValue).length() > 0)) {
                    return defaultValue;
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Get parameter default value
     *
     * @param parameter Parameter object
     * @return Parameter default value
     */
    protected Object getDefaultValue(java.lang.reflect.Parameter parameter) {
        Objects.requireNonNull(parameter, "parameter not specified");
        String defaultValue;
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
        if (annotation != null && !(defaultValue = annotation.defaultValue()).isEmpty()
                && !defaultValue.equals(ValueConstants.DEFAULT_NONE)) {
            return defaultValue;
        }
        return null;
    }

    /**
     * Get field description
     *
     * @param field Field object
     * @return Field description
     */
    protected String getDescription(Field field) {
        Objects.requireNonNull(field, "field not specified");
        FieldDoc document = DocumentHelper.getDocument(field);
        String comment = document == null ? null : document.commentText();
        return comment == null || (comment = comment.trim()).isEmpty() ? null : comment;
    }

    /**
     * Get parameter description
     *
     * @param parameter Parameter object
     * @return Parameter description
     */
    protected String getDescription(java.lang.reflect.Parameter parameter) {
        Objects.requireNonNull(parameter, "parameter not specified");
        return DocumentHelper.getParameterNote(parameter.getName(), DocumentHelper.getDocument(this.method));
    }

    /**
     * Get parameter options with class
     *
     * @param clazz Class object
     * @return Parameter option list
     */
    protected List<Parameter.Option> getOptions(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz not specified");
        List<Parameter.Option> options = new LinkedList<>();
        if (Enum.class.isAssignableFrom(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isSynthetic() || !field.isEnumConstant()) {
                    continue;
                }
                options.add(Parameter.Option.builder().key(field.getName()).value(this.getDescription(field))
                        .deprecated(field.isAnnotationPresent(Deprecated.class)).build());
            }
        }
        return options;
    }

    /**
     * The parameter iteration of class
     *
     * @param clazz    Class object
     * @param consumer Field consumer
     */
    private void classParameterIterating(Class<?> clazz, Consumer<Field> consumer) {
        Objects.requireNonNull(clazz, "clazz not specified");
        Objects.requireNonNull(consumer, "consumer not specified");
        for (Field field : clazz.getDeclaredFields()) {
            if (this.isActiveParameter(field)) {
                consumer.accept(field);
            }
        }
    }

    /**
     * Handle parameter after initialized
     *
     * @param parameter Parameter instance
     */
    protected void afterInitializeParameter(Parameter parameter) {
    }

    /**
     * Get parameters with class fields
     *
     * @param clazz    Class object
     * @param consumer Field consumer
     * @return Parameter list
     */
    protected List<Parameter> class2parameters(Class<?> clazz, Function<Field, Parameter> consumer) {
        Objects.requireNonNull(clazz, "clazz not specified");
        Objects.requireNonNull(consumer, "consumer not specified");
        Class<?> original = clazz;
        List<Parameter> parameters = new LinkedList<>();

        // Load current and parent class fields
        do {
            this.classParameterIterating(clazz, field -> parameters.add(consumer.apply(field)));
        } while ((clazz = clazz.getSuperclass()) != null && !ClassHelper.isMetaClass(clazz)
                && !Collection.class.isAssignableFrom(clazz));

        // Load subclass field by @JsonTypeInfo
        JsonTypeInfo jsonType = original.getAnnotation(JsonTypeInfo.class);
        if (jsonType != null && jsonType.use() == JsonTypeInfo.Id.NAME && !jsonType.property().isEmpty()) {
            for (JsonSubTypes.Type type : original.getAnnotation(JsonSubTypes.class).value()) {
                this.classParameterIterating(type.value(), field -> parameters.add(consumer.apply(field)));
            }
        }
        return parameters;
    }

    /**
     * Convert field to parameter
     *
     * @param input     Is input parameter
     * @param instance  Class instance
     * @param field     Field object
     * @param variables Type variable and type mappings
     * @param stack     Class stack
     * @return Parameter object
     */
    protected Parameter field2parameter(boolean input, Object instance, Field field,
                                        Map<TypeVariable<?>, Type> variables, LinkedList<Class<?>> stack) {
        Objects.requireNonNull(field, "field not specified");
        Type type = field.getGenericType();
        if (type instanceof TypeVariable && variables != null && variables.containsKey(type)) {
            type = variables.get(type);
        }
        Class<?> clazz = ClassHelper.type2class(type), target = clazz;
        if (clazz.isArray()) {
            target = clazz.getComponentType();
        } else if (Collection.class.isAssignableFrom(clazz)) {
            target = ClassHelper.type2class(type = ClassHelper.getCollectionActualType(type, variables));
        }
        boolean multiple = clazz.isArray() || Collection.class.isAssignableFrom(clazz);
        Parameter parameter = Parameter.builder().field(field).input(input).type(this.getType(target)).original(target)
                .name(this.getName(field)).size(this.getSize(field)).format(this.getFormat(field))
                .required(this.isRequired(field)).multiple(multiple).example(this.getExample(field))
                .deprecated(this.isDeprecated(field)).defaultValue(this.getDefaultValue(instance, field))
                .description(this.getDescription(field)).options(this.getOptions(target)).build();
        if (!ClassHelper.isMetaClass(target) && !this.isRecursion(stack, target)) {
            stack.addLast(target);
            Object targetInstance = multiple ? null : ClassHelper.getInstance(target);
            Map<TypeVariable<?>, Type> finalVariables = ClassHelper.getVariableParameterizedMappings(type);
            parameter.setFields(this.class2parameters(target,
                    f -> this.field2parameter(input, targetInstance, f, finalVariables, stack)));
            stack.removeLast();
        }
        this.afterInitializeParameter(parameter);
        return parameter;
    }

    /**
     * Get request parameters of current method
     *
     * @return Parameter list
     */
    public List<Parameter> getParameters() {
        List<Parameter> parameters = new LinkedList<>();
        for (java.lang.reflect.Parameter parameter : this.method.getParameters()) {
            Type type = parameter.getParameterizedType();
            Class<?> clazz = ClassHelper.type2class(type), target = clazz;
            if (clazz.isArray()) {
                target = clazz.getComponentType();
            } else if (Collection.class.isAssignableFrom(clazz)) {
                Map<TypeVariable<?>, Type> variables = ClassHelper.getVariableParameterizedMappings(type);
                target = ClassHelper.type2class(type = ClassHelper.getCollectionActualType(type, variables));
            }
            if (!this.isRequestParameter(parameter, target)) {
                continue;
            }

            boolean multiple = clazz.isArray() || Collection.class.isAssignableFrom(clazz);
            Parameter parent = Parameter.builder().input(true).type(this.getType(target)).original(target)
                    .name(this.getName(parameter)).size(this.getSize(parameter)).entry(this.getEntry(parameter))
                    .format(this.getFormat(parameter)).required(this.isRequired(parameter)).multiple(multiple)
                    .deprecated(this.isDeprecated(parameter)).defaultValue(this.getDefaultValue(parameter))
                    .description(this.getDescription(parameter)).options(this.getOptions(target)).build();
            if (ClassHelper.isMetaClass(target)) {
                this.afterInitializeParameter(parent);
                parameters.add(parent);
            } else {
                Object instance = multiple ? null : ClassHelper.getInstance(target);
                LinkedList<Class<?>> stack = new LinkedList<>();
                Map<TypeVariable<?>, Type> variables = ClassHelper.getVariableParameterizedMappings(type);
                List<Parameter> fields = this.class2parameters(
                        target, f -> this.field2parameter(true, instance, f, variables, stack)
                );
                if (multiple) {
                    parent.setName("/");
                    parent.setFields(fields);
                    this.afterInitializeParameter(parent);
                    parameters.add(parent);
                } else {
                    parameters.addAll(fields);
                }
            }
        }
        return parameters;
    }

    /**
     * Get return parameter of current method
     *
     * @return Parameter object
     */
    public Parameter getReturned() {
        Type type = this.method.getGenericReturnType();
        if (type == void.class) {
            return null;
        }
        Class<?> clazz = ClassHelper.type2class(type), target = clazz;
        if (clazz.isArray()) {
            target = clazz.getComponentType();
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Map<TypeVariable<?>, Type> variables = ClassHelper.getVariableParameterizedMappings(type);
            target = ClassHelper.type2class(type = ClassHelper.getCollectionActualType(type, variables));
        }
        MethodDoc document = DocumentHelper.getDocument(this.method);
        String example = DocumentHelper.getExampleNote(document);
        String description = DocumentHelper.getReturnNote(document);
        boolean multiple = clazz.isArray() || Collection.class.isAssignableFrom(clazz);
        Parameter parameter = Parameter.builder().input(false).type(this.getType(target)).original(target)
                .multiple(multiple).name("/").example(example).description(description)
                .options(this.getOptions(target)).build();
        if (!ClassHelper.isMetaClass(target)) {
            LinkedList<Class<?>> stack = new LinkedList<>();
            stack.addLast(target);
            Map<TypeVariable<?>, Type> variables = ClassHelper.getVariableParameterizedMappings(type);
            parameter.setFields(
                    this.class2parameters(target, f -> this.field2parameter(false, null, f, variables, stack))
            );
        }
        this.afterInitializeParameter(parameter);
        return parameter;
    }
}
