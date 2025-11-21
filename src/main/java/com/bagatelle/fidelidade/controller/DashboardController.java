package com.bagatelle.fidelidade.controller;

import com.bagatelle.fidelidade.model.RegistroCashback;
import com.bagatelle.fidelidade.model.Venda;
import com.bagatelle.fidelidade.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public String index(Model model) {
        // Busca dados para o dashboard
        List<RegistroCashback> cashbacksExpirando = dashboardService.buscarCashbackExpirando();
        List<Venda> sugestoesRecompra = dashboardService.buscarSugestoesRecompra();

        model.addAttribute("paginaAtiva", "dashboard");
        model.addAttribute("cashbacksExpirando", cashbacksExpirando);
        model.addAttribute("sugestoesRecompra", sugestoesRecompra);
        model.addAttribute("totalCashbacksExpirando", cashbacksExpirando.size());
        model.addAttribute("totalSugestoesRecompra", sugestoesRecompra.size());

        return "dashboard/index";
    }

    @GetMapping("/cashback-expirando")
    public String cashbackExpirando(Model model) {
        List<RegistroCashback> cashbacksExpirando = dashboardService.buscarCashbackExpirando();
        model.addAttribute("paginaAtiva", "dashboard");
        model.addAttribute("registros", cashbacksExpirando);
        return "dashboard/cashback-expirando";
    }

    @GetMapping("/sugestoes-recompra")
    public String sugestoesRecompra(Model model) {
        List<Venda> sugestoesRecompra = dashboardService.buscarSugestoesRecompra();
        model.addAttribute("paginaAtiva", "dashboard");
        model.addAttribute("vendas", sugestoesRecompra);
        return "dashboard/sugestoes-recompra";
    }
}