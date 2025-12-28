package de.extio.game_engine.i18n;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import de.extio.game_engine.util.ObjectSerialization;

@SuppressWarnings("serial")
public class LocalizationEditor extends JFrame {
	
	private LocalizationServiceImpl localizationManagerImpl;
	
	private String lastFileName;
	
	private JPanel contentPane;
	
	private JButton btnAdd;
	
	private JButton btnRemove;
	
	private JScrollPane scrollPane;
	
	private JTable table;
	
	private JLabel lblDefaultDescre;
	
	private JTextField textField;
	
	private JMenuBar menuBar;
	
	private JMenu mnFile;
	
	private JMenuItem mntmLoad;
	
	private JMenuItem mntmSave;
	
	private JMenuItem mntmSaveAs;
	
	private JMenu mnLanguage;
	
	private JMenuItem mntmAdd;
	
	private JMenuItem mntmDel;
	
	private JMenu mnGoto;
	
	private JMenuItem mntmId;
	
	private JMenuItem mntmSearch;
	
	private JLabel lblPrefix;
	
	private JTextField txtTextprefix;
	
	private JMenuItem mntmNew;
	
	private JButton btnClone;
	
	private static int scale(final int value, final double scaleFactor) {
		return (int) Math.round(value * scaleFactor);
	}
	
