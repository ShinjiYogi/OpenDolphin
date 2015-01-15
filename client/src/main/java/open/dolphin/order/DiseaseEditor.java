package open.dolphin.order;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import open.dolphin.client.AutoKanjiListener;
import open.dolphin.client.AutoRomanListener;
import open.dolphin.client.ClientContext;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.delegater.OrcaDelegaterFactory;
import open.dolphin.infomodel.DiseaseEntry;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.TensuMaster;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;
import open.dolphin.table.StripeTableCellRenderer;
import open.dolphin.util.StringTool;

/**
 * 傷病名編集テーブルクラス。
 *
 * @author Kazushi Minagawa
 */
public final class DiseaseEditor extends AbstractStampEditor {
    
    // 傷病名の修飾語コード
    private static final String MODIFIER_CODE = "ZZZ";
    
    // 傷病名手入力時につけるコード
    private static final String HAND_CODE = "0000999";
    
    // Diagnosis table のパラメータ
    private static final int NAME_COL       = 1;
    private static final int ALIAS_COL      = 2;
    private static final int DISEASE_NUM_ROWS = 5;
    
    private static final String TOOLTIP_COMBINE  = "テーブルの行を連結して修飾語付きの傷病名にします";
    
    // Table model
    private IDiseaseView view;

    private ListTableModel<RegisteredDiagnosisModel> tableModel;

    private ListTableModel<DiseaseEntry> searchResultModel;

////s.oh^ 2014/07/11 スタンプエディタのフォーカス制御
//    private boolean editCellStart;
//    private boolean editCellEnter;
////s.oh$

    @Override
    public JPanel getView() {
        return (JPanel)view;
    }

    @Override
    public void dispose() {

        if (tableModel != null) {
            tableModel.clear();
        }

        if (searchResultModel != null) {
            searchResultModel.clear();
        }

        super.dispose();
    }


    @Override
    public Object getValue() {

        if (hasModifier()) {
            return getValue1();
        } else {
            return getValue2();
        }
    }

    /**
     * 傷病名テーブルをスキャンし修飾語つきの傷病にして返す。
     */
    private Object getValue1() {

        RegisteredDiagnosisModel diagnosis = null;

        StringBuilder name = new StringBuilder();
        StringBuilder code = new StringBuilder();

        // テーブルをスキャンする
        int count = tableModel.getObjectCount();
        for (int i = 0; i < count; i++) {

            RegisteredDiagnosisModel diag = tableModel.getObject(i);
            String diagCode = diag.getDiagnosisCode();

            if (!diagCode.startsWith(MODIFIER_CODE)) {
                //
                // 修飾語でない場合は基本病名と見なし、パラメータを設定する
                //
                diagnosis = new RegisteredDiagnosisModel();
                diagnosis.setDiagnosisCodeSystem(diag.getDiagnosisCodeSystem());

            } else {
                // ZZZ をトリムする ORCA 実装
                diagCode = diagCode.substring(MODIFIER_CODE.length());
            }

            // コードを . で連結する
            if (code.length() > 0) {
                code.append(".");
            }
            code.append(diagCode);

            // 名前を連結する
            name.append(diag.getDiagnosis());

        }

        if (diagnosis != null && name.length() > 0 && code.length() > 0) {

            // 名前とコードを設定する
            diagnosis.setDiagnosis(name.toString());
            diagnosis.setDiagnosisCode(code.toString());
            ArrayList<RegisteredDiagnosisModel> ret = new ArrayList<RegisteredDiagnosisModel>(1);
            ret.add(diagnosis);

            return ret;

        } else {
            return null;
        }
    }

    /**
     * 傷病名テーブルをスキャンし修飾語つきの傷病にして返す。
     */
    private Object getValue2() {

        return tableModel.getDataProvider();
    }

    @Override
    public void setValue(Object o) {
        clear();
    }
    

    @Override
    protected void checkValidation() {

        setIsEmpty = tableModel.getObjectCount() == 0 ? true : false;

        setIsValid = true;

        int diseaseCnt = 0;
        List<RegisteredDiagnosisModel> itemList = tableModel.getDataProvider();

        for (RegisteredDiagnosisModel diag : itemList) {

            if (diag.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                continue;

            } else {
                diseaseCnt++;
            }
        }

        setIsValid = setIsValid && (diseaseCnt > 0);

        // ButtonControl
        view.getClearBtn().setEnabled(!setIsEmpty);
        view.getOkCntBtn().setEnabled(setIsValid && getFromStampEditor());
        view.getOkBtn().setEnabled(setIsValid && getFromStampEditor());

        // 傷病名チェックボックス
        view.getDiseaseCheck().setSelected((diseaseCnt > 0));

        // 通知する
        super.checkValidation();
    }

