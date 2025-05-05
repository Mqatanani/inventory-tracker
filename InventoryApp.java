import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class InventoryApp {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new InventoryUI().setVisible(true));
    }
}

class InventoryItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String name;
    private int quantity;

    public InventoryItem(long id, String name) {
        this.id = id;
        this.name = name;
        this.quantity = 0;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public void add(int qty) {
        if (qty < 1 || qty > 100 || quantity + qty > 9999)
            throw new IllegalArgumentException("Quantity must be 1-100 and total ≤ 9999");
        quantity += qty;
    }
    public void remove(int qty) {
        if (qty < 1 || qty > 100 || quantity - qty < 0)
            throw new IllegalArgumentException("Quantity must be 1-100 and total ≥ 0");
        quantity -= qty;
    }
}

class InventoryManager {
    private ArrayList<InventoryItem> items = new ArrayList<>();
    public void create(long id, String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name required");
        for (InventoryItem it : items) if (it.getId() == id) throw new IllegalArgumentException("Duplicate ID");
        items.add(new InventoryItem(id, name));
    }
    public void add(long id, int qty) { find(id).add(qty); }
    public void remove(long id, int qty) { find(id).remove(qty); }
    public ArrayList<InventoryItem> list() { return items; }
    private InventoryItem find(long id) {
        for (InventoryItem it : items) if (it.getId() == id) return it;
        throw new IllegalArgumentException("Item not found");
    }
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(items);
        }
    }
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            items = (ArrayList<InventoryItem>)ois.readObject();
        }
    }
}

class BackgroundPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 120));
        FontMetrics fm = g2d.getFontMetrics();
        String initials = "MQ";
        int x = (w - fm.stringWidth(initials)) / 2;
        int y = (h + fm.getAscent()) / 2;
        g2d.drawString(initials, x, y);
        g2d.dispose();
    }
}

class InventoryUI extends JFrame {
    private InventoryManager manager = new InventoryManager();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField idField, nameField, qtyField, fileField;

    public InventoryUI() {
        setTitle("MQ Data Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Brand colors
        Color brandBlue = new Color(45, 85, 130);
        Color brandGray = new Color(245, 245, 245);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout(10, 10));
        bg.setBackground(brandGray);
        setContentPane(bg);

        // Input operations panel with GridBagLayout
        idField = new JTextField();
        nameField = new JTextField();
        qtyField = new JTextField();
        JButton createBtn = new JButton("Create");
        JButton addBtn = new JButton("Add Qty");
        JButton removeBtn = new JButton("Remove Qty");

        JPanel inputPane = new JPanel(new GridBagLayout());
        inputPane.setOpaque(false);
        TitledBorder opBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(brandBlue), "Operations"
        );
        opBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        opBorder.setTitleColor(brandBlue);
        inputPane.setBorder(opBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPane.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        idField.setColumns(10);
        inputPane.add(idField, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 1;
        inputPane.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField.setColumns(15);
        inputPane.add(nameField, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 2;
        inputPane.add(new JLabel("Qty:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        qtyField.setColumns(5);
        inputPane.add(qtyField, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 3;
        inputPane.add(createBtn, gbc);
        gbc.gridx = 1;
        inputPane.add(addBtn, gbc);
        gbc.gridx = 2;
        inputPane.add(removeBtn, gbc);

        bg.add(inputPane, BorderLayout.NORTH);

        // Table panel
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Quantity"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.setRowHeight(24);
        table.setSelectionBackground(new Color(200, 230, 255));

        JScrollPane tableScroll = new JScrollPane(table);
        TitledBorder listBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(brandBlue), "Inventory List"
        );
        listBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        listBorder.setTitleColor(brandBlue);
        tableScroll.setBorder(listBorder);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        bg.add(tableScroll, BorderLayout.CENTER);

        // File operations panel
        fileField = new JTextField();
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JPanel filePane = new JPanel(new BorderLayout(5, 5));
        filePane.setOpaque(false);
        TitledBorder fileBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(brandBlue), "Save/Load File"
        );
        fileBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        fileBorder.setTitleColor(brandBlue);
        filePane.setBorder(fileBorder);
        JPanel btnPane = new JPanel(); btnPane.setOpaque(false);
        btnPane.add(saveBtn); btnPane.add(loadBtn);
        filePane.add(new JLabel("Filename:"), BorderLayout.WEST);
        filePane.add(fileField, BorderLayout.CENTER);
        filePane.add(btnPane, BorderLayout.EAST);
        bg.add(filePane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);

        // Action listeners without pop-ups
        createBtn.addActionListener(e -> { manager.create(parseId(), nameField.getText()); refresh(); });
        addBtn.addActionListener(e -> { manager.add(parseId(), parseQty()); refresh(); });
        removeBtn.addActionListener(e -> { manager.remove(parseId(), parseQty()); refresh(); });
        saveBtn.addActionListener(e -> { try { manager.saveToFile(fileField.getText()); refresh(); } catch (Exception ex) {} });
        loadBtn.addActionListener(e -> { try { manager.loadFromFile(fileField.getText()); refresh(); } catch (Exception ex) {} });
    }

    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (InventoryItem it : manager.list())
                tableModel.addRow(new Object[]{it.getId(), it.getName(), it.getQuantity()});
        });
    }

    private long parseId() {
        try { return Long.parseLong(idField.getText().trim()); }
        catch (Exception e) { return -1; }
    }

    private int parseQty() {
        try { return Integer.parseInt(qtyField.getText().trim()); }
        catch (Exception e) { return 0; }
    }
}
