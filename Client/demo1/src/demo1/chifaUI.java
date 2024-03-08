/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package demo1;

import DataPackage.Data;
import DataPackage.Product;
import DataPackage.Patient;
import DataPackage.ProductOnCart;
import DataPackage.Order;
import DataPackage.User;
import static demo1.AuthFrame.mainUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author MoHaTiGha
 */
public class chifaUI extends javax.swing.JFrame {

    static String ClientName;
    static String ClientID;
    static AuthFrame authMain;
    User currentUser;
    int ID;
    int orderID;
    int patientID;
    ArrayList<String> Categories;
    ArrayList<String> Suppliers;
    ArrayList<Product> ProductsStock;
    ArrayList<Product> ProductsOnCart;
    ArrayList<ProductOnCart> ProductsOnCartHelper;
    ArrayList<Order> OrdersList;
    ArrayList<Patient> PatientsList;
    ArrayList<Integer> selectedIndexes;
    Product selectedProductForCart;
    Order selectedOrder;
    DateTimeFormatter formatterDateOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter formatterDateAndHour = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static Socket socket;
    static ObjectOutputStream objectOutputStream;
    static ObjectInputStream objectInputStream;

    public chifaUI(User currentUser) {
        try {
            initComponents();
            this.currentUser = currentUser;
            getMainProductsTable().setDefaultRenderer(Object.class, new MonCellRenderer());
            getRunningStockTable().setDefaultRenderer(Object.class, new MonCellRenderer());
            ID = 0;
            orderID = 0;
            getMainSPanel().setVisible(true);
            getStockSPanel().setVisible(false);
            getSalesSPanel().setVisible(false);
            getOrdersSPanel().setVisible(false);
            Categories = new ArrayList();
            Suppliers = new ArrayList();
            ProductsStock = new ArrayList();
            PatientsList = new ArrayList();
            selectedIndexes = new ArrayList();
            OrdersList = new ArrayList();
            ProductsOnCart = new ArrayList();
            ProductsOnCartHelper = new ArrayList();
            getAddCategoryButton().setEnabled(false);
            getAddProductButton().setEnabled(false);
            getUpdateProductButton().setEnabled(false);
            getDeleteProductButton().setEnabled(false);
            getAddToCartButton().setEnabled(false);
            getRemoveFromCartButton().setEnabled(false);
            getSaveAndPrintButton().setEnabled(false);
            String hostName = "Unknown";
            InetAddress adr;
            adr = InetAddress.getLocalHost();
            hostName = adr.getHostName();
            ClientName = hostName;
            System.out.println(hostName + " HERE");
            objectOutputStream.writeObject(new Data("CLIENT NAME", hostName));
            Data sentData = new Data("FETCH PRODUCTS", null);
            objectOutputStream.writeObject(sentData);
            objectOutputStream.writeObject(new Data("FETCH PATIENTS", null));
            objectOutputStream.writeObject(new Data("FETCH ORDERS", null));
            getCategories();
            Thread receiveData = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            Data receivedData = (Data) objectInputStream.readObject();
                            System.out.println(receivedData.getInstruction());
                            if (receivedData.getInstruction().equals("PRODUCTS GIVEN")) {
                                ProductsStock = (ArrayList<Product>) receivedData.getObject();
                                fetchProductsOnMainAndRunning();
                                System.out.println(ProductsStock.size());
                            } else if (receivedData.getInstruction().equals("CLIENT ID")) {
                                ClientID = (String) receivedData.getObject();
                            } else if (receivedData.getInstruction().equals("ORDERS GIVEN")) {
                                OrdersList = (ArrayList<Order>) receivedData.getObject();
                                fetchOrdersOnLog();
                            }else if(receivedData.getInstruction().equals("LOGIN ERROR")){
                                authMain.getAuthErrorLabel().setText("WRONG CREDENTIALS");
                                authMain.getAuthErrorLabel().setForeground(Color.red);
                            } 
                            else if (receivedData.getInstruction().equals("PATIENTS GIVEN")) {
                                PatientsList = (ArrayList<Patient>) receivedData.getObject();
                            } else if (receivedData.getInstruction().equals("ORDER UPDATED")) {
                                objectOutputStream.writeObject(new Data("FETCH ORDERS", null));
                            } else if (receivedData.getInstruction().equals("EXIT NOW")) {
                                System.out.println("EXITING");
                                System.exit(0);
                            } else if (receivedData.getInstruction().equals("LOGIN OK")) {
                                authMain.setVisible(false);
                                User currentUserNEW = (User) receivedData.getObject();
                                if (!currentUserNEW.isIsAdmin()) {
                                    stockTabPanel.setVisible(false);
                                    getRemoveOrderButton().setVisible(false);
                                    getUpdateOrderButton().setVisible(false);
                                }
                                mainUI.setVisible(true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            receiveData.start();
        } catch (IOException ex) {
            Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void getCategories() {
        ArrayList<String> cats = new ArrayList();
        cats.add("None");
        for (Product curProd : ProductsStock) {
            if (!cats.contains(curProd.getCategory())) {
                cats.add(curProd.getCategory());
            }
        }
        Categories = cats;
        addCategoryToComboBox();
    }

    void fetchOrdersOnLog() {

        DefaultTableModel OrdersTableModel = (DefaultTableModel) getOrdersLogTable().getModel();
        int rowCount = OrdersTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            OrdersTableModel.removeRow(0);
        }
        //objectOutputStream.writeObject(new Data("FETCH ORDERS",null));
        for (Order order : OrdersList) {
            OrdersTableModel.addRow(new Object[]{order.getID(), order.getPatient().getFirstName(), order.getPatient().getLastName(), order.getPatient().getAdress(), order.getDate().format(formatterDateOnly)});
        }

    }

    void fetchProductsOnCart() {
        DefaultTableModel CartTableModel = (DefaultTableModel) getProductsOnCartTable().getModel();
        int rowCount = CartTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            CartTableModel.removeRow(0);
        }
        for (ProductOnCart product : ProductsOnCartHelper) {
            CartTableModel.addRow(new Object[]{product.getProduct().getID(), product.getProduct().getName(), product.getQuantity(), product.getProduct().getSellPrice()});
        }
    }

    void fetchProductsOnMainAndRunning() {

        DefaultTableModel MainProductsTableModel = (DefaultTableModel) getMainProductsTable().getModel();
        DefaultTableModel RunningStockTableModel = (DefaultTableModel) getRunningStockTable().getModel();
        int rowCount = MainProductsTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            MainProductsTableModel.removeRow(0);
            RunningStockTableModel.removeRow(0);
        }

        for (Product product : ProductsStock) {
            MainProductsTableModel.addRow(new Object[]{product.getID(), product.getName(), product.getCategory(), product.getQuantity(), product.getSellPrice()});
            RunningStockTableModel.addRow(new Object[]{product.getID(), product.getName(), product.getCategory(), product.getQuantity(), product.getSellPrice()});
        }
        getCategories();
    }

    void fetchProductOnRecent(int productID, String productName, String productCategory, int productQty, double productPrice) {
        DefaultTableModel AddedRecentlyTableModel = (DefaultTableModel) getAddedRecentlyTable().getModel();
        AddedRecentlyTableModel.addRow(new Object[]{productID, productName, productCategory, productQty, productPrice});
    }

    public JButton getAddCategoryButton() {
        return addCategoryButton;
    }

    public void setAddCategoryButton(JButton addCategoryButton) {
        this.addCategoryButton = addCategoryButton;
    }

    public JComboBox<String> getFilterComboBox() {
        return filterComboBox;
    }

    public JButton getAddProductButton() {
        return addProductButton;
    }

    public JTable getProductsOnCartTable() {
        return ProductsOnCartTable;
    }

    public void addCategoryToComboBox() {
        Object[] categoriess = Categories.toArray();
        getCategoriesComboBox().setModel(new DefaultComboBoxModel(categoriess));
        getCategoriesComboBox2().setModel(new DefaultComboBoxModel(categoriess));
        getCategoryComboBox3().setModel(new DefaultComboBoxModel(categoriess));
    }

    public JTextField getCategoriesNameField() {
        return categoriesNameField;
    }

    public JTextField getPatientAddPhoneField() {
        return patientAddPhoneField;
    }

    public JTable getRunningStockTable() {
        return runningStockTable;
    }

    public JButton getUpdateProductButton() {
        return updateProductButton;
    }

    public void setCategoriesNameField(JTextField categoriesNameField) {
        this.categoriesNameField = categoriesNameField;
    }

    public JTable getMainProductsTable() {
        return mainProductsTable;
    }

    public JLabel getProductDetailCategoryLabel() {
        return productDetailCategoryLabel;
    }

    public JLabel getProductDetailNameLabel() {
        return productDetailNameLabel;
    }

    public JLabel getProductDetailPriceLabel() {
        return productDetailPriceLabel;
    }

    public JTextField getProductDetailQtyLabelField() {
        return productDetailQtyLabelField;
    }

    public JComboBox<String> getCategoryComboBox3() {
        return categoryComboBox3;
    }

    public JComboBox<String> getSupplierComboBox3() {
        return filterComboBox;
    }

    public void setMainProductsTable(JTable mainProductsTable) {
        this.mainProductsTable = mainProductsTable;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel65 = new javax.swing.JLabel();
        leftPanel = new javax.swing.JPanel();
        mainTabPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        stockTabPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        stockPanelIcon = new javax.swing.JLabel();
        sellingTabPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        salesPanelIcon = new javax.swing.JLabel();
        ordersLogTabPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        ordersPanelIcon = new javax.swing.JLabel();
        mainLogoLabel = new javax.swing.JLabel();
        mainPanel = new javax.swing.JLayeredPane();
        salesSPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        firstNameCartField = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        ageCartField = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        addPatientGenderBox = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ProductsOnCartTable = new javax.swing.JTable();
        removeFromCartButton = new javax.swing.JButton();
        jLabel57 = new javax.swing.JLabel();
        adressCartField = new javax.swing.JTextField();
        jLabel58 = new javax.swing.JLabel();
        lastNameCartField = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        noteCartField = new javax.swing.JTextField();
        jLabel63 = new javax.swing.JLabel();
        patientAddPhoneField = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        runningStockTable = new javax.swing.JTable();
        jLabel33 = new javax.swing.JLabel();
        searchingSalesSectionField = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        categoryComboBox3 = new javax.swing.JComboBox<>();
        jLabel50 = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox<>();
        jPanel10 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        productDetailNameLabel = new javax.swing.JLabel();
        productDetailCategoryLabel = new javax.swing.JLabel();
        productDetailPriceLabel = new javax.swing.JLabel();
        productDetailQtyLabelField = new javax.swing.JTextField();
        addToCartButton = new javax.swing.JButton();
        jLabel43 = new javax.swing.JLabel();
        saveAndPrintButton = new javax.swing.JButton();
        jLabel44 = new javax.swing.JLabel();
        priceTotalLabel = new javax.swing.JLabel();
        salesPanelErrorLabel = new javax.swing.JLabel();
        stockSPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainProductsTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        productDetailIDField = new javax.swing.JTextField();
        productDetailNameField = new javax.swing.JTextField();
        productDetailQtyField = new javax.swing.JTextField();
        productDetailBuyPriceField = new javax.swing.JTextField();
        productDetailSellPriceField = new javax.swing.JTextField();
        productDetailExpDateField = new javax.swing.JTextField();
        categoriesComboBox2 = new javax.swing.JComboBox<>();
        updateProductButton = new javax.swing.JButton();
        deleteProductButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        productAddName = new javax.swing.JTextField();
        productAddQuantity = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        productAddBuyPrice = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        productAddSellPrice = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        productAddExpDate = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        categoriesComboBox1 = new javax.swing.JComboBox<>();
        addProductButton = new javax.swing.JButton();
        errorLabel1 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        addedRecentlyTable = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        categoriesNameField = new javax.swing.JTextField();
        addCategoryButton = new javax.swing.JButton();
        ordersSPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        ordersLogTable = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        orderIDLabel = new javax.swing.JLabel();
        orderDateLabel = new javax.swing.JLabel();
        firstNameField = new javax.swing.JTextField();
        lastNameField = new javax.swing.JTextField();
        ageField = new javax.swing.JTextField();
        adressField = new javax.swing.JTextField();
        noteField = new javax.swing.JTextField();
        removeOrderButton = new javax.swing.JButton();
        updateOrderButton = new javax.swing.JButton();
        jLabel62 = new javax.swing.JLabel();
        patientInfoGenderBox = new javax.swing.JComboBox<>();
        jLabel64 = new javax.swing.JLabel();
        phoneNumberField = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        purchasesInfoTable = new javax.swing.JTable();
        jLabel56 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel61 = new javax.swing.JLabel();
        mainSPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        topPanel.setBackground(new java.awt.Color(51, 204, 0));
        topPanel.setForeground(new java.awt.Color(0, 153, 0));

        jLabel8.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Pharmacy Management");

        jButton2.setBackground(new java.awt.Color(0, 204, 0));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("EXIT");
        jButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel65.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo6.png"))); // NOI18N

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel65)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel65))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addContainerGap())
        );

        leftPanel.setBackground(new java.awt.Color(255, 255, 255));
        leftPanel.setMaximumSize(new java.awt.Dimension(105, 140));

        mainTabPanel.setBackground(new java.awt.Color(102, 204, 0));
        mainTabPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mainTabPanelMousePressed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Main Panel");

        jLabel66.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/homeIcon.png"))); // NOI18N

        javax.swing.GroupLayout mainTabPanelLayout = new javax.swing.GroupLayout(mainTabPanel);
        mainTabPanel.setLayout(mainTabPanelLayout);
        mainTabPanelLayout.setHorizontalGroup(
            mainTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel66)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mainTabPanelLayout.setVerticalGroup(
            mainTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainTabPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(mainTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel66)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        stockTabPanel.setBackground(new java.awt.Color(102, 204, 0));
        stockTabPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                stockTabPanelMousePressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Stock");

        stockPanelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/stockIcon.png"))); // NOI18N

        javax.swing.GroupLayout stockTabPanelLayout = new javax.swing.GroupLayout(stockTabPanel);
        stockTabPanel.setLayout(stockTabPanelLayout);
        stockTabPanelLayout.setHorizontalGroup(
            stockTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stockTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(stockPanelIcon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        stockTabPanelLayout.setVerticalGroup(
            stockTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stockTabPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(stockTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(stockPanelIcon)
                    .addComponent(jLabel2))
                .addContainerGap())
        );

        sellingTabPanel.setBackground(new java.awt.Color(102, 204, 0));
        sellingTabPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                sellingTabPanelMousePressed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Sales");

        salesPanelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/salesIcon.png"))); // NOI18N

        javax.swing.GroupLayout sellingTabPanelLayout = new javax.swing.GroupLayout(sellingTabPanel);
        sellingTabPanel.setLayout(sellingTabPanelLayout);
        sellingTabPanelLayout.setHorizontalGroup(
            sellingTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sellingTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(salesPanelIcon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sellingTabPanelLayout.setVerticalGroup(
            sellingTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sellingTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sellingTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(salesPanelIcon))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ordersLogTabPanel.setBackground(new java.awt.Color(102, 204, 0));
        ordersLogTabPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                ordersLogTabPanelMousePressed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Orders Log");

        ordersPanelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ordersIcon.png"))); // NOI18N

        javax.swing.GroupLayout ordersLogTabPanelLayout = new javax.swing.GroupLayout(ordersLogTabPanel);
        ordersLogTabPanel.setLayout(ordersLogTabPanelLayout);
        ordersLogTabPanelLayout.setHorizontalGroup(
            ordersLogTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ordersLogTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ordersPanelIcon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ordersLogTabPanelLayout.setVerticalGroup(
            ordersLogTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ordersLogTabPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(ordersLogTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ordersPanelIcon)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        mainLogoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo5.png"))); // NOI18N

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(stockTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(sellingTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(ordersLogTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(mainLogoLabel)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainLogoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(stockTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(sellingTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(ordersLogTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainPanel.setMaximumSize(new java.awt.Dimension(1024, 460));
        mainPanel.setMinimumSize(new java.awt.Dimension(1024, 460));

        salesSPanel.setBackground(new java.awt.Color(204, 204, 204));
        salesSPanel.setEnabled(false);
        salesSPanel.setMaximumSize(new java.awt.Dimension(1024, 460));
        salesSPanel.setMinimumSize(new java.awt.Dimension(1024, 460));
        salesSPanel.setPreferredSize(new java.awt.Dimension(1024, 460));

        jPanel7.setBackground(new java.awt.Color(51, 204, 0));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Sales Management Section");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(263, 263, 263)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel27.setText("Patient Info.");

        jLabel28.setText("First Name");

        jLabel29.setText("Age");

        jLabel30.setText("Gender");

        addPatientGenderBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel31.setText("Cart");

        ProductsOnCartTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Quantity", "Price/Unit"
            }
        ));
        ProductsOnCartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                ProductsOnCartTableMousePressed(evt);
            }
        });
        jScrollPane3.setViewportView(ProductsOnCartTable);

        removeFromCartButton.setForeground(new java.awt.Color(255, 0, 0));
        removeFromCartButton.setText("Remove");
        removeFromCartButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                removeFromCartButtonMousePressed(evt);
            }
        });

        jLabel57.setText("Adress");

        jLabel58.setText("Last Name");

        jLabel59.setText("Note");

        jLabel63.setText("Phone");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel58)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lastNameCartField))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(firstNameCartField, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel57)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(adressCartField))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel59)
                                .addGap(18, 18, 18)
                                .addComponent(noteCartField))))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel29)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ageCartField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(addPatientGenderBox, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel31)
                            .addComponent(jLabel27)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel63)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(patientAddPhoneField, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(126, 126, 126)
                .addComponent(removeFromCartButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(firstNameCartField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel57)
                    .addComponent(adressCartField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel58)
                    .addComponent(lastNameCartField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel59)
                    .addComponent(noteCartField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(ageCartField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30)
                    .addComponent(addPatientGenderBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel63)
                    .addComponent(patientAddPhoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeFromCartButton)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel32.setText("Running Stock");

        runningStockTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Category", "Quantity on stock", "Price/Unit"
            }
        ));
        runningStockTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                runningStockTableMousePressed(evt);
            }
        });
        jScrollPane4.setViewportView(runningStockTable);

        jLabel33.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel33.setText("Search By Name/ID");

        searchingSalesSectionField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                searchingSalesSectionFieldKeyTyped(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel34.setText("Category");

        categoryComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryComboBox3ActionPerformed(evt);
            }
        });

        jLabel50.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel50.setText("Filter");

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No filtering", "Expired", "Non-Available", "Available and non-Expired" }));
        filterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(searchingSalesSectionField))
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(63, 63, 63)
                                .addComponent(jLabel32)
                                .addGap(58, 58, 58))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel34)
                                    .addComponent(categoryComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(23, 23, 23)
                                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel50)
                                    .addComponent(filterComboBox, 0, 1, Short.MAX_VALUE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel32)
                .addGap(2, 2, 2)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(jLabel34)
                    .addComponent(jLabel50))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchingSalesSectionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63))
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel35.setText("Details");

        jLabel36.setText("Name:");

        jLabel37.setText("Price/Unit:");

        jLabel38.setText("Category:");

        jLabel39.setText("Quantity:");

        productDetailNameLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        productDetailNameLabel.setText("none");

        productDetailCategoryLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        productDetailCategoryLabel.setText("none");

        productDetailPriceLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        productDetailPriceLabel.setText("none");

        productDetailQtyLabelField.setText("1");
        productDetailQtyLabelField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productDetailQtyLabelFieldActionPerformed(evt);
            }
        });

        addToCartButton.setForeground(new java.awt.Color(0, 153, 0));
        addToCartButton.setText("Add to cart");
        addToCartButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                addToCartButtonMousePressed(evt);
            }
        });

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel43.setText("Billing");

        saveAndPrintButton.setText("Save and Print");
        saveAndPrintButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveAndPrintButtonMousePressed(evt);
            }
        });

        jLabel44.setText("Net to pay:");

        priceTotalLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        priceTotalLabel.setForeground(new java.awt.Color(51, 153, 0));
        priceTotalLabel.setText("0DA");
        priceTotalLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(saveAndPrintButton, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38))
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(salesPanelErrorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(priceTotalLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel35)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(jLabel37)
                                .addGap(18, 18, 18)
                                .addComponent(productDetailPriceLabel))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addGap(18, 18, 18)
                                .addComponent(productDetailQtyLabelField, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(addToCartButton, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel43)
                            .addComponent(jLabel44)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel38)
                                    .addComponent(jLabel36))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(productDetailNameLabel)
                                    .addComponent(productDetailCategoryLabel))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel35)
                .addGap(12, 12, 12)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(productDetailNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(productDetailCategoryLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(productDetailPriceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(productDetailQtyLabelField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addToCartButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(salesPanelErrorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel44)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(priceTotalLabel)
                .addGap(30, 30, 30)
                .addComponent(saveAndPrintButton)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout salesSPanelLayout = new javax.swing.GroupLayout(salesSPanel);
        salesSPanel.setLayout(salesSPanelLayout);
        salesSPanelLayout.setHorizontalGroup(
            salesSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(salesSPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        salesSPanelLayout.setVerticalGroup(
            salesSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(salesSPanelLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(salesSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 399, Short.MAX_VALUE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        stockSPanel.setBackground(new java.awt.Color(255, 255, 255));
        stockSPanel.setEnabled(false);
        stockSPanel.setMaximumSize(new java.awt.Dimension(1024, 460));
        stockSPanel.setMinimumSize(new java.awt.Dimension(1024, 460));
        stockSPanel.setPreferredSize(new java.awt.Dimension(1024, 460));

        jPanel1.setBackground(new java.awt.Color(0, 204, 0));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Stock Management Section");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(228, 228, 228)
                .addComponent(jLabel9)
                .addContainerGap(616, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(1100, 470));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(1100, 470));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1100, 470));

        mainProductsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Category", "Quantity", "Price/Unit"
            }

        )
        {public boolean isCellEditable(int row, int column){return false;}}
    );
    mainProductsTable.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            mainProductsTableMousePressed(evt);
        }
    });
    jScrollPane1.setViewportView(mainProductsTable);

    jPanel4.setBackground(new java.awt.Color(255, 255, 255));

    jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel6.setText("Product Details");

    jLabel10.setText("ID:");

    jLabel11.setText("Name:");

    jLabel12.setText("Category:");

    jLabel14.setText("Quantity:");

    jLabel23.setText("Buy Price:");

    jLabel24.setText("Sell Price:");

    jLabel26.setText("Exp. Date:");

    productDetailIDField.setEditable(false);

    updateProductButton.setText("Update");
    updateProductButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            updateProductButtonMousePressed(evt);
        }
    });

    deleteProductButton.setText("Delete");
    deleteProductButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            deleteProductButtonMousePressed(evt);
        }
    });

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addComponent(jLabel6)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel10)
                        .addComponent(jLabel11)
                        .addComponent(jLabel12)
                        .addComponent(jLabel14)
                        .addComponent(jLabel23)
                        .addComponent(jLabel24)
                        .addComponent(jLabel26))
                    .addGap(21, 21, 21)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(productDetailExpDateField)
                        .addComponent(productDetailIDField)
                        .addComponent(productDetailNameField)
                        .addComponent(productDetailQtyField)
                        .addComponent(productDetailBuyPriceField)
                        .addComponent(productDetailSellPriceField)
                        .addComponent(categoriesComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addGap(0, 212, Short.MAX_VALUE)
                    .addComponent(deleteProductButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(updateProductButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel6)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel10)
                .addComponent(productDetailIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel11)
                .addComponent(productDetailNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel12)
                .addComponent(categoriesComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(34, 34, 34)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel14)
                .addComponent(productDetailQtyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel23)
                .addComponent(productDetailBuyPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel24)
                .addComponent(productDetailSellPriceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel26)
                .addComponent(productDetailExpDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(updateProductButton)
                .addComponent(deleteProductButton))
            .addContainerGap(65, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 567, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jTabbedPane1.addTab("Show Products", jPanel2);

    jPanel5.setBackground(new java.awt.Color(255, 255, 255));

    jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel15.setText("Add Product");

    jLabel16.setText("Name:");

    productAddName.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            productAddNameKeyTyped(evt);
        }
    });

    productAddQuantity.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            productAddQuantityKeyTyped(evt);
        }
    });

    jLabel17.setText("Quantity:");

    productAddBuyPrice.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            productAddBuyPriceKeyTyped(evt);
        }
    });

    jLabel18.setText("Buy Price:");

    productAddSellPrice.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            productAddSellPriceKeyTyped(evt);
        }
    });

    jLabel19.setText("Sell Price:");

    jLabel20.setText("Expiry Date:");

    productAddExpDate.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            productAddExpDateKeyTyped(evt);
        }
    });

    jLabel21.setText("Category:");

    addProductButton.setText("Add Product");
    addProductButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            addProductButtonMousePressed(evt);
        }
    });
    addProductButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            addProductButtonActionPerformed(evt);
        }
    });

    errorLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(errorLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                    .addComponent(jLabel15)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel21)
                        .addComponent(jLabel17)
                        .addComponent(jLabel18)
                        .addComponent(jLabel16))
                    .addGap(34, 34, 34)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(categoriesComboBox1, javax.swing.GroupLayout.Alignment.TRAILING, 0, 178, Short.MAX_VALUE)
                        .addComponent(productAddQuantity, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(productAddBuyPrice)
                        .addComponent(productAddName)))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel19)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(12, 12, 12)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(productAddExpDate)
                        .addComponent(productAddSellPrice))))
            .addContainerGap())
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(addProductButton)
            .addGap(47, 47, 47))
    );
    jPanel5Layout.setVerticalGroup(
        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel5Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel15)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel16)
                .addComponent(productAddName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(3, 3, 3)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel21)
                .addComponent(categoriesComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(31, 31, 31)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel17)
                .addComponent(productAddQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel18)
                .addComponent(productAddBuyPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel19)
                .addComponent(productAddSellPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel20)
                .addComponent(productAddExpDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
            .addComponent(addProductButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(errorLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    jPanel6.setBackground(new java.awt.Color(255, 255, 255));

    jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel25.setText("Added Recently");

    addedRecentlyTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "ID", "Name", "Category", "Quantity", "Price/Unit"
        }
    ));
    jScrollPane2.setViewportView(addedRecentlyTable);

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel6Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addComponent(jLabel25)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addComponent(jScrollPane2))
            .addContainerGap())
    );
    jPanel6Layout.setVerticalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel6Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel25)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
            .addContainerGap())
    );

    jPanel11.setBackground(new java.awt.Color(255, 255, 255));

    jLabel46.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel46.setText("Add Category");

    jLabel47.setText("Category's Name");

    categoriesNameField.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            categoriesNameFieldKeyTyped(evt);
        }
    });

    addCategoryButton.setText("Add");
    addCategoryButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            addCategoryButtonMousePressed(evt);
        }
    });

    javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
    jPanel11.setLayout(jPanel11Layout);
    jPanel11Layout.setHorizontalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel46)
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addComponent(jLabel47)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(categoriesNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(addCategoryButton)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel11Layout.setVerticalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel46)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel47)
                .addComponent(categoriesNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(addCategoryButton))
            .addContainerGap(60, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(247, 247, 247))
    );
    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
    );

    jTabbedPane1.addTab("Add Product", jPanel3);

    javax.swing.GroupLayout stockSPanelLayout = new javax.swing.GroupLayout(stockSPanel);
    stockSPanel.setLayout(stockSPanelLayout);
    stockSPanelLayout.setHorizontalGroup(
        stockSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(stockSPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addContainerGap())
    );
    stockSPanelLayout.setVerticalGroup(
        stockSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(stockSPanelLayout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 421, Short.MAX_VALUE)
            .addContainerGap())
    );

    ordersSPanel.setEnabled(false);
    ordersSPanel.setMaximumSize(new java.awt.Dimension(1024, 460));
    ordersSPanel.setMinimumSize(new java.awt.Dimension(1024, 460));
    ordersSPanel.setPreferredSize(new java.awt.Dimension(1024, 460));

    ordersLogTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "ID", "First Name", "Last Name", "Adress", "Date"
        }
    ));
    ordersLogTable.setMaximumSize(new java.awt.Dimension(450, 450));
    ordersLogTable.setMinimumSize(new java.awt.Dimension(450, 450));
    ordersLogTable.setPreferredSize(new java.awt.Dimension(450, 450));
    ordersLogTable.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            ordersLogTableMousePressed(evt);
        }
    });
    jScrollPane5.setViewportView(ordersLogTable);

    jPanel12.setBackground(new java.awt.Color(255, 255, 255));
    jPanel12.setMaximumSize(new java.awt.Dimension(280, 450));
    jPanel12.setMinimumSize(new java.awt.Dimension(280, 450));

    jLabel40.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel40.setText("Order Info.");

    jLabel41.setText("ID");

    jLabel42.setText("Date ordered");

    jLabel45.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel45.setText("Patient's Info.");

    jLabel51.setText("First Name");

    jLabel52.setText("Last Name");

    jLabel53.setText("Age");

    jLabel54.setText("Adress");

    jLabel55.setText("Note");

    orderIDLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
    orderIDLabel.setText("no order were selected");

    orderDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
    orderDateLabel.setText("no order were selected");

    removeOrderButton.setText("Remove");
    removeOrderButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            removeOrderButtonMousePressed(evt);
        }
    });

    updateOrderButton.setText("Update");
    updateOrderButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            updateOrderButtonMousePressed(evt);
        }
    });

    jLabel62.setText("Gender");

    patientInfoGenderBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));

    jLabel64.setText("Phone");

    javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
    jPanel12.setLayout(jPanel12Layout);
    jPanel12Layout.setHorizontalGroup(
        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel12Layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel40)
                .addComponent(jLabel45)
                .addGroup(jPanel12Layout.createSequentialGroup()
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel41)
                        .addComponent(jLabel42))
                    .addGap(27, 27, 27)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(orderDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(orderIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel12Layout.createSequentialGroup()
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel51)
                        .addComponent(jLabel52)
                        .addComponent(jLabel53))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lastNameField, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(ageField, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(firstNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))))
            .addGap(57, 57, 57))
        .addGroup(jPanel12Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(jPanel12Layout.createSequentialGroup()
                    .addComponent(removeOrderButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(updateOrderButton))
                .addGroup(jPanel12Layout.createSequentialGroup()
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel54)
                        .addComponent(jLabel55)
                        .addComponent(jLabel62)
                        .addComponent(jLabel64))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(patientInfoGenderBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(adressField)
                        .addComponent(noteField)
                        .addComponent(phoneNumberField))))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel12Layout.setVerticalGroup(
        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel12Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel40)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel41)
                .addComponent(orderIDLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel42)
                .addComponent(orderDateLabel))
            .addGap(18, 18, 18)
            .addComponent(jLabel45)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel51)
                .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel52)
                .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel53)
                .addComponent(ageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(5, 5, 5)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel62)
                .addComponent(patientInfoGenderBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(1, 1, 1)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel64)
                .addComponent(phoneNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel54)
                .addComponent(adressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel55)
                .addComponent(noteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(removeOrderButton)
                .addComponent(updateOrderButton))
            .addGap(121, 121, 121))
    );

    purchasesInfoTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "ID", "Name", "Category", "Qty Purchased"
        }
    ));
    purchasesInfoTable.setMaximumSize(new java.awt.Dimension(450, 280));
    purchasesInfoTable.setMinimumSize(new java.awt.Dimension(450, 280));
    purchasesInfoTable.setPreferredSize(new java.awt.Dimension(450, 280));
    jScrollPane6.setViewportView(purchasesInfoTable);

    jLabel56.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel56.setText("Purchases Info");

    jLabel60.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
    jLabel60.setText("Orders List");

    jPanel13.setBackground(new java.awt.Color(51, 204, 0));

    jLabel61.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
    jLabel61.setForeground(new java.awt.Color(255, 255, 255));
    jLabel61.setText("Orders Management");

    javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
    jPanel13.setLayout(jPanel13Layout);
    jPanel13Layout.setHorizontalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel61)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel13Layout.setVerticalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel61)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout ordersSPanelLayout = new javax.swing.GroupLayout(ordersSPanel);
    ordersSPanel.setLayout(ordersSPanelLayout);
    ordersSPanelLayout.setHorizontalGroup(
        ordersSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(ordersSPanelLayout.createSequentialGroup()
            .addGroup(ordersSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ordersSPanelLayout.createSequentialGroup()
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                .addGroup(ordersSPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel60)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(ordersSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel56)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(12, 12, 12))
    );
    ordersSPanelLayout.setVerticalGroup(
        ordersSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ordersSPanelLayout.createSequentialGroup()
            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel60)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(ordersSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(ordersSPanelLayout.createSequentialGroup()
                    .addComponent(jLabel56)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18))
    );

    mainSPanel.setBackground(new java.awt.Color(255, 255, 255));
    mainSPanel.setMaximumSize(new java.awt.Dimension(1024, 460));
    mainSPanel.setMinimumSize(new java.awt.Dimension(1024, 460));
    mainSPanel.setPreferredSize(new java.awt.Dimension(1024, 460));

    jLabel5.setText("Main");

    javax.swing.GroupLayout mainSPanelLayout = new javax.swing.GroupLayout(mainSPanel);
    mainSPanel.setLayout(mainSPanelLayout);
    mainSPanelLayout.setHorizontalGroup(
        mainSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(mainSPanelLayout.createSequentialGroup()
            .addGap(109, 109, 109)
            .addComponent(jLabel5)
            .addContainerGap(889, Short.MAX_VALUE))
    );
    mainSPanelLayout.setVerticalGroup(
        mainSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(mainSPanelLayout.createSequentialGroup()
            .addGap(104, 104, 104)
            .addComponent(jLabel5)
            .addContainerGap(340, Short.MAX_VALUE))
    );

    mainPanel.setLayer(salesSPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
    mainPanel.setLayer(stockSPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
    mainPanel.setLayer(ordersSPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
    mainPanel.setLayer(mainSPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

    javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
    mainPanel.setLayout(mainPanelLayout);
    mainPanelLayout.setHorizontalGroup(
        mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(mainPanelLayout.createSequentialGroup()
            .addComponent(ordersSPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 12, Short.MAX_VALUE))
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(stockSPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(salesSPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainSPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    mainPanelLayout.setVerticalGroup(
        mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(mainPanelLayout.createSequentialGroup()
            .addComponent(ordersSPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 12, Short.MAX_VALUE))
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(stockSPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap()))
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(salesSPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainSPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(6, 6, 6)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    public JTextField getAdressField() {
        return adressField;
    }

    public JTextField getAgeField() {
        return ageField;
    }

    public JTextField getFirstNameField() {
        return firstNameField;
    }

    public JTextField getLastNameField() {
        return lastNameField;
    }

    public JTextField getNoteField() {
        return noteField;
    }

    public JLabel getOrderDateLabel() {
        return orderDateLabel;
    }

    public JLabel getOrderIDLabel() {
        return orderIDLabel;
    }

    public JTable getOrdersLogTable() {
        return ordersLogTable;
    }

    public JTable getPurchasesInfoTable() {
        return purchasesInfoTable;
    }

    public JButton getRemoveOrderButton() {
        return removeOrderButton;
    }

    public JButton getUpdateOrderButton() {
        return updateOrderButton;
    }

    Product getProductByID(int ID) {

        Product productReturned = null;
        for (Product currentProd : ProductsStock) {
            if (currentProd.getID() == ID) {
                productReturned = currentProd;
            }
        }
        return productReturned;
    }

    void setCategoryComboBox(String category) {
        for (int i = 0; i < getCategoriesComboBox().getItemCount(); i++) {
            if (getCategoriesComboBox().getItemAt(i).equals(category)) {
                getCategoriesComboBox().getModel().setSelectedItem(category);
                System.out.println(i);
            }
        }
    }

    void deleteProduct(int productID) {
        for (int i = 0; i < ProductsStock.size(); i++) {
            if (ProductsStock.get(i).getID() == productID) {
                ProductsStock.remove(i);
            }
        }
        fetchProductsOnMainAndRunning();
    }

    void updateProduct(int productID) {

        try {
            String productName = getProductDetailNameField().getText();
            int productQuantity = Integer.valueOf(getProductDetailQtyField().getText());
            double productBuyPrice = Double.parseDouble(getProductDetailBuyPriceField().getText());
            double productSellPrice = Double.parseDouble(getProductDetailSellPriceField().getText());
            String productCategory = getCategoriesComboBox2().getSelectedItem().toString();
            String productExpDate = getProductDetailExpDateField().getText();
            Product newProd = new Product(productID, productName, productCategory, productQuantity, productBuyPrice, productSellPrice, productExpDate);
            objectOutputStream.writeObject(new Data("UPDATE PRODUCT", newProd));
//        for (int i = 0; i < ProductsStock.size(); i++) {
//            if (ProductsStock.get(i).ID == productID) {
//                ProductsStock.set(i, new Product(productID, productName, productCategory, productSupplier, productQuantity, productBuyPrice, productSellPrice, productExpDate));
//            }
//        }
        } catch (IOException ex) {
            Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void deleteProdFromMain(Product prodToDelete) {
        try {
            objectOutputStream.writeObject(new Data("DELETE PRODUCT", prodToDelete));
//        for (int i = 0; i < ProductsStock.size(); i++) {
//            if (ProductsStock.get(i).ID == prodToDelete.ID) {
//                ProductsStock.remove(i);
//            }
//        }
        } catch (IOException ex) {
            Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void deleteFromCart(Product productToDelete) {
        for (int i = 0; i < ProductsOnCart.size(); i++) {
            if (ProductsOnCart.get(i).getID() == productToDelete.getID()) {
                ProductsOnCart.remove(i);
                ProductsOnCartHelper.remove(i);
            }
        }
        double total = 0;
        for (ProductOnCart prod : ProductsOnCartHelper) {
            total = total + prod.getPrice();
        }
        getPriceTotalLabel().setText(String.valueOf(total) + "DA");
    }
    private void mainTabPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainTabPanelMousePressed
        // TODO add your handling code here:
        getMainSPanel().setVisible(true);
        getSalesSPanel().setVisible(false);
        getStockSPanel().setVisible(false);
        getOrdersSPanel().setVisible(false);
    }//GEN-LAST:event_mainTabPanelMousePressed

    private void stockTabPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stockTabPanelMousePressed
        // TODO add your handling code here:
        getMainSPanel().setVisible(false);
        getSalesSPanel().setVisible(false);
        getStockSPanel().setVisible(true);
        getOrdersSPanel().setVisible(false);
    }//GEN-LAST:event_stockTabPanelMousePressed

    private void sellingTabPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sellingTabPanelMousePressed
        // TODO add your handling code here:
        getMainSPanel().setVisible(false);
        getSalesSPanel().setVisible(true);
        getStockSPanel().setVisible(false);
        getOrdersSPanel().setVisible(false);
    }//GEN-LAST:event_sellingTabPanelMousePressed

    private void ordersLogTabPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ordersLogTabPanelMousePressed
        // TODO add your handling code here:
        getMainSPanel().setVisible(false);
        getSalesSPanel().setVisible(false);
        getStockSPanel().setVisible(false);
        getOrdersSPanel().setVisible(true);
    }//GEN-LAST:event_ordersLogTabPanelMousePressed

    Order getOrderByID(int ID) {
        Order selectedOrder = null;
        for (Order order : OrdersList) {
            if (order.getID() == ID) {
                selectedOrder = order;
            }
        }
        return selectedOrder;
    }

    void fetchPurchasesInfoTable(Order order) {
        DefaultTableModel PurchasesTableModel = (DefaultTableModel) getPurchasesInfoTable().getModel();
        int rowCount = PurchasesTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            PurchasesTableModel.removeRow(0);
        }
        for (ProductOnCart prod : order.getPurchases()) {
            PurchasesTableModel.addRow(new Object[]{prod.getProduct().getID(), prod.getProduct().getName(), prod.getProduct().getCategory(), prod.getQuantity()});
        }
    }

    void deleteOrder(int ID) {
        try {
            Order orderToDelete = getOrderByID(ID);
            objectOutputStream.writeObject(new Data("DELETE ORDER", orderToDelete));
        } catch (IOException ex) {
            Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void updateOrder(int ID) {
        for (int i = 0; i < OrdersList.size(); i++) {
            if (OrdersList.get(i).getID() == ID) {
                try {
                    String FirstName = getFirstNameField().getText();
                    String LastName = getLastNameField().getText();
                    int Age = Integer.parseInt(getAgeField().getText());
                    String Adress = getAdressField().getText();
                    String Note = getNoteField().getText();
                    String PhoneNumber = getPhoneNumberField().getText();
                    String Gender = (String) getPatientInfoGenderBox().getSelectedItem();
                    Patient newPatient = new Patient(OrdersList.get(i).getPatient().getID(), FirstName, LastName, PhoneNumber, Adress, Note, Age, Gender);
                    System.out.println("PATIENT ID " + newPatient.getID());
                    objectOutputStream.writeObject(new Data("UPDATE ORDER", newPatient));
                } catch (IOException ex) {
                    Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            // TODO add your handling code here:
            objectOutputStream.writeObject(new Data("EXIT", ClientName));
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void mainProductsTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainProductsTableMousePressed
        // TODO add your handling code here:

        int selectedRow = getMainProductsTable().getSelectedRow() + 1;
        int selectedProductID = Integer.valueOf(getMainProductsTable().getModel().getValueAt(selectedRow - 1, 0).toString());
        selectedIndexes.add(selectedProductID);
        Product selectedProduct = getProductByID(selectedProductID);
        System.out.println("EXPIRED " + selectedProduct.getExpired());
        getProductDetailIDField().setText(Integer.toString(selectedProduct.getID()));
        getProductDetailNameField().setText(selectedProduct.getName());
        getProductDetailQtyField().setText(Integer.valueOf(selectedProduct.getQuantity()).toString());
        getProductDetailBuyPriceField().setText(Double.toString(selectedProduct.getBuyPrice()));
        getProductDetailSellPriceField().setText(Double.toString(selectedProduct.getSellPrice()));
        getProductDetailExpDateField().setText(selectedProduct.getExpDate());
        getCategoriesComboBox2().setSelectedItem(selectedProduct.getCategory());
        getUpdateProductButton().setEnabled(true);
        getDeleteProductButton().setEnabled(true);
    }//GEN-LAST:event_mainProductsTableMousePressed

    private void updateProductButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateProductButtonMousePressed

        // TODO add your handling code here:
        int selectedRow = getMainProductsTable().getSelectedRow() + 1;
        int selectedProductID = Integer.valueOf(getMainProductsTable().getModel().getValueAt(selectedRow - 1, 0).toString());
        Product selectedProd = getProductByID(selectedProductID);

        updateProduct(selectedProductID);
//            fetchProductsOnMainAndRunning();
        selectedIndexes.clear();
        getMainProductsTable().clearSelection();
        getUpdateProductButton().setEnabled(false);
        getDeleteProductButton().setEnabled(false);

    }//GEN-LAST:event_updateProductButtonMousePressed

    private void deleteProductButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteProductButtonMousePressed
        // TODO add your handling code here:
        int selectedRows[] = getMainProductsTable().getSelectedRows();
        for (int i : selectedRows) {
            Product prodToDelete = getProductByID(Integer.parseInt(getMainProductsTable().getValueAt(i, 0).toString()));
            deleteProdFromMain(prodToDelete);
        }
        fetchProductsOnMainAndRunning();
        getProductDetailIDField().setText("");
        getProductDetailNameField().setText("");
        getProductDetailQtyField().setText("");
        getProductDetailBuyPriceField().setText("");
        getProductDetailSellPriceField().setText("");
        getProductDetailExpDateField().setText("");
        getCategoriesComboBox2().setSelectedItem("None");
        selectedIndexes.clear();
        getMainProductsTable().clearSelection();
        getDeleteProductButton().setEnabled(false);
        getUpdateProductButton().setEnabled(false);
    }//GEN-LAST:event_deleteProductButtonMousePressed

    private void productAddNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productAddNameKeyTyped
        // TODO add your handling code here:
        if (getProductAddName().getText().equals("") || getProductAddQuantity().getText().equals("") || getProductAddBuyPrice().getText().equals("") || getProductAddSellPrice().getText().equals("") || getProductAddExpDate().getText().equals(""))
            getAddProductButton().setEnabled(false);
        else
            getAddProductButton().setEnabled(true);
    }//GEN-LAST:event_productAddNameKeyTyped

    private void productAddQuantityKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productAddQuantityKeyTyped
        // TODO add your handling code here:
        if (getProductAddName().getText().equals("") || getProductAddQuantity().getText().equals("") || getProductAddBuyPrice().getText().equals("") || getProductAddSellPrice().getText().equals("") || getProductAddExpDate().getText().equals(""))
            getAddProductButton().setEnabled(false);
        else
            getAddProductButton().setEnabled(true);
    }//GEN-LAST:event_productAddQuantityKeyTyped

    private void productAddBuyPriceKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productAddBuyPriceKeyTyped
        // TODO add your handling code here:
        if (getProductAddName().getText().equals("") || getProductAddQuantity().getText().equals("") || getProductAddBuyPrice().getText().equals("") || getProductAddSellPrice().getText().equals("") || getProductAddExpDate().getText().equals(""))
            getAddProductButton().setEnabled(false);
        else
            getAddProductButton().setEnabled(true);
    }//GEN-LAST:event_productAddBuyPriceKeyTyped

    private void productAddSellPriceKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productAddSellPriceKeyTyped
        // TODO add your handling code here:
        if (getProductAddName().getText().equals("") || getProductAddQuantity().getText().equals("") || getProductAddBuyPrice().getText().equals("") || getProductAddSellPrice().getText().equals("") || getProductAddExpDate().getText().equals(""))
            getAddProductButton().setEnabled(false);
        else
            getAddProductButton().setEnabled(true);
    }//GEN-LAST:event_productAddSellPriceKeyTyped

    private void productAddExpDateKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_productAddExpDateKeyTyped
        // TODO add your handling code here:
        if (getProductAddName().getText().equals("") || getProductAddQuantity().getText().equals("") || getProductAddBuyPrice().getText().equals("") || getProductAddSellPrice().getText().equals("") || getProductAddExpDate().getText().equals(""))
            getAddProductButton().setEnabled(false);
        else
            getAddProductButton().setEnabled(true);
    }//GEN-LAST:event_productAddExpDateKeyTyped

    private void addProductButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addProductButtonMousePressed
        // TODO add your handling code here:
        if (ProductsStock.isEmpty()) {
            ID = 1;
        } else {
            ID = ProductsStock.get(ProductsStock.size() - 1).getID() + 1;
        }
        if (getCategoriesComboBox().getSelectedItem() != null) {
            try {
                String productName = getProductAddName().getText();
                int productQuantity = Integer.valueOf(getProductAddQuantity().getText());
                double productBuyPrice = Double.parseDouble(getProductAddBuyPrice().getText());
                double productSellPrice = Double.parseDouble(getProductAddSellPrice().getText());
                String productCategory = getCategoriesComboBox().getSelectedItem().toString();
                String productExpDate = getProductAddExpDate().getText() + " 00:00:00";
                Product prod = new Product(ID, productName, productCategory, productQuantity, productBuyPrice, productSellPrice, productExpDate);
                objectOutputStream.writeObject(new Data("ADD PRODUCT", prod));
                fetchProductOnRecent(ID, productName, productCategory, productQuantity, productSellPrice);
                fetchProductsOnMainAndRunning();
                getErrorLabel1().setForeground(Color.green);
                getErrorLabel1().setText("Product added");
                getAddProductButton().setEnabled(false);
                getProductAddName().setText("");
                getProductAddQuantity().setText("");
                getProductAddBuyPrice().setText("");
                getProductAddSellPrice().setText("");
                getProductAddExpDate().setText("");
            } catch (IOException ex) {
                Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            getErrorLabel1().setForeground(Color.red);
            getErrorLabel1().setText("Please choose a category or a supplier");
        }
    }//GEN-LAST:event_addProductButtonMousePressed

    private void addProductButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProductButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addProductButtonActionPerformed

    private void categoriesNameFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_categoriesNameFieldKeyTyped
        // TODO add your handling code here:
        if (getCategoriesNameField().equals(""))
            getAddCategoryButton().setEnabled(false);
        else
            getAddCategoryButton().setEnabled(true);
    }//GEN-LAST:event_categoriesNameFieldKeyTyped

    private void addCategoryButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addCategoryButtonMousePressed
        // TODO add your handling code here:
        try {

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        String categoryToAdd = getCategoriesNameField().getText();
        Categories.add(categoryToAdd);
        addCategoryToComboBox();
        getAddCategoryButton().setEnabled(false);
        getCategoriesNameField().setText("");
    }//GEN-LAST:event_addCategoryButtonMousePressed

    private void ProductsOnCartTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ProductsOnCartTableMousePressed
        // TODO add your handling code here:
        getRemoveFromCartButton().setEnabled(true);
    }//GEN-LAST:event_ProductsOnCartTableMousePressed

    private void removeFromCartButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeFromCartButtonMousePressed
        // TODO add your handling code here:
        int selectedRows[] = getProductsOnCartTable().getSelectedRows();
        for (int i : selectedRows) {
            Product prodToDelete = getProductByID(Integer.parseInt(getProductsOnCartTable().getValueAt(i, 0).toString()));
            deleteFromCart(prodToDelete);
        }
        fetchProductsOnCart();
        getRemoveFromCartButton().setEnabled(false);
        if (!ProductsOnCart.isEmpty())
            getSaveAndPrintButton().setEnabled(true);
        else
            getSaveAndPrintButton().setEnabled(false);
    }//GEN-LAST:event_removeFromCartButtonMousePressed

    private void runningStockTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_runningStockTableMousePressed
        // TODO add your handling code here:
        int selectedRow = getRunningStockTable().getSelectedRow();
        int selectedProductID = Integer.valueOf(getRunningStockTable().getValueAt(selectedRow, 0).toString());
        selectedProductForCart = getProductByID(selectedProductID);
        getProductDetailNameLabel().setText(selectedProductForCart.getName());
        getProductDetailCategoryLabel().setText(selectedProductForCart.getCategory());
        getProductDetailPriceLabel().setText(Double.toString(selectedProductForCart.getSellPrice()));
        getAddToCartButton().setEnabled(true);
    }//GEN-LAST:event_runningStockTableMousePressed

    private void productDetailQtyLabelFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productDetailQtyLabelFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_productDetailQtyLabelFieldActionPerformed

    private void addToCartButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addToCartButtonMousePressed
        // TODO add your handling code here:
        int qtyToOrder = Integer.parseInt(getProductDetailQtyLabelField().getText());
        if (qtyToOrder <= selectedProductForCart.getQuantity() && !selectedProductForCart.getExpired()) {
            ProductsOnCart.add(selectedProductForCart);
            ProductOnCart prodOnCart = new ProductOnCart(selectedProductForCart, Integer.parseInt(getProductDetailQtyLabelField().getText()));
            ProductsOnCartHelper.add(prodOnCart);
            fetchProductsOnCart();
            getAddToCartButton().setEnabled(false);
            double total = 0;
            for (ProductOnCart prod : ProductsOnCartHelper) {
                total = total + prod.getPrice();
            }
            getPriceTotalLabel().setText(String.valueOf(total) + "DA");
            if (!ProductsOnCart.isEmpty()) {
                getSaveAndPrintButton().setEnabled(true);
            } else {
                getSaveAndPrintButton().setEnabled(false);
            }
            getProductDetailQtyLabelField().setText("1");
            getSalesPanelErrorLabel().setText("");
        } else if (qtyToOrder > selectedProductForCart.getQuantity()) {
            if (selectedProductForCart.getQuantity() == 0) {
                getSalesPanelErrorLabel().setForeground(Color.red);
                getSalesPanelErrorLabel().setText("Out of stock !");
            } else {
                getSalesPanelErrorLabel().setForeground(Color.red);
                getSalesPanelErrorLabel().setText("There are only " + selectedProductForCart.getQuantity() + " in stock !");
            }
        } else if (selectedProductForCart.getExpired()) {
            getSalesPanelErrorLabel().setForeground(Color.red);
            getSalesPanelErrorLabel().setText("That's an Expired product !");
        }
    }//GEN-LAST:event_addToCartButtonMousePressed

    private void saveAndPrintButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveAndPrintButtonMousePressed
        try {
            orderID = OrdersList.size() + 1;
            patientID = PatientsList.size() + 1;
            System.out.println("HERE ID IS " + patientID);
            String FirstName = getFirstNameCartField().getText();
            String LastName = getLastNameCartField().getText();
            int Age = Integer.parseInt(getAgeCartField().getText());
            String Adress = getAdressCartField().getText();
            String Note = getNoteCartField().getText();
            String PhoneNumber = getPatientAddPhoneField().getText();
            String Gender = (String) getAddPatientGenderBox().getSelectedItem();
            Patient newPatient = new Patient(patientID, FirstName, LastName, PhoneNumber, Adress, Note, Age, Gender);
            ArrayList<ProductOnCart> newPurchase = ProductsOnCartHelper;
            Order orderToAdd = new Order(orderID, new Patient(newPatient.getID(), newPatient.getFirstName(), newPatient.getLastName(), newPatient.getPhoneNumber(), newPatient.getAdress(), newPatient.getNote(), newPatient.getAge(), newPatient.getGender()), new ArrayList<>(newPurchase), LocalDateTime.now());
            objectOutputStream.writeObject(new Data("ADD ORDER", orderToAdd));
            System.out.println("PATIENT ID " + orderToAdd.getPatient().getID());
            OrdersList.add(orderToAdd);
            PatientsList.add(newPatient);
            for (ProductOnCart prod : ProductsOnCartHelper) {
                Product prod1 = new Product(prod.getProduct().getID(), prod.getProduct().getName(), prod.getProduct().getCategory(), prod.getProduct().getQuantity(), prod.getProduct().getBuyPrice(), prod.getProduct().getSellPrice(), prod.getProduct().getExpDate());
                prod1.updateQty(prod.getQuantity());
                objectOutputStream.writeObject(new Data("UPDATE PRODUCT", prod1));
            }
            ProductsOnCartHelper.clear();
            fetchProductsOnCart();
            fetchOrdersOnLog();
        } catch (IOException ex) {
            Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_saveAndPrintButtonMousePressed

    private void ordersLogTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ordersLogTableMousePressed
        // TODO add your handling code here:
        int selectedRow = getOrdersLogTable().getSelectedRow();
        int selectedOrderID = Integer.parseInt(getOrdersLogTable().getValueAt(selectedRow, 0).toString());
        Order selectedOrder = getOrderByID(selectedOrderID);
        getOrderIDLabel().setText(String.valueOf(selectedOrder.getID()));
        getOrderDateLabel().setText(selectedOrder.getDate().format(formatterDateAndHour));
        getFirstNameField().setText(selectedOrder.getPatient().getFirstName());
        getLastNameField().setText(selectedOrder.getPatient().getLastName());
        getAgeField().setText(String.valueOf(selectedOrder.getPatient().getAge()));
        getPhoneNumberField().setText(selectedOrder.getPatient().getPhoneNumber());
        getAdressField().setText(selectedOrder.getPatient().getAdress());
        getNoteField().setText(selectedOrder.getPatient().getNote());
        getPatientInfoGenderBox().setSelectedItem(selectedOrder.getPatient().getGender());
        fetchPurchasesInfoTable(selectedOrder);
    }//GEN-LAST:event_ordersLogTableMousePressed

    private void removeOrderButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeOrderButtonMousePressed
        // TODO add your handling code here:
        int selectedRows[] = getOrdersLogTable().getSelectedRows();
        for (int i : selectedRows) {
            deleteOrder(Integer.parseInt(getOrdersLogTable().getValueAt(i, 0).toString()));
        }
