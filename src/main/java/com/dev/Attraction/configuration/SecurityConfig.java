package com.dev.Attraction.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dev.Attraction.handler.LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	private DataSource dataSource;
	private PasswordEncoder passwordEncoder;
	
	public SecurityConfig(@Lazy DataSource dataSource, @Lazy BCryptPasswordEncoder passwordEncoder) {
		this.dataSource = dataSource;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().disable();
		http.csrf().disable();
		http.authorizeRequests()
				.antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
				.antMatchers("/**").permitAll()
			.anyRequest().authenticated()
			.and()
		.formLogin()
//			.loginPage("/admin/loginForm")
			.successHandler(new LoginSuccessHandler())
			.permitAll()
			.and()
		.logout()
			.logoutUrl("/logout")
			.logoutSuccessUrl("/")
			.deleteCookies("JSESSIONID")
			.invalidateHttpSession(true)
			.permitAll();
	}	
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) 
	  throws Exception {
	    auth.jdbcAuthentication()
	      .dataSource(dataSource)
	      .passwordEncoder(passwordEncoder)
	      .usersByUsernameQuery("select username, password, enabled "
	        + "from member "
	        + "where username = ?")
	      .authoritiesByUsernameQuery("select u.username, r.ROLE_NAME "
	        + "from member_role ur inner join member u on ur.MEMBER_ID = u.id "
	        + "inner join role r on ur.ROLE_ID = r.ROLE_ID "
	        + "where u.username = ?");
	}
}
