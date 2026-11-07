package com.tabaratodemais.admin;

import com.tabaratodemais.offer.PriceHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PriceHistoryController {
    private final PriceHistoryService service;

    public PriceHistoryController(PriceHistoryService service) { this.service = service; }

    @GetMapping("/admin/produtos/{id}/ofertas/{offerId}/historico")
    public String history(@PathVariable Long id, @PathVariable Long offerId,
                          @RequestParam(defaultValue = "1") int pagina, Model model) {
        model.addAttribute("history", service.get(id, offerId, pagina));
        model.addAttribute("historyUrl", "/admin/produtos/" + id + "/ofertas/" + offerId + "/historico");
        return "admin/products/history";
    }
}
