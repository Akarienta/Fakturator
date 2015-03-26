/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.akarienta.fakturator;

import cz.akarienta.fakturator.data.Contractor;
import cz.akarienta.fakturator.xml.XMLReader;
import cz.akarienta.fakturator.xml.XMLWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

/**
 *
 * @author akarienta
 */
public class GUI extends javax.swing.JFrame {
    
    private static final String CONTRACTOR_DATA = "src/cz/akarienta/fakturator/data/contractor.xml";
    private static final String CONTRACTOR_BASE_XPATH_Q = "/contractor/";

    private final List<JPanel> panels = new ArrayList<JPanel>();
    private static final List<String> customers = new ArrayList<String>();
    static {
        customers.add("Test1");
        customers.add("Test2");
    }
    private final Map<Contractor, JTextField> contractorFields = new EnumMap<Contractor, JTextField>(Contractor.class);
    private final Map<Contractor, JLabel> contractorLabels = new EnumMap<Contractor, JLabel>(Contractor.class);

    private int rowId;
    
    private final XMLReader xmlReader = new XMLReader(CONTRACTOR_DATA);
    private final XMLWriter xmlWriter = new XMLWriter(CONTRACTOR_DATA);

    /**
     * Creates new form gui
     */
    public GUI() {
        initComponents();
        localizeFileChooser();
        buildPanels();
        buildContractor();
        initContractorFieldNames();
        switchToPanel(welcomePanel);
        customerList.setModel(new DefaultComboBoxModel());
        for (String customer : customers) {
            customerList.addItem(customer);
        }
        loadContractor();
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
    }
    
