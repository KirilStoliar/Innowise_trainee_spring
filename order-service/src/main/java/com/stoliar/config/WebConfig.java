package com.stoliar.config;

import com.stoliar.converter.StringToOrderStatusConverter;
import com.stoliar.converter.StringToOrderStatusListConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final StringToOrderStatusConverter stringToOrderStatusConverter;
    private final StringToOrderStatusListConverter stringToOrderStatusListConverter;
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToOrderStatusConverter);
        registry.addConverter(stringToOrderStatusListConverter);
    }
}