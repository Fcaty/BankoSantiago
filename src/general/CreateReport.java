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
import java.util.*;

/**
 *
 * @author Fcaty
 */
public class CreateReport extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CreateReport.class.getName());
    
    private boolean checkInput(){
        if(txtCompanyName.getText().isEmpty() || txtMonthDay.getText().isEmpty() || txtYr.getText().isEmpty()){
            JOptionPane.showMessageDialog(this, "You have a missing input!");
            return false;
        }
        return true;
    }
    
    private void generateBalanceSheet(){
        String currentTypes[] = {"C", "N", "V"};
        String filepath = "";
        double totalAssets = 0;
        double totalLE = 0;
        double netIncome = 0;
        File count = new File("Output"+File.separator+"BlSheet"+File.separator+"count.txt");
        
        //To track amount of files
        try(Scanner myScan = new Scanner((count))) {
            int newCount = myScan.nextInt() + 1; //Name for new file
            myScan.close();
            FileWriter fw = new FileWriter(count);
            fw.write(Integer.toString(newCount));
            fw.close();
            
            filepath = "Output"+File.separator+"BlSheet"+File.separator+ "BalanceSheetNo"+(newCount) +".txt"; //Filepath string
            
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found!");
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, "IO Error! You must've messed up so badly for this to show up, huh?");
        }
        
        //File generation proper
        //Length of string: 100
        try(
            PrintWriter pw = new PrintWriter(filepath);
            Connection con = DBConn.attemptConnection();
            PreparedStatement pstmtScraper = con.prepareStatement("SELECT AName, Final_Value "
                    + "FROM accountingsystem.account_title INNER JOIN accountingsystem.ledger "
                    + "ON account_title.AID = ledger.AID "
                    + "WHERE Account_Type = ? AND Current_Type = ?"); 
                ){
            
            //Heading
            pw.printf("%-45s %10s %45s\n", " ", txtCompanyName.getText(), " ");
            pw.printf("%-35s %30s %35s\n", " ", "Statement of Financial Position", " ");
            pw.printf("%-40s %20s %40s\n", " ", "As of "+txtMonthDay.getText()+", "+txtYr.getText(), " ");
            
            //Assets
            pw.println("ASSETS");
            for(String c : currentTypes){
                pstmtScraper.setString(1, "A");
                pstmtScraper.setString(2, c);
                ResultSet rs = pstmtScraper.executeQuery();
                
                if(c == "C"){
                    pw.println("    CURRENT ASSETS: ");
                } else if (c == "N"){
                    pw.println("    NON-CURRENT ASSETS: ");
                } 
                
                while(rs.next()){
                    pw.printf("%-50s %25s %25s\n", "        "+rs.getString("AName"), Double.toString(rs.getDouble("Final_Value")), " ");
                    totalAssets += rs.getDouble("Final_Value");
                }
            }
            pw.printf("%-50s %25s %25s\n\n", "    TOTAL ASSETS", Double.toString(totalAssets), "");
            
            //Liabilities and Owner's Equity
            pw.printf("%s\n", "LIABILITIES AND OWNER'S EQUITY");
            pw.println("    LIABILITIES");
            for(String c : currentTypes){
                pstmtScraper.setString(1, "L");
                pstmtScraper.setString(2, c);
                ResultSet rs = pstmtScraper.executeQuery();
                
                if(c == "C"){
                    pw.println("    CURRENT LIABILITIES: ");
                } else if (c == "N"){
                    pw.println("    NON-CURRENT LIABILITIES: ");
                } 
                
                while(rs.next()){
                    pw.printf("%-50s %25s %25s\n", "        "+rs.getString("AName"), " ",Double.toString(rs.getDouble("Final_Value")));
                    totalLE += rs.getDouble("Final_Value");
                }
            }
            //Equity
            pw.println("\n    EQUITY");
            pstmtScraper.setString(1, "C");
            pstmtScraper.setString(2, "V");
            ResultSet rs = pstmtScraper.executeQuery();
            while(rs.next()){
                    pw.printf("%-50s %25s %-25s\n", "        "+rs.getString("AName"), Double.toString(rs.getDouble("Final_Value")), " ");
                    totalLE += rs.getDouble("Final_Value");
            }
            
            //EQUITY ADD
            pw.println("    Add: ");
            //Net Income
            pstmtScraper.setString(1, "I"); //Income
            rs = pstmtScraper.executeQuery();
            while(rs.next()){
                netIncome += rs.getDouble("Final_Value");
            }
            pstmtScraper.setString(1, "E"); //Expenses
            rs = pstmtScraper.executeQuery();
            while(rs.next()){
                netIncome -= rs.getDouble("Final_Value");
            }
            totalLE += netIncome;
            
            pw.printf("%-50s %25s %25s\n", "        NET INCOME", Double.toString(netIncome), "");
            
            //EQUITY LOSS
            pw.println("    Less: ");
            
            //Drawings
            pstmtScraper.setString(1,"C");
            pstmtScraper.setString(2,"N");
            rs = pstmtScraper.executeQuery();
            while(rs.next()){
                    pw.printf("%-50s %25s %-25s\n", "        "+rs.getString("AName"), Double.toString(rs.getDouble("Final_Value")), " ");
                    totalLE -= rs.getDouble("Final_Value");
            }
            
            pw.printf("%-50s %25s %25s", "TOTAL LIABILITIES AND OWNER'S EQUITY", Double.toString(totalLE), "");
            
            
        } catch  (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Connection failed! "+ e.getMessage());
        }
    
    }

    private void generateIncomeStatement(){
        File count = new File("Output"+File.separator+"InStat"+File.separator+"count.txt");
        String filepath = "";
        double totalIncome = 0;
        double totalExpenses = 0;
        double netProfit = 0;
        
        try(Scanner myScan = new Scanner(count)){
            int newCount = myScan.nextInt() + 1; //Name for new file
            myScan.close();
            FileWriter fw = new FileWriter(count);
            fw.write(Integer.toString(newCount));
            fw.close();
            filepath = "Output"+File.separator+"InStat"+File.separator+ "IncomeStatementNo"+(newCount) +".txt"; //Filepath string
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "IO Error! What did you do? This shouldn't be here!");
        }
        
        try(
                PrintWriter pw = new PrintWriter(filepath);
                Connection con = DBConn.attemptConnection();
                PreparedStatement pstmtScraper = con.prepareStatement("SELECT AName, Final_Value "
                    + "FROM accountingsystem.account_title INNER JOIN accountingsystem.ledger "
                    + "ON account_title.AID = ledger.AID "
                    + "WHERE Account_Type = ?");
           ){
            
            //Heading
            pw.printf("%-45s %10s %45s\n", " ", txtCompanyName.getText(), " ");
            pw.printf("%-35s %30s %35s\n", " ", "Statement of Profit or Loss", " ");
            pw.printf("%-40s %20s %40s\n", " ", "For the Year Ended "+txtYr.getText(), " ");
            
            //Income
            pstmtScraper.setString(1, "I");
            ResultSet rs = pstmtScraper.executeQuery();
            while(rs.next()){
                pw.printf("%-50s %25s %25s\n", rs.getString("AName"), " ",Double.toString(rs.getDouble("Final_Value")));
                totalIncome += rs.getDouble("Final_Value");
            }
            
            //TODO: maybe i could add a "cost of goods sold"? if may time lmao - ri
            
            pw.printf("%-50s %25s %25s\n", "Gross Profit", " ", totalIncome);
            
            //Expenses
            pw.printf("%-50s %25s %25s\n", "Less: Operating Expenses", " ", " ");
            pstmtScraper.setString(1, "E");
            rs = pstmtScraper.executeQuery();
            while(rs.next()){
                pw.printf("%-50s %25s %25s\n", "    "+rs.getString("AName"), Double.toString(rs.getDouble("Final_Value"))," ");
                totalExpenses += rs.getDouble("Final_Value");
            }
            
            //Calculate net profit
            netProfit = totalIncome - totalExpenses;
            
            pw.printf("%-50s %25s %25s\n", "NET INCOME / NET LOSS", " ", netProfit);
            
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found!");
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "Connection failed! "+ e.getMessage());
        }
    }
    
    /**
     * Creates new form CreateReport
     */
    public CreateReport() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtCompanyName = new javax.swing.JTextField();
        txtMonthDay = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnGenerate = new javax.swing.JButton();
        btnReturn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtYr = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Company Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("HYWenHei-85W", 0, 24))); // NOI18N

        jLabel1.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel1.setText("Company Name");

        txtCompanyName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCompanyNameActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel2.setText("Month, Day");

        btnGenerate.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        btnGenerate.setText("Generate");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });

        btnReturn.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        btnReturn.setText("Close");
        btnReturn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReturnActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("HYWenHei-85W", 0, 18)); // NOI18N
        jLabel3.setText("Year");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(txtYr, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(47, 47, 47)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(txtMonthDay)))
                            .addComponent(txtCompanyName)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(btnReturn, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                                .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(16, 16, 16))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCompanyName, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMonthDay, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtYr, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnGenerate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnReturn, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtCompanyNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCompanyNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCompanyNameActionPerformed

    private void btnReturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnActionPerformed
        HomeForm home = new HomeForm();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnReturnActionPerformed

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        if(checkInput()){
            generateBalanceSheet();
            generateIncomeStatement();
        }
    }//GEN-LAST:event_btnGenerateActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new CreateReport().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnReturn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtCompanyName;
    private javax.swing.JTextField txtMonthDay;
    private javax.swing.JTextField txtYr;
    // End of variables declaration//GEN-END:variables
}
