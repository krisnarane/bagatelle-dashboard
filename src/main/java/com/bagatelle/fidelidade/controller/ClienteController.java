package com.bagatelle.fidelidade.controller;

import com.bagatelle.fidelidade.dto.ClienteDTO;
import com.bagatelle.fidelidade.model.Cliente;
import com.bagatelle.fidelidade.service.ClienteService;
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
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        List<Cliente> clientes = clienteService.listarTodos();
        model.addAttribute("paginaAtiva", "clientes");
        model.addAttribute("clientes", clientes);
        return "cliente/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("paginaAtiva", "clientes");
        model.addAttribute("acao", "Cadastrar");
        return "cliente/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Cliente cliente,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (result.hasErrors()) {
            model.addAttribute("acao", "Cadastrar");
            return "cliente/form";
        }

        try {
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente cadastrado com sucesso!");
            return "redirect:/clientes";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("acao", "Cadastrar");
            return "cliente/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        try {
            Cliente cliente = clienteService.buscarPorId(id);
            model.addAttribute("cliente", cliente);
            model.addAttribute("paginaAtiva", "clientes");
            model.addAttribute("acao", "Atualizar");
            return "cliente/form";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                           @Valid @ModelAttribute Cliente cliente,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("acao", "Atualizar");
            return "cliente/form";
        }

        try {
            clienteService.atualizar(id, cliente);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente atualizado com sucesso!");
            return "redirect:/clientes";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("acao", "Atualizar");
            return "cliente/form";
        }
    }

    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente deletado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao deletar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    // Endpoint AJAX para buscar clientes
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<List<ClienteDTO>> buscar(@RequestParam String termo) {
        List<Cliente> clientes = clienteService.buscarPorCpfOuNome(termo);
        List<ClienteDTO> clientesDTO = clientes.stream()
                .map(ClienteDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientesDTO);
    }
}