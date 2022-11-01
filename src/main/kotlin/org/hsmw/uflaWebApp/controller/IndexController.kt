package org.hsmw.uflaWebApp.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class IndexController {

    @RequestMapping("/")
    fun getIndex(model: Model): String {
        model.addAttribute("headline", "ChoosingScreen")
        return "index/index"
    }
}