import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

public class TestadorNextGen {
    private static String senhaCadastrada = null;
    private static File arquivoWordlist = null;

    public static void main(String[] args) {
        while (true) {
            String[] opcoes = {"Cadastrar Senha", "Escolher Wordlist", "Testar Senha", "Sair"};
            int escolha = JOptionPane.showOptionDialog(
                    null,
                    "Testador de Senhas - Escolha uma opção:\n" +
                            (arquivoWordlist != null ? "Wordlist atual: " + arquivoWordlist.getName() : "Nenhuma wordlist selecionada"),
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
                    escolherWordlist();
                    break;
                case 2:
                    testarSenha();
                    break;
                case 3:
                case -1:
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

        if (senhaCadastrada == null) return;

        if (senhaCadastrada.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Senha não pode estar vazia!", "Erro", JOptionPane.ERROR_MESSAGE);
            senhaCadastrada = null;
        } else {
            JOptionPane.showMessageDialog(null, "Senha cadastrada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void escolherWordlist() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Escolha o arquivo de wordlist");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".txt") ||
                        f.getName().toLowerCase().endsWith(".lst");
            }

            public String getDescription() {
                return "Arquivos de texto (*.txt, *.lst)";
            }
        });

        int resultado = fileChooser.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            arquivoWordlist = fileChooser.getSelectedFile();

