package com.arsframework.apidoc.core;

import lombok.Builder;
import lombok.Getter;

/**
 * Api document configuration
 *
 * @author Woody
 */
@Getter
@Builder
public class Configuration {
    /**
     * Whether the date is displayed
     */
    private boolean displayDate;

    /**
     * Whether the author is displayed
     */
    private boolean displayAuthor;

    /**
     * Whether the sample request is enabled
     */
    private boolean enableSampleRequest;

    /**
     * Whether the response example is enabled
     */
    private boolean enableResponseExample;

    /**
     * Whether the snake and underline conversion is enabled
     */
    private boolean enableSnakeUnderlineConversion;
}
