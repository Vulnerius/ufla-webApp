package org.hsmw.uflaWebApp.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class CascadeController {
    @RequestMapping("/netzentlastung")
    fun showCascade(model: Model): String{
        model.addAttribute("headline", "Kaskade-Tool")
        model.addAttribute("query", "Abfragen")
        model.addAttribute("block", "Blocklist")
        model.addAttribute("reset", "Info löschen")
        model.addAttribute("saveAs", "Speichern unter")
        model.addAttribute("help", "Hilfe")

        return "netzentlastung/index"
    }
}