            if (!arquivoWordlist.exists() || !arquivoWordlist.canRead()) {
                JOptionPane.showMessageDialog(null,
                        "Não foi possível ler o arquivo selecionado!",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                arquivoWordlist = null;
            } else {
                long tamanho = arquivoWordlist.length();
                String tamanhoFormatado = formatarTamanho(tamanho);

                JOptionPane.showMessageDialog(null,
                        "Wordlist selecionada com sucesso!\n" +
                                "Arquivo: " + arquivoWordlist.getName() + "\n" +
                                "Tamanho: " + tamanhoFormatado + "\n" +
                                "Local: " + arquivoWordlist.getParent(),
                        "Wordlist Carregada",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private static String formatarTamanho(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024L * 1024L) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private static void testarSenha() {
        if (senhaCadastrada == null) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma senha cadastrada! Por favor, cadastre uma senha primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (arquivoWordlist == null) {
            JOptionPane.showMessageDialog(null,
                    "Nenhuma wordlist selecionada! Por favor, escolha um arquivo de wordlist.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long tamanhoLimite = 100 * 1024 * 1024; // 100MB
        if (arquivoWordlist.length() > tamanhoLimite) {
            String tamanho = formatarTamanho(arquivoWordlist.length());
            int confirm = JOptionPane.showConfirmDialog(null,
                    "A wordlist selecionada é muito grande (" + tamanho + ").\n" +
                            "O teste pode demorar vários minutos e consumir muita memória.\n\n" +
                            "Deseja continuar mesmo assim?",
                    "Wordlist Grande",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        JOptionPane.showMessageDialog(null,
                "Iniciando teste de senha...\n" +
                        "Arquivo: " + arquivoWordlist.getName() + "\n" +
                        "Tamanho: " + formatarTamanho(arquivoWordlist.length()) + "\n" +
                        "Isso pode demorar dependendo do tamanho do arquivo.",
                "Testando Senha",
                JOptionPane.INFORMATION_MESSAGE);

        try {
            String resultado = buscarSenhaNoArquivo();
            JOptionPane.showMessageDialog(null, resultado, "Resultado do Teste", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao ler o arquivo: " + e.getMessage() + "\n" +
                            "Verifique se o arquivo não está corrompido e tente novamente.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String buscarSenhaNoArquivo() throws IOException {
        Path path = arquivoWordlist.toPath();

        Charset[] charsetsToTry = new Charset[] {
                StandardCharsets.UTF_8,
                Charset.forName("windows-1252"),
                StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16
        };

        IOException ultimaEx = null;

        for (Charset cs : charsetsToTry) {
            try {
                return buscarSenhaComCharset(path, cs);
            } catch (IOException e) {
                // Detecta se é problema de decodificação (mensagens comuns: "Input length = 1", "malformed", "unmappable")
                if (isDecodingError(e)) {
                    System.err.println("Problema de decodificação com " + cs.name() + " — tentando próximo charset.");
                    // tentar próximo charset
                    continue;
                } else {
                    // outro problema de E/S: repassa
                    ultimaEx = e;
                    break;
                }
            }
        }

        if (ultimaEx != null) throw ultimaEx;
        throw new IOException("Não foi possível decodificar o arquivo usando os charsets testados.");
    }

    private static String buscarSenhaComCharset(Path path, Charset cs) throws IOException {
        int linhaAtual = 0;
        int senhasEncontradas = 0;
        List<String> senhasIguais = new ArrayList<>();
        long inicio = System.currentTimeMillis();

        try (BufferedReader reader = Files.newBufferedReader(path, cs)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linhaAtual++;
                if (!linha.trim().isEmpty() && linha.equals(senhaCadastrada)) {
                    senhasEncontradas++;
                    senhasIguais.add("Linha " + linhaAtual + ": " + linha);
                }
                if (linhaAtual % 50000 == 0) {
                    System.out.println("Processadas " + linhaAtual + " linhas... (charset " + cs.name() + ")");
                }
            }
        }

        long tempoExecucao = System.currentTimeMillis() - inicio;
        String resultado = construirResultado(linhaAtual, senhasEncontradas, senhasIguais, tempoExecucao);
        return "Charset detectado/sucedido: " + cs.name() + "\n\n" + resultado;
    }

    private static boolean isDecodingError(Throwable e) {
        if (e == null) return false;
        // procura por causas conhecidas e mensagens típicas
        if (e instanceof CharacterCodingException) return true;
        String msg = e.getMessage();
        if (msg != null) {
            String m = msg.toLowerCase();
            if (m.contains("input length") || m.contains("malformed") || m.contains("unmappable") || m.contains("invalid")) return true;
        }
        return isDecodingError(e.getCause());
    }

    private static String construirResultado(int totalLinhas, int encontradas, List<String> ocorrencias, long tempoMs) {
        StringBuilder resultado = new StringBuilder();
        resultado.append("═".repeat(50)).append("\n");
        resultado.append("RESULTADO DO TESTE DE SENHA\n");
        resultado.append("═".repeat(50)).append("\n");
        resultado.append("Arquivo testado: ").append(arquivoWordlist.getName()).append("\n");
        //resultado.append("Senha testada: ").append(senhaCadastrada).append("\n");
        resultado.append("Tempo de execução: ").append(tempoMs / 1000.0).append(" segundos\n");
        resultado.append("Total de linhas processadas: ").append(String.format("%,d", totalLinhas)).append("\n");
        resultado.append("Ocorrências encontradas: ").append(encontradas).append("\n\n");

        if (encontradas > 0) {
            resultado.append("🚨 ATENÇÃO: SENHA ENCONTRADA NA WORDLIST!\n");
            resultado.append("Esta senha é fraca/comum e deve ser alterada!\n\n");
            resultado.append("Ocorrências encontradas:\n");
            for (String ocorrencia : ocorrencias) {
                resultado.append("• ").append(ocorrencia).append("\n");
            }
            resultado.append("\n🔒 RECOMENDAÇÃO: Use uma senha mais forte e única!");
        } else {
            resultado.append("✅ Senha não encontrada na wordlist\n");
            resultado.append("Isso é um bom sinal de segurança.\n\n");
            resultado.append("💡 Dicas para senhas seguras:\n");
            resultado.append("• Use pelo menos 12 caracteres\n");
            resultado.append("• Combine letras, números e símbolos\n");
            resultado.append("• Evite palavras do dicionário\n");
            resultado.append("• Não reuse senhas entre serviços");
        }

        return resultado.toString();
    }
}
