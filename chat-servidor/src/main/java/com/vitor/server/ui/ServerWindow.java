package com.vitor.server.ui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

public class ServerWindow extends JFrame {

    private static final Color FUNDO_TERMINAL = new Color(0x1e, 0x1e, 0x1e);
    private static final Color TEXTO_TERMINAL = new Color(0x7e, 0xe7, 0x87);

    private final DefaultTableModel tabelaModel;
    private final JTable tabelaUsuarios;
    private final JTextArea areaUltimoRecebido;
    private final JTextArea areaUltimoEnviado;

    public ServerWindow(int porta) {
        super("Chat Server — porta " + porta);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        tabelaModel = new DefaultTableModel(new String[]{"Usuário", "IP", "Porta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaUsuarios = new JTable(tabelaModel);
        tabelaUsuarios.setFillsViewportHeight(true);

        areaUltimoRecebido = criarAreaLogTerminal();
        areaUltimoEnviado = criarAreaLogTerminal();

        montarLayout();
    }

    private static JTextArea criarAreaLogTerminal() {
        JTextArea area = new JTextArea();
        area.setBackground(FUNDO_TERMINAL);
        area.setForeground(TEXTO_TERMINAL);
        area.setCaretColor(Color.WHITE);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private void montarLayout() {
        JPanel painelUsuarios = new JPanel(new BorderLayout(0, 6));
        painelUsuarios.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        painelUsuarios.add(new JLabel("Usuários logados"), BorderLayout.NORTH);
        painelUsuarios.add(new JScrollPane(tabelaUsuarios), BorderLayout.CENTER);

        JPanel painelRecebido = new JPanel(new BorderLayout(0, 6));
        painelRecebido.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        painelRecebido.add(new JLabel("Último JSON Recebido"), BorderLayout.NORTH);
        painelRecebido.add(new JScrollPane(areaUltimoRecebido), BorderLayout.CENTER);

        JPanel painelEnviado = new JPanel(new BorderLayout(0, 6));
        painelEnviado.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        painelEnviado.add(new JLabel("Último JSON Enviado / Broadcast"), BorderLayout.NORTH);
        painelEnviado.add(new JScrollPane(areaUltimoEnviado), BorderLayout.CENTER);

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.add(painelUsuarios, BorderLayout.NORTH);
        conteudo.add(painelRecebido, BorderLayout.CENTER);
        conteudo.add(painelEnviado, BorderLayout.SOUTH);

        painelUsuarios.setPreferredSize(new Dimension(0, 180));
        painelEnviado.setPreferredSize(new Dimension(0, 200));

        setContentPane(conteudo);
        pack();
    }

    public void atualizarTabela(List<String[]> dados) {
        SwingUtilities.invokeLater(() -> {
            tabelaModel.setRowCount(0);
            if (dados == null) {
                return;
            }
            for (String[] linha : dados) {
                if (linha != null && linha.length >= 3) {
                    tabelaModel.addRow(new Object[]{linha[0], linha[1], linha[2]});
                }
            }
        });
    }

    public void atualizarUltimoRecebido(String json) {
        SwingUtilities.invokeLater(() -> areaUltimoRecebido.setText(json != null ? json : ""));
    }

    public void atualizarUltimoEnviado(String json) {
        SwingUtilities.invokeLater(() -> areaUltimoEnviado.setText(json != null ? json : ""));
    }
}