    @Override
    protected void addSelectedTensu(TensuMaster tm) {
        // No use
    }

    @Override
    protected void search(final String text, boolean hitReturn) {

        boolean pass = true;
        pass = pass && ipOk();

        final int searchType = getSearchType(text, hitReturn);
        pass = pass && (searchType==TT_LETTER_SEARCH);

        if (!pass) {
            return;
        }

        // 件数をゼロにしておく
        view.getCountField().setText("0");

        // 検索を実行する
        SwingWorker worker = new SwingWorker<List<DiseaseEntry>, Void>() {

            @Override
            protected List<DiseaseEntry> doInBackground() throws Exception {
                //SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create("dao.master");
                //OrcaRestDelegater dao = new OrcaRestDelegater();
                OrcaDelegater dao = OrcaDelegaterFactory.create();
                String d = new SimpleDateFormat("yyyyMMdd").format(new Date());
                List<DiseaseEntry> result = dao.getDiseaseByName(StringTool.hiraganaToKatakana(text), d, view.getPartialChk().isSelected());
//s.oh^ 2013/11/08 傷病名検索不具合
                if(result == null || result.size() <= 0) {
                    result = dao.getDiseaseByName(text, d, view.getPartialChk().isSelected());
                }
//s.oh$
//                if (!dao.isNoError()) {
//                    throw new Exception(dao.getErrorMessage());
//                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    List<DiseaseEntry> result = get();
                    searchResultModel.setDataProvider(result);
                    int cnt = searchResultModel.getObjectCount();
                    view.getCountField().setText(String.valueOf(cnt));
                    Rectangle r = view.getSearchResultTable().getCellRect(0, 0, true);
                    view.getSearchResultTable().scrollRectToVisible(r);
                    
                } catch (InterruptedException ex) {

                } catch (ExecutionException ex) {
                    alertSearchError(ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    @Override
    protected void clear() {
        tableModel.clear();
        view.getStampNameField().setText("");
        checkValidation();
    }

    @Override
    protected void initComponents() {

        //view = new DiseaseView();
        view = editorButtonTypeIsIcon() ? new DiseaseView() : new DiseaseViewText();
        
        // Info Label
//minagawa^ Icon Server
        view.getInfoLabel().setIcon(ClientContext.getImageIconArias("icon_info_small"));
//minagawa$        
        
        // テーブルのカラム名を取得する
        String[] diganosisColumns = new String[]{
            "コード", "疾患名/修飾語", "エイリアス"
        };

        // テーブルのカラム名を取得する
        String[] methodNames = new String[]{
            "getDiagnosisCode", "getDiagnosisName", "getDiagnosisAlias"
        };
        
        // 病名テーブルを生成する
        tableModel = new ListTableModel<RegisteredDiagnosisModel>(diganosisColumns, DISEASE_NUM_ROWS, methodNames, null) {
            
            // 病名カラムも修飾語の編集が可能
            @Override
            public boolean isCellEditable(int row, int col) {
                
                boolean ret = false;
                
                RegisteredDiagnosisModel model = getObject(row);
                
                if (col == NAME_COL) {
                    if (model == null) {
                        ret = true;
                    } else if (!model.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                        ret = true;
                    }
                    
                } else if (col == ALIAS_COL) {
                    if (model != null && (!model.getDiagnosisCode().startsWith(MODIFIER_CODE))) {
                        ret = true;
                    }
                }
                
                return ret;
            }
            
            @Override
            public void setValueAt(Object o, int row, int col) {
                
                if (o == null) {
                    return;
                }
                
                int index = ((String)o).indexOf(',');
                if (index > 0) {
                    return;
                }
                
                RegisteredDiagnosisModel model = getObject(row);
                String value = (String) o;
                
                switch (col) {
                    
                    case NAME_COL:
                        //
                        // 病名が手入力された場合は、コードに 0000999 を設定する
                        //
                        if (!value.equals("")) {
                            if (model != null) {
                                model.setDiagnosis(value);
                                model.setDiagnosisCode(HAND_CODE);
                                fireTableCellUpdated(row, col);

                            } else {
                                model = new RegisteredDiagnosisModel();
                                model.setDiagnosis(value);
                                model.setDiagnosisCode(HAND_CODE);
                                addObject(model);
                                checkValidation();
                            }
                        }
                        break;
                        
                    case ALIAS_COL:
                        //
                        // エイリアスの入力があった場合
                        //
                        if (model != null) {
                            String test = model.getDiagnosis();
                            int idx = test.indexOf(',');
                            if (idx >0 ) {
                                test = test.substring(0, idx);
                                test = test.trim();
                            }
                            if (value.equals("")) {
                                model.setDiagnosis(test);
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append(test);
                                sb.append(",");
                                sb.append(value);
                                model.setDiagnosis(sb.toString());
                            }
                        }
                        break;
                }
            }
        };
        
        // SetTable を生成し transferHandler を生成する
        JTable table = view.getSetTable();
        table.setModel(tableModel);

        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT);
        table.setTransferHandler(new RegisteredDiagnosisTransferHandler()); // TransferHandler
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        ListSelectionModel m = table.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    int row = view.getSetTable().getSelectedRow();
                    if (tableModel.getObject(row)!= null) {
                        view.getDeleteBtn().setEnabled(true);
                    } else {
                        view.getDeleteBtn().setEnabled(false);
                    }
                }
            }
        });
        
