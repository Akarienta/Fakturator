package cz.akarienta.fakturator;

import cz.akarienta.fakturator.data.Contractor;
import cz.akarienta.fakturator.data.Customer;
import cz.akarienta.fakturator.data.Detail;
import cz.akarienta.fakturator.xml.XMLConstants;
import cz.akarienta.fakturator.xml.XMLReader;
import cz.akarienta.fakturator.xml.XMLWriter;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.javatuples.Pair;
import org.xml.sax.SAXException;

/**
 * Fakturator GUI.
 *
 * @author akarienta
 */
public class GUI extends javax.swing.JFrame {

    private final List<JPanel> panels = new ArrayList<JPanel>();
    private final Map<Contractor, JTextField> contractorFields = new EnumMap<Contractor, JTextField>(Contractor.class);
    private final Map<Contractor, JLabel> contractorLabels = new EnumMap<Contractor, JLabel>(Contractor.class);
    private final Map<Customer, JTextField> customerFields = new EnumMap<Customer, JTextField>(Customer.class);
    private final Map<Customer, JLabel> customerLabels = new EnumMap<Customer, JLabel>(Customer.class);
    private final Map<Detail, JTextField> detailFields = new EnumMap<Detail, JTextField>(Detail.class);
    private final Map<Detail, JLabel> detailLabels = new EnumMap<Detail, JLabel>(Detail.class);

    private int rowId;

    private final XMLReader contractorReader = new XMLReader(XMLConstants.CONTRACTOR_DATA);
    private final XMLWriter contractorWriter = new XMLWriter(XMLConstants.CONTRACTOR_DATA);
    private final XMLReader customersReader = new XMLReader(XMLConstants.CUSTOMERS_DATA);
    private final XMLWriter customersWriter = new XMLWriter(XMLConstants.CUSTOMERS_DATA);
    private final XMLReader numberReader = new XMLReader(XMLConstants.NUMBER_DATA);
    private final XMLWriter numberWriter = new XMLWriter(XMLConstants.NUMBER_DATA);

    /** Creates new form gui */
    public GUI() {
        setIcon();
        initFiles();
        initComponents();
        localizeFileChooser();
        buildPanels();
        buildContractor();
        buildCustomer();
        buildDetails();
        switchToPanel(welcomePanel);
    }

