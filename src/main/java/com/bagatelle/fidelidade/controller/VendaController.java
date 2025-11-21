package com.bagatelle.fidelidade.controller;

//import com.bagatelle.fidelidade.dto.ItemVendaDTO;
import com.bagatelle.fidelidade.model.ItemVenda;
import com.bagatelle.fidelidade.model.Produto;
import com.bagatelle.fidelidade.model.Venda;
import com.bagatelle.fidelidade.service.ProdutoService;
import com.bagatelle.fidelidade.service.VendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;
    private final ProdutoService produtoService;

    @GetMapping
    public String listar(Model model) {
        List<Venda> vendas = vendaService.listarTodas();
        model.addAttribute("paginaAtiva", "vendas");
        model.addAttribute("vendas", vendas);
        return "venda/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("paginaAtiva", "nova-venda");
        return "venda/nova";
    }

    @PostMapping("/finalizar")
    public String finalizar(@RequestParam Long clienteId,
                           @RequestParam(required = false) BigDecimal valorCashbackUsado,
                           @RequestParam List<Long> produtoIds,
                           @RequestParam List<Integer> quantidades,
                           RedirectAttributes redirectAttributes) {
        try {
            // Valida parâmetros
            if (produtoIds == null || produtoIds.isEmpty()) {
                throw new RuntimeException("É necessário adicionar pelo menos um produto");
            }

            if (produtoIds.size() != quantidades.size()) {
                throw new RuntimeException("Dados inválidos: produtos e quantidades não correspondem");
            }

            // Cria lista de itens
            List<ItemVenda> itens = new ArrayList<>();
            for (int i = 0; i < produtoIds.size(); i++) {
                Produto produto = produtoService.buscarPorId(produtoIds.get(i));
                Integer quantidade = quantidades.get(i);

                if (quantidade == null || quantidade < 1) {
                    throw new RuntimeException("Quantidade inválida para o produto: " + produto.getNomePerfume());
                }

                ItemVenda item = ItemVenda.builder()
                        .produto(produto)
                        .quantidade(quantidade)
                        .precoUnitario(produto.getPrecoVenda())
                        .build();

                itens.add(item);
            }

            // Registra a venda
            if (valorCashbackUsado == null || valorCashbackUsado.compareTo(BigDecimal.ZERO) < 0) {
                valorCashbackUsado = BigDecimal.ZERO;
            }

            Venda venda = vendaService.registrarVenda(clienteId, itens, valorCashbackUsado);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    String.format("Venda #%d registrada com sucesso! Cashback gerado: R$ %.2f",
                            venda.getId(),
                            venda.getValorTotal().multiply(new BigDecimal("0.05"))));

            return "redirect:/vendas/" + venda.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao registrar venda: " + e.getMessage());
            return "redirect:/vendas/nova";
        }
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        try {
            Venda venda = vendaService.buscarPorId(id);
            model.addAttribute("venda", venda);
            model.addAttribute("paginaAtiva", "vendas");

            // Calcula informações adicionais
            BigDecimal cashbackGerado = venda.calcularValorFinal().multiply(new BigDecimal("0.05"));
            model.addAttribute("cashbackGerado", cashbackGerado);

            return "venda/detalhes";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "redirect:/vendas";
        }
    }
}
