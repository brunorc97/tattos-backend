package com.tattos.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Classe de auditoria de rotas que varre todos os endpoints registrados na aplicação
 * ao iniciar. É útil para detectar inadvertidamente endpoints expostos sem
 * proteção de segurança.
 *
 * O bean será carregado automaticamente pelo Spring (annotation @Component)
 * e executado via CommandLineRunner assim que o contexto for inicializado.
 */
@Component
public class RouteAuditor implements CommandLineRunner {

    private final ApplicationContext context;

    public RouteAuditor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== VARREDURA DE SEGURANÇA: ENDPOINTS REGISTRADOS ===");
        // Usa o nome do bean principal do WebMvc, pois o Actuator registra outro
        // RequestMappingHandlerMapping (controllerEndpointHandlerMapping).
        RequestMappingHandlerMapping mapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        
        mapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
            // Imprime o padrão exato da URL (ex: [/api/v1/users] ou [/**])
            var patterns = requestMappingInfo.getPatternValues();
            
            System.out.printf("Rota: %s -> Método: %s.%s()%n", 
                patterns, 
                handlerMethod.getBeanType().getSimpleName(), 
                handlerMethod.getMethod().getName());
        });
    }
}