    private void buildContractor() {
        buildContractorFields();
        buildContractorLabels();
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
    
    private boolean isContractorOk() {
        for(Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
            Contractor field = contractorField.getKey();
            
            try {
                String formFieldText = this.xmlReader.getElementContentText(CONTRACTOR_BASE_XPATH_Q + field.getNodeName());
                if(field.isMandatory() && formFieldText.isEmpty()) {
                    return false;
                }
            } catch (ParserConfigurationException|SAXException|XPathExpressionException|IOException ex) {
                setError("Nastala vnitřní chyba programu - nepodařilo se načíst uložená data pro dodavatele.");
                return false;
            }
        }
        
        return true;
    }
    
    private void initContractorFieldNames() {      
        for(Map.Entry<Contractor, JLabel> contractorField : this.contractorLabels.entrySet()) {
            JLabel formLabel = contractorField.getValue();
            Contractor field = contractorField.getKey();

            formLabel.setText(field.getLabel());
            if(field.isMandatory()) {
                formLabel.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
            }
        }

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
            XMLReader xmlReader = new XMLReader(CONTRACTOR_DATA);
            
            for(Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
                JTextField formField = contractorField.getValue();
                String nodeName = contractorField.getKey().getNodeName();
                
                formField.setText(xmlReader.getElementContentText(CONTRACTOR_BASE_XPATH_Q + nodeName));
            }
        } catch (ParserConfigurationException|SAXException|XPathExpressionException|IOException ex) {
            setError("Nastala vnitřní chyba programu - nepodařilo se načíst uložená data pro dodavatele.");
        }
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
        dateOfValidtyField = new javax.swing.JTextField();
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
        userAsInvoiceNumber = new javax.swing.JCheckBox();
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

        invoiceNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invoiceNumberFieldActionPerformed(evt);
            }
        });

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
                java.lang.Integer.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
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

        userAsInvoiceNumber.setText("použít pro číslování");

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
                                .addComponent(userAsInvoiceNumber)))
                        .addGap(49, 49, 49)
                        .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(newInvoicePanelLayout.createSequentialGroup()
                                .addComponent(dateOfValidity)
                                .addGap(18, 18, 18)
                                .addComponent(dateOfValidtyField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(userAsInvoiceNumber))
                .addGap(9, 9, 9)
                .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(newInvoicePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dateOfValidtyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        settings.add(customerEntry);

        menu.add(settings);

        help.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/life-buoy.png"))); // NOI18N
        help.setText("Nápověda");

        about.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        about.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cz/akarienta/fakturator/img/light-bulb.png"))); // NOI18N
        about.setText("O programu");
        help.add(about);

        menu.add(help);

        setJMenuBar(menu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void invoiceNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceNumberFieldActionPerformed
        // TODO
    }//GEN-LAST:event_invoiceNumberFieldActionPerformed

    private void printInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printInvoiceActionPerformed
        // TODO
    }//GEN-LAST:event_printInvoiceActionPerformed

    private void newInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newInvoiceActionPerformed
        switchToPanel(newInvoicePanel);
        printInvoice.setEnabled(true);
        if (!isContractorOk()) {
            printInvoice.setEnabled(false);
            setError("Dodavatel není vypleň korektně a nelze tedy vystavovat faktury. Pro opravu zmáčkněte Ctrl+D.");
        }
    }//GEN-LAST:event_newInvoiceActionPerformed

    private void addItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemActionPerformed
        addItem();
    }//GEN-LAST:event_addItemActionPerformed

    private void deleteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteItemActionPerformed
        removeItem();
    }//GEN-LAST:event_deleteItemActionPerformed

    private void contractorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractorActionPerformed
        switchToPanel(contractorPanel);
        for(Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
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
        
        for(Map.Entry<Contractor, JTextField> contractorField : this.contractorFields.entrySet()) {
            String formFieldText = contractorField.getValue().getText();
            Contractor field = contractorField.getKey();
            
            if (field.isMandatory()) {
                warnIfIsEmpty(field.getLabel(), formFieldText);
            }
            
            if (Contractor.SIGNATURE_PATH.equals(field) && !isSvg(new File(formFieldText))) {
                if(!formFieldText.isEmpty() || field.isMandatory()) {
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
                xmlWriter.changeNodeValue(field.getNodeName(), formFieldText);
            } catch (ParserConfigurationException|SAXException|TransformerException|IOException ex) {
                setError("Nastala vnitřní chyba programu - nepodařilo uložit pro dodavatele.");
            }
        }
        
        if (notification.getText().isEmpty()) {
            setInfo("Informace o dodavateli byly v pořádku uloženy.");
        }
    }//GEN-LAST:event_contractorSaveButtonActionPerformed
    
    private boolean isSvg(File file) {
            String ext = FilenameUtils.getExtension(file.getAbsolutePath());
            return "svg".equals(ext) && file.isFile();
    }
    
    private void addItem() {
        String itemNameText = itemNameField.getText();
        String priceText = itemPriceField.getText();
        if (isEmpty(itemName.getText(), itemNameText)) {
            return;
        }
        if (isEmpty(itemPrice.getText(), priceText)) {
            return;
        }

        Double price = stringToDouble(itemPrice.getText(), priceText);
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

    private Double stringToDouble(String valueName, String value) {
        String czDecimalFormat = value.replace(",", ".");
        Double result = null;
        try {
            result = Double.parseDouble(czDecimalFormat);
        } catch (NumberFormatException ex) {
            setError("Hodnota '" + valueName + "' musí být desetinné číslo.");
        }
        return result;
    }

    private Integer stringToInteger(String valueName, String value) {
        Integer result = null;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            setError("Hodnota '" + valueName + "' musí být celé číslo.");
        }
        return result;
    }

    private void setError(String msg) {
        notification.setForeground(new java.awt.Color(205, 10, 0));
        notification.setText("CHYBA: " + msg);
    }

    private void setInfo(String msg) {
        notification.setForeground(new java.awt.Color(2, 159, 9));
        notification.setText("INFO: " + msg);
    }
    
    private void setWarn(String msg) {
        notification.setForeground(new java.awt.Color(255, 128, 0));
        notification.setText("WARN: " + msg);
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
    private javax.swing.JMenuItem customerEntry;
    private javax.swing.JComboBox customerList;
    private javax.swing.JLabel dateOfIssue;
    private javax.swing.JTextField dateOfIssueField;
    private javax.swing.JLabel dateOfValidity;
    private javax.swing.JTextField dateOfValidtyField;
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
    private javax.swing.JLabel logo;
    private javax.swing.JMenuBar menu;
    private javax.swing.JMenuItem newInvoice;
    private javax.swing.JPanel newInvoicePanel;
    private javax.swing.JLabel newItemHeadline;
    private javax.swing.JLabel notification;
    private javax.swing.JButton printInvoice;
    private javax.swing.JMenu settings;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tablePanel;
    private javax.swing.JCheckBox userAsInvoiceNumber;
    private javax.swing.JPanel welcomePanel;
    // End of variables declaration//GEN-END:variables
}