        //table.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        StripeTableCellRenderer rederer = new StripeTableCellRenderer();
        rederer.setTable(table);
        rederer.setDefaultRenderer();
        // Set Table の行の高さ
        table.setRowHeight(ClientContext.getMoreHigherRowHeight());

        // CellEditor を設定する
        // 疾患名
        TableColumn column = table.getColumnModel().getColumn(NAME_COL);
        JTextField nametf = new JTextField();
//s.oh^ 2014/02/24 スタンプ内項目削除不具合
        //nametf.addFocusListener(AutoKanjiListener.getInstance());
        FocusListener flRoman = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                Object source = e.getSource();
                if (source != null && source instanceof JTextComponent) {
                    JTextComponent tc = (JTextComponent) source;
                    if (tc.getInputContext() != null) {
                        tc.getInputContext().setCharacterSubsets(null);
                    }
                }
                view.getDeleteBtn().setEnabled(false);
                view.getClearBtn().setEnabled(false);
////s.oh^ 2014/07/11 スタンプエディタのフォーカス制御
//                editCellStart = true;
////s.oh$
            }
            @Override
            public void focusLost(FocusEvent e) {
                if(view.getSetTable().isEditing()) {
                    view.getSetTable().getCellEditor().stopCellEditing();
                }
                int row = view.getSetTable().getSelectedRow();
                if (tableModel.getObject(row)!= null) {
                    view.getDeleteBtn().setEnabled(true);
                } else {
                    view.getDeleteBtn().setEnabled(false);
                }
                view.getClearBtn().setEnabled(!setIsEmpty);
////s.oh^ 2014/07/11 スタンプエディタのフォーカス制御
//                if(editCellStart && editCellEnter) {
//                    view.getSearchTextField().requestFocus();
//                }
//                editCellStart = false;
//                editCellEnter = false;
////s.oh$
            }
        };
        FocusListener flKanji = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (ClientContext.isWin()) {
                    Object source = e.getSource();
                    if (source != null && source instanceof JTextComponent) {
                        JTextComponent tc = (JTextComponent) source;
                        if (tc.getInputContext() != null) {
                            tc.getInputContext().setCompositionEnabled(true);
                        }
                    }
                }
                view.getDeleteBtn().setEnabled(false);
                view.getClearBtn().setEnabled(false);
////s.oh^ 2014/07/11 スタンプエディタのフォーカス制御
//                editCellStart = true;
////s.oh$
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (ClientContext.isWin()) {
                    Object source = e.getSource();
                    if (source != null && source instanceof JTextComponent) {
                        JTextComponent tc = (JTextComponent) source;
                        if (tc.getInputContext() != null) {
                            tc.getInputContext().setCompositionEnabled(false);
                        }
                    }
                }else{
                    if(view.getSetTable().isEditing()) {
                        view.getSetTable().getCellEditor().stopCellEditing();
                    }
                }
                int row = view.getSetTable().getSelectedRow();
                if (tableModel.getObject(row)!= null) {
                    view.getDeleteBtn().setEnabled(true);
                } else {
                    view.getDeleteBtn().setEnabled(false);
                }
                view.getClearBtn().setEnabled(!setIsEmpty);
