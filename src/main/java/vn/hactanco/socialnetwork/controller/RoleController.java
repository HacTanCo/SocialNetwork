package vn.hactanco.socialnetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.exception.ResourceAlreadyExistsException;
import vn.hactanco.socialnetwork.model.Role;
import vn.hactanco.socialnetwork.service.RoleService;

@Controller
@RequiredArgsConstructor
public class RoleController {

	private final RoleService roleService;

	@GetMapping("/roles")
	public String showRoles(Model model) {
		model.addAttribute("roles", roleService.getAllRoles());
		return "roles/show";
	}

	@GetMapping("/roles/create")
	public String getCreatePage(Model model) {
		model.addAttribute("role", new Role());
		return "roles/create";
	}

	@PostMapping("/roles/create")
	public String createRole(@Valid @ModelAttribute Role role, BindingResult result,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			return "roles/create";
		}

		try {

			roleService.createRole(role);
			redirectAttributes.addFlashAttribute("success", "Tạo role thành công!");

		} catch (ResourceAlreadyExistsException ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());

			return "redirect:/roles/create";
		}

		return "redirect:/roles";
	}

	//
	@GetMapping("/roles/{id}")
	public String updateRole(@PathVariable Long id, Model model) {

		Role updateRole = roleService.getRoleById(id);
		model.addAttribute("role", updateRole);
		model.addAttribute("id", id);

		return "roles/update";
	}

	@PostMapping("roles/update")
	public String updateRole(@Valid @ModelAttribute Role role, BindingResult result, Model model) {

		if (result.hasErrors()) {
			return "roles/edit";
		}

		roleService.updateRole(role.getId(), role);

		return "redirect:/roles";
	}

	@PostMapping("/roles/delete/{id}")
	public String deleteRole(@PathVariable Long id) {

		roleService.deleteRole(id);

		return "redirect:/roles";
	}
}