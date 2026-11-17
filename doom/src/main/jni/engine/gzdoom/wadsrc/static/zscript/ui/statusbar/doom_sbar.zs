/*
** doom_sbar.zs
**
**
**
**---------------------------------------------------------------------------
**
** Copyright 1993-1996 id Software
** Copyright 1999-2016 Marisa Heit
** Copyright 2006-2016 Christoph Oelckers
** Copyright 2017-2025 GZDoom Maintainers and Contributors
** Copyright 2025-2026 UZDoom Maintainers and Contributors
**
** SPDX-License-Identifier: GPL-3.0-or-later
**
**---------------------------------------------------------------------------
**
*/

class DoomStatusBar : BaseStatusBar
{
	HUDFont mHUDFont;
	HUDFont mIndexFont;
	HUDFont mAmountFont;
	InventoryBarState diparms;
	

	override void Init()
	{
		Super.Init();
		SetSize(32, 320, 200);

		// Create the font used for the fullscreen HUD
		Font fnt = "HUDFONT_DOOM";
		mHUDFont = HUDFont.Create(fnt, fnt.GetCharWidth("0"), Mono_CellLeft, 1, 1);
		fnt = "INDEXFONT_DOOM";
		mIndexFont = HUDFont.Create(fnt, fnt.GetCharWidth("0"), Mono_CellLeft);
		mAmountFont = HUDFont.Create("INDEXFONT");
		diparms = InventoryBarState.Create();
	}

	override void Draw (int state, double TicFrac)
	{
		Super.Draw (state, TicFrac);

		if (state == HUD_StatusBar)
		{
			BeginStatusBar();
			DrawMainBar (TicFrac);
		}
		else if (state == HUD_Fullscreen)
		{
			BeginHUD();
			DrawFullScreenStuff ();
		}
	}

	protected void DrawMainBar (double TicFrac)
	{
		// Use the original Doom status-bar art, face and weapon indicators: the
		// 25th-anniversary look is rooted in the classic steel panel, not a generic overlay.
		DrawImage("STBAR", (0, 168), DI_ITEM_OFFSETS);
		DrawImage("STTPRCNT", (90, 171), DI_ITEM_OFFSETS);
		DrawImage("STTPRCNT", (221, 171), DI_ITEM_OFFSETS);
		
		Inventory a1 = GetCurrentAmmo();
		if (a1 != null) DrawString(mHUDFont, FormatNumber(a1.Amount, 3), (44, 171), DI_TEXT_ALIGN_RIGHT|DI_NOSHADOW);
		DrawString(mHUDFont, FormatNumber(CPlayer.health, 3), (90, 171), DI_TEXT_ALIGN_RIGHT|DI_NOSHADOW);
		DrawString(mHUDFont, FormatNumber(GetArmorAmount(), 3), (221, 171), DI_TEXT_ALIGN_RIGHT|DI_NOSHADOW);

		DrawBarKeys();
		DrawBarAmmo();
		
		if (deathmatch || teamplay)
		{
			DrawString(mHUDFont, FormatNumber(CPlayer.FragCount, 3), (138, 171), DI_TEXT_ALIGN_RIGHT);
		}
		else
		{
			DrawBarWeapons();
		}
		
		if (multiplayer)
		{
			DrawImage("STFBANY", (143, 168), DI_ITEM_OFFSETS|DI_TRANSLATABLE);
		}
		
		if (CPlayer.mo.InvSel != null && !Level.NoInventoryBar)
		{
			DrawInventoryIcon(CPlayer.mo.InvSel, (160, 198), DI_DIMDEPLETED);
			if (CPlayer.mo.InvSel.Amount > 1)
			{
				DrawString(mAmountFont, FormatNumber(CPlayer.mo.InvSel.Amount), (175, 198-mIndexFont.mFont.GetHeight()), DI_TEXT_ALIGN_RIGHT, Font.CR_GOLD);
			}
		}
		else
		{
			DrawTexture(GetMugShot(5), (143, 168), DI_ITEM_OFFSETS);
		}
		if (isInventoryBarVisible())
		{
			DrawInventoryBar(diparms, (48, 169), 7, DI_ITEM_LEFT_TOP);
		}
		
	}
	
