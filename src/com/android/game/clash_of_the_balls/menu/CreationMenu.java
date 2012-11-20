package com.android.game.clash_of_the_balls.menu;

import android.util.Log;

import com.android.game.clash_of_the_balls.Font2D;
import com.android.game.clash_of_the_balls.TextureManager;
import com.android.game.clash_of_the_balls.game.Vector;

public class CreationMenu extends GameMenuBase {
	
	private String LOG_TAG="debug";
	
	MenuItem m_test_button;
	
	public CreationMenu(Font2D item_font, MenuBackground background
			, float screen_width, float screen_height,TextureManager m_tex_manager) {
		super(item_font, background);
		
		Vector pos=new Vector(0.f, 0.f);
		Vector size=new Vector(screen_width, screen_height);
		
		if(m_background != null)
			m_background.getViewport(screen_width, screen_height, pos, size);
		
		
		//add menu items
		float button_width = size.x * 0.45f;
		float button_height=0.2f*button_width;
		float distanceButtons = screen_height/34.f;
		
		m_menu_items.add(m_test_button = new MenuItemButton(
				new Vector(pos.x+size.x/2.f, pos.y+size.y*3.f/5.f),
				new Vector(button_width, button_height), 
				m_item_font,
				m_tex_manager));
	}

	@Override
	protected void onTouchDown(MenuItem item) {
		// do nothing
	}

	@Override
	protected void onTouchUp(MenuItem item) {
		if(item == m_test_button) {
			//m_ui_change = UIChange.new_menu;
			Log.d(LOG_TAG,"I'm in CreationMenu");
		}else{
			Log.d(LOG_TAG,"No button pressed");
		}
	}

}
