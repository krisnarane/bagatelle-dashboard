package com.bagatelle.fidelidade.controller;

import com.bagatelle.fidelidade.dto.ProdutoDTO;
import com.bagatelle.fidelidade.model.Produto;
import com.bagatelle.fidelidade.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public String listar(Model model) {
        List<Produto> produtos = produtoService.listarTodos();
        model.addAttribute("paginaAtiva", "produtos");
        model.addAttribute("produtos", produtos);
        return "produto/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("paginaAtiva", "produtos");
        model.addAttribute("acao", "Cadastrar");
        return "produto/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Produto produto,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (result.hasErrors()) {
            model.addAttribute("acao", "Cadastrar");
            return "produto/form";
        }

        try {
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto cadastrado com sucesso!");
            return "redirect:/produtos";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("acao", "Cadastrar");
            return "produto/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        try {
            Produto produto = produtoService.buscarPorId(id);
            model.addAttribute("produto", produto);
            model.addAttribute("paginaAtiva", "produtos");
            model.addAttribute("acao", "Atualizar");
            return "produto/form";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "redirect:/produtos";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                           @Valid @ModelAttribute Produto produto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("acao", "Atualizar");
            return "produto/form";
        }

        try {
            produtoService.atualizar(id, produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto atualizado com sucesso!");
            return "redirect:/produtos";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("acao", "Atualizar");
            return "produto/form";
        }
    }

    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto deletado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao deletar produto: " + e.getMessage());
        }
        return "redirect:/produtos";
    }

    // Endpoint AJAX para buscar produtos
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<List<ProdutoDTO>> buscar(@RequestParam String termo) {
        List<Produto> produtos = produtoService.buscarPorNomeOuMarca(termo);
        List<ProdutoDTO> produtosDTO = produtos.stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtosDTO);
    }
}