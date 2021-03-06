package org.mei.core.security.filter;

import org.mei.core.security.access.AccessMatchingService;
import org.mei.core.security.enums.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 16. 6. 10.
 */
public class SecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AccessMatchingService accessMatchingRole;

	public SecurityMetadataSource(AccessMatchingService accessMatchingRole) {
		this.accessMatchingRole = accessMatchingRole;
	}

	@Override
	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
		FilterInvocation filter = (FilterInvocation) object;
		HttpServletRequest request = filter.getRequest();

		String path = request.getServletPath();
		String method = request.getMethod();

		Collection<ConfigAttribute> attributes = accessMatchingRole.needConfigAttributes(path, Method.valueOf(method));

		return attributes;
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return false;
	}
}
