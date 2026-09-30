import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PetManagementSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pet_management_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private PetManagementSystem() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void showDatabaseError(JFrame parent, SQLException exception) {
        JOptionPane.showMessageDialog(
                parent,
                "Could not complete the database operation. Check that MySQL is running, "
                        + "the schema is installed, and the connection settings are correct.\n\n"
                        + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static final class LoginFrame extends JFrame {
        private final JTextField usernameField = new JTextField(18);
        private final JPasswordField passwordField = new JPasswordField(18);

        private LoginFrame() {
            super("Pet Management System - Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(false);
            setContentPane(createLoginPanel());
            pack();
            setLocationRelativeTo(null);
        }

        private JPanel createLoginPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(7, 6, 7, 6);
            constraints.anchor = GridBagConstraints.WEST;

            JLabel title = new JLabel("Pet Management System");
            title.setFont(title.getFont().deriveFont(20.0f));
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            panel.add(title, constraints);

            constraints.gridwidth = 1;
            constraints.gridy++;
            panel.add(new JLabel("Username:"), constraints);
            constraints.gridx = 1;
            panel.add(usernameField, constraints);

            constraints.gridx = 0;
            constraints.gridy++;
            panel.add(new JLabel("Password:"), constraints);
            constraints.gridx = 1;
            panel.add(passwordField, constraints);

            JButton loginButton = new JButton("Login");
            JButton exitButton = new JButton("Exit");
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.add(loginButton);
            buttons.add(exitButton);
            constraints.gridx = 0;
            constraints.gridy++;
            constraints.gridwidth = 2;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            panel.add(buttons, constraints);

            loginButton.addActionListener(event -> authenticate());
            passwordField.addActionListener(event -> authenticate());
            exitButton.addActionListener(event -> dispose());
            return panel;
        }

        private void authenticate() {
            String username = usernameField.getText().trim();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
            java.util.Arrays.fill(passwordChars, '\0');

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter both a username and password.",
                        "Login Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
            try (Connection connection = getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, password);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        dispose();
                        new DashboardFrame(username).setVisible(true);
                    } else {
                        passwordField.setText("");
                        JOptionPane.showMessageDialog(
                                this,
                                "The username or password is incorrect.",
                                "Login Failed",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (SQLException exception) {
                showDatabaseError(this, exception);
            }
        }
    }

    private static final class DashboardFrame extends JFrame {
        private final JTextField petIdField = new JTextField(14);
        private final JTextField nameField = new JTextField(14);
        private final JTextField speciesField = new JTextField(14);
        private final JTextField breedField = new JTextField(14);
        private final JTextField ageField = new JTextField(14);
        private final JTextField ownerField = new JTextField(14);
        private final JComboBox<String> statusCombo = new JComboBox<>(
                new String[] {"Available", "Adopted"});
        private final JTextField searchField = new JTextField(24);
        private final DefaultTableModel tableModel = new DefaultTableModel(
                new Object[] {"Pet ID", "Name", "Species", "Breed", "Age", "Owner", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable petTable = new JTable(tableModel);
        private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

        private DashboardFrame(String username) {
            super("Pet Management System - Dashboard");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(950, 560));
            setContentPane(createDashboardPanel(username));
            petTable.setRowSorter(sorter);
            petTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            petIdField.setEditable(false);
            petTable.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    populateFormFromSelection();
                }
            });
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    applySearchFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    applySearchFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    applySearchFilter();
                }
            });
            pack();
            setLocationRelativeTo(null);
            loadPets();
        }

        private JPanel createDashboardPanel(String username) {
            JPanel panel = new JPanel(new BorderLayout(12, 12));
            panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            JLabel heading = new JLabel("Pets  |  Signed in as " + username);
            heading.setFont(heading.getFont().deriveFont(18.0f));
            panel.add(heading, BorderLayout.NORTH);
            panel.add(createFormPanel(), BorderLayout.WEST);

            JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            searchPanel.add(new JLabel("Search name or species:"));
            searchPanel.add(searchField);
            tablePanel.add(searchPanel, BorderLayout.NORTH);
            tablePanel.add(new JScrollPane(petTable), BorderLayout.CENTER);
            panel.add(tablePanel, BorderLayout.CENTER);
            panel.add(createActionPanel(), BorderLayout.SOUTH);
            return panel;
        }

        private JPanel createFormPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Pet Details"));
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(5, 6, 5, 6);
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;

            addFormRow(panel, constraints, 0, "Pet ID:", petIdField);
            addFormRow(panel, constraints, 1, "Name:", nameField);
            addFormRow(panel, constraints, 2, "Species:", speciesField);
            addFormRow(panel, constraints, 3, "Breed:", breedField);
            addFormRow(panel, constraints, 4, "Age:", ageField);
            addFormRow(panel, constraints, 5, "Owner:", ownerField);
            addFormRow(panel, constraints, 6, "Status:", statusCombo);
            return panel;
        }

        private void addFormRow(
                JPanel panel,
                GridBagConstraints constraints,
                int row,
                String label,
                java.awt.Component field) {
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.weightx = 0;
            panel.add(new JLabel(label), constraints);
            constraints.gridx = 1;
            constraints.weightx = 1;
            panel.add(field, constraints);
        }

        private JPanel createActionPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            JButton addButton = new JButton("Add Pet");
            JButton updateButton = new JButton("Update Pet");
            JButton deleteButton = new JButton("Delete Pet");
            JButton clearButton = new JButton("Clear Form");
            panel.add(addButton);
            panel.add(updateButton);
            panel.add(deleteButton);
            panel.add(clearButton);
            addButton.addActionListener(event -> addPet());
            updateButton.addActionListener(event -> updatePet());
            deleteButton.addActionListener(event -> deletePet());
            clearButton.addActionListener(event -> clearForm());
            return panel;
        }

        private void applySearchFilter() {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query), 1, 2));
            }
        }

        private void populateFormFromSelection() {
            int selectedViewRow = petTable.getSelectedRow();
            if (selectedViewRow < 0) {
                return;
            }
            int selectedModelRow = petTable.convertRowIndexToModel(selectedViewRow);
            petIdField.setText(String.valueOf(tableModel.getValueAt(selectedModelRow, 0)));
            nameField.setText(String.valueOf(tableModel.getValueAt(selectedModelRow, 1)));
            speciesField.setText(String.valueOf(tableModel.getValueAt(selectedModelRow, 2)));
            breedField.setText(String.valueOf(tableModel.getValueAt(selectedModelRow, 3)));
            ageField.setText(String.valueOf(tableModel.getValueAt(selectedModelRow, 4)));
            ownerField.setText(String.valueOf(tableModel.getValueAt(selectedModelRow, 5)));
            statusCombo.setSelectedItem(String.valueOf(tableModel.getValueAt(selectedModelRow, 6)));
        }

        private PetDetails readForm() {
            String name = nameField.getText().trim();
            String species = speciesField.getText().trim();
            String breed = breedField.getText().trim();
            String owner = ownerField.getText().trim();
            if (name.isEmpty() || species.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Name and species are required.",
                        "Invalid Pet Details",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (name.length() > 50 || species.length() > 30 || breed.length() > 30 || owner.length() > 50) {
                JOptionPane.showMessageDialog(
                        this,
                        "Name, species, breed, or owner exceeds the database column limit.",
                        "Invalid Pet Details",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }

            int age;
            try {
                age = Integer.parseInt(ageField.getText().trim());
                if (age < 0) {
                    throw new NumberFormatException("Age cannot be negative.");
                }
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter a whole number of zero or greater for age.",
                        "Invalid Pet Details",
                        JOptionPane.WARNING_MESSAGE);
                ageField.requestFocusInWindow();
                return null;
            }

            return new PetDetails(
                    name,
                    species,
                    breed,
                    age,
                    owner.isEmpty() ? "None" : owner,
                    String.valueOf(statusCombo.getSelectedItem()));
        }

        private Integer selectedPetId() {
            String value = petIdField.getText().trim();
            if (value.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Select a pet from the table first.",
                        "No Pet Selected",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(
                        this,
                        "The selected pet ID is invalid. Refresh the table and try again.",
                        "Invalid Pet ID",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        private void addPet() {
            PetDetails pet = readForm();
            if (pet == null) {
                return;
            }
            String sql = "INSERT INTO pets (name, species, breed, age, owner_name, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection connection = getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                setPetParameters(statement, pet, 1);
                statement.executeUpdate();
                loadPets();
                clearForm();
                JOptionPane.showMessageDialog(this, "Pet added successfully.");
            } catch (SQLException exception) {
                showDatabaseError(this, exception);
            }
        }

        private void updatePet() {
            Integer petId = selectedPetId();
            if (petId == null) {
                return;
            }
            PetDetails pet = readForm();
            if (pet == null) {
                return;
            }
            String sql = "UPDATE pets SET name = ?, species = ?, breed = ?, age = ?, "
                    + "owner_name = ?, status = ? WHERE pet_id = ?";
            try (Connection connection = getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                setPetParameters(statement, pet, 1);
                statement.setInt(7, petId);
                if (statement.executeUpdate() == 0) {
                    JOptionPane.showMessageDialog(this, "That pet no longer exists in the database.");
                } else {
                    loadPets();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Pet updated successfully.");
                }
            } catch (SQLException exception) {
                showDatabaseError(this, exception);
            }
        }

        private void deletePet() {
            Integer petId = selectedPetId();
            if (petId == null) {
                return;
            }
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Delete pet ID " + petId + "? This action cannot be undone.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            String sql = "DELETE FROM pets WHERE pet_id = ?";
            try (Connection connection = getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, petId);
                if (statement.executeUpdate() == 0) {
                    JOptionPane.showMessageDialog(this, "That pet no longer exists in the database.");
                } else {
                    loadPets();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Pet deleted successfully.");
                }
            } catch (SQLException exception) {
                showDatabaseError(this, exception);
            }
        }

        private void loadPets() {
            String sql = "SELECT pet_id, name, species, breed, age, owner_name, status "
                    + "FROM pets ORDER BY pet_id";
            try (Connection connection = getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet result = statement.executeQuery()) {
                tableModel.setRowCount(0);
                while (result.next()) {
                    tableModel.addRow(new Object[] {
                        result.getInt("pet_id"),
                        result.getString("name"),
                        result.getString("species"),
                        result.getString("breed"),
                        result.getInt("age"),
                        result.getString("owner_name"),
                        result.getString("status")
                    });
                }
            } catch (SQLException exception) {
                showDatabaseError(this, exception);
            }
        }

        private void clearForm() {
            petTable.clearSelection();
            petIdField.setText("");
            nameField.setText("");
            speciesField.setText("");
            breedField.setText("");
            ageField.setText("");
            ownerField.setText("");
            statusCombo.setSelectedItem("Available");
            nameField.requestFocusInWindow();
        }

        private static void setPetParameters(PreparedStatement statement, PetDetails pet, int startIndex)
                throws SQLException {
            statement.setString(startIndex, pet.name);
            statement.setString(startIndex + 1, pet.species);
            statement.setString(startIndex + 2, pet.breed);
            statement.setInt(startIndex + 3, pet.age);
            statement.setString(startIndex + 4, pet.owner);
            statement.setString(startIndex + 5, pet.status);
        }
    }

    private static final class PetDetails {
        private final String name;
        private final String species;
        private final String breed;
        private final int age;
        private final String owner;
        private final String status;

        private PetDetails(String name, String species, String breed, int age, String owner, String status) {
            this.name = name;
            this.species = species;
            this.breed = breed;
            this.age = age;
            this.owner = owner;
            this.status = status;
        }
    }
}