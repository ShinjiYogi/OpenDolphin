/*
 * PatientSearchView.java
 *
 * Created on 2007/11/22, 18:43
 */

package open.dolphin.impl.psearch;

/**
 *
 * @author  kazushi
 */
public class PatientSearchView extends javax.swing.JPanel {
   
    
    /** Creates new form PatientSearchView */
    public PatientSearchView() {
        initComponents();
    }

    public javax.swing.JLabel getCountLbl() {
        return countLbl;
    }

    public javax.swing.JTable getTable() {
        return table;
    }

    public javax.swing.JTextField getKeywordFld() {
        return keywordFld;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new AddressTipsTable();
        keywordFld = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        autoIme = new javax.swing.JCheckBox();
        countLbl = new javax.swing.JLabel();
        sortItem = new javax.swing.JComboBox();
        tmpKarteButton = new javax.swing.JButton();

        table.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(table);

        keywordFld.setToolTipText("患者氏名、電話番号、ID等の一部、右クリックで来院日を入力します。");

        jLabel1.setText("デフォルトソート: ");

        autoIme.setText("自動IME");

        countLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        countLbl.setText("0 件");

        sortItem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "患者ID", "カ ナ" }));

        tmpKarteButton.setText("仮保存カルテ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sortItem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoIme)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 118, Short.MAX_VALUE)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keywordFld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 188, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tmpKarteButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(countLbl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(9, 9, 9)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(keywordFld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(countLbl)
                        .add(tmpKarteButton))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel1)
                        .add(autoIme)
                        .add(sortItem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(5, 5, 5)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoIme;
    private javax.swing.JLabel countLbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField keywordFld;
    private javax.swing.JComboBox sortItem;
    private javax.swing.JTable table;
    private javax.swing.JButton tmpKarteButton;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JCheckBox getAutoIme() {
        return autoIme;
    }

    public void setAutoIme(javax.swing.JCheckBox autoIme) {
        this.autoIme = autoIme;
    }

    public javax.swing.JComboBox getSortItem() {
        return sortItem;
    }

    public javax.swing.JButton getTmpKarteButton() {
        return tmpKarteButton;
    }
}