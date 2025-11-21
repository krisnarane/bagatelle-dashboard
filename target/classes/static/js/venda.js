// venda.js - Script para funcionalidades AJAX da tela de Nova Venda

let clienteSelecionado = null;
let carrinho = [];

$(document).ready(function() {
    configurarBuscaCliente();
    configurarBuscaProduto();
    configurarCashback();
});

// === BUSCA DE CLIENTE ===
function configurarBuscaCliente() {
    let timeout = null;

    $('#buscaCliente').on('keyup', function() {
        const termo = $(this).val().trim();
        clearTimeout(timeout);

        if (termo.length < 2) {
            $('#resultadosCliente').empty();
            return;
        }

        timeout = setTimeout(() => buscarClientes(termo), 500);
    });
}

function buscarClientes(termo) {
    $.ajax({
        url: '/clientes/api/buscar',
        method: 'GET',
        data: { termo: termo },
        success: function(clientes) {
            exibirResultadosCliente(clientes);
        },
        error: function() {
            alert('Erro ao buscar clientes');
        }
    });
}

function exibirResultadosCliente(clientes) {
    const container = $('#resultadosCliente');
    container.empty();

    if (clientes.length === 0) {
        container.append('<div class="list-group-item">Nenhum cliente encontrado</div>');
        return;
    }

    clientes.forEach(cliente => {
        const item = $('<a>')
            .addClass('list-group-item list-group-item-action')
            .attr('href', '#')
            .html(`
                <strong>${cliente.nomeCompleto}</strong><br>
                <small>CPF: ${formatarCPF(cliente.cpf)} | Cashback: R$ ${cliente.saldoCashback.toFixed(2)}</small>
            `)
            .click(function(e) {
                e.preventDefault();
                selecionarCliente(cliente);
            });

        container.append(item);
    });
}

function selecionarCliente(cliente) {
    clienteSelecionado = cliente;
    $('#clienteId').val(cliente.id);
    $('#clienteNome').text(cliente.nomeCompleto);
    $('#clienteCpf').text(formatarCPF(cliente.cpf));
    $('#clienteSaldo').text(cliente.saldoCashback.toFixed(2));
    $('#clienteSelecionado').removeClass('d-none');
    $('#resultadosCliente').empty();
    $('#buscaCliente').val('');

    // Mostrar seção de produtos
    $('#secaoProdutos').show();
}

// === BUSCA DE PRODUTO ===
function configurarBuscaProduto() {
    let timeout = null;

    $('#buscaProduto').on('keyup', function() {
        const termo = $(this).val().trim();
        clearTimeout(timeout);

        if (termo.length < 2) {
            $('#resultadosProduto').empty();
            return;
        }

        timeout = setTimeout(() => buscarProdutos(termo), 500);
    });
}

function buscarProdutos(termo) {
    $.ajax({
        url: '/produtos/api/buscar',
        method: 'GET',
        data: { termo: termo },
        success: function(produtos) {
            exibirResultadosProduto(produtos);
        },
        error: function() {
            alert('Erro ao buscar produtos');
        }
    });
}

function exibirResultadosProduto(produtos) {
    const container = $('#resultadosProduto');
    container.empty();

    if (produtos.length === 0) {
        container.append('<div class="list-group-item">Nenhum produto encontrado</div>');
        return;
    }

    produtos.forEach(produto => {
        const item = $('<a>')
            .addClass('list-group-item list-group-item-action')
            .attr('href', '#')
            .html(`
                <strong>${produto.nomePerfume}</strong> - ${produto.marca}<br>
                <small>${produto.volume} | R$ ${produto.precoVenda.toFixed(2)}</small>
            `)
            .click(function(e) {
                e.preventDefault();
                adicionarProdutoCarrinho(produto);
            });

        container.append(item);
    });
}

function adicionarProdutoCarrinho(produto) {
    carrinho.push({
        produtoId: produto.id,
        nomePerfume: produto.nomePerfume,
        marca: produto.marca,
        volume: produto.volume,
        quantidade: 1,
        precoUnitario: produto.precoVenda
    });

    atualizarCarrinho();
    $('#resultadosProduto').empty();
    $('#buscaProduto').val('');
}

function atualizarCarrinho() {
    const tbody = $('#carrinhoItens');
    tbody.empty();

    let subtotal = 0;

    carrinho.forEach((item, index) => {
        const itemSubtotal = item.quantidade * item.precoUnitario;
        subtotal += itemSubtotal;

        const row = $('<tr>').html(`
            <td>${item.nomePerfume} - ${item.marca} (${item.volume})</td>
            <td>
                <input type="number" class="form-control form-control-sm" style="width: 80px"
                       min="1" value="${item.quantidade}" data-index="${index}">
            </td>
            <td>R$ ${item.precoUnitario.toFixed(2)}</td>
            <td>R$ ${itemSubtotal.toFixed(2)}</td>
            <td>
                <button class="btn btn-sm btn-danger" data-index="${index}">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `);

        tbody.append(row);
    });

    // Event handlers para quantidade e remover
    tbody.find('input[type="number"]').on('change', function() {
        const index = $(this).data('index');
        const novaQtd = parseInt($(this).val());
        if (novaQtd >= 1) {
            carrinho[index].quantidade = novaQtd;
            atualizarCarrinho();
        }
    });

    tbody.find('button').on('click', function() {
        const index = $(this).data('index');
        carrinho.splice(index, 1);
        atualizarCarrinho();
    });

    $('#subtotalVenda').text(subtotal.toFixed(2));

    // Atualizar valor final
    const cashbackUsado = parseFloat($('#valorCashback').val()) || 0;
    const valorFinal = Math.max(0, subtotal - cashbackUsado);
    $('#valorFinal').text(valorFinal.toFixed(2));

    // Mostrar seções de cashback e finalizar se houver itens
    if (carrinho.length > 0) {
        $('#secaoCashback').show();
        $('#secaoFinalizar').show();
    } else {
        $('#secaoCashback').hide();
        $('#secaoFinalizar').hide();
    }
}

// === CASHBACK ===
function configurarCashback() {
    $('#valorCashback').on('input', function() {
        let valor = parseFloat($(this).val()) || 0;
        const saldoDisponivel = clienteSelecionado ? clienteSelecionado.saldoCashback : 0;
        const subtotal = parseFloat($('#subtotalVenda').text()) || 0;

        // Validar limites
        if (valor > saldoDisponivel) {
            valor = saldoDisponivel;
            $(this).val(valor.toFixed(2));
            alert('Valor de cashback não pode ser maior que o saldo disponível!');
        }

        if (valor > subtotal) {
            valor = subtotal;
            $(this).val(valor.toFixed(2));
            alert('Valor de cashback não pode ser maior que o subtotal da compra!');
        }

        const valorFinal = Math.max(0, subtotal - valor);
        $('#valorFinal').text(valorFinal.toFixed(2));
    });

    // Preparar dados para envio
    $('#formVenda').on('submit', function() {
        // Atualizar campo hidden de cashback
        $('#cashbackUsado').val($('#valorCashback').val() || 0);

        // Adicionar produtos como campos hidden
        const container = $('#produtosHidden');
        container.empty();

        carrinho.forEach(item => {
            container.append(`<input type="hidden" name="produtoIds" value="${item.produtoId}">`);
            container.append(`<input type="hidden" name="quantidades" value="${item.quantidade}">`);
        });

        return true;
    });
}

// === UTILITÁRIOS ===
function formatarCPF(cpf) {
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}
