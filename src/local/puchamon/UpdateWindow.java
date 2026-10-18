package local.puchamon;
import java.awt.*;import javax.swing.*;

/** Visible update check for launcher double-clicks; never asks for credentials. */
final class UpdateWindow {
 private static JDialog dialog;private static JLabel label;
 static void open()throws Exception{
  if(GraphicsEnvironment.isHeadless())return;
  EventQueue.invokeAndWait(()->{
   dialog=new JDialog((Frame)null,"Puchamon: Origins — actualizador",false);
   dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
   JPanel panel=new JPanel(new BorderLayout(0,12));panel.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));
   label=new JLabel("Buscando actualizaciones…");panel.add(label,BorderLayout.NORTH);
   JProgressBar progress=new JProgressBar();progress.setIndeterminate(true);panel.add(progress,BorderLayout.CENTER);
   panel.add(new JLabel("Minecraft se abrirá cuando la versión esté verificada."),BorderLayout.SOUTH);
   dialog.setContentPane(panel);dialog.setMinimumSize(new Dimension(500,135));dialog.pack();dialog.setLocationRelativeTo(null);dialog.setVisible(true);
  });
 }
 static void message(String text){if(label!=null)EventQueue.invokeLater(()->{if(label!=null)label.setText(text);});}
 static void close(){if(dialog!=null)EventQueue.invokeLater(()->{dialog.dispose();dialog=null;label=null;});}
}
