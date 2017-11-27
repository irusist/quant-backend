package com.irusist.quant.quantbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author <a href="mailto:zhulx521@gmail.com">zhulx</a>
 */
@Controller
@EnableAutoConfiguration
public class IndexController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "index2.html";
    }

    public static void main(String[] args) {
        SpringApplication.run(IndexController.class, args);
    }

}
