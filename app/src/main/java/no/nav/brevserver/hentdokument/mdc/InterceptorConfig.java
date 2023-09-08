package no.nav.brevserver.hentdokument.mdc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

	private final MDCInterceptor mdcInterceptor;

	public InterceptorConfig(MDCInterceptor mdcInterceptor) {
		this.mdcInterceptor = mdcInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(mdcInterceptor).addPathPatterns("/rest/hentdokument/**");
	}
}
