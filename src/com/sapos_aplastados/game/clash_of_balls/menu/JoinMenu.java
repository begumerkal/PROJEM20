/*
 * Copyright (C) 2012-2013 Hans Hardmeier <hanshardmeier@gmail.com>
 * Copyright (C) 2012-2013 Andrin Jenal
 * Copyright (C) 2012-2013 Beat Küng <beat-kueng@gmx.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.sapos_aplastados.game.clash_of_balls.menu;

import android.content.Context;
import android.util.Log;

import com.sapos_aplastados.game.clash_of_balls.Font2D;
import com.sapos_aplastados.game.clash_of_balls.GameSettings;
import com.sapos_aplastados.game.clash_of_balls.TextureManager;
import com.sapos_aplastados.game.clash_of_balls.Font2D.Font2DSettings;
import com.sapos_aplastados.game.clash_of_balls.Font2D.TextAlign;
import com.sapos_aplastados.game.clash_of_balls.UIHandler.UIChange;
import com.sapos_aplastados.game.clash_of_balls.game.RenderHelper;
import com.sapos_aplastados.game.clash_of_balls.game.Vector;
import com.sapos_aplastados.game.clash_of_balls.network.NetworkClient;
import com.sapos_aplastados.game.clash_of_balls.network.Networking;

public class JoinMenu extends GameMenuBase {
	
	private String LOG_TAG="JoinMenu";
	
	private MenuItemButton m_join_button;
	private MenuItemButton m_cancel_button;
	
	private MenuItemKeyboard m_name_button;
	private MenuItemStringMultiline m_name_label;
	
	private MenuItemStringMultiline m_game_label;
	private MenuItemList m_game_list;
	private float m_item_height;
	private int m_selected_item_protocol_version = -1;
	
	private MenuItemStringMultiline m_older_version_warning;
	private MenuItemStringMultiline m_newer_version_warning;
	
	private TextureManager m_tex_manager;
	
	private Font2DSettings m_list_item_font_settings;
	
	private GameSettings m_settings;
	private NetworkClient m_network_client;
	
	
	public JoinMenu(MenuBackground background
			, float screen_width, float screen_height
			, TextureManager tex_manager, Context context
			, Font2D.Font2DSettings font_settings, int label_font_color
			, GameSettings settings
			, NetworkClient network_client) {
		super(background,context);
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		Font2D.Font2DSettings label_font_settings 
			= new Font2D.Font2DSettings(font_settings.m_typeface,
				TextAlign.LEFT, label_font_color);
		Font2D.Font2DSettings keyboard_font_settings 
			= new Font2D.Font2DSettings(font_settings.m_typeface,
					font_settings.m_align, font_settings.m_color);
		
		m_tex_manager = tex_manager;
		m_network_client = network_client;
		m_settings = settings;
		
		if(m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);

		//add menu items
		float button_width = size.x * 0.35f;
		float button_height=0.25f*button_width;
		float label_height = 0.75f*button_height;
		
		float offset_y = size.y*0.025f;
		float offset_x = offset_y;
		
		//game list
		m_item_height = button_height * 0.8f;
		m_list_item_font_settings = new Font2DSettings(font_settings.m_typeface
				, font_settings.m_align, font_settings.m_color);
		m_menu_items.add(m_game_list = new MenuItemList(
				new Vector(pos.x + offset_x, pos.y + offset_y), 
				new Vector(size.x - offset_x*3.f - button_width
						, size.y - offset_y - label_height), 
				new Vector(m_item_height*1.5f, m_item_height), 
				tex_manager));
		m_menu_items.add(m_game_label = new MenuItemStringMultiline(
				new Vector(m_game_list.pos().x
						, m_game_list.pos().y + m_game_list.size().y),
				new Vector(m_game_list.size().x, label_height),
				label_font_settings, " Choose a Game:", m_tex_manager));
		
		// Name
		m_menu_items.add(m_name_label = new MenuItemStringMultiline(
				new Vector(pos.x+size.x - offset_x - button_width
						, pos.y + size.y - label_height),
				new Vector(button_width, label_height),
				label_font_settings, " Your Name:", m_tex_manager));
		m_menu_items.add(m_name_button = new MenuItemKeyboard(
				new Vector(pos.x+size.x - offset_x - button_width
						, pos.y + size.y - button_height - label_height),
				new Vector(button_width, button_height),
				keyboard_font_settings, m_tex_manager, m_activity_context,
				"Please Enter your Nickname:"));

		//right Column
		m_menu_items.add(m_cancel_button = new MenuItemButton(
				new Vector(pos.x+size.x - offset_x - button_width, pos.y+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Cancel", tex_manager));
		m_menu_items.add(m_join_button = new MenuItemButton(
				new Vector(m_cancel_button.pos().x, 
						m_cancel_button.pos().y+button_height+offset_y),
				new Vector(button_width, button_height), 
				font_settings, "Join", tex_manager));
		
		Vector warning_size = new Vector(button_width, button_height*1.4f);
		Vector warning_pos = new Vector(m_join_button.pos().x, 
				m_join_button.pos().y+button_height+offset_y);
		m_older_version_warning = new MenuItemStringMultiline(
				warning_pos, warning_size, label_font_settings, 
				" Version mismatch:\n You're using an old version!\n Please update", m_tex_manager);
		m_newer_version_warning = new MenuItemStringMultiline(
				warning_pos, warning_size, label_font_settings, 
				" Version mismatch:\n You're using a newer version!\n ", m_tex_manager);

	}
	
	public void move(float dsec) {
		super.move(dsec);
		
		//update available games
		m_network_client.handleReceive();
		//remove old ones
		for(int i=0; i<m_game_list.itemCount(); ++i) {
			MenuItemString item = (MenuItemString)m_game_list.item(i);
			String server_id = (String)item.obj;
			boolean found=false;
			for(int k=0; k<m_network_client.serverIdCount(); ++k) {
				if(server_id.equals(m_network_client.serverId(k)))
					found = true;
			}
			if(!found)
				m_game_list.removeItem(i--);
		}
		//add new ones
		for(int k=0; k<m_network_client.serverIdCount(); ++k) {
			boolean found=false;
			for(int i=0; i<m_game_list.itemCount(); ++i) {
				MenuItemString item = (MenuItemString)m_game_list.item(i);
				String server_id = (String)item.obj;
				if(server_id.equals(m_network_client.serverId(k)))
					found = true;
			}
			if(!found) {
				String server_name = Networking.getNameFromServerId(m_network_client.serverId(k));
				Log.i(LOG_TAG, "Found a new server: "+server_name
						+ ", protocol version: "+Networking.getProtocolVersionFromServerId(
								m_network_client.serverId(k)));
				addListItem(Networking.toDisplayableName(server_name)
						, m_network_client.serverId(k));
			}
		}
		
		m_selected_item_protocol_version = selectedItemGetProtocolVersion();
		String name = Networking.fromDisplayableName(m_name_button.getString());
		m_join_button.enable(m_game_list.getSelectedItem() != null 
				&& m_selected_item_protocol_version == Networking.protocol_version
				&& name.length() > 0);
		if(name.length() > 0) m_settings.user_name = new String(name);
	}
	
	private void addListItem(String str_display, Object additional) {
		MenuItemString item = new MenuItemString(new Vector()
			, new Vector(m_game_list.size().x, m_item_height)
			, m_list_item_font_settings, str_display, m_tex_manager);
		item.obj = additional;
		m_game_list.addItem(item);
		
	}
	
	private int selectedItemGetProtocolVersion() {
		MenuItem sel_item = m_game_list.getSelectedItem();
		if(sel_item!=null) {
			Object obj = ((MenuItemString)sel_item).obj;
			if(obj != null) {
				String server_id = (String)obj;
				return Networking.getProtocolVersionFromServerId(server_id);
			}
		}
		return -1;
	}
	
	public void draw(RenderHelper renderer) {
		super.draw(renderer);
		
		if(m_selected_item_protocol_version!=-1) {
			if(m_selected_item_protocol_version < Networking.protocol_version) {
				m_newer_version_warning.draw(renderer);
			} else if(m_selected_item_protocol_version > Networking.protocol_version) {
				m_older_version_warning.draw(renderer);
			}
		}
	}

	@Override
	protected void onTouchDown(MenuItem item) {
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if(item == m_join_button) {
			if(!m_join_button.isDisabled()) {
				//get the server to join
				MenuItem sel_item = m_game_list.getSelectedItem();
				if(sel_item!=null) {
					Object obj = ((MenuItemString)sel_item).obj;
					if(obj != null) {
						String server_id = (String)obj;
						String user_name = Networking.fromDisplayableName(
								m_name_button.getString());
						m_settings.user_name = user_name;
						m_settings.is_host = false;
						m_network_client.setOwnName(user_name);
						m_network_client.connectToServer(server_id);
						m_ui_change = UIChange.WAIT_MENU;
					}
				}
			}
		}else if(item == m_cancel_button){
			m_ui_change = UIChange.MAIN_MENU;
		}
	}
	
	public void onActivate() {
		super.onActivate();
		m_network_client.startDiscovery();
		
		String name =  Networking.toDisplayableName(m_settings.user_name);
		if(name.length() > 0) m_name_button.setString(name);
	}
	
	public void onDeactivate() {
		super.onDeactivate();
		//clear the list
		while(m_game_list.itemCount() > 0) m_game_list.removeItem(0);
		m_network_client.stopDiscovery();
	}

}
