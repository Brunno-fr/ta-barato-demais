package com.tabaratodemais.admin;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin/login")
    public String login() { return "admin/login"; }

    @GetMapping({"/admin", "/admin/"})
    public String dashboard(Principal principal, Model model) {
        model.addAttribute("email", principal.getName());
        return "admin/dashboard";
    }
}
