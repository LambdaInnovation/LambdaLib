/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.annoreg.mc.RegEventHandler.Bus;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cn.lambdalib.annoreg.mc.RegPostInitCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
@Registrant
public class CheckManger
{
	/**
	 * {modid:[modname,localVersion,apiurl]}
	 */
	HashMap<String,String[]> modPool=new HashMap<>();
	HashMap<String,String> latestVersion=new HashMap<>();
	
	private static CheckManger instance=new CheckManger();
	private static boolean alerted=false;
	private CheckManger(){}
	public static CheckManger instance()
	{
		return instance;
	}
	/**
	 * 
	 * @param modid
	 * @param pars String[]{modname,localVersion,apiUrl}
	 */
	public void addMod(String modid,String[] pars)
	{
		this.modPool.put(modid, pars);
	} 
	
	public void addNewVersion(String modid,String newVersion)
	{
		this.latestVersion.put(modid,newVersion);
	}
	
	@SubscribeEvent
	public void enterWorldHandler(PlayerLoggedInEvent e)
	{
		if(!alerted)
		{
			 EntityPlayer player=e.player;
			 String[] pars;
			 for(String modid:this.latestVersion.keySet())
			 {
				 pars=this.modPool.get(modid);
				 //modname,latestestversion//
				 player.addChatMessage(new ChatComponentTranslation("chat.newversion",pars[0],this.latestVersion.get(modid)));
				 
			 }
		}
	}
	@RegPostInitCallback
	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(instance);
		FMLCommonHandler.instance().bus().register(instance);
		//run threads
		for(String modid:instance.modPool.keySet())
		{
			Fetcher fetcher=new Fetcher(modid);
			Thread thread=new Thread(fetcher);
			thread.start();
		}
	}
}

class Fetcher implements Runnable
{
	private boolean alive=true;
	final URL api_url;
	final String modid;
	final String localVersion;
	public Fetcher(String modid)
	{
		this.modid=modid;
		/**modname,localVersion,apiurl*/
		String[] pars=CheckManger.instance().modPool.get(modid);
		this.localVersion=pars[1];
		URL url;
		try {
			url=new URL(pars[2]);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			url=null;
		}
		this.api_url=url;
	}
	@Override
	public void run()
	{
		if(this.api_url==null)
			return;
		Gson gson=new Gson();
		Type dict=new TypeToken<List<Map<String,Object>>>(){}.getType();
		List<Map<String,Object>> releases = null;
		try {
			releases=gson.fromJson(new BufferedReader(new InputStreamReader(this.api_url.openStream())),dict);
		} catch (Exception e) {
			e.printStackTrace();
		}
		HashMap<String,Integer> versions=new HashMap<>(releases.size());
		for(int i=0;i<releases.size();++i)
		{
			versions.put((String) releases.get(i).get("tag_name"),i);
		}
		if(!versions.keySet().contains(this.localVersion))
			return;
		String latestVersion=(String) releases.get(0).get("tag_name");
        if(latestVersion==null||latestVersion.equals(this.localVersion))
            return;
		CheckManger.instance().addNewVersion(this.modid, latestVersion);
	}
	
}