////s.oh^ 2014/07/11 スタンプエディタのフォーカス制御
//                if(editCellStart && editCellEnter) {
//                    view.getSearchTextField().requestFocus();
//                }
//                editCellStart = false;
//                editCellEnter = false;
////s.oh$
            }
        };
        nametf.addFocusListener(flKanji);
//s.oh$
        DefaultCellEditor nameEditor = new DefaultCellEditor(nametf);
        int clickCountToStart = Project.getInt("diagnosis.table.clickCountToStart", 1);
        nameEditor.setClickCountToStart(clickCountToStart);
        column.setCellEditor(nameEditor);

        // 病名エイリアス
        column = table.getColumnModel().getColumn(ALIAS_COL);
        JTextField aliastf = new JTextField();
//s.oh^ 2014/02/24 スタンプ内項目削除不具合
        //aliastf.addFocusListener(AutoRomanListener.getInstance()); // alias 
        aliastf.addFocusListener(flRoman); // alias 
//s.oh$
        DefaultCellEditor aliasEditor = new DefaultCellEditor(aliastf);
        aliasEditor.setClickCountToStart(clickCountToStart);
        column.setCellEditor(aliasEditor);
        
////s.oh^ 2014/07/11 スタンプエディタのフォーカス制御
//        nametf.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {}
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if(e.getKeyCode() == KeyEvent.VK_ENTER && editCellStart) {
//                    editCellEnter = true;
//                }else{
//                    editCellEnter = false;
//                }
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
//        aliastf.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {}
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if(e.getKeyCode() == KeyEvent.VK_ENTER && editCellStart) {
//                    editCellEnter = true;
//                }else{
//                    editCellEnter = false;
//                }
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
////s.oh$
        
        // 列幅設定
        int[] columnWidth = new int[]{20, 135, 135};
        int len = columnWidth.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidth[i]);
        }

        //
        // 病名マスタ検索結果テーブル
        //
        String[] srColumnNames = new String[]{"コード", "名 称", "カナ", "ICD10"};
        String[] srMthodNames = new String[]{"getCode", "getName", "getKana", "getIcdTen"};
        int[] srColumnWidth = new int[]{10, 135, 135, 10};

        searchResultModel = new ListTableModel<DiseaseEntry>(srColumnNames, 0, srMthodNames, null);

        JTable searchResultTable = view.getSearchResultTable();
        searchResultTable.setModel(searchResultModel);
        searchResultTable.setRowHeight(ClientContext.getHigherRowHeight());
        searchResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultTable.setRowSelectionAllowed(true);
        ListSelectionModel lm = searchResultTable.getSelectionModel();
        lm.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                if (e.getValueIsAdjusting() == false) {

                    int row = view.getSearchResultTable().getSelectedRow();

                    DiseaseEntry o = searchResultModel.getObject(row);

                    if (o != null) {

                        String codeSystem = ClientContext.getString("mml.codeSystem.diseaseMaster");
                        RegisteredDiagnosisModel model = new RegisteredDiagnosisModel();
                        model.setDiagnosis(o.getName());
                        model.setDiagnosisCode(o.getCode());
                        model.setDiagnosisCodeSystem(codeSystem);
                        tableModel.addObject(model);
                        reconstractDiagnosis();
                        checkValidation();
                    }
                    searchTextField.requestFocus();
                }
            }
        });

        len = srColumnWidth.length;
        for (int i = 0; i < len; i++) {
            column = searchResultTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(srColumnWidth[i]);
        }
        
        //searchResultTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        StripeTableCellRenderer str = new StripeTableCellRenderer();
        str.setTable(searchResultTable);
        str.setDefaultRenderer();
        
        // 複合病名フィールド
        JTextField combinedDiagnosis = view.getStampNameField();
        combinedDiagnosis.setEditable(false);
        combinedDiagnosis.setToolTipText(TOOLTIP_COMBINE);
        
        // 検索フィールド
        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
//s.oh^ 2014/04/14 RT検索改善
                //if (view.getRtBtn().isSelected()) {
                //    search(view.getSearchTextField().getText().trim(), false);
                //}
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (view.getRtBtn().isSelected()) {
                            search(view.getSearchTextField().getText().trim(), false);
                        }
                    }
                });
//s.oh$
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
//s.oh^ 2014/04/14 RT検索改善
                //if (view.getRtBtn().isSelected()) {
                //    search(view.getSearchTextField().getText().trim(), false);
                //}
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (view.getRtBtn().isSelected()) {
                            search(view.getSearchTextField().getText().trim(), false);
                        }
                    }
                });
