package org.mei.core.security.authorization;

import org.mei.core.security.enums.Permission;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * 인증(로그인) 후 허가받은 권한(Role) 정보
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 16. 6. 15.
 */
public class Authority implements GrantedAuthority {
	private static final long serialVersionUID = -7125481982234132933L;

	private final String roleName;
	private final List<Permission> privilege;

	public Authority(String roleName, List<Permission> privilege) {
		this.roleName = roleName;
		this.privilege = privilege;
	}

	@Override
	public String getAuthority() {
		return roleName;
	}

	public List<Permission> getPrivilege() {
		return privilege;
	}

	@Override
	public String toString() {
		return super.toString() + '{' +
				"roleName='" + roleName + '\'' +
				", privilege=" + privilege +
				'}';
	}
}