    private void setIcon() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/cz/akarienta/fakturator/img/logo.png")));
    }

    private void initFiles() {
        try {
            File fileBase = new File(XMLConstants.USER_HOME, XMLConstants.APP_CONF_DIR);
            if (!fileBase.exists() || !fileBase.isDirectory()) {
                fileBase.mkdirs();
            }

            File contractorFile = new File(fileBase, XMLConstants.CONTRACTOR_DATA);
            File customersFile = new File(fileBase, XMLConstants.CUSTOMERS_DATA);
            File numberFile = new File(fileBase, XMLConstants.NUMBER_DATA);

            if (!contractorFile.exists()) {
                contractorWriter.createEmptyContractor();
            }
            if (!customersFile.exists()) {
                customersWriter.createEmptyCustomers();
            }
            if (!numberFile.exists()) {
                numberWriter.createEmptyNumber();
            }
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            ex.printStackTrace();
        }
    }

    private void loadCustomers() {
        customerList.removeAllItems();
        try {
            for (String customer : this.customersReader.getCustomers()) {
                customerList.addItem(customer);
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            setError("Nastala vnitřní chyba programu - nepodařilo se odběratele.");
            ex.printStackTrace();
        }
    }

    private void localizeFileChooser() {
        UIManager.put("FileChooser.acceptAllFileFilterText", "Všechny soubory");
        UIManager.put("FileChooser.lookInLabelText", "Hledat v:");
        UIManager.put("FileChooser.cancelButtonText", "Zrušit");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Zrušit");
        UIManager.put("FileChooser.openButtonText", "Vybrat");
        UIManager.put("FileChooser.openButtonToolTipText", "Vybrat");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Typ souboru");
        UIManager.put("FileChooser.fileNameLabelText", "Soubor");
        UIManager.put("FileChooser.folderNameLabelText", "Složka");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Seznam");
        UIManager.put("FileChooser.listViewButtonAccessibleName", "Seznam");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Podrobnosti");
        UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Podrobnosti");
        UIManager.put("FileChooser.upFolderToolTipText", "O stupeň výš");
        UIManager.put("FileChooser.upFolderAccessibleName", "O stupeň výš");
        UIManager.put("FileChooser.homeFolderToolTipText", "Domů");
        UIManager.put("FileChooser.homeFolderAccessibleName", "Domů");
        UIManager.put("FileChooser.fileNameHeaderText", "Jméno");
        UIManager.put("FileChooser.fileSizeHeaderText", "Velikost");
        UIManager.put("FileChooser.fileTypeHeaderText", "Typ souboru");
        UIManager.put("FileChooser.fileDateHeaderText", "Datum");
        UIManager.put("FileChooser.fileAttrHeaderText", "Atributy");
        UIManager.put("FileChooser.openDialogTitleText", "Otevřít");
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
    }

    private void buildPanels() {
        panels.add(welcomePanel);
        panels.add(newInvoicePanel);
        panels.add(contractorPanel);
        panels.add(customerPanel);
        panels.add(aboutPanel);
    }

    private void buildContractor() {
        buildContractorFields();
        buildContractorLabels();
        initContractorFieldNames();
        loadContractor();
    }

    private void buildContractorFields() {
        contractorFields.put(Contractor.NAME, contractorNameField);
        contractorFields.put(Contractor.ADDRESS, contractorAddressField);
        contractorFields.put(Contractor.CITY, contractorCityField);
        contractorFields.put(Contractor.POSTAL_CODE, contractorPostalCodeField);
        contractorFields.put(Contractor.ICO, contractorIcoField);
        contractorFields.put(Contractor.BANK, contractorBankField);
        contractorFields.put(Contractor.ACCOUNT_NUMBER, contractorAccountNumberField);
        contractorFields.put(Contractor.PHONE, contractorPhoneField);
        contractorFields.put(Contractor.MAIL, contractorMailField);
        contractorFields.put(Contractor.WEB, contractorWebField);
        contractorFields.put(Contractor.SIGNATURE_PATH, contractorSignatureFileField);
        contractorFields.put(Contractor.RESULT_FOLDER, contractorResultFolderField);
    }

    private void buildContractorLabels() {
        contractorLabels.put(Contractor.NAME, contractorName);
        contractorLabels.put(Contractor.ADDRESS, contractorAddress);
        contractorLabels.put(Contractor.CITY, contractorCity);
        contractorLabels.put(Contractor.POSTAL_CODE, contractorPostalCode);
        contractorLabels.put(Contractor.ICO, contractorIco);
        contractorLabels.put(Contractor.BANK, contractorBank);
        contractorLabels.put(Contractor.ACCOUNT_NUMBER, contractorAccountNumber);
        contractorLabels.put(Contractor.PHONE, contractorPhone);
        contractorLabels.put(Contractor.MAIL, contractorMail);
        contractorLabels.put(Contractor.WEB, contractorWeb);
        contractorLabels.put(Contractor.SIGNATURE_PATH, contractorSignatureFile);
        contractorLabels.put(Contractor.RESULT_FOLDER, contractorResultFolder);
    }

    private void buildCustomer() {
        buildCustomerFields();
        buildCustomerLabels();
        initCustomerFieldNames();
        loadCustomers();
    }

    private void buildCustomerFields() {
        customerFields.put(Customer.NAME, customerNameField);
        customerFields.put(Customer.ADDRESS, customerAddressField);
        customerFields.put(Customer.CITY, customerCityField);
        customerFields.put(Customer.POSTAL_CODE, customerPostalCodeField);
        customerFields.put(Customer.ICO, customerIcoField);
        customerFields.put(Customer.DIC, customerDicField);
    }

    private void buildCustomerLabels() {
        customerLabels.put(Customer.NAME, customerName);
        customerLabels.put(Customer.ADDRESS, customerAddress);
        customerLabels.put(Customer.CITY, customerCity);
        customerLabels.put(Customer.POSTAL_CODE, customerPostalCode);
        customerLabels.put(Customer.ICO, customerIco);
        customerLabels.put(Customer.DIC, customerDic);
    }

    private void buildDetails() {
        buildDetailsFields();
        buildDetailsLabels();
        initDetailsFieldNames();
    }

    private void buildDetailsFields() {
        detailFields.put(Detail.ISSUE_DATE, dateOfIssueField);
        detailFields.put(Detail.PAYMENT_DUE, dateOfValidityField);
        detailFields.put(Detail.INVOICE_NUMBER, invoiceNumberField);
    }

    private void buildDetailsLabels() {
        detailLabels.put(Detail.ISSUE_DATE, dateOfIssue);
        detailLabels.put(Detail.PAYMENT_DUE, dateOfValidity);
        detailLabels.put(Detail.INVOICE_NUMBER, invoiceNumber);
    }

    private void initContractorFieldNames() {
        for (Map.Entry<Contractor, JLabel> contractorField : this.contractorLabels.entrySet()) {
            JLabel formLabel = contractorField.getValue();
            Contractor field = contractorField.getKey();

            formLabel.setText(field.getLabel());
            if (field.isMandatory()) {
                setFieldAsMandatory(formLabel);
            }
        }
    }

    private void initCustomerFieldNames() {
        for (Map.Entry<Customer, JLabel> customerField : this.customerLabels.entrySet()) {
            JLabel formLabel = customerField.getValue();
            Customer field = customerField.getKey();

            formLabel.setText(field.getLabel());
        }
    }

    private void initDetailsFieldNames() {
        for (Map.Entry<Detail, JLabel> detailField : this.detailLabels.entrySet()) {
            JLabel formLabel = detailField.getValue();
            Detail field = detailField.getKey();

            formLabel.setText(field.getLabel());
        }
    }

    private void setFieldAsMandatory(JLabel formLabel) {
        formLabel.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
    }

    private void hideAllPanels() {
        for (JPanel panel : panels) {
            panel.setVisible(false);
        }
    }

    private void clearAllPanels() {
        clearNewInvoicePanel();
    }

    private void switchToPanel(JPanel panel) {
        hideAllPanels();
        clearAllPanels();
        clearNotifications();
        panel.setVisible(true);
    }

    private void loadContractor() {
        try {
            for (Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
                JTextField formField = contractorField.getValue();
                String nodeName = contractorField.getKey().getNodeName();

                formField.setText(this.contractorReader.getElementContentText(XMLConstants.CONTRACTOR_DATA_XPATH + "/" + nodeName));
            }
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException ex) {
            setError("Nastala vnitřní chyba programu - nepodařilo se načíst uložená data pro dodavatele.");
            ex.printStackTrace();
        }
    }

    private boolean isContractorOk() {
        for (Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
            Contractor field = contractorField.getKey();

            try {
                String formFieldText = this.contractorReader.getElementContentText(XMLConstants.CONTRACTOR_DATA_XPATH + "/" + field.getNodeName());
                if (field.isMandatory() && formFieldText.isEmpty()) {
                    return false;
                }
            } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException ex) {
                setError("Nastala vnitřní chyba programu - nepodařilo se načíst uložená data pro dodavatele.");
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        welcomePanel = new javax.swing.JPanel();
        logo = new javax.swing.JLabel();
        appName = new javax.swing.JLabel();
        newInvoicePanel = new javax.swing.JPanel();
        customer = new javax.swing.JLabel();
        invoiceNumber = new javax.swing.JLabel();
        customerList = new javax.swing.JComboBox();
        invoiceNumberField = new javax.swing.JTextField();
        dateOfIssue = new javax.swing.JLabel();
        dateOfValidity = new javax.swing.JLabel();
        dateOfIssueField = new javax.swing.JTextField();
        dateOfValidityField = new javax.swing.JTextField();
        newItemHeadline = new javax.swing.JLabel();
        itemName = new javax.swing.JLabel();
        itemPrice = new javax.swing.JLabel();
        itemNameField = new javax.swing.JTextField();
        itemPriceField = new javax.swing.JTextField();
        tablePanel = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addItem = new javax.swing.JButton();
        deleteItemHeadline = new javax.swing.JLabel();
        currency = new javax.swing.JLabel();
        itemIdField = new javax.swing.JTextField();
        itemId = new javax.swing.JLabel();
        deleteItem = new javax.swing.JButton();
        printInvoice = new javax.swing.JButton();
        useAsInvoiceNumber = new javax.swing.JCheckBox();
        contractorPanel = new javax.swing.JPanel();
        contractorName = new javax.swing.JLabel();
        contractorNameField = new javax.swing.JTextField();
        contractorAddress = new javax.swing.JLabel();
        contractorAddressField = new javax.swing.JTextField();
        contractorCity = new javax.swing.JLabel();
        contractorPostalCode = new javax.swing.JLabel();
        contractorIco = new javax.swing.JLabel();
        contractorBank = new javax.swing.JLabel();
        contractorAccountNumber = new javax.swing.JLabel();
        contractorPhone = new javax.swing.JLabel();
        contractorMail = new javax.swing.JLabel();
        contractorWeb = new javax.swing.JLabel();
        contractorCityField = new javax.swing.JTextField();
        contractorIcoField = new javax.swing.JTextField();
        contractorPostalCodeField = new javax.swing.JTextField();
        contractorBankField = new javax.swing.JTextField();
        contractorAccountNumberField = new javax.swing.JTextField();
        contractorPhoneField = new javax.swing.JTextField();
        contractorMailField = new javax.swing.JTextField();
        contractorWebField = new javax.swing.JTextField();
        contractorSignatureFile = new javax.swing.JLabel();
        contractorSignatureFileField = new javax.swing.JTextField();
        contractorSaveButton = new javax.swing.JButton();
        contractorNameNote = new javax.swing.JLabel();
        contractorBankNote = new javax.swing.JLabel();
        contractorAccountNumberNote = new javax.swing.JLabel();
        contractorSignatureBrowseButton = new javax.swing.JButton();
        contractorResultFolder = new javax.swing.JLabel();
        contractorResultFolderField = new javax.swing.JTextField();
        contractorResultFolderBrowseButton = new javax.swing.JButton();
        notification = new javax.swing.JLabel();
        customerPanel = new javax.swing.JPanel();
        newCustomer = new javax.swing.JLabel();
        deleteCustomer = new javax.swing.JLabel();
        customerToDeletionList = new javax.swing.JComboBox();
        deleteCustomerButton = new javax.swing.JButton();
        customerName = new javax.swing.JLabel();
        customerAddress = new javax.swing.JLabel();
        customerCity = new javax.swing.JLabel();
        customerPostalCode = new javax.swing.JLabel();
        customerIco = new javax.swing.JLabel();
        customerDic = new javax.swing.JLabel();
        customerNameField = new javax.swing.JTextField();
        customerAddressField = new javax.swing.JTextField();
        customerCityField = new javax.swing.JTextField();
        customerPostalCodeField = new javax.swing.JTextField();
        customerIcoField = new javax.swing.JTextField();
        customerDicField = new javax.swing.JTextField();
        addCustomerButton = new javax.swing.JButton();
        customerSaveAsField = new javax.swing.JTextField();
        customerSaveAs = new javax.swing.JLabel();
        customerSaveAsTip = new javax.swing.JLabel();
        aboutPanel = new javax.swing.JPanel();
        aboutAppName = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        aboutLicense = new javax.swing.JTextArea();
        aboutDate = new javax.swing.JLabel();
        aboutAuthor = new javax.swing.JLabel();
        menu = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        newInvoice = new javax.swing.JMenuItem();
        settings = new javax.swing.JMenu();
        contractor = new javax.swing.JMenuItem();
        customerEntry = new javax.swing.JMenuItem();
        help = new javax.swing.JMenu();
        about = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Fakturator");
        setMinimumSize(new java.awt.Dimension(784, 620));
        setName("mainFrame"); // NOI18N
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        welcomePanel.setMaximumSize(new java.awt.Dimension(780, 540));
        welcomePanel.setMinimumSize(new java.awt.Dimension(780, 540));
        welcomePanel.setPreferredSize(new java.awt.Dimension(780, 540));
        welcomePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/logo.png"))); // NOI18N
        welcomePanel.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 170, -1, -1));

        appName.setFont(new java.awt.Font("Berlin Sans FB Demi", 1, 48)); // NOI18N
        appName.setText("Fakturator");
        welcomePanel.add(appName, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 220, -1, 47));

        getContentPane().add(welcomePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 780, 540));

        newInvoicePanel.setMaximumSize(new java.awt.Dimension(780, 540));
        newInvoicePanel.setMinimumSize(new java.awt.Dimension(780, 540));
        newInvoicePanel.setPreferredSize(new java.awt.Dimension(780, 540));

        customer.setText("Odběratel");

        invoiceNumber.setText("Číslo dokladu");

        customerList.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        dateOfIssue.setText("Datum vystavení");

        dateOfValidity.setText("Datum splatnosti");

        newItemHeadline.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        newItemHeadline.setText("Nová položka");

        itemName.setText("Název");

        itemPrice.setText("Cena");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Název", "Cena (Kč)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablePanel.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(0).setPreferredWidth(20);
            table.getColumnModel().getColumn(1).setResizable(false);
            table.getColumnModel().getColumn(1).setPreferredWidth(500);
            table.getColumnModel().getColumn(2).setResizable(false);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
        }

        addItem.setText("Přidat");
        addItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemActionPerformed(evt);
            }
        });

        deleteItemHeadline.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        deleteItemHeadline.setText("Smazat položku");

        currency.setText("Kč");

        itemId.setText("ID");

        deleteItem.setText("Smazat");
        deleteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteItemActionPerformed(evt);
            }
        });

        printInvoice.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        printInvoice.setText("VYSTAVIT FAKTURU");
        printInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printInvoiceActionPerformed(evt);
            }
        });

        useAsInvoiceNumber.setSelected(true);
        useAsInvoiceNumber.setText("použít pro číslování");

        javax.swing.GroupLayout newInvoicePanelLayout = new javax.swing.GroupLayout(newInvoicePanel);
        newInvoicePanel.setLayout(newInvoicePanelLayout);
        newInvoicePanelLayout.setHorizontalGroup(
            newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tablePanel)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newInvoicePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newInvoicePanelLayout.createSequentialGroup()
                        .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dateOfIssue)
                            .addComponent(invoiceNumber, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dateOfIssueField, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                                .addComponent(invoiceNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(useAsInvoiceNumber)))
                        .addGap(49, 49, 49)
                        .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                                .addComponent(dateOfValidity)
                                .addGap(18, 18, 18)
                                .addComponent(dateOfValidityField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                                .addComponent(customer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(customerList, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newInvoicePanelLayout.createSequentialGroup()
                        .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(itemName)
                                    .addComponent(itemPrice))
                                .addGap(18, 18, 18)
                                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(newInvoicePanelLayout.createSequentialGroup()
                                        .addComponent(itemPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(3, 3, 3)
                                        .addComponent(currency)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(addItem, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(itemNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(newItemHeadline))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(deleteItemHeadline, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                                .addComponent(itemId)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(itemIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(deleteItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                .addGap(238, 238, 238)
                .addComponent(printInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        newInvoicePanelLayout.setVerticalGroup(
            newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(invoiceNumber)
                    .addComponent(invoiceNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customer)
                    .addComponent(customerList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useAsInvoiceNumber))
                .addGap(9, 9, 9)
                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dateOfValidityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dateOfValidity))
                    .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dateOfIssueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dateOfIssue)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newItemHeadline)
                    .addComponent(deleteItemHeadline))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(itemName)
                    .addComponent(itemNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(itemPrice)
                    .addComponent(itemPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(currency)
                    .addComponent(addItem)
                    .addComponent(deleteItem))
                .addGap(18, 18, 18)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(printInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        getContentPane().add(newInvoicePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 780, 540));

        contractorPanel.setMaximumSize(new java.awt.Dimension(780, 540));
        contractorPanel.setMinimumSize(new java.awt.Dimension(780, 540));
        contractorPanel.setPreferredSize(new java.awt.Dimension(780, 600));

        contractorName.setText("Jméno");

        contractorAddress.setText("Ulice a číslo popisné/orientační");

        contractorCity.setText("Město");

        contractorPostalCode.setText("PSČ");

        contractorIco.setText("IČ");

        contractorBank.setText("Banka");

        contractorAccountNumber.setText("Číslo účtu");

        contractorPhone.setText("Telefon");

        contractorMail.setText("E-mail");

        contractorWeb.setText("Web");

        contractorSignatureFile.setText("Obrázek podpisu");

        contractorSaveButton.setText("Uložit údaje");
        contractorSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contractorSaveButtonActionPerformed(evt);
            }
        });

        contractorNameNote.setFont(new java.awt.Font("Cantarell", 2, 15)); // NOI18N
        contractorNameNote.setText("včetně příp. titulů a prostředního jména");

        contractorBankNote.setFont(new java.awt.Font("Cantarell", 2, 15)); // NOI18N
        contractorBankNote.setText("název Vaší banky");

        contractorAccountNumberNote.setFont(new java.awt.Font("Cantarell", 2, 15)); // NOI18N
        contractorAccountNumberNote.setText("včetně kódu banky");

        contractorSignatureBrowseButton.setText("Procházet...");
        contractorSignatureBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contractorSignatureBrowseButtonActionPerformed(evt);
            }
        });

        contractorResultFolder.setText("Složka pro nové faktury");

        contractorResultFolderBrowseButton.setText("Procházet...");
        contractorResultFolderBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contractorResultFolderBrowseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout contractorPanelLayout = new javax.swing.GroupLayout(contractorPanel);
        contractorPanel.setLayout(contractorPanelLayout);
        contractorPanelLayout.setHorizontalGroup(
            contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contractorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contractorPanelLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(contractorAccountNumber)
                            .addComponent(contractorBank)
                            .addComponent(contractorPhone)
                            .addComponent(contractorMail)
                            .addComponent(contractorPostalCode)
                            .addComponent(contractorIco)
                            .addComponent(contractorWeb)
                            .addComponent(contractorSignatureFile)
                            .addComponent(contractorResultFolder))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(contractorPanelLayout.createSequentialGroup()
                                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(contractorIcoField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorBankField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorAccountNumberField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorPhoneField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorMailField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorWebField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorPostalCodeField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorBankNote)
                                    .addComponent(contractorAccountNumberNote))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(contractorPanelLayout.createSequentialGroup()
                                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(contractorResultFolderField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(contractorSignatureFileField)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, contractorPanelLayout.createSequentialGroup()
                                        .addComponent(contractorSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(contractorSignatureBrowseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(contractorResultFolderBrowseButton)))))
                    .addGroup(contractorPanelLayout.createSequentialGroup()
                        .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(contractorPanelLayout.createSequentialGroup()
                                .addComponent(contractorName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(contractorNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(contractorPanelLayout.createSequentialGroup()
                                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(contractorAddress)
                                    .addComponent(contractorCity))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(contractorAddressField)
                                    .addComponent(contractorCityField, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contractorNameNote)
                        .addGap(0, 24, Short.MAX_VALUE)))
                .addContainerGap())
        );
        contractorPanelLayout.setVerticalGroup(
            contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contractorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorName)
                    .addComponent(contractorNameNote))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorCityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorCity))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorPostalCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorPostalCode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorIcoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorIco))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorBankField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorBank)
                    .addComponent(contractorBankNote))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorAccountNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorAccountNumber)
                    .addComponent(contractorAccountNumberNote))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorPhoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorPhone))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorMailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorMail))
                .addGap(10, 10, 10)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorWebField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorWeb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorSignatureFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorSignatureFile)
                    .addComponent(contractorSignatureBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contractorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractorResultFolderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contractorResultFolder)
                    .addComponent(contractorResultFolderBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(contractorSaveButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(contractorPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 780, 540));

        notification.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        notification.setText("Místo pro notifikace");
        getContentPane().add(notification, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 5, -1, -1));
        notification.getAccessibleContext().setAccessibleDescription("");

        customerPanel.setMaximumSize(new java.awt.Dimension(780, 540));
        customerPanel.setMinimumSize(new java.awt.Dimension(780, 540));
        customerPanel.setPreferredSize(new java.awt.Dimension(780, 600));

        newCustomer.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        newCustomer.setText("Nový odběratel");

        deleteCustomer.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        deleteCustomer.setText("Smazat odběratele");

        customerToDeletionList.setModel(customerList.getModel());

        deleteCustomerButton.setText("Smazat odběratele");
        deleteCustomerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteCustomerButtonActionPerformed(evt);
            }
        });

        customerName.setText("Jméno");

        customerAddress.setText("Ulice a číslo popisné/orientační");

        customerCity.setText("Město");

        customerPostalCode.setText("PSČ");

        customerIco.setText("IČ");

        customerDic.setText("DIČ");

        addCustomerButton.setText("Přidat odběratele");
        addCustomerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCustomerButtonActionPerformed(evt);
            }
        });

        customerSaveAs.setText("Uložit odběratele jako");

        customerSaveAsTip.setFont(new java.awt.Font("Cantarell", 2, 15)); // NOI18N
        customerSaveAsTip.setText("název musí být jedinečný");

        javax.swing.GroupLayout customerPanelLayout = new javax.swing.GroupLayout(customerPanel);
        customerPanel.setLayout(customerPanelLayout);
        customerPanelLayout.setHorizontalGroup(
            customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customerPanelLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(customerName)
                            .addComponent(customerAddress))
                        .addComponent(customerCity, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(customerPostalCode, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(customerIco, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(customerDic, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(customerSaveAs))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(customerSaveAsField)
                    .addComponent(addCustomerButton, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                    .addComponent(customerDicField)
                    .addComponent(customerIcoField)
                    .addComponent(customerPostalCodeField)
                    .addComponent(customerAddressField)
                    .addComponent(customerNameField)
                    .addComponent(customerCityField)
                    .addComponent(newCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(customerToDeletionList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteCustomerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customerSaveAsTip)
                .addContainerGap(82, Short.MAX_VALUE))
        );
        customerPanelLayout.setVerticalGroup(
            customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customerPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(newCustomer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerCityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerCity))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerPostalCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerPostalCode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerIcoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerIco))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerDicField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerDic))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerSaveAsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerSaveAs)
                    .addComponent(customerSaveAsTip))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addCustomerButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(deleteCustomer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(customerToDeletionList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(deleteCustomerButton)
                .addGap(25, 25, 25))
        );

        getContentPane().add(customerPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 780, 540));

        aboutPanel.setMaximumSize(new java.awt.Dimension(780, 540));
        aboutPanel.setMinimumSize(new java.awt.Dimension(780, 540));
        aboutPanel.setPreferredSize(new java.awt.Dimension(780, 600));
        aboutPanel.setRequestFocusEnabled(false);

        aboutAppName.setFont(new java.awt.Font("Cantarell", 1, 48)); // NOI18N
        aboutAppName.setText("Fakturator 1.0");

        aboutLicense.setColumns(20);
        aboutLicense.setRows(5);
        aboutLicense.setText("BEERWARE LICENCE (ČESKÁ VERZE)\n\n<ivana.dolezalova@gmail.com> napsala tuto aplikaci. Pokud zachováte tento text,\nmůžete si s ní dělat, co chcete. Jestli mě někdy potkáte a budete si myslet, že tato\naplikace stojí za jedno pivo, kupte mi ho.");
        aboutLicense.setFocusable(false);
        aboutLicense.setMargin(new java.awt.Insets(10, 10, 10, 10));
        jScrollPane1.setViewportView(aboutLicense);

        aboutDate.setFont(new java.awt.Font("Cantarell", 0, 24)); // NOI18N
        aboutDate.setText("2. 4. 2015");

        aboutAuthor.setFont(new java.awt.Font("Cantarell", 0, 24)); // NOI18N
        aboutAuthor.setText("Ivana Doležalová");

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(321, 321, 321)
                        .addComponent(aboutDate))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(287, 287, 287)
                        .addComponent(aboutAuthor)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(223, 223, 223)
                        .addComponent(aboutAppName))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(93, Short.MAX_VALUE))
        );
        aboutPanelLayout.setVerticalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addComponent(aboutAppName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(aboutDate)
                .addGap(12, 12, 12)
                .addComponent(aboutAuthor)
                .addGap(80, 80, 80)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(158, Short.MAX_VALUE))
        );

        getContentPane().add(aboutPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 780, 540));

        file.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/house.png"))); // NOI18N
        file.setText("Soubor");

        newInvoice.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newInvoice.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/file-text.png"))); // NOI18N
        newInvoice.setText("Nová faktura");
        newInvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newInvoiceActionPerformed(evt);
            }
        });
        file.add(newInvoice);

        menu.add(file);

        settings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/wrench-screwdriver.png"))); // NOI18N
        settings.setText("Nastavení");

        contractor.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        contractor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/user-male.png"))); // NOI18N
        contractor.setText("Dodavatel");
        contractor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contractorActionPerformed(evt);
            }
        });
        settings.add(contractor);

        customerEntry.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        customerEntry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/address-book-alt.png"))); // NOI18N
        customerEntry.setText("Odběratelé");
        customerEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerEntryActionPerformed(evt);
            }
        });
        settings.add(customerEntry);

        menu.add(settings);

        help.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/life-buoy.png"))); // NOI18N
        help.setText("Nápověda");

        about.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        about.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/light-bulb.png"))); // NOI18N
        about.setText("O programu");
        about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutActionPerformed(evt);
            }
        });
        help.add(about);

        menu.add(help);

        setJMenuBar(menu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newInvoiceActionPerformed
        switchToPanel(newInvoicePanel);
        printInvoice.setEnabled(true);
        if (!isContractorOk()) {
            printInvoice.setEnabled(false);
            setError("Dodavatel není vypleň korektně a nelze tedy vystavovat faktury. Pro opravu zmáčkněte Ctrl+D.");
        }
        if (customerList.getItemCount() == 0) {
            printInvoice.setEnabled(false);
            setError("Neexistuje žádný odběratel a nelze tedy vystavovat faktury. Pro opravu zmáčkněte Ctrl+O.");
        }
        loadInvoiceNumber();
    }//GEN-LAST:event_newInvoiceActionPerformed

    private void addItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemActionPerformed
        addItem();
    }//GEN-LAST:event_addItemActionPerformed

    private void deleteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteItemActionPerformed
        removeItem();
    }//GEN-LAST:event_deleteItemActionPerformed

    private void contractorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractorActionPerformed
        switchToPanel(contractorPanel);
        for (Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
            String formFieldText = contractorField.getValue().getText();
            Contractor field = contractorField.getKey();

            if (field.isMandatory()) {
                warnIfIsEmpty(field.getLabel(), formFieldText);
            }
        }
    }//GEN-LAST:event_contractorActionPerformed

    private void contractorSignatureBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractorSignatureBrowseButtonActionPerformed
        String origFilename = contractorSignatureFileField.getText();

        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Obrázek SVG", "svg"));
        chooser.showOpenDialog(null);
        File signatureFile = chooser.getSelectedFile();

        if (signatureFile == null) {
            contractorSignatureFileField.setText(origFilename);
        } else {
            contractorSignatureFileField.setText(signatureFile.getAbsolutePath());
        }
    }//GEN-LAST:event_contractorSignatureBrowseButtonActionPerformed

    private void contractorResultFolderBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractorResultFolderBrowseButtonActionPerformed
        String origFilename = contractorResultFolderField.getText();

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Všechny složky", "*"));

        if (chooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
            contractorResultFolderField.setText(origFilename);
        } else {
            contractorResultFolderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_contractorResultFolderBrowseButtonActionPerformed

    private void contractorSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractorSaveButtonActionPerformed
        clearNotifications();

        for (Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
            String formFieldText = contractorField.getValue().getText();
            Contractor field = contractorField.getKey();

            if (field.isMandatory()) {
                warnIfIsEmpty(field.getLabel(), formFieldText);
            }

            if (Contractor.SIGNATURE_PATH.equals(field) && !isSvg(new File(formFieldText))) {
                if (!formFieldText.isEmpty() || field.isMandatory()) {
                    setError("Soubor s podpisem musí být SVG obrázek.");
                    formFieldText = "";
                    contractorField.getValue().setText("");
                }
            }

            if (Contractor.RESULT_FOLDER.equals(field) && !(new File(formFieldText)).isDirectory()) {
                setError("Složka pro nové faktury musí být existující složka.");
                formFieldText = "";
                contractorField.getValue().setText("");
            }

            try {
                contractorWriter.changeNodeValue(field.getNodeName(), formFieldText);
            } catch (ParserConfigurationException | SAXException | TransformerException | IOException ex) {
                setError("Nastala vnitřní chyba programu - nepodařilo se uložit dodavatele.");
                ex.printStackTrace();
            }
        }

        if (notification.getText().isEmpty()) {
            setInfo("Informace o dodavateli byly v pořádku uloženy.");
        }
    }//GEN-LAST:event_contractorSaveButtonActionPerformed

    private void customerEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerEntryActionPerformed
        switchToPanel(customerPanel);

        if (customerList.getItemCount() == 0) {
            printInvoice.setEnabled(false);
            setWarn("Měl by existovat alespoň jeden odběratel.");
        }
    }//GEN-LAST:event_customerEntryActionPerformed

    private void addCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCustomerButtonActionPerformed
        Map<String, String> nodes = new TreeMap<String, String>();
        String newCustomerName = customerSaveAsField.getText();

        if (isEmpty(customerSaveAs.getText(), newCustomerName)) {
            return;
        }

        for (int i = 0; i < customerList.getItemCount(); i++) {
            String customerName = customerList.getItemAt(i).toString();
            if (newCustomerName.equals(customerName)) {
                setError("Odběratel s názvem '" + customerName + "' již existuje.");
                return;
            }
        }

        for (Map.Entry<Customer, JTextField> customerField : this.customerFields.entrySet()) {
            Customer field = customerField.getKey();
            String formFieldText = customerField.getValue().getText();
            String formFieldLabel = this.customerLabels.get(field).getText();

            if (isEmpty(formFieldLabel, formFieldText)) {
                return;
            }

            nodes.put(field.getNodeName(), formFieldText);
        }

        try {
            this.customersWriter.addCustomer(newCustomerName, nodes);
        } catch (ParserConfigurationException | SAXException | TransformerException | IOException ex) {
            setError("Nastala vnitřní chyba programu - nepodařilo se uložit nového odběratele.");
            ex.printStackTrace();
            return;
        }

        clearCustomer();
        loadCustomers();
        setInfo("Odběratel '" + newCustomerName + "' byl v pořádku přidán.");
    }//GEN-LAST:event_addCustomerButtonActionPerformed

    private void deleteCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCustomerButtonActionPerformed
        String customerName = customerList.getSelectedItem().toString();

        try {
            this.customersWriter.removeCustomer(customerName);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | TransformerException ex) {
            setError("Nastala vnitřní chyba programu - nepodařilo se smazat odběratele '" + customerName + "'.");
            ex.printStackTrace();
            return;
        }

        loadCustomers();
        setInfo("Odběratel '" + customerName + "' byl v pořádku odebrán.");
    }//GEN-LAST:event_deleteCustomerButtonActionPerformed

    private void printInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printInvoiceActionPerformed
        Map<String, String> details = new TreeMap<String, String>();
        List<Pair<String, BigDecimal>> items = new ArrayList<Pair<String, BigDecimal>>();

        Date first = null;
        Date second = null;

        try {
            Integer invNumber = Integer.parseInt(invoiceNumberField.getText());
        } catch (NumberFormatException ex) {
            setError("Hodnota '" + invoiceNumber.getText() + "' musí být číslo.");
            ex.printStackTrace();
            return;
        }

        for (Map.Entry<Detail, JTextField> detailField : this.detailFields.entrySet()) {
            String detailFieldText = detailField.getValue().getText();
            Detail field = detailField.getKey();
            if (isEmpty(detailLabels.get(field).getText(), detailFieldText)) {
                return;
            }

            // check dates
            if (Detail.ISSUE_DATE.equals(field) || Detail.PAYMENT_DUE.equals(field)) {
                DateFormat inputDf = new SimpleDateFormat("dd.MM.yyyy");
                inputDf.setLenient(false);
                DateFormat outputDf = new SimpleDateFormat("d. M. yyyy");
                try {
                    Date result = inputDf.parse(detailFieldText);
                    if (Detail.ISSUE_DATE.equals(field)) {
                        first = result;
                    }
                    if (Detail.PAYMENT_DUE.equals(field)) {
                        second = result;
                    }
                    details.put(field.getNodeName(), outputDf.format(result));
                } catch (ParseException ex) {
                    setError("Pole '" + detailLabels.get(field).getText() + "' musí obsahovat platné datum ve formátu 'dd.mm.yyyy'.");
                    ex.printStackTrace();
                    return;
                }
            } else {
                details.put(field.getNodeName(), detailFieldText);
            }
        }

        // check if the date of payment is after the date of issue
        if (second.compareTo(first) < 0) {
            setError("Datum splatnosti musí být stejné nebo větší jako datum vystavení.");
            return;
        }

        // check if invoice has at least one item
        if (table.getModel().getRowCount() == 0) {
            setError("Faktura musí obsahovat alespoň jednu položku.");
            return;
        }

        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            String itemName = (String) table.getModel().getValueAt(i, 1);
            BigDecimal itemPrice = (BigDecimal) table.getModel().getValueAt(i, 2);
            items.add(new Pair(itemName, itemPrice));
        }

        try {
            InvoiceFactory iFac = new InvoiceFactory(customerList.getSelectedItem().toString(), details, items);
            InvoiceCreator iCre = new InvoiceCreator(iFac.renderInvoiceXml());
            iCre.createInvoice();
            setInfo("Byla vygenerována faktura '" + iFac.getInvoiceName() + "'.");
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException | TransformerException | ConfigurationException | URISyntaxException ex) {
            setError("Nastala vnitřní chyba programu - nepodařilo se vygenerovat fakturu.");
            ex.printStackTrace();
            return;
        }

        if (this.useAsInvoiceNumber.isSelected()) {
            try {
                this.numberWriter.changeNodeValue(XMLConstants.NUMBER_ROOT, Integer.toString(Integer.parseInt(invoiceNumberField.getText()) + 1));
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
                setError("Nastala vnitřní chyba programu - nepodařilo se uložit číslování.");
                ex.printStackTrace();
                return;
            }
        }

        clearInvoice();
    }//GEN-LAST:event_printInvoiceActionPerformed

    private void aboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutActionPerformed
        switchToPanel(aboutPanel);
    }//GEN-LAST:event_aboutActionPerformed

    private void clearInvoice() {
        loadInvoiceNumber();
        dateOfIssueField.setText("");
        dateOfValidityField.setText("");
        itemNameField.setText("");
        itemPriceField.setText("");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rows = table.getModel().getRowCount();
        for (int i = 0; i < rows; i++) {
            model.removeRow(0);
        }
        table.setModel(model);
        this.rowId = 0;
    }

    private void loadInvoiceNumber() {
        try {
            invoiceNumberField.setText(numberReader.getElementContentText(XMLConstants.NUMBER_XPATH));
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void clearCustomer() {
        for (Map.Entry<Customer, JTextField> customerField : this.customerFields.entrySet()) {
            JTextField formField = customerField.getValue();
            formField.setText("");
        }
        customerSaveAsField.setText("");
    }

    private boolean isSvg(File file) {
        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
        return "svg".equals(ext) && file.isFile();
    }

    private void addItem() {
        clearNotifications();

        String itemNameText = itemNameField.getText();
        String priceText = itemPriceField.getText();
        if (isEmpty(itemName.getText(), itemNameText)) {
            return;
        }
        if (isEmpty(itemPrice.getText(), priceText)) {
            return;
        }

        BigDecimal price = stringToBigDecimal(itemPrice.getText(), priceText);
        if (price == null) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Object[] row = {this.rowId, itemNameText, price};

        model.addRow(row);
        table.setModel(model);

        this.rowId++;
        clearNewInvoicePanel();
    }

    private void removeItem() {
        String idText = itemIdField.getText();
        if (isEmpty(itemId.getText(), idText)) {
            return;
        }

        Integer id = stringToInteger(itemId.getText(), idText);
        if (id == null) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0) == id) {
                model.removeRow(i);
                clearNewInvoicePanel();
                return;
            }
        }
        setError("Položka s ID " + id + " neexistuje.");
    }

    private BigDecimal stringToBigDecimal(String valueName, String value) {
        String czDecimalFormat = value.replace(",", ".");
        BigDecimal result = null;
        try {
            result = new BigDecimal(czDecimalFormat);
        } catch (NumberFormatException ex) {
            setError("Hodnota '" + valueName + "' musí být desetinné číslo.");
            ex.printStackTrace();
        }
        return result;
    }

    private Integer stringToInteger(String valueName, String value) {
        Integer result = null;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            setError("Hodnota '" + valueName + "' musí být celé číslo.");
            ex.printStackTrace();
        }
        return result;
    }

    private void setError(String msg) {
        notification.setForeground(new java.awt.Color(205, 10, 0));
        notification.setText("CHYBA: " + msg);
    }

    private void setInfo(String msg) {
        notification.setForeground(new java.awt.Color(2, 159, 9));
        notification.setText("INFORMACE: " + msg);
    }

    private void setWarn(String msg) {
        notification.setForeground(new java.awt.Color(255, 128, 0));
        notification.setText("VAROVÁNÍ: " + msg);
    }

    private void clearNotifications() {
        notification.setText("");
    }

    private void clearNewInvoicePanel() {
        itemNameField.setText("");
        itemPriceField.setText("");
        itemIdField.setText("");
    }

    private boolean isEmpty(String valueName, String value) {
        if ("".equals(value)) {
            setError("Hodnota '" + valueName + "' musí být vyplněna a není.");
            return true;
        } else {
            return false;
        }
    }

    private boolean warnIfIsEmpty(String valueName, String value) {
        if ("".equals(value)) {
            setWarn("Hodnota '" + valueName + "' by měla být vyplněna.");
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem about;
    private javax.swing.JLabel aboutAppName;
    private javax.swing.JLabel aboutAuthor;
    private javax.swing.JLabel aboutDate;
    private javax.swing.JTextArea aboutLicense;
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JButton addCustomerButton;
    private javax.swing.JButton addItem;
    private javax.swing.JLabel appName;
    private javax.swing.JMenuItem contractor;
    private javax.swing.JLabel contractorAccountNumber;
    private javax.swing.JTextField contractorAccountNumberField;
    private javax.swing.JLabel contractorAccountNumberNote;
    private javax.swing.JLabel contractorAddress;
    private javax.swing.JTextField contractorAddressField;
    private javax.swing.JLabel contractorBank;
    private javax.swing.JTextField contractorBankField;
    private javax.swing.JLabel contractorBankNote;
    private javax.swing.JLabel contractorCity;
    private javax.swing.JTextField contractorCityField;
    private javax.swing.JLabel contractorIco;
    private javax.swing.JTextField contractorIcoField;
    private javax.swing.JLabel contractorMail;
    private javax.swing.JTextField contractorMailField;
    private javax.swing.JLabel contractorName;
    private javax.swing.JTextField contractorNameField;
    private javax.swing.JLabel contractorNameNote;
    private javax.swing.JPanel contractorPanel;
    private javax.swing.JLabel contractorPhone;
    private javax.swing.JTextField contractorPhoneField;
    private javax.swing.JLabel contractorPostalCode;
    private javax.swing.JTextField contractorPostalCodeField;
    private javax.swing.JLabel contractorResultFolder;
    private javax.swing.JButton contractorResultFolderBrowseButton;
    private javax.swing.JTextField contractorResultFolderField;
    private javax.swing.JButton contractorSaveButton;
    private javax.swing.JButton contractorSignatureBrowseButton;
    private javax.swing.JLabel contractorSignatureFile;
    private javax.swing.JTextField contractorSignatureFileField;
    private javax.swing.JLabel contractorWeb;
    private javax.swing.JTextField contractorWebField;
    private javax.swing.JLabel currency;
    private javax.swing.JLabel customer;
    private javax.swing.JLabel customerAddress;
    private javax.swing.JTextField customerAddressField;
    private javax.swing.JLabel customerCity;
    private javax.swing.JTextField customerCityField;
    private javax.swing.JLabel customerDic;
    private javax.swing.JTextField customerDicField;
    private javax.swing.JMenuItem customerEntry;
    private javax.swing.JLabel customerIco;
    private javax.swing.JTextField customerIcoField;
    private javax.swing.JComboBox customerList;
    private javax.swing.JLabel customerName;
    private javax.swing.JTextField customerNameField;
    private javax.swing.JPanel customerPanel;
    private javax.swing.JLabel customerPostalCode;
    private javax.swing.JTextField customerPostalCodeField;
    private javax.swing.JLabel customerSaveAs;
    private javax.swing.JTextField customerSaveAsField;
    private javax.swing.JLabel customerSaveAsTip;
    private javax.swing.JComboBox customerToDeletionList;
    private javax.swing.JLabel dateOfIssue;
    private javax.swing.JTextField dateOfIssueField;
    private javax.swing.JLabel dateOfValidity;
    private javax.swing.JTextField dateOfValidityField;
    private javax.swing.JLabel deleteCustomer;
    private javax.swing.JButton deleteCustomerButton;
    private javax.swing.JButton deleteItem;
    private javax.swing.JLabel deleteItemHeadline;
    private javax.swing.JMenu file;
    private javax.swing.JMenu help;
    private javax.swing.JLabel invoiceNumber;
    private javax.swing.JTextField invoiceNumberField;
    private javax.swing.JLabel itemId;
    private javax.swing.JTextField itemIdField;
    private javax.swing.JLabel itemName;
    private javax.swing.JTextField itemNameField;
    private javax.swing.JLabel itemPrice;
    private javax.swing.JTextField itemPriceField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel logo;
    private javax.swing.JMenuBar menu;
    private javax.swing.JLabel newCustomer;
    private javax.swing.JMenuItem newInvoice;
    private javax.swing.JPanel newInvoicePanel;
    private javax.swing.JLabel newItemHeadline;
    private javax.swing.JLabel notification;
    private javax.swing.JButton printInvoice;
    private javax.swing.JMenu settings;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tablePanel;
    private javax.swing.JCheckBox useAsInvoiceNumber;
    private javax.swing.JPanel welcomePanel;
    // End of variables declaration//GEN-END:variables
}
