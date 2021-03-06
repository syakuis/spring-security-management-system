package org.mei.config.context;

import org.mei.core.common.object.Collecting;
import org.mei.core.security.access.*;
import org.mei.core.security.authentication.ConsumerAuthenticationProvider;
import org.mei.core.security.authentication.ConsumerDetailsService;
import org.mei.core.security.authentication.UserDetailsServiceImpl;
import org.mei.core.security.authorization.ConsumerPermit;
import org.mei.core.security.enums.Permission;
import org.mei.core.security.filter.SecurityMetadataSource;
import org.mei.core.security.handler.*;
import org.mei.core.security.password.ShaPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.session.ConcurrentSessionFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 16. 5. 30.
*/
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled=true)
public class SecurityContext {
	private static final Logger logger = LoggerFactory.getLogger(SecurityContext.class);

	@Autowired private Properties mei;

	@Autowired
	private ConsumerDetailsService consumerDetailsService;

	private PasswordEncoder passwordEncoder;
	private UserDetailsService userDetailsService;
	private InMemoryAuthorization inMemoryAuthorization;

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return passwordEncoder;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return userDetailsService;
	}

	@Bean
	public AccessControlService getAccessControlService() {
		return inMemoryAuthorization;
	}

	/**
	 * 암호화를 생성한다.
	 *
	 * @return
	 */
	private PasswordEncoder getPasswordEncoder() {
		String passwordEncoderType = mei.getProperty("security.passwordEncoder");
		String salt = mei.getProperty("security.sha.salt", null);

		if (passwordEncoderType.equals("noOpPasswordEncoder")) {
			return NoOpPasswordEncoder.getInstance();
		} else if (passwordEncoderType.equals("standardPasswordEncoder")) {
			return new StandardPasswordEncoder(salt);
		} else if (passwordEncoderType.equals("shaPasswordEncoder")) {
			int strength = Integer.parseInt(mei.getProperty("security.sha.strength", "256"));
			return new ShaPasswordEncoder(strength);
		} else {
			return new BCryptPasswordEncoder();
		}
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		/*
		auth.inMemoryAuthentication()
				.withUser("user").password("1234").roles("USER")
				.and().withUser("admin").password("1234").roles("ADMIN", "USER");
		 */

		inMemoryAuthorization = new InMemoryAuthorization();

		// 기본 권한 설정
		inMemoryAuthorization
				.add(new ConsumerPermit("admin", "ROLE_PERM_0001", Collecting.createList(Permission.ADMIN)))
				.add(new ConsumerPermit("test", "ROLE_PERM_0001", Collecting.createList(Permission.LIST, Permission.WRITE)))

				.add(new BasicPermit("ROLE_ADMIN", Collecting.createList(Permission.ADMIN)))

				.add(new AccessRule(Collecting.createList("/member/login"), null, true, null))
				.add(new AccessRule(Collecting.createList("/member/mypage"), null, true, Collecting.createList("ROLE_USER", "ROLE_ADMIN")))
				.add(new AccessRule(Collecting.createList("/member/visitor"), null, true, Collecting.createList("ROLE_ADMIN")))
				.add(new AccessRule(Collecting.createList("/**"), null, true, Collecting.createList("ROLE_USER", "ROLE_ADMIN")))

				.add(new AccessPermit("ROLE_PERM_0001", Collecting.createList("/board/**"), null, Collecting.createList(
						new RulePermit(Collecting.createList("/board"), null, Permission.LIST),
						new RulePermit(Collecting.createList("/board/view"), null, Permission.VIEW),
						new RulePermit(Collecting.createList("/board/write"), null, Permission.WRITE),
						new RulePermit(Collecting.createList("/board/delete"), null, Permission.MANAGER))));


		passwordEncoder = getPasswordEncoder();
		//UserDetailsService userDetailsService = new ConsumerDetailsService(new JsonConsumerService());
		UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(consumerDetailsService);

		this.userDetailsService = userDetailsService;

		ConsumerAuthenticationProvider consumerAuthenticationProvider = new ConsumerAuthenticationProvider(userDetailsService, inMemoryAuthorization);
		consumerAuthenticationProvider.setPasswordEncoder(passwordEncoder);

		auth.authenticationProvider(consumerAuthenticationProvider);
	}

	@Configuration
	public static class SecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		private static final Logger logger = LoggerFactory.getLogger(SecurityConfigurationAdapter.class);

		@Autowired Properties mei;
		@Autowired SessionRegistry sessionRegistry;
		@Autowired PasswordEncoder passwordEncoder;
		@Autowired UserDetailsService userDetailsService;
		@Autowired AccessControlService accessControlService;

		private AuthenticationManager authenticationManager;

		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception {
			authenticationManager = super.authenticationManagerBean();
			return authenticationManager;
		}

		@Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring().antMatchers("/resources/**");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			String loginUrl = mei.getProperty("security.loginUrl");

			String usernameParameter = mei.getProperty("security.usernameParameter");
			String passwordParameter = mei.getProperty("security.passwordParameter");

			String logoutUrl = mei.getProperty("security.logoutUrl");
			String cookieName = mei.getProperty("security.cookieName");

			String targetUrlParameter = mei.getProperty("security.targetUrlParameter");
			boolean alwaysUseDefaultTargetUrl = Boolean.parseBoolean( mei.getProperty("security.alwaysUseDefaultTargetUrl") );
			String defaultTargetUrl = mei.getProperty("security.defaultTargetUrl");
			String expiredUrl = mei.getProperty("security.expiredUrl");

			// 오류가 발생했을 때
			String errorPageUrl = mei.getProperty("security.errorPageUrl");
			String redirectUrlParameter = mei.getProperty("security.redirectUrlParameter");

			int maximumSessions = Integer.parseInt(mei.getProperty("security.maximumSessions", "-1"));
			boolean exceptionIfMaximumExceeded = Boolean.parseBoolean(mei.getProperty("security.exceptionIfMaximumExceeded"));

			if (logger.isDebugEnabled()) {
				logger.debug("loginUrl: " + loginUrl);
				logger.debug("usernameParameter: " + usernameParameter);
				logger.debug("passwordParameter: " + passwordParameter);
				logger.debug("logoutUrl: " + logoutUrl);
				logger.debug("targetUrlParameter: " + targetUrlParameter);
				logger.debug("alwaysUseDefaultTargetUrl: " + alwaysUseDefaultTargetUrl);
				logger.debug("defaultTargetUrl: " + defaultTargetUrl);
				logger.debug("expiredUrl: " + expiredUrl);
				logger.debug("errorPageUrl: " + errorPageUrl);
				logger.debug("redirectUrlParameter: " + redirectUrlParameter);

				logger.debug("maximumSessions: " + maximumSessions);
				logger.debug("exceptionIfMaximumExceeded: " + exceptionIfMaximumExceeded);
			}

			UnauthorizedEntryPoint unauthorizedEntryPoint = new UnauthorizedEntryPoint(loginUrl);

			SignInSuccessHandler signInSuccessHandler = new SignInSuccessHandler();
			signInSuccessHandler.setTargetUrlParameter(targetUrlParameter);
			signInSuccessHandler.setAlwaysUseDefaultTargetUrl(alwaysUseDefaultTargetUrl);
			signInSuccessHandler.setDefaultTargetUrl(defaultTargetUrl);

			SignOutSuccessHandler signOutSuccessHandler = new SignOutSuccessHandler();
			signOutSuccessHandler.setAlwaysUseDefaultTargetUrl(alwaysUseDefaultTargetUrl);
			signOutSuccessHandler.setDefaultTargetUrl(defaultTargetUrl);
			signOutSuccessHandler.setTargetUrlParameter(targetUrlParameter);

			SignInFailureHandler signInFailureHandler = new SignInFailureHandler();
			signInFailureHandler.setDefaultFailureUrl(defaultTargetUrl);

			AccessFailureHandler accessFailureHandler = new AccessFailureHandler(loginUrl, errorPageUrl);
			accessFailureHandler.setRedirectUrlParameter(redirectUrlParameter);

			SessionFixationProtectionStrategy sessionFixationProtectionStrategy = new SessionFixationProtectionStrategy();
			RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry);

			// SessionAuthenticationStrategy : SAS

			// @see ConcurrentSessionControlAuthenticationHandler
			ConcurrentSessionControlAuthenticationStrategy controlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
			controlAuthenticationStrategy.setMaximumSessions(maximumSessions);
			controlAuthenticationStrategy.setExceptionIfMaximumExceeded(exceptionIfMaximumExceeded);

			List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>();
			delegateStrategies.add(controlAuthenticationStrategy);
			delegateStrategies.add(sessionFixationProtectionStrategy);
			delegateStrategies.add(registerSessionAuthenticationStrategy);
			SessionAuthenticationStrategy sas =  new CompositeSessionAuthenticationStrategy(delegateStrategies);
			// SessionAuthenticationStrategy : SAS

			// Custom Filter Setting
			UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter();
			usernamePasswordAuthenticationFilter.setSessionAuthenticationStrategy(sas);
			usernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager);

			ConcurrentSessionFilter concurrentSessionFilter = new ConcurrentSessionFilter(sessionRegistry, expiredUrl);
			// Custom Filter Setting

			// Remember ME Service
			String rememberMeKey = "_MEI_";
			TokenBasedRememberMeServices rememberMeServices = new TokenBasedRememberMeServices(rememberMeKey, userDetailsService);
			rememberMeServices.setCookieName("mei_remember_me");
			rememberMeServices.setParameter("remember_me");
			rememberMeServices.setTokenValiditySeconds(60 * 60 * 24 * 31); // 1 month
			// Remember ME Service))));

			AccessMatchingService accessMatchingRole = new AccessMatchingService(accessControlService);

			FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
			filterSecurityInterceptor.setSecurityMetadataSource(new SecurityMetadataSource(accessMatchingRole));
			filterSecurityInterceptor.setAuthenticationManager(authenticationManager);
			filterSecurityInterceptor.setAccessDecisionManager(new AccessDecisionService(accessMatchingRole));

			http
					.sessionManagement()
					.sessionAuthenticationStrategy(sas)
					.sessionCreationPolicy(SessionCreationPolicy.NEVER)

					.and().exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint)
					.and().exceptionHandling().accessDeniedHandler(accessFailureHandler)

					.and()
					.formLogin()
					.loginPage(loginUrl)
					.usernameParameter(usernameParameter)
					.passwordParameter(passwordParameter)
					.loginProcessingUrl(loginUrl).permitAll()
					.failureHandler(signInFailureHandler)
					.successHandler(signInSuccessHandler)

					.and()
					.logout()
					.invalidateHttpSession(true)
					.logoutUrl(logoutUrl)
					.logoutSuccessHandler(signOutSuccessHandler)
					.deleteCookies(cookieName)

					.and()
					.rememberMe()
					.key(rememberMeKey)
					.rememberMeServices(rememberMeServices)

					.and()
					.addFilter(concurrentSessionFilter)
					.addFilter(usernamePasswordAuthenticationFilter)
					.addFilter(filterSecurityInterceptor)
					.csrf().disable()
					.authorizeRequests();
		}
	}

}