//s.oh$
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
//s.oh^ 2014/04/14 RT検索改善
                //if (view.getRtBtn().isSelected()) {
                //    search(view.getSearchTextField().getText().trim(), false);
                //}
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (view.getRtBtn().isSelected()) {
                            search(view.getSearchTextField().getText().trim(), false);
                        }
                    }
                });
//s.oh$
            }
        };
        searchTextField = view.getSearchTextField();
        searchTextField.getDocument().addDocumentListener(dl);
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search(view.getSearchTextField().getText().trim(),true);
            }
        });
        searchTextField.addFocusListener(AutoKanjiListener.getInstance());
        // マスター検索ができない場合を追加
        searchTextField.setEnabled(Project.canSearchMaster());

        // Real Time Search
        boolean rt = Project.getBoolean("masterSearch.realTime", true);
        view.getRtBtn().setSelected(rt);
        view.getRtBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Project.setBoolean("masterSearch.realTime", view.getRtBtn().isSelected());
            }
        });

        // 部分一致
        boolean pmatch = Project.getBoolean("masterSearch.partialMatch", false);
        view.getPartialChk().setSelected(pmatch);
        view.getPartialChk().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Project.setBoolean("masterSearch.partialMatch", view.getPartialChk().isSelected());
            }
        });

        // 件数フィールド
        countField = view.getCountField();

        // OK & 連続ボタン
        view.getOkCntBtn().setEnabled(false);
//minagawa^ Icon Server
        if(editorButtonTypeIsIcon()) {
            view.getOkCntBtn().setIcon(ClientContext.getImageIconArias("icon_gear_small"));
        }
//s.oh^ 2014/10/22 Icon表示
        view.getSearchLabel().setIcon(ClientContext.getImageIconArias("icon_search_small"));
//s.oh$
//minagawa$          
        view.getOkCntBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boundSupport.firePropertyChange(VALUE_PROP, null, getValue());
                clear();
            }
        });


        // OK ボタン
        view.getOkBtn().setEnabled(false);
//minagawa^ Icon Server
        if(editorButtonTypeIsIcon()) {
            view.getOkBtn().setIcon(ClientContext.getImageIconArias("icon_accept_small"));
        }
//minagawa$         
        view.getOkBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boundSupport.firePropertyChange(VALUE_PROP, null, getValue());
                dispose();
                boundSupport.firePropertyChange(EDIT_END_PROP, false, true);
            }
        });

        // 削除ボタン
        view.getDeleteBtn().setEnabled(false);
//minagawa^ Icon Server
        if(editorButtonTypeIsIcon()) {
            view.getDeleteBtn().setIcon(ClientContext.getImageIconArias("icon_delete_small"));
        }
//minagawa$        
        view.getDeleteBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = view.getSetTable().getSelectedRow();
                if ((!view.getSetTable().isEditing()) &&  tableModel.getObject(row) != null) {
                    tableModel.deleteAt(row);
                    checkValidation();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        // クリアボタン
        view.getClearBtn().setEnabled(false);
//minagawa^ Icon Server
        if(editorButtonTypeIsIcon()) {
            view.getClearBtn().setIcon(ClientContext.getImageIconArias("icon_clear_small"));
        }
//minagawa$        
        view.getClearBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
    }

    /**
     * テーブルをスキャンし、傷病名コンポジットする。
     */
    public void reconstractDiagnosis() {
        
        if (hasModifier()) {
            StringBuilder sb = new StringBuilder();
            int count = tableModel.getObjectCount();
            for (int i = 0; i < count; i++) {
                RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
                sb.append(diag.getDiagnosis());
            }
            view.getStampNameField().setText(sb.toString());
        } else {
            view.getStampNameField().setText("");
        }
    }
    
    /**
     * 修飾語をふくんでいるかどうかを返す。
     */
    private boolean hasModifier() {
        boolean hasModifier = false;
        int count = tableModel.getObjectCount();
        for (int i = 0; i < count; i++) {
            RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
            if (diag.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                hasModifier = true;
                break;
            }
        }
        return hasModifier;
    }

    public DiseaseEditor() {
        this(true);
    }

    public DiseaseEditor(boolean mode) {
        super();
        this.setFromStampEditor(mode);
        this.setOrderName("傷病名");
    }
}

