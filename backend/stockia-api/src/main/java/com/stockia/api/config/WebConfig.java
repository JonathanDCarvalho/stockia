
package com.stockia.api.config;

import com.stockia.api.security.EmpresaAuthorizationInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EmpresaAuthorizationInterceptor interceptor;

    public WebConfig(EmpresaAuthorizationInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/empresas/**");
    }
}
