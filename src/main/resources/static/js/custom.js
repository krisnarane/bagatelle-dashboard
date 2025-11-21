// custom.js - Scripts gerais do sistema

$(document).ready(function() {
    // Auto-fechar alertas após 5 segundos
    setTimeout(function() {
        $('.alert').fadeOut('slow');
    }, 5000);

    // Adicionar confirmação em formulários de delete
    $('form[onsubmit*="confirm"]').on('submit', function(e) {
        if (!confirm($(this).attr('onsubmit').match(/'([^']+)'/)[1])) {
            e.preventDefault();
            return false;
        }
    });

    // Máscaras de formatação
    formatarCampos();
});

function formatarCampos() {
    // Formatar CPF em tempo real
    $('input[name="cpf"]').on('input', function() {
        let valor = $(this).val().replace(/\D/g, '');
        $(this).val(valor);
    });

    // Formatar telefone em tempo real
    $('input[name="telefone"]').on('input', function() {
        let valor = $(this).val().replace(/\D/g, '');
        $(this).val(valor);
    });

    // Formatar valores monetários
    $('input[type="number"][step="0.01"]').on('blur', function() {
        const valor = parseFloat($(this).val());
        if (!isNaN(valor)) {
            $(this).val(valor.toFixed(2));
        }
    });
}
