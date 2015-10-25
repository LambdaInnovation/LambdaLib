package gisdyt.liu.md2lang;

import gisdyt.liu.md2lang.ProgramMain;
import gisdyt.liu.md2lang.converters.Converter;
import gisdyt.liu.md2lang.util.Log;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.TextArea;
import java.awt.Label;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class GUIMain {

	private static JFrame frmMD2Lang;
	private static GUIMain instance;
	static{
		instance=new GUIMain();
	}
	
	public static void message_exception(Throwable e){
		if(instance!=null && instance.frmMD2Lang!=null)
			instance.exception(e);
			
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if(args.length==3 && args[0].equals("--command")){
			ProgramMain.command_main(args[1], args[2], true, false);
		}else if(args.length==4 && args[0].equals("--command") && args[3].equals("--stacktrace")){
			ProgramMain.command_main(args[1], args[2], true, true);
		}else{
			Log.info=false;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						instance.frmMD2Lang.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Create the application.
	 */
	public GUIMain() {
		initialize();
	}
	
	public void exception(Throwable e){
		StringWriter writer=new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		writer.flush();
		String message=writer.toString();
		JOptionPane.showMessageDialog(frmMD2Lang, "Exception!\n"+message);
		try {
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			exception(e1);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMD2Lang = new JFrame();
		frmMD2Lang.setTitle("Markdown to Language File Converter");
		frmMD2Lang.setBounds(100, 100, 480, 567);
		frmMD2Lang.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMD2Lang.getContentPane().setLayout(null);
		
		TextArea WelcomeText = new TextArea();
		WelcomeText.setEditable(false);
		WelcomeText.setText("Hello User! Welcome to use MD2LangGUI. Before you using this software,\r\nplease read the following information carefully. And there should be a \r\nhelping documentation but it is omitted.\r\n\r\nAs you can see, this is a GUI tool. And if you want to use this tool in\r\ncommand line mode, please use the following grammar:\r\njava -jar md2lang.jar --command <input file> <output file> [--stacktrace]\r\nThe arg --stacktrace means use debug mode and output all of the\r\nstacktrace information.\r\n\r\nAnd if you want to use GUI tool, please input the input file path or Markdown\r\ncontent into the first text-panel. As the file path, use the prefix \"%%file%%:\" and\r\nthen input the path.\r\nIf you want to use the file path, please input the output file path into the second\r\ntext-panel.\r\nLike this:\r\n(first text-panel) %%file%%:C:\\test.md\r\n(second text-panel) C:\\test.lang\r\n(Linux/Unix file path is also enable.)\r\nOr you can also input Markdown content directly.\r\n\r\nIf you have any question about this software, don't ask me, GISDYT, please\r\nask the group leader of LI, WeAthFolD.\r\nThis software was developed by GISDYT in the FUTURE WORLD!");
		WelcomeText.setBounds(0, 0, 464, 150);
		frmMD2Lang.getContentPane().add(WelcomeText);
		
		TextArea input = new TextArea();
		input.setBounds(0, 166, 464, 150);
		frmMD2Lang.getContentPane().add(input);
		
		Label firstlable = new Label("First Text-Panel");
		firstlable.setBounds(0, 148, 87, 23);
		frmMD2Lang.getContentPane().add(firstlable);
		
		TextArea output = new TextArea();
		output.setBounds(0, 332, 464, 150);
		frmMD2Lang.getContentPane().add(output);
		
		Label secondlable = new Label("Second Text-Panel");
		secondlable.setBounds(0, 310, 110, 23);
		frmMD2Lang.getContentPane().add(secondlable);
		
		JButton action = new JButton("Action Start");
		action.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(input.getText().substring(0, 9).equals("%%file%%:")){
					ProgramMain.command_main(input.getText().substring(9), output.getText(), false, false);
				}else{
					output.setText(MainConverter.convertMD2Lang(input.getText()));
				}
				JOptionPane.showMessageDialog(frmMD2Lang, "Action finished, success? ["+MainConverter.success+"]");
			}
		});
		action.setBounds(0, 478, 464, 50);
		frmMD2Lang.getContentPane().add(action);
	}
}