	protected virtual void DrawBarKeys()
	{
		bool locks[6];
		String image;
		for(int i = 0; i < 6; i++) locks[i] = CPlayer.mo.CheckKeys(i + 1, false, true);
		// key 1
		if (locks[1] && locks[4]) image = "STKEYS6";
		else if (locks[1]) image = "STKEYS0";
		else if (locks[4]) image = "STKEYS3";
		DrawImage(image, (239, 171), DI_ITEM_OFFSETS);
		// key 2
		if (locks[2] && locks[5]) image = "STKEYS7";
		else if (locks[2]) image = "STKEYS1";
		else if (locks[5]) image = "STKEYS4";
		else image = "";
		DrawImage(image, (239, 181), DI_ITEM_OFFSETS);
		// key 3
		if (locks[0] && locks[3]) image = "STKEYS8";
		else if (locks[0]) image = "STKEYS2";
		else if (locks[3]) image = "STKEYS5";
		else image = "";
		DrawImage(image, (239, 191), DI_ITEM_OFFSETS);
	}
	
	protected virtual void DrawBarAmmo()
	{
		int amt1, maxamt;
		[amt1, maxamt] = GetAmount("Clip");
		DrawString(mIndexFont, FormatNumber(amt1, 3), (288, 173), DI_TEXT_ALIGN_RIGHT);
		DrawString(mIndexFont, FormatNumber(maxamt, 3), (314, 173), DI_TEXT_ALIGN_RIGHT);
		
		[amt1, maxamt] = GetAmount("Shell");
		DrawString(mIndexFont, FormatNumber(amt1, 3), (288, 179), DI_TEXT_ALIGN_RIGHT);
		DrawString(mIndexFont, FormatNumber(maxamt, 3), (314, 179), DI_TEXT_ALIGN_RIGHT);
		
		[amt1, maxamt] = GetAmount("RocketAmmo");
		DrawString(mIndexFont, FormatNumber(amt1, 3), (288, 185), DI_TEXT_ALIGN_RIGHT);
		DrawString(mIndexFont, FormatNumber(maxamt, 3), (314, 185), DI_TEXT_ALIGN_RIGHT);
		
		[amt1, maxamt] = GetAmount("Cell");
		DrawString(mIndexFont, FormatNumber(amt1, 3), (288, 191), DI_TEXT_ALIGN_RIGHT);
		DrawString(mIndexFont, FormatNumber(maxamt, 3), (314, 191), DI_TEXT_ALIGN_RIGHT);
	}
	
	protected virtual void DrawBarWeapons()
	{
		DrawImage("STARMS", (104, 168), DI_ITEM_OFFSETS);
		DrawImage(CPlayer.HasWeaponsInSlot(2)? "STYSNUM2" : "STGNUM2", (111, 172), DI_ITEM_OFFSETS);
		DrawImage(CPlayer.HasWeaponsInSlot(3)? "STYSNUM3" : "STGNUM3", (123, 172), DI_ITEM_OFFSETS);
		DrawImage(CPlayer.HasWeaponsInSlot(4)? "STYSNUM4" : "STGNUM4", (135, 172), DI_ITEM_OFFSETS);
		DrawImage(CPlayer.HasWeaponsInSlot(5)? "STYSNUM5" : "STGNUM5", (111, 182), DI_ITEM_OFFSETS);
		DrawImage(CPlayer.HasWeaponsInSlot(6)? "STYSNUM6" : "STGNUM6", (123, 182), DI_ITEM_OFFSETS);
		DrawImage(CPlayer.HasWeaponsInSlot(7)? "STYSNUM7" : "STGNUM7", (135, 182), DI_ITEM_OFFSETS);
	}

	protected void DrawFullScreenStuff ()
	{
		DrawCompactFullscreenBar();
		if (deathmatch)
		{
			DrawString(mHUDFont, FormatNumber(CPlayer.FragCount, 3),
				(HorizontalResolution * 0.92, -9), DI_SCREEN_LEFT_BOTTOM|DI_NOSHADOW, Font.CR_GOLD);
		}
		else
		{
			DrawFullscreenKeys();
		}

		if (isInventoryBarVisible())
		{
			// Keep the inventory selector above the custom bottom HUD strip.
			DrawInventoryBar(diparms, (0, -36), 7, DI_SCREEN_CENTER_BOTTOM, HX_SHADOW);
		}
	}

