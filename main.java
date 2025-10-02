import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestadorSenhas {
    private static String senhaCadastrada = null;
    private static final String ARQUIVO_SENHAS = "rockyou.txt";

    public static void main(String[] args) {
        while (true) {
            String[] opcoes = {"Cadastrar Senha", "Testar Senha", "Sair"};
            int escolha = JOptionPane.showOptionDialog(
                null,
                "Testador de Senhas - Escolha uma opção:",
                "Menu Principal",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opcoes,
                opcoes[0]
            );

            switch (escolha) {
                case 0:
                    cadastrarSenha();
                    break;
                case 1:
                    testarSenha();
                    break;
                case 2:
                case -1: // Fechar a janela
                    JOptionPane.showMessageDialog(null, "Programa encerrado!");
                    System.exit(0);
                    break;
            }
        }
    }

    private static void cadastrarSenha() {
        senhaCadastrada = JOptionPane.showInputDialog(
            null,
            "Digite a senha a ser testada:",
            "Cadastrar Senha",
            JOptionPane.QUESTION_MESSAGE
        );

        if (senhaCadastrada == null) {
            // Usuário cancelou
            return;
        }

        if (senhaCadastrada.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                "Senha não pode estar vazia!",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            senhaCadastrada = null;
        } else {
            JOptionPane.showMessageDialog(
                null,
                "Senha cadastrada com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private static void testarSenha() {
        if (senhaCadastrada == null) {
            JOptionPane.showMessageDialog(
                null,
                "Nenhuma senha cadastrada! Por favor, cadastre uma senha primeiro.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Verificar se o arquivo rockyou.txt existe
        if (!Files.exists(Paths.get(ARQUIVO_SENHAS))) {
            JOptionPane.showMessageDialog(
                null,
                "Arquivo " + ARQUIVO_SENHAS + " não encontrado!\n" +
                "Por favor, coloque o arquivo rockyou.txt na mesma pasta do programa.",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Mostrar diálogo de progresso
        JOptionPane.showMessageDialog(
            null,
            "Iniciando teste de senha...\nIsso pode demorar alguns minutos.",
            "Testando Senha",
            JOptionPane.INFORMATION_MESSAGE
        );

        try {
            String resultado = buscarSenhaNoArquivo();
            JOptionPane.showMessageDialog(
                null,
                resultado,
                "Resultado do Teste",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Erro ao ler o arquivo: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String buscarSenhaNoArquivo() throws IOException {
        int linhaAtual = 0;
        int senhasEncontradas = 0;
        List<String> senhasIguais = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ARQUIVO_SENHAS))) {
            String linha;
            
            while ((linha = reader.readLine()) != null) {
                linhaAtual++;
                
                // Verificar se a linha não está vazia e é igual à senha cadastrada
                if (!linha.trim().isEmpty() && linha.equals(senhaCadastrada)) {
                    senhasIguais.add("Linha " + linhaAtual + ": " + linha);
                    senhasEncontradas++;
                }
                
                // Mostrar progresso a cada 100.000 linhas
                if (linhaAtual % 100000 == 0) {
                    System.out.println("Processadas " + linhaAtual + " linhas...");
                }
            }
        }

        // Construir o resultado
        StringBuilder resultado = new StringBuilder();
        resultado.append("Senha testada: ").append(senhaCadastrada).append("\n");
        resultado.append("Total de linhas processadas: ").append(linhaAtual).append("\n");
        resultado.append("Ocorrências encontradas: ").append(senhasEncontradas).append("\n\n");

        if (senhasEncontradas > 0) {
            resultado.append("A SENHA FOI ENCONTRADA NO ARQUIVO!\n");
            resultado.append("Isso significa que é uma senha fraca/comum.\n\n");
            resultado.append("Ocorrências encontradas:\n");
            for (String ocorrencia : senhasIguais) {
                resultado.append(ocorrencia).append("\n");
            }
            
            resultado.append("\nRECOMENDAÇÃO: Troque esta senha por uma mais segura!");
        } else {
            resultado.append("Senha não encontrada no arquivo rockyou.txt.\n");
            resultado.append("Isso é um bom sinal, mas verifique outros aspectos de segurança:\n");
            resultado.append("- Use pelo menos 12 caracteres\n");
            resultado.append("- Combine letras maiúsculas, minúsculas, números e símbolos\n");
            resultado.append("- Evite palavras comuns ou informações pessoais");
        }

        return resultado.toString();
    }
}
