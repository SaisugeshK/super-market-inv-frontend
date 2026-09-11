package com.example.InventoryManagementSystem.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// The React dist/ build is served as static content from resources/static.
// This forwards deep-link routes like /products/42 back to index.html so
// React Router can take over — without it, refreshing on a non-root route 404s.
@Controller
public class SpaForwardingController {

    @RequestMapping(value = "/{path:^(?!api|assets|.*\\..*).*$}/**")
    public String forward() {
        return "forward:/index.html";
    }
}