	// Fullscreen games hide the classic status bar, so draw the same live
	// information as a screen-edge anchored overlay instead.
	protected void DrawCompactFullscreenBar()
	{
		double width = HorizontalResolution;
		double healthX = width * 0.04;
		double armorX = width * 0.28;
		double weaponX = width * 0.52;
		double ammoX = width * 0.72;
		double meterWidth = width * 0.18;
		int armor = GetArmorAmount();
		Ammo a1, a2;
		int ammoCount1, ammoCount2;
		[a1, a2, ammoCount1, ammoCount2] = GetCurrentAmmo();
		int hpWidth = int(clamp(CPlayer.health, 0, 100) * meterWidth / 100);
		int armorWidth = int(clamp(armor, 0, 200) * meterWidth / 200);
		int anchor = DI_SCREEN_LEFT_BOTTOM;

		// A compact steel-and-crimson edge HUD for widescreen play. Keep it
		// shallow and translucent so the 3D view remains the visual priority.
		Fill(color(220, 8, 10, 13), 0, -32, width, 32, anchor);
		Fill(color(255, 166, 28, 20), 0, -32, width, 1, anchor);
		Fill(color(255, 108, 112, 116), 0, -31, width, 1, anchor);
		Fill(color(216, 31, 34, 38), width * 0.025, -28, width * 0.21, 23, anchor);
		Fill(color(216, 31, 34, 38), width * 0.265, -28, width * 0.21, 23, anchor);
		Fill(color(216, 31, 34, 38), width * 0.505, -28, width * 0.18, 23, anchor);
		Fill(color(216, 31, 34, 38), width * 0.715, -28, width * 0.25, 23, anchor);
		Fill(color(255, 104, 108, 112), width * 0.025, -28, width * 0.21, 1, anchor);
		Fill(color(255, 104, 108, 112), width * 0.265, -28, width * 0.21, 1, anchor);
		Fill(color(255, 104, 108, 112), width * 0.505, -28, width * 0.18, 1, anchor);
		Fill(color(255, 104, 108, 112), width * 0.715, -28, width * 0.25, 1, anchor);
		Fill(color(255, 70, 74, 78), width * 0.25, -25, 1, 18, anchor);
		Fill(color(255, 70, 74, 78), width * 0.49, -25, 1, 18, anchor);
		Fill(color(255, 70, 74, 78), width * 0.69, -25, 1, 18, anchor);

		DrawString(mIndexFont, "HEALTH", (healthX, -22), anchor|DI_NOSHADOW);
		DrawString(mHUDFont, FormatNumber(CPlayer.health, 3), (healthX, -9),
			anchor|DI_NOSHADOW, Font.CR_RED);
		Fill(color(255, 42, 45, 48), healthX, -4, meterWidth, 2, anchor);
		if (hpWidth > 0) Fill(color(255, 190, 34, 24), healthX, -4, hpWidth, 2, anchor);

		DrawString(mIndexFont, "ARMOR", (armorX, -22), anchor|DI_NOSHADOW);
		DrawString(mHUDFont, FormatNumber(armor, 3), (armorX, -9),
			anchor|DI_NOSHADOW, Font.CR_RED);
		Fill(color(255, 42, 45, 48), armorX, -4, meterWidth, 2, anchor);
		if (armorWidth > 0) Fill(color(255, 154, 158, 160), armorX, -4, armorWidth, 2, anchor);

		DrawString(mIndexFont, "WEAPON", (weaponX, -22), anchor|DI_NOSHADOW);
		if (CPlayer.ReadyWeapon != null)
		{
			DrawInventoryIcon(CPlayer.ReadyWeapon, (weaponX + width * 0.055, -8),
				anchor|DI_DIMDEPLETED, 1.0, (24, 18));
		}

		DrawString(mIndexFont, "AMMO", (ammoX, -22), anchor|DI_NOSHADOW);
		if (a1 != null)
		{
			DrawString(mHUDFont, FormatNumber(ammoCount1, 3), (ammoX, -9),
				anchor|DI_NOSHADOW, Font.CR_RED);
			DrawInventoryIcon(a1, (ammoX + width * 0.075, -8),
				anchor|DI_DIMDEPLETED, 1.0, (16, 16));
		}
		else
		{
			DrawString(mHUDFont, "---", (ammoX, -9), anchor|DI_NOSHADOW, Font.CR_RED);
		}
	}

	protected virtual void DrawFullscreenKeys()
	{
		// Draw the keys. This does not use a special draw function like SBARINFO because the specifics will be different for each mod
		// so it's easier to copy or reimplement the following piece of code instead of trying to write a complicated all-encompassing solution.
		Vector2 keypos = (-10, 2);
		int rowc = 0;
		double roww = 0;
		for(let i = CPlayer.mo.Inv; i != null; i = i.Inv)
		{
			if (i is "Key" && i.Icon.IsValid())
			{
				DrawTexture(i.Icon, keypos, DI_SCREEN_RIGHT_TOP|DI_ITEM_LEFT_TOP);
				Vector2 size = TexMan.GetScaledSize(i.Icon);
				keypos.Y += size.Y + 2;
				roww = max(roww, size.X);
				if (++rowc == 3)
				{
					keypos.Y = 2;
					keypos.X -= roww + 2;
					roww = 0;
					rowc = 0;
				}
			}
		}
	}
}
