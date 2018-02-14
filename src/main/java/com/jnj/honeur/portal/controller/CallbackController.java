package com.jnj.honeur.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CallbackController {

    @RequestMapping("/callback2")
    public String callback() {

        return "hello";
    }
}
