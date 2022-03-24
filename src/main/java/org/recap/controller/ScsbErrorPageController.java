package org.recap.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by rajeshbabuk on 1/11/16.
 */
@Controller
public class ScsbErrorPageController implements ErrorController {

    private static final String PATH = "/error";

    /**
     * This method is used to display the error page.
     *
     * @return the string
     */
    @RequestMapping(value = PATH, method = {RequestMethod.GET,RequestMethod.POST})
    public String recapErrorPage() {
        return "error";
    }


    public String getErrorPath() {
        return PATH;
    }
}
