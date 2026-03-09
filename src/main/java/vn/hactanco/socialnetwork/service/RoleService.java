package vn.hactanco.socialnetwork.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.exception.ResourceAlreadyExistsException;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.Role;
import vn.hactanco.socialnetwork.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {

	private final RoleRepository roleRepository;

	public Role createRole(Role role) {

		if (roleRepository.existsByName(role.getName())) {
			throw new ResourceAlreadyExistsException("Role với tên: " + role.getName() + " đã tồn tại");
		}

		return roleRepository.save(role);
	}

	public List<Role> getAllRoles() {
		return roleRepository.findAll();
	}

	public void updateRole(Long id, Role roleData) {

		Role role = roleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với id: " + id));

		role.setName(roleData.getName());
		role.setDescription(roleData.getDescription());

		roleRepository.save(role);
	}

	public void deleteRole(Long id) {
		roleRepository.deleteById(id);
	}

	public Role getRoleById(Long id) {
		return roleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với id: " + id));
	}
}