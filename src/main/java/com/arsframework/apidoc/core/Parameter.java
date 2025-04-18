package com.arsframework.apidoc.core;

import java.lang.reflect.Field;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Api parameter model
 *
 * @author Woody
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameter {
    /**
     * Parameter field
     */
    private Field field;

    /**
     * Is input parameter
     */
    private boolean input;

    /**
     * Parameter type
     */
    private Class<?> type;

    /**
     * Parameter original type
     */
    private Class<?> original;

    /**
     * Parameter name
     */
    private String name;

    /**
     * Parameter size
     */
    private Size size;

    /**
     * Parameter entry
     */
    private String entry;

    /**
     * Parameter format
     */
    private String format;

    /**
     * Parameter example
     */
    private String example;

    /**
     * Parameter is required
     */
    private boolean required;

    /**
     * Parameter is multiple
     */
    private boolean multiple;

    /**
     * Parameter is deprecated
     */
    private boolean deprecated;

    /**
     * Parameter default value
     */
    private Object defaultValue;

    /**
     * Parameter description
     */
    private String description;

    /**
     * Parameter options
     */
    private List<Option> options;

    /**
     * Parameter fields
     */
    private List<Parameter> fields;

    /**
     * Parameter size
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Size {
        /**
         * Min value
         */
        private Double min;

        /**
         * Max value
         */
        private Double max;
    }

    /**
     * Parameter option
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        /**
         * Option key
         */
        private String key;

        /**
         * Option value
         */
        private String value;

        /**
         * Option is deprecated
         */
        private boolean deprecated;
    }
}
