package org.mei.core.security.authorization;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 인증(로그인) 후 얻는 사용자 정보(User) 정보
 * @see UserDetails
 * @see User
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 16. 6. 17.
 */
public class Consumer implements UserDetails, CredentialsContainer {
	private User user;
	/**
	 * 사용자 권한 1개.
	 */
	private String groupRole;

	public Consumer(String username, String password) {
		this(username, password, true, true, true, true, Collections.EMPTY_LIST, null);
	}

	public Consumer(String username, String password, String groupRole) {
		this(username, password, true, true, true, true, Collections.EMPTY_LIST, groupRole);
	}

	public Consumer(String username, String password, boolean accountNonLocked, String groupRole) {
		this(username, password, true, true, true, accountNonLocked, Collections.EMPTY_LIST, groupRole);
	}

	public Consumer(String username, String password, boolean enabled,
						  boolean accountNonExpired, boolean credentialsNonExpired,
						  boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, String groupRole) {

		if (((username == null) || "".equals(username)) || (password == null)) {
			throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
		}

		this.user = new User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		this.groupRole = groupRole;
	}

	public String getGroupRole() {
		return groupRole;
	}

	@Override
	public void eraseCredentials() {
		user.eraseCredentials();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getAuthorities();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return user.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return user.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return user.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}
}