//        fetchOrdersOnLog();
    }//GEN-LAST:event_removeOrderButtonMousePressed

    private void updateOrderButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateOrderButtonMousePressed
        // TODO add your handling code here:
        int selectedRow = getOrdersLogTable().getSelectedRow();
        updateOrder(Integer.parseInt(getOrdersLogTable().getValueAt(getOrdersLogTable().getSelectedRow(), 0).toString()));
        fetchOrdersOnLog();
    }//GEN-LAST:event_updateOrderButtonMousePressed

    void fetchSearch(ArrayList<Product> searchList) {
        DefaultTableModel RunningTableModel = (DefaultTableModel) getRunningStockTable().getModel();
        int rowCount = RunningTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            RunningTableModel.removeRow(0);
        }
        for (Product product : searchList) {
            RunningTableModel.addRow(new Object[]{product.getID(), product.getName(), product.getCategory(), product.getQuantity(), product.getSellPrice()});
        }
    }

    private void searchingSalesSectionFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchingSalesSectionFieldKeyTyped
        // TODO add your handling code here:
        ArrayList<Product> searchingList = new ArrayList();
        String searchingName = getSearchingSalesSectionField().getText().toLowerCase();
        for (Product prod : ProductsStock) {
            String prodName = prod.getName().toLowerCase();
            if (prodName.contains(searchingName) || prodName.startsWith(searchingName) || prodName.endsWith(searchingName)) {
                searchingList.add(prod);
            }
        }
        fetchSearch(searchingList);
    }//GEN-LAST:event_searchingSalesSectionFieldKeyTyped

    private void categoryComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryComboBox3ActionPerformed
        // TODO add your handling code here:
        ArrayList<Product> searchingList = new ArrayList();
        for (Product prod : ProductsStock) {
            String categorySearched = (String) getCategoryComboBox3().getSelectedItem();
            if (prod.getCategory().equals(categorySearched)) {
                searchingList.add(prod);
            }
        }
        fetchSearch(searchingList);
    }//GEN-LAST:event_categoryComboBox3ActionPerformed

    private void filterComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterComboBoxActionPerformed
        // TODO add your handling code here:
        ArrayList<Product> filteringList = new ArrayList();
        String selectedItem = (String) getFilterComboBox().getSelectedItem();
        if (selectedItem.equals("No filtering")) {
            filteringList = ProductsStock;
            fetchSearch(filteringList);
        } else if (selectedItem.equals("Expired")) {
            for (Product prod : ProductsStock) {
                if (prod.getExpired()) {
                    filteringList.add(prod);
                }
            }
            fetchSearch(filteringList);
        } else if (selectedItem.equals("Non-Available")) {
            for (Product prod : ProductsStock) {
                if (prod.getQuantity() == 0) {
                    filteringList.add(prod);
                }
            }
            fetchSearch(filteringList);
        } else if (selectedItem.equals("Available and non-Expired")) {
            for (Product prod : ProductsStock) {
                if (!prod.getExpired() && prod.getQuantity() > 0) {
                    filteringList.add(prod);
                }
            }
            fetchSearch(filteringList);
        }
    }//GEN-LAST:event_filterComboBoxActionPerformed

    public JTextField getAdressCartField() {
        return adressCartField;
    }

    public JTextField getAgeCartField() {
        return ageCartField;
    }

    public JTextField getFirstNameCartField() {
        return firstNameCartField;
    }

    public JTextField getLastNameCartField() {
        return lastNameCartField;
    }

    public JTextField getNoteCartField() {
        return noteCartField;
    }

    public JPanel getOrdersSPanel() {
        return ordersSPanel;
    }

    public JComboBox<String> getPatientInfoGenderBox() {
        return patientInfoGenderBox;
    }

    public JLabel getErrorLabel1() {
        return errorLabel1;
    }

    public JTable getAddedRecentlyTable() {
        return addedRecentlyTable;
    }

    public JPanel getMainTabPanel() {
        return mainTabPanel;
    }

    public JPanel getOrdersLogTabPanel() {
        return ordersLogTabPanel;
    }

    public JPanel getSellingTabPanel() {
        return sellingTabPanel;
    }

    public JPanel getStockTabPanel() {
        return stockTabPanel;
    }

    public JButton getDeleteProductButton() {
        return deleteProductButton;
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
            java.util.logging.Logger.getLogger(chifaUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(chifaUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(chifaUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(chifaUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    authMain = new AuthFrame();
                    authMain.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(chifaUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        try {
            socket = new Socket("localhost", 4999);
            System.out.println("Connected");
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public class MonCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            if (column == 0) {
                int prodID = (int) value;
                Product prod = getProductByID(prodID);
                if (prod.getExpired()) {
                    setBackground(Color.red.brighter());
                } else {
                    setBackground(Color.white);
                }
            }

            return this;
        }
    }

    public JTextField getProductAddBuyPrice() {
        return productAddBuyPrice;
    }

    public JTextField getProductAddExpDate() {
        return productAddExpDate;
    }

    public JTextField getProductAddName() {
        return productAddName;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public JTextField getProductAddQuantity() {
        return productAddQuantity;
    }

    public JTextField getProductDetailIDField() {
        return productDetailIDField;
    }

    public JTextField getProductDetailBuyPriceField() {
        return productDetailBuyPriceField;
    }

    public JTextField getProductDetailExpDateField() {
        return productDetailExpDateField;
    }

    public JComboBox<String> getCategoriesComboBox2() {
        return categoriesComboBox2;
    }

    public JTextField getProductDetailNameField() {
        return productDetailNameField;
    }

    public JTextField getProductDetailQtyField() {
        return productDetailQtyField;
    }

    public JTextField getProductDetailSellPriceField() {
        return productDetailSellPriceField;
    }

    public JTextField getProductAddSellPrice() {
        return productAddSellPrice;
    }

    public JPanel getMainSPanel() {
        return mainSPanel;
    }

    public JButton getAddToCartButton() {
        return addToCartButton;
    }

    public JButton getRemoveFromCartButton() {
        return removeFromCartButton;
    }

    public JButton getSaveAndPrintButton() {
        return saveAndPrintButton;
    }

    public void setMainSPanel(JPanel mainSPanel) {
        this.mainSPanel = mainSPanel;
    }

    public JPanel getSalesSPanel() {
        return salesSPanel;
    }

    public void setSalesSPanel(JPanel salesSPanel) {
        this.salesSPanel = salesSPanel;
    }

    public JPanel getStockSPanel() {
        return stockSPanel;
    }

    public JLabel getSalesPanelErrorLabel() {
        return salesPanelErrorLabel;
    }

    public void setStockSPanel(JPanel stockSPanel) {
        this.stockSPanel = stockSPanel;
    }

    public JComboBox<String> getCategoriesComboBox() {
        return categoriesComboBox1;
    }

    public void setCategoriesComboBox(JComboBox<String> categoriesComboBox) {
        this.categoriesComboBox1 = categoriesComboBox;
    }

    public JComboBox<String> getAddPatientGenderBox() {
        return addPatientGenderBox;
    }

    public JTextField getPhoneNumberField() {
        return phoneNumberField;
    }

    public JTextField getSearchingSalesSectionField() {
        return searchingSalesSectionField;
    }

    public JLabel getPriceTotalLabel() {
        return priceTotalLabel;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable ProductsOnCartTable;
    private javax.swing.JButton addCategoryButton;
    private javax.swing.JComboBox<String> addPatientGenderBox;
    private javax.swing.JButton addProductButton;
    private javax.swing.JButton addToCartButton;
    private javax.swing.JTable addedRecentlyTable;
    private javax.swing.JTextField adressCartField;
    private javax.swing.JTextField adressField;
    private javax.swing.JTextField ageCartField;
    private javax.swing.JTextField ageField;
    private javax.swing.JComboBox<String> categoriesComboBox1;
    private javax.swing.JComboBox<String> categoriesComboBox2;
    private javax.swing.JTextField categoriesNameField;
    private javax.swing.JComboBox<String> categoryComboBox3;
    private javax.swing.JButton deleteProductButton;
    private javax.swing.JLabel errorLabel1;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JTextField firstNameCartField;
    private javax.swing.JTextField firstNameField;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField lastNameCartField;
    private javax.swing.JTextField lastNameField;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JLabel mainLogoLabel;
    private javax.swing.JLayeredPane mainPanel;
    private javax.swing.JTable mainProductsTable;
    private javax.swing.JPanel mainSPanel;
    private javax.swing.JPanel mainTabPanel;
    private javax.swing.JTextField noteCartField;
    private javax.swing.JTextField noteField;
    private javax.swing.JLabel orderDateLabel;
    private javax.swing.JLabel orderIDLabel;
    private javax.swing.JPanel ordersLogTabPanel;
    private javax.swing.JTable ordersLogTable;
    private javax.swing.JLabel ordersPanelIcon;
    private javax.swing.JPanel ordersSPanel;
    private javax.swing.JTextField patientAddPhoneField;
    private javax.swing.JComboBox<String> patientInfoGenderBox;
    private javax.swing.JTextField phoneNumberField;
    private javax.swing.JLabel priceTotalLabel;
    private javax.swing.JTextField productAddBuyPrice;
    private javax.swing.JTextField productAddExpDate;
    private javax.swing.JTextField productAddName;
    private javax.swing.JTextField productAddQuantity;
    private javax.swing.JTextField productAddSellPrice;
    private javax.swing.JTextField productDetailBuyPriceField;
    private javax.swing.JLabel productDetailCategoryLabel;
    private javax.swing.JTextField productDetailExpDateField;
    private javax.swing.JTextField productDetailIDField;
    private javax.swing.JTextField productDetailNameField;
    private javax.swing.JLabel productDetailNameLabel;
    private javax.swing.JLabel productDetailPriceLabel;
    private javax.swing.JTextField productDetailQtyField;
    private javax.swing.JTextField productDetailQtyLabelField;
    private javax.swing.JTextField productDetailSellPriceField;
    private javax.swing.JTable purchasesInfoTable;
    private javax.swing.JButton removeFromCartButton;
    private javax.swing.JButton removeOrderButton;
    private javax.swing.JTable runningStockTable;
    private javax.swing.JLabel salesPanelErrorLabel;
    private javax.swing.JLabel salesPanelIcon;
    private javax.swing.JPanel salesSPanel;
    private javax.swing.JButton saveAndPrintButton;
    private javax.swing.JTextField searchingSalesSectionField;
    private javax.swing.JPanel sellingTabPanel;
    private javax.swing.JLabel stockPanelIcon;
    private javax.swing.JPanel stockSPanel;
    private javax.swing.JPanel stockTabPanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JButton updateOrderButton;
    private javax.swing.JButton updateProductButton;
    // End of variables declaration//GEN-END:variables
}