	/**
	 * Launch the application.
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	public static void main(final String[] args) throws InvocationTargetException, InterruptedException {
		System.setProperty("java.awt.headless", "false");
		System.setProperty("sun.java2d.uiScale", "1");
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		
		EventQueue.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				try {
					final LocalizationEditor frame = new LocalizationEditor();
					// try (InputStream in = frame.getClass().getClassLoader().getResourceAsStream("icon.png")) {
					// 	final BufferedImage icon = ImageIO.read(in);
					// 	frame.setIconImage(icon);
					// }
					// catch (final Exception e) {
					// }
					frame.setVisible(true);
					
					frame.localizationManagerImpl.reset();
					((AbstractTableModel) frame.table.getModel()).fireTableStructureChanged();
				}
				catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public LocalizationEditor() {
		this.localizationManagerImpl = new LocalizationServiceImpl();
		
		this.setTitle("i18n Editor");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		// this.setBounds(100, 100, 816, 813);
		final var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final var bounds = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		
		final double referenceHeight = 1080.0;
		final double actualHeight = bounds.getHeight();
		final double scaleFactor = actualHeight / referenceHeight;
		
		this.setLocation(bounds.x + scale(50, scaleFactor), bounds.y + scale(50, scaleFactor));
		this.setMinimumSize(new Dimension(scale(600, scaleFactor), scale(600, scaleFactor)));
		
		final Font baseFont = new Font("Dialog", Font.PLAIN, scale(12, scaleFactor));
		final Font largeFont = new Font("Dialog", Font.PLAIN, scale(20, scaleFactor));
		
		this.menuBar = new JMenuBar();
		this.menuBar.setFont(baseFont);
		this.setJMenuBar(this.menuBar);
		
		this.mnFile = new JMenu("File");
		this.mnFile.setFont(baseFont);
		this.menuBar.add(this.mnFile);
		
		this.mntmLoad = new JMenuItem("Load...");
		this.mntmLoad.setFont(baseFont);
		this.mntmLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final File cwd = new File(".");
				final JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(cwd);
				chooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return "*.yaml";
					}
					
					@Override
					public boolean accept(final File f) {
						return !f.isFile() || f.getName().endsWith("yaml");
					}
				});
				
				final int ret = chooser.showDialog(null, "Select file to import");
				if (ret == JFileChooser.CANCEL_OPTION) {
					return;
				}
				
				final String fileName = chooser.getSelectedFile().getAbsolutePath();
				
				LocalizationEditor.this.localizationManagerImpl.reset();
				try {
					LocalizationEditor.this.localizationManagerImpl.reset();
					LocalizationEditor.this.localizationManagerImpl.load(new FileInputStream(fileName));
				}
				catch (final FileNotFoundException e1) {
					JOptionPane.showMessageDialog(null, "An exception occured while loading: " + e1.toString());
					return;
				}
				((AbstractTableModel) LocalizationEditor.this.table.getModel()).fireTableStructureChanged();
				LocalizationEditor.this.lastFileName = fileName;
				LocalizationEditor.this.mntmSave.setEnabled(true);
				LocalizationEditor.this.txtTextprefix.setText(LocalizationEditor.this.localizationManagerImpl.getLocalizations().getPrefix());
			}
		});
		
		this.mntmNew = new JMenuItem("New");
		this.mntmNew.setFont(baseFont);
		this.mntmNew.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String prefix = JOptionPane.showInputDialog(LocalizationEditor.this, "Enter prefix for localizations. Example: shortModName-");
				if (prefix == null || prefix.isEmpty()) {
					return;
				}
				
				LocalizationEditor.this.localizationManagerImpl.reset();
				LocalizationEditor.this.localizationManagerImpl.getLocalizations().setPrefix(prefix);
				
				LocalizationEditor.this.txtTextprefix.setText(prefix);
				((AbstractTableModel) LocalizationEditor.this.table.getModel()).fireTableStructureChanged();
				LocalizationEditor.this.lastFileName = null;
				LocalizationEditor.this.mntmSave.setEnabled(false);
				
				LocalizationEditor.this.addLanguage();
			}
		});
		this.mnFile.add(this.mntmNew);
		this.mnFile.add(this.mntmLoad);
		
		this.mntmSave = new JMenuItem("Save");
		this.mntmSave.setFont(baseFont);
		this.mntmSave.setEnabled(false);
		this.mntmSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					LocalizationEditor.this.localizationManagerImpl.getLocalizations().setPrefix(LocalizationEditor.this.txtTextprefix.getText());
					
					try (OutputStream output = new FileOutputStream(LocalizationEditor.this.lastFileName)) {
						ObjectSerialization.serialize(LocalizationEditor.this.localizationManagerImpl.getLocalizations(), output, false, false, false, null, null);
					}
				}
				catch (final Exception exc) {
					JOptionPane.showMessageDialog(null, "An exception occured while saving: " + exc.toString());
					return;
				}
				
				JOptionPane.showMessageDialog(LocalizationEditor.this, "ok");
			}
		});
		this.mnFile.add(this.mntmSave);
		
		this.mntmSaveAs = new JMenuItem("Save as...");
		this.mntmSaveAs.setFont(baseFont);
		this.mntmSaveAs.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final File cwd = new File(".");
				final JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(cwd);
				chooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return "*.yaml";
					}
					
					@Override
					public boolean accept(final File f) {
						return !f.isFile() || f.getName().endsWith("yaml");
					}
				});

				final int ret = chooser.showSaveDialog(null);
				if (ret == JFileChooser.CANCEL_OPTION) {
					return;
				}
				
				final String fileName = chooser.getSelectedFile().getAbsolutePath();
				
				LocalizationEditor.this.localizationManagerImpl.getLocalizations().setPrefix(LocalizationEditor.this.txtTextprefix.getText());
				
				try {
					try (OutputStream output = new FileOutputStream(fileName)) {
						ObjectSerialization.serialize(LocalizationEditor.this.localizationManagerImpl.getLocalizations(), output, false, false, false, null, null);
					}
				}
				catch (final Exception exc) {
					JOptionPane.showMessageDialog(null, "An exception occured while saving: " + exc.toString());
					return;
				}
				
				LocalizationEditor.this.lastFileName = fileName;
				LocalizationEditor.this.mntmSave.setEnabled(true);
			}
		});
		this.mnFile.add(this.mntmSaveAs);
		
		this.mnLanguage = new JMenu("Language");
		this.mnLanguage.setFont(baseFont);
		this.menuBar.add(this.mnLanguage);
		
		this.mntmAdd = new JMenuItem("Add");
		this.mntmAdd.setFont(baseFont);
		this.mntmAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				LocalizationEditor.this.addLanguage();
			}
		});
		this.mnLanguage.add(this.mntmAdd);
		
		this.mntmDel = new JMenuItem("Del");
		this.mntmDel.setFont(baseFont);
		this.mntmDel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				JOptionPane.showMessageDialog(LocalizationEditor.this, new UnsupportedOperationException());
			}
		});
		this.mnLanguage.add(this.mntmDel);
		
		this.mnGoto = new JMenu("Goto");
		this.mnGoto.setFont(baseFont);
		this.menuBar.add(this.mnGoto);
		
		this.mntmId = new JMenuItem("ID");
		this.mntmId.setFont(baseFont);
		this.mntmId.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String idStr = JOptionPane.showInputDialog(LocalizationEditor.this, "Enter localization id");
				
				final Map<String, String> mapping = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().values().iterator().next();
				int idx = 0;
				for (final String mid : mapping.keySet()) {
					if (mid.equals(idStr)) {
						LocalizationEditor.this.table.changeSelection(idx, 0, false, false);
						LocalizationEditor.this.table.scrollRectToVisible(LocalizationEditor.this.table.getCellRect(idx, 0, true));
						return;
					}
					idx++;
				}
			}
		});
		this.mnGoto.add(this.mntmId);
		
		this.mntmSearch = new JMenuItem("Search");
		this.mntmSearch.setFont(baseFont);
		this.mntmSearch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String search = JOptionPane.showInputDialog(LocalizationEditor.this, "Enter search term");
				
				final Iterator<Map<String, String>> it = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().values().iterator();
				while (it.hasNext()) {
					int idx = 0;
					for (final String text : it.next().values()) {
						if (text.toLowerCase().contains(search.toLowerCase())) {
							LocalizationEditor.this.table.changeSelection(idx, 0, false, false);
							LocalizationEditor.this.table.scrollRectToVisible(LocalizationEditor.this.table.getCellRect(idx, 0, true));
							return;
						}
						idx++;
					}
				}
			}
		});
		this.mnGoto.add(this.mntmSearch);
		
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(scale(5, scaleFactor), scale(5, scaleFactor), scale(5, scaleFactor), scale(5, scaleFactor)));
		this.setContentPane(this.contentPane);
		final GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		this.contentPane.setLayout(gbl_contentPane);
		
		final JPanel panel = new JPanel();
		final GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, scale(5, scaleFactor), 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		this.contentPane.add(panel, gbc_panel);
		panel.setLayout(new GridLayout(1, 6, 0, 0));
		
		this.btnAdd = new JButton("Add");
		this.btnAdd.setFont(baseFont);
		this.btnAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Integer id = LocalizationEditor.this.localizationManagerImpl.getNextId();
				final String idStr;
				if (LocalizationEditor.this.txtTextprefix.getText() == null || LocalizationEditor.this.txtTextprefix.getText().isEmpty()) {
					idStr = id.toString();
				}
				else {
					idStr = LocalizationEditor.this.txtTextprefix.getText() + "-" + id.toString();
				}
				
				for (final Language lang : LocalizationEditor.this.localizationManagerImpl.getLanguages()) {
					final String value = JOptionPane.showInputDialog(LocalizationEditor.this, lang.getShortName());
					if (value != null && !value.isEmpty()) {
						LocalizationEditor.this.localizationManagerImpl.put(lang.getShortName(), idStr, value);
					}
				}
				
				LocalizationEditor.this.localizationManagerImpl.getLocalizations().getDescriptions().put(idStr, LocalizationEditor.this.textField.getText() == null ? "" : LocalizationEditor.this.textField.getText());
				
				((AbstractTableModel) LocalizationEditor.this.table.getModel()).fireTableDataChanged();
			}
		});
		panel.add(this.btnAdd);
		
		this.btnRemove = new JButton("Remove");
		this.btnRemove.setFont(baseFont);
		this.btnRemove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String id = (String) LocalizationEditor.this.table.getValueAt(LocalizationEditor.this.table.getSelectedRow(), 0);
				LocalizationEditor.this.localizationManagerImpl.remove(id);
				((AbstractTableModel) LocalizationEditor.this.table.getModel()).fireTableDataChanged();
			}
		});
		panel.add(this.btnRemove);
		
		this.btnClone = new JButton("Clone");
		this.btnClone.setFont(baseFont);
		this.btnClone.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String oldId = (String) LocalizationEditor.this.table.getValueAt(LocalizationEditor.this.table.getSelectedRow(), 0);
				
				final Integer id = LocalizationEditor.this.localizationManagerImpl.getNextId();
				final String idStr;
				if (LocalizationEditor.this.txtTextprefix.getText() == null || LocalizationEditor.this.txtTextprefix.getText().isEmpty()) {
					idStr = id.toString();
				}
				else {
					idStr = LocalizationEditor.this.txtTextprefix.getText() + "-" + id.toString();
				}
				
				for (final Language lang : LocalizationEditor.this.localizationManagerImpl.getLanguages()) {
					final String value = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().get(lang.getShortName()).get(oldId);
					LocalizationEditor.this.localizationManagerImpl.put(lang.getShortName(), idStr, value);
				}
				
				final String oldDescr = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getDescriptions().get(oldId);
				LocalizationEditor.this.localizationManagerImpl.getLocalizations().getDescriptions().put(idStr, oldDescr);
				
				((AbstractTableModel) LocalizationEditor.this.table.getModel()).fireTableDataChanged();
			}
		});
		panel.add(this.btnClone);
		
		this.lblDefaultDescre = new JLabel("Description:");
		this.lblDefaultDescre.setFont(baseFont);
		this.lblDefaultDescre.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(this.lblDefaultDescre);
		
		this.textField = new JTextField();
		this.textField.setFont(baseFont);
		panel.add(this.textField);
		this.textField.setColumns(10);
		
		this.lblPrefix = new JLabel("Prefix:");
		this.lblPrefix.setFont(baseFont);
		this.lblPrefix.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(this.lblPrefix);
		
		this.txtTextprefix = new JTextField();
		this.txtTextprefix.setFont(baseFont);
		panel.add(this.txtTextprefix);
		this.txtTextprefix.setColumns(10);
		
		this.scrollPane = new JScrollPane();
		this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		this.contentPane.add(this.scrollPane, gbc_scrollPane);
		
		this.table = new JTable();
		this.table.setFont(largeFont);
		this.table.setRowHeight(scale(22, scaleFactor));
		this.table.setFillsViewportHeight(true);
		this.table.setCellSelectionEnabled(true);
		this.table.setModel(new AbstractTableModel() {
			
			@Override
			public Object getValueAt(final int rowIndex, final int columnIndex) {
				final String id = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().values().stream()
						.findFirst()
						.map(mapping -> mapping.keySet().stream().skip(rowIndex).findFirst().orElse("-1"))
						.orElse("-1");
				if (columnIndex == 0) {
					return id;
				}
				
				if (columnIndex == 1) {
					final var desc = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getDescriptions().get(id);
					return desc == null ? "" : desc;
				}
				
				final String lang = LocalizationEditor.this.localizationManagerImpl.getLanguages().get(columnIndex - 2).getShortName();
				final String value = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().entrySet().stream()
						.filter(entry -> entry.getKey().equals(lang))
						.map(entry -> entry.getValue().getOrDefault(id, ""))
						.findFirst()
						.orElse("");
				return value;
			}
			
			@Override
			public int getRowCount() {
				return LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().values().stream().findFirst().map(mapping -> Integer.valueOf(mapping.size())).orElse(Integer.valueOf(0));
			}
			
			@Override
			public int getColumnCount() {
				return LocalizationEditor.this.localizationManagerImpl.getLanguages().size() + 2;
			}
			
			@Override
			public String getColumnName(final int arg0) {
				if (arg0 == 0) {
					return "id";
				}
				else if (arg0 == 1) {
					return "description";
				}
				
				return LocalizationEditor.this.localizationManagerImpl.getLanguages().get(arg0 - 2).getName() + " " + LocalizationEditor.this.localizationManagerImpl.getLanguages().get(arg0 - 2).getShortName();
			}
			
			@Override
			public boolean isCellEditable(final int rowIndex, final int columnIndex) {
				return columnIndex > 0;
			}
			
			@Override
			public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
				final String id = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().values().stream()
						.findFirst()
						.map(mapping -> mapping.keySet().stream().skip(rowIndex).findFirst().orElse(null))
						.orElse(null);
				if (id == null) {
					return;
				}
				
				if (columnIndex == 1) {
					LocalizationEditor.this.localizationManagerImpl.getLocalizations().getDescriptions().put(id, String.valueOf(aValue));
					return;
				}
				
				final String langShortName = LocalizationEditor.this.localizationManagerImpl.getLocalizations().getLanguages().keySet().stream().skip(columnIndex - 2).findFirst().orElse(null);
				if (langShortName == null) {
					return;
				}
				
				LocalizationEditor.this.localizationManagerImpl.put(langShortName, id, String.valueOf(aValue));
			}
			
		});
		this.table.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(final ComponentEvent e) {
				LocalizationEditor.this.table.scrollRectToVisible(LocalizationEditor.this.table.getCellRect(LocalizationEditor.this.table.getRowCount() - 1, 0, true));
			}
		});
		
		this.scrollPane.setViewportView(this.table);
	}
	
	private Language addLanguage() {
		final String lang = JOptionPane.showInputDialog(this, "Language short name (ISO-639-3)");
		if (lang == null || lang.isEmpty()) {
			return null;
		}
		final String name = JOptionPane.showInputDialog(this, "Language name in native lang");
		if (name == null || name.isEmpty() || this.localizationManagerImpl.getLanguages().stream().anyMatch(entry -> entry.getShortName().equals(lang))) {
			return null;
		}
		final Language result = new Language(name, lang);
		this.localizationManagerImpl.getLocalizations().getLanguagesInfo().put(result.getShortName(), result);
		this.localizationManagerImpl.getLocalizations().getLanguages().put(result.getShortName(), new LinkedHashMap<>());
		
		((AbstractTableModel) this.table.getModel()).fireTableStructureChanged();
		
		return result;
	}
}
