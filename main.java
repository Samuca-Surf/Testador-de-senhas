import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestadorSenhasFlexivel {
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
        
        // Filtro para arquivos de texto
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || 
                       f.getName().toLowerCase().endsWith(".txt") ||
                       f.getName().toLowerCase().endsWith(".lst") ||
                       f.getName().toLowerCase().endsWith(".wordlist");
            }
            
            public String getDescription() {
                return "Arquivos de Wordlist (*.txt, *.lst, *.wordlist)";
            }
        });

        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            arquivoWordlist = fileChooser.getSelectedFile();
            
            // Verificar se o arquivo existe e é legível
            if (!arquivoWordlist.exists() || !arquivoWordlist.canRead()) {
                JOptionPane.showMessageDialog(null, 
                    "Não foi possível ler o arquivo selecionado!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
                arquivoWordlist = null;
            } else {
                // Mostrar informações básicas do arquivo
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

    private static String formatarTaman