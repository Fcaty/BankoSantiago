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
import java.util.Scanner;


/**
 *
 * @author Fcaty
 */
public class LedgerForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LedgerForm.class.getName());
    
    private void generateLedgerSheet(){
        //ArrayList<Double> = new ArrayList;
        double totalDebit = 0;
        double totalCredit = 0;
        File count = new File("Output"+File.separator+"Ledgers"+File.separator+"count.txt");
        String filepath = "";
        
        try(Scanner myScan = new Scanner(count)){
            int newCount = myScan.nextInt() + 1; //Name for new file
            myScan.close();
            FileWriter fw = new FileWriter(count);
            fw.write(Integer.toString(newCount));
            fw.close();
            filepath = "Output"+File.separator+"Ledgers"+File.separator+ "JournalNo"+(newCount) +".txt"; //Filepath string
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "IO Error! What did you do? This shouldn't be here!");
        }
        
        try(
                PrintWriter pw = new PrintWriter(filepath);
                Connection con = DBConn.attemptConnection();
                PreparedStatement pstmtScraper = con.prepareStatement("SELECT Amount, Record_Type "
                        + "FROM accountingsystem.journal_entries "
                        + "WHERE AID = ?")
           ){
            
            pw.printf("%-15s %21s %15s\n"," ", "LedgerName", " ");
            pw.println("==================================================");
            pw.printf("%-25s %1s %25s\n", " ", "|", " ");
            
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found!");
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "Connection error! "+ e.getMessage());
        }
    }
    
    private void generateLedgers(){
        int AID = 0;
        String normalSide;
        try(
            Connection con = DBConn.attemptConnection();
            Statement stmtAccounts = con.createStatement();
            PreparedStatement pstmtJournals = con.prepareStatement("SELECT Amount, Record_Type FROM accountingsystem.journal_entries WHERE AID = ?");
            PreparedStatement pstmtLedgers = con.prepareStatement("INSERT INTO accountingsystem.ledger (AID, Total_Credit, Total_Debit, Final_Value) values (?, ?, ?, ?)");
                ){
            
            //Outer query: collect accountTitles
            stmtAccounts.executeUpdate("DELETE FROM accountingsystem.ledger");
            ResultSet rsAccounts = stmtAccounts.executeQuery("SELECT AID, Normal_Side FROM accountingsystem.account_title");
            
            while(rsAccounts.next()){
                AID = rsAccounts.getInt("AID");
                normalSide = rsAccounts.getString("Normal_Side");
                double totalCredit = 0;
                double totalDebit = 0;
                double finalValue = 0;
                
                //Inner query: collect Journal Entries, Calculate totalCredit and totalDebit
                pstmtJournals.setInt(1, AID);
                ResultSet rsJournals = pstmtJournals.executeQuery();
                while(rsJournals.next()){
                    double amount = rsJournals.getDouble("Amount");
                    String recType = rsJournals.getString("Record_Type");
                    
                    //Identifies debits from credits and calculates total.
                    if("D".equals(recType)){
                       totalDebit += amount;
                    } else if ("C".equals(recType)){
                        totalCredit += amount;
                    }
                }
                
                
                //Will skip any account titles without any entries
                if(totalDebit == 0 && totalCredit == 0) continue;
                
                //Identifies normal side and calculates final value.
                if("D".equals(normalSide)){
                    finalValue = totalDebit - totalCredit;
                } else if("C".equals(normalSide)){
                    finalValue = totalCredit - totalDebit;
                }
                
                pstmtLedgers.setInt(1, AID);
                pstmtLedgers.setDouble(2, totalCredit);
                pstmtLedgers.setDouble(3, totalDebit);
                pstmtLedgers.setDouble(4, finalValue);
                pstmtLedgers.executeUpdate();
            }
            rsAccounts.close();
            
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "Connection Failed! "+ e.getMessage());
        }
        
    }
    
    private void loadLedger(){
        
        
        String selectedLedger;
        DefaultTableModel dTb = (DefaultTableModel) debitsTable.getModel();
        DefaultTableModel cTb = (DefaultTableModel) creditsTable.getModel();
        dTb.setRowCount(0);
        cTb.setRowCount(0);
        
        if(ledgerSelection.getSelectedIndex() == 0){
            txtDebit.setText("0.0");
            txtCredit.setText("0.0");
            txtAccountTitle.setText("Account Title");
            txtFinalVal.setText("0.0");
            txtNormalSide.setText("UNKNOWN");
            
            JOptionPane.showMessageDialog(this, "No account title selected.");
            
            return;
        }
        
        int AID = 0;
        String normalSide = "";
        try(
                Connection con = DBConn.attemptConnection();
                PreparedStatement accountNameIdentify = con.prepareStatement("SELECT AID, Normal_Side FROM accountingsystem.account_title where AName = ?");
                PreparedStatement amountLoader = con.prepareStatement("SELECT Amount from accountingsystem.journal_entries where AID = ? AND Record_Type = ?");
                PreparedStatement ledgerInfo = con.prepareStatement("SELECT Total_Credit, Total_Debit, Final_Value FROM ledger where AID = ?");
                )
        {
            selectedLedger = (String) ledgerSelection.getSelectedItem();
            txtAccountTitle.setText(selectedLedger);
            accountNameIdentify.setString(1, selectedLedger);
            ResultSet rs = accountNameIdentify.executeQuery();
            
            while(rs.next()){
                AID = rs.getInt("AID");
                normalSide = rs.getString("Normal_Side");
            }
            //Loads in debit entries from a given account title
            amountLoader.setInt(1, AID);
            amountLoader.setString(2, "D");
            rs = amountLoader.executeQuery();
            while(rs.next()){
                Object[] row = { rs.getDouble("Amount") };
                dTb.addRow(row);
            }
            
            //Loads in credit entries from a given account title
            amountLoader.setString(2, "C");
            rs = amountLoader.executeQuery();
            while(rs.next()){
                Object[] row = { rs.getDouble("Amount")};
                cTb.addRow(row);
            }
            
            //Acquires total_credits, total_debits, & final_value from ledger table
            ledgerInfo.setInt(1, AID);
            rs = ledgerInfo.executeQuery();
            while(rs.next()){
                txtDebit.setText(Double.toString(rs.getDouble("Total_Debit")));
                txtCredit.setText(Double.toString(rs.getDouble("Total_Credit")));
                txtFinalVal.setText(Double.toString(rs.getDouble("Final_Value")));
                if("D".equals(normalSide)){
                    txtNormalSide.setText("DEBIT");
                } else if ("C".equals(normalSide)){
                    txtNormalSide.setText("CREDIT");
                } else {
                    txtNormalSide.setText("UNKNOWN");
                }
            }
            con.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Connection failed! "+ e.getMessage());
        }
        
    }
    
    private void loadLedgerOptions(){
        
        try(
                Connection con = DBConn.attemptConnection();
                Statement stmtLedgers = con.createStatement();
                ) 
        {
            ResultSet ledgerNames = stmtLedgers.executeQuery("SELECT AName FROM ledger INNER JOIN account_title ON ledger.AID = account_title.AID");
            ledgerSelection.removeAllItems();
            ledgerSelection.addItem("Select");
            while(ledgerNames.next()){
                ledgerSelection.addItem(ledgerNames.getString("AName"));
            }
        con.close();
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "Connection failed! "+ e.getMessage());
        }
            
            }

    /**
     * Creates new form LedgerForm
     */
    public LedgerForm() {
        initComponents();
        loadLedgerOptions();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        ledgerSelection = new javax.swing.JComboBox<>();
        btnLoadLedger = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        txtDebit = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        debitsTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        creditsTable = new javax.swing.JTable();
        txtAccountTitle = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txtFinalVal = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        txtNormalSide = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        txtCredit = new javax.swing.JLabel();
        btnReturn = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnPost = new javax.swing.JButton();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jDesktopPane2 = new javax.swing.JDesktopPane();

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        ledgerSelection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ledgerSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ledgerSelectionActionPerformed(evt);
            }
        });

        btnLoadLedger.setFont(new java.awt.Font("HYWenHei-85W", 0, 14)); // NOI18N
        btnLoadLedger.setText("Select");
        btnLoadLedger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadLedgerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ledgerSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLoadLedger, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnLoadLedger, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ledgerSelection, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total Debit", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        txtDebit.setFont(new java.awt.Font("HYWenHei-85W", 0, 24)); // NOI18N
        txtDebit.setText("0.00");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtDebit, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(txtDebit)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ledger Display", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator1.setToolTipText("");
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));

        jSeparator2.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));

        debitsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Debits"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(debitsTable);

        creditsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Credits"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(creditsTable);

        txtAccountTitle.setFont(new java.awt.Font("HYWenHei-85W", 0, 24)); // NOI18N
        txtAccountTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtAccountTitle.setText("Account Title");
        txtAccountTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtAccountTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(190, 190, 190))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(txtAccountTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(null, "Final Value", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24)))); // NOI18N

        txtFinalVal.setFont(new java.awt.Font("HYWenHei-85W", 0, 24)); // NOI18N
        txtFinalVal.setText("0.00");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtFinalVal, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(txtFinalVal)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Normal Side", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        txtNormalSide.setFont(new java.awt.Font("HYWenHei-85W", 0, 24)); // NOI18N
        txtNormalSide.setText("UNKNOWN");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtNormalSide, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(txtNormalSide)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Total Credit", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        txtCredit.setFont(new java.awt.Font("HYWenHei-85W", 0, 24)); // NOI18N
        txtCredit.setText("0.00");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtCredit, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(35, Short.MAX_VALUE)
                .addComponent(txtCredit)
                .addGap(30, 30, 30))
        );

        btnReturn.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        btnReturn.setText("Go Back");
        btnReturn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReturnActionPerformed(evt);
            }
        });

        btnExport.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        btnExport.setText("Export All");
        btnExport.setMargin(new java.awt.Insets(3, 14, 3, 14));
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnPost.setFont(new java.awt.Font("HYWenHei-85W", 0, 36)); // NOI18N
        btnPost.setText("Post");
        btnPost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPostActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jDesktopPane1Layout.setVerticalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 72, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jDesktopPane2Layout = new javax.swing.GroupLayout(jDesktopPane2);
        jDesktopPane2.setLayout(jDesktopPane2Layout);
        jDesktopPane2Layout.setHorizontalGroup(
            jDesktopPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jDesktopPane2Layout.setVerticalGroup(
            jDesktopPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnPost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnReturn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnExport, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addComponent(jDesktopPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jDesktopPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jDesktopPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnReturn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnPost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jDesktopPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnReturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnActionPerformed
        HomeForm home = new HomeForm();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnReturnActionPerformed

    private void btnPostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPostActionPerformed
        generateLedgers();
        loadLedgerOptions();
    }//GEN-LAST:event_btnPostActionPerformed

    private void ledgerSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ledgerSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ledgerSelectionActionPerformed

    private void btnLoadLedgerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadLedgerActionPerformed
        loadLedger();
    }//GEN-LAST:event_btnLoadLedgerActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        generateLedgerSheet();
    }//GEN-LAST:event_btnExportActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new LedgerForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnLoadLedger;
    private javax.swing.JButton btnPost;
    private javax.swing.JButton btnReturn;
    private javax.swing.JTable creditsTable;
    private javax.swing.JTable debitsTable;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JDesktopPane jDesktopPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JComboBox<String> ledgerSelection;
    private javax.swing.JLabel txtAccountTitle;
    private javax.swing.JLabel txtCredit;
    private javax.swing.JLabel txtDebit;
    private javax.swing.JLabel txtFinalVal;
    private javax.swing.JLabel txtNormalSide;
    // End of variables declaration//GEN-END:variables
}
