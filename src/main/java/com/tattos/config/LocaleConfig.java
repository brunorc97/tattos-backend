package com.tattos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Resolve o locale da requisição pelo header Accept-Language.
 * Padrão: pt-BR. Suportados: pt-BR e en.
 */
@Configuration
public class LocaleConfig {

    @Bean
    public AcceptHeaderLocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.of("pt", "BR"));
        resolver.setSupportedLocales(List.of(Locale.of("pt", "BR"), Locale.ENGLISH));
        return resolver;
    }
}
