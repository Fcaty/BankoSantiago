/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package general;
import java.sql.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import database.DBConn;
import java.text.SimpleDateFormat;

/**
 *
 * @author Fcaty
 */
public class JournalizingForm extends javax.swing.JFrame {
    //Logger
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JournalizingForm.class.getName());
    
    //Temporarily stores entries before finalizing and sending them to the database
    private final java.util.List<Entry> entryList = new java.util.ArrayList<>();
    
    //Loads options for titleSelection combo box
    private void loadTitleOptions(){
        Connection con = DBConn.attemptConnection();
        String status = null;
        
        if(con == null){
            status = "FAILED";
            lblStatusConn.setText(status);
            lblStatusConn.setForeground(Color.red);
            return;
        } else {
            status = "ONLINE";
        } 
        
        try {
            lblStatusConn.setText(status);
            lblStatusConn.setForeground(Color.decode("#00CC00"));
            
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT AName from accountingsystem.account_title");
            titleSelection.removeAllItems();
            titleSelection.addItem("Select");
            while(rs.next()){
                titleSelection.addItem(rs.getString("AName"));
            } 
            con.close();
            
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Connection Failed! " + e.getMessage());
        }
    }
    
    //Will insert an unfinalized record, locally.
    private void insertRecord(){
        String accountTitle;
        char recordType = '\0';
        double amount = 0;
        
        if(((String) titleSelection.getSelectedItem()).equals("Select") || typeSelection.getSelectedIndex() == 0 || amountTxt.getText().isEmpty()){
            JOptionPane.showMessageDialog(this, "Invalid input for entries! Try again!");
            return;
        }
        
        accountTitle = (String) titleSelection.getSelectedItem();
        amount = Double.parseDouble(amountTxt.getText());
        if(typeSelection.getSelectedIndex() == 1){
            recordType = 'D';
        } else if (typeSelection.getSelectedIndex() == 2){
            recordType = 'C';
        } 
       
           
        Entry entry = new Entry(accountTitle, recordType, amount);
        entryList.add(entry);
        
        titleSelection.setSelectedIndex(0);
        typeSelection.setSelectedIndex(0);
        amountTxt.setText("");
        
        updateHistory();
        
        if(!trackBalance()){
            lblStatusConn.setText("INVALID");
            lblStatusConn.setForeground(Color.red);
        } else {
            lblStatusConn.setText("ONLINE");
            lblStatusConn.setForeground(Color.decode("#00CC00"));
        }
    }
    
    private void updateHistory(){
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);
        
            for(Entry e : entryList){
            Object[] row = {
                e.getAName(),
                e.getAmount(),
                e.getRecType(),
            };
            model.addRow(row);
        }
    }
    
    
    private boolean trackBalance(){
        double debitAmount = 0;
        double creditAmount = 0;
        
        for(Entry e : entryList){
            if(e.getRecType() == 'D') {
                debitAmount += e.getAmount();
                
            } else if (e.getRecType() == 'C'){
                creditAmount += e.getAmount();
            }
        }
        
        lblDebit.setText(String.valueOf(debitAmount));
        lblCredit.setText(String.valueOf(creditAmount));
        
        if (debitAmount == creditAmount){
            return true;
        } else {
            return false;
        }
    }
    
    private boolean verifyDate(int year, int month, int day){
     year = 2005;
     month = 2005;
        return true;
    }
    
    private void addEntry(){
        String sql;
        String date;
        int journalID = 0;
        
        //Balance Verification, will be false if inbalanced
        if(!trackBalance()){
            JOptionPane.showMessageDialog(this, "Your debits and credits aren't balanced!");
            return;
        }
        
        //Field verificaiton, will be false if any field is empty
        if(notesTxt.getText().isEmpty() || yearTxt.getText().isEmpty() || monthTxt.getText().isEmpty() || dayTxt.getText().isEmpty()){
            JOptionPane.showMessageDialog(this, "A field is empty!");
            return;
        }
        
        //Date format verification
        if(!verifyDate(Integer.parseInt(yearTxt.getText()), Integer.parseInt(monthTxt.getText()), Integer.parseInt(dayTxt.getText()))){
            JOptionPane.showMessageDialog(this, "Invalid Date Input!");
            return;
        }
        
        date = yearTxt.getText() + "-" + monthTxt.getText() + "-" + dayTxt.getText();
        
        sql = "INSERT INTO accountingsystem.journal (Entry_Date, Notes) VALUES (?, ?)";
        
        try(
                Connection con = DBConn.attemptConnection();
                PreparedStatement pstmt = con.prepareStatement(sql); 
           ){
            
            Object[] params = {
                date,
                notesTxt.getText()
            };
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Successfully recorded journal entry!");
            notesTxt.setText("");
            yearTxt.setText("");
            monthTxt.setText("");
            dayTxt.setText("");
            
            con.close();
            
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "Connection Failed! " + e.getMessage());
        }
                
        try{
            Connection con = DBConn.attemptConnection();
            
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Max(JID) FROM accountingsystem.journal");
            
            journalID = rs.getInt("JID");
            con.close();
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "Connection Failed! " + e.getMessage());
        }
    }
    
    /**
     * Creates new form JournalizingForm
     */
    public JournalizingForm() {
        
        initComponents();
        loadTitleOptions();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        btnAddAccountTitle = new javax.swing.JButton();
        accountTitleLabel = new javax.swing.JLabel();
        titleSelection = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        typeSelection = new javax.swing.JComboBox<>();
        accountTitleLabel1 = new javax.swing.JLabel();
        amountTxt = new javax.swing.JTextField();
        btnEnterRecord = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        notesTxt = new javax.swing.JTextArea();
        btnSubmitEntry = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        dayTxt = new javax.swing.JTextField();
        monthTxt = new javax.swing.JTextField();
        yearTxt = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        lblStatusConn = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        debit = new javax.swing.JLabel();
        credit = new javax.swing.JLabel();
        lblDebit = new javax.swing.JLabel();
        lblCredit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Banko Santiago Accounting System");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Insert Records", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        btnAddAccountTitle.setFont(new java.awt.Font("JetBrains Mono", 0, 14)); // NOI18N
        btnAddAccountTitle.setText("+");
        btnAddAccountTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddAccountTitle.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btnAddAccountTitle.setMaximumSize(new java.awt.Dimension(35, 35));
        btnAddAccountTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAccountTitleActionPerformed(evt);
            }
        });

        accountTitleLabel.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        accountTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        accountTitleLabel.setText("Account Title");

        titleSelection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        titleSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                titleSelectionActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Record Type");

        typeSelection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "Debit", "Credit" }));
        typeSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeSelectionActionPerformed(evt);
            }
        });

        accountTitleLabel1.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        accountTitleLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        accountTitleLabel1.setText("Amount");

        amountTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amountTxtActionPerformed(evt);
            }
        });

        btnEnterRecord.setFont(new java.awt.Font("HYWenHei-85W", 0, 14)); // NOI18N
        btnEnterRecord.setText("Enter Record");
        btnEnterRecord.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEnterRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnterRecordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(accountTitleLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(accountTitleLabel)
                                .addGap(131, 131, 131)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(55, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAddAccountTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(titleSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(typeSelection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(amountTxt, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEnterRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accountTitleLabel)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddAccountTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(titleSelection)
                    .addComponent(typeSelection))
                .addGap(18, 18, 18)
                .addComponent(accountTitleLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(amountTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEnterRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "History", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Account Title", "Amount", "Record Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.String.class
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
        jScrollPane1.setViewportView(historyTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Entry Submission", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        jLabel3.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel3.setText("Enter Notes");

        notesTxt.setColumns(20);
        notesTxt.setLineWrap(true);
        notesTxt.setRows(3);
        jScrollPane2.setViewportView(notesTxt);

        btnSubmitEntry.setFont(new java.awt.Font("HYWenHei-85W", 0, 14)); // NOI18N
        btnSubmitEntry.setText("Submit Entry");
        btnSubmitEntry.setMaximumSize(new java.awt.Dimension(128, 29));
        btnSubmitEntry.setMinimumSize(new java.awt.Dimension(128, 29));
        btnSubmitEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitEntryActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Month (mm)");

        jLabel4.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel4.setText("Day (dd)");

        jLabel5.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel5.setText("Year (yyyy)");

        dayTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dayTxtActionPerformed(evt);
            }
        });

        monthTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthTxtActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSubmitEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(monthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(31, 31, 31)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(dayTxt))
                                .addGap(54, 54, 54)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(yearTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yearTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dayTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(monthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(btnSubmitEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        lblStatusConn.setFont(new java.awt.Font("HYWenHei-85W", 0, 24)); // NOI18N
        lblStatusConn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblStatusConn.setText("Standby");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblStatusConn, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblStatusConn)
                .addGap(53, 53, 53))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Balance", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        debit.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        debit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        debit.setText("Debit");

        credit.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        credit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        credit.setText("Credit");

        lblDebit.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        lblDebit.setText("0.00");

        lblCredit.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        lblCredit.setText("0.00");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCredit, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(credit)
                    .addComponent(debit)
                    .addComponent(lblDebit, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(debit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDebit)
                .addGap(13, 13, 13)
                .addComponent(credit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCredit)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddAccountTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAccountTitleActionPerformed
        // TODO add your handling code here:apac
        AddAccountTitle addTitle = new AddAccountTitle();
        addTitle.setVisible(true);
    }//GEN-LAST:event_btnAddAccountTitleActionPerformed

    private void titleSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_titleSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_titleSelectionActionPerformed

    private void amountTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_amountTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_amountTxtActionPerformed

    private void btnEnterRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnterRecordActionPerformed
        insertRecord();
    }//GEN-LAST:event_btnEnterRecordActionPerformed

    private void typeSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_typeSelectionActionPerformed

    private void btnSubmitEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitEntryActionPerformed
    
    }//GEN-LAST:event_btnSubmitEntryActionPerformed

    private void dayTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dayTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dayTxtActionPerformed

    private void monthTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_monthTxtActionPerformed

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new JournalizingForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel accountTitleLabel;
    private javax.swing.JLabel accountTitleLabel1;
    private javax.swing.JTextField amountTxt;
    private javax.swing.JButton btnAddAccountTitle;
    private javax.swing.JButton btnEnterRecord;
    private javax.swing.JButton btnSubmitEntry;
    private javax.swing.JLabel credit;
    private javax.swing.JTextField dayTxt;
    private javax.swing.JLabel debit;
    private javax.swing.JTable historyTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblCredit;
    private javax.swing.JLabel lblDebit;
    private javax.swing.JLabel lblStatusConn;
    private javax.swing.JTextField monthTxt;
    private javax.swing.JTextArea notesTxt;
    private javax.swing.JComboBox<String> titleSelection;
    private javax.swing.JComboBox<String> typeSelection;
    private javax.swing.JTextField yearTxt;
    // End of variables declaration//GEN-END:variables
}
