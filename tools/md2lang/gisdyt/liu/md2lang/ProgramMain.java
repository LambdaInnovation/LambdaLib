package gisdyt.liu.md2lang;

import gisdyt.liu.md2lang.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ProgramMain {

	private static void file_converting(String in, String out){
		try{
			Log.infoln("Converting is starting...");
			File input=new File(in);
			File output=new File(out);
			if(input.exists()==false) Log.infoln("Input file not found.");
			if(output.exists()==true) {Log.infoln("Output has already exists. I will delete it."); output.delete();}
			output.createNewFile();
			BufferedReader br=new BufferedReader(new FileReader(input));
			StringBuffer buffer=new StringBuffer();
			String temp;
			while((temp=br.readLine())!=null){
				buffer.append(temp+"\n");
			}
			br.close();
			Log.infoln("Reading input file finished...");
			String md=buffer.toString();
			Log.println("File Content:\n"+md);
			md=MainConverter.convertMD2Lang(md);
			Log.infoln("Converting finished...");
			Log.println("Result Preview:\n"+md);
			PrintWriter pw=new PrintWriter(output);
			pw.print(md);
			pw.flush();
			pw.close();
			Log.infoln("Writing result finished...");
			if(MainConverter.success){
				Log.infoln("MD2Lang process finished successfully! Thanks for your using.");
			}else{
				Log.infoln("MD2Lang process is failure to finish!");
				output.delete();
				System.exit(1);
			}
		}catch(IOException e){
			Log.printStackTrace(e);
		}
	}
	
	public static void command_main(String input_file, String output_file, boolean command, boolean stacktrace){
		if(stacktrace){
			Log.log=true;
			System.out.println("Stacktrace Debug Mode Enabled.");
		}else{
			if(command)
				System.out.println("Welcome to use MD2Lang Command Line tools.");
		}
		file_converting(input_file, output_file);
	}
}
