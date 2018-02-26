package com.jnj.honeur.portal.controller;

import com.jnj.honeur.security.SecurityUtils2;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PortalNavigationController {

    private static final String ROLE_ADMIN = "admin";

    @Value("${portal.atlasUrl}")
    private String atlasUrl;
    @Value("${portal.notebooksUrl}")
    private String notebooksUrl;
    @Value("${portal.userManagementUrl}")
    private String userManagementUrl;

    @RequiresAuthentication
    @RequestMapping("/portal")
    public String home(HttpServletRequest request, Model model) {
        final Subject subject = SecurityUtils.getSubject();
        model.addAttribute("subject", subject);
        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("subjectIsAdmin", subject.hasRole(ROLE_ADMIN));
        return "portal";
    }

    @RequiresAuthentication
    @RequestMapping("/atlas")
    public String atlas(HttpServletRequest request, Model model) {
        return "redirect:" + atlasUrl;
    }

    @RequiresAuthentication
    @RequestMapping("/notebooks")
    public String notebooks(HttpServletRequest request, Model model) {
        return "redirect:" + notebooksUrl;
    }

    @RequiresRoles("admin")
    @RequestMapping("/userManagement")
    public String userManagement(HttpServletRequest request, Model model) {
        return "redirect:" + userManagementUrl;
    }

}

