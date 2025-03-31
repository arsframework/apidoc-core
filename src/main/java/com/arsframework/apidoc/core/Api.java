package com.arsframework.apidoc.core;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Api model
 *
 * @author Woody
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Api {
    /**
     * Api key
     */
    private String key;

    /**
     * Api name
     */
    private String name;

    /**
     * Api tag
     */
    private String tag;

    /**
     * Api uri
     */
    private String uri;

    /**
     * Api request mode
     */
    private String mode;

    /**
     * Api date
     */
    private String date;

    /**
     * Api version
     */
    private String version;

    /**
     * Api description
     */
    private String description;

    /**
     * Whether the deprecated is enabled
     */
    private boolean deprecated;

    /**
     * Api authors
     */
    private List<String> authors;

    /**
     * Api request methods
     */
    private List<String> methods;

    /**
     * Api request parameters
     */
    private List<Parameter> parameters;

    /**
     * Api return parameter
     */
    private Parameter returned;
}
