/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.CommonActions;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class Scroll extends Item implements UnknownItem {

	protected static final float TIME_TO_READ	= 1f;
	
	private static final Class<?>[] scrolls = {
		ScrollOfIdentify.class, 
		ScrollOfMagicMapping.class, 
		ScrollOfRecharging.class, 
		ScrollOfRemoveCurse.class, 
		ScrollOfTeleportation.class, 
		ScrollOfUpgrade.class, 
		ScrollOfChallenge.class,
		ScrollOfTerror.class,
		ScrollOfLullaby.class,
		ScrollOfWeaponUpgrade.class,
		ScrollOfPsionicBlast.class,
		ScrollOfMirrorImage.class,
		ScrollOfDomination.class,
		ScrollOfSummoning.class,
		ScrollOfCurse.class
	};

	private static final Class<?>[] inscribableScrolls = {
		ScrollOfIdentify.class, 
		ScrollOfMagicMapping.class, 
		ScrollOfRecharging.class, 
		ScrollOfRemoveCurse.class, 
		ScrollOfTeleportation.class, 
		ScrollOfUpgrade.class, 
		ScrollOfChallenge.class,
		ScrollOfTerror.class,
		ScrollOfLullaby.class,
		ScrollOfPsionicBlast.class,
		ScrollOfMirrorImage.class,
		ScrollOfDomination.class,
		ScrollOfSummoning.class,
		ScrollOfCurse.class
	};

	private static final Integer[] images = {
		ItemSpriteSheet.SCROLL_KAUNAN, 
		ItemSpriteSheet.SCROLL_SOWILO, 
		ItemSpriteSheet.SCROLL_LAGUZ, 
		ItemSpriteSheet.SCROLL_YNGVI, 
		ItemSpriteSheet.SCROLL_GYFU, 
		ItemSpriteSheet.SCROLL_RAIDO, 
		ItemSpriteSheet.SCROLL_ISAZ, 
		ItemSpriteSheet.SCROLL_MANNAZ, 
		ItemSpriteSheet.SCROLL_NAUDIZ, 
		ItemSpriteSheet.SCROLL_BERKANAN, 
		ItemSpriteSheet.SCROLL_ODAL, 
		ItemSpriteSheet.SCROLL_TIWAZ,
		ItemSpriteSheet.SCROLL_ANSUZ,
		ItemSpriteSheet.SCROLL_IWAZ,
		ItemSpriteSheet.SCROLL_ALGIZ,
		ItemSpriteSheet.SCROLL_DAGAZ};
	
	private static ItemStatusHandler<Scroll> handler;

	private String rune;
	
	@SuppressWarnings("unchecked")
	public static void initLabels() {
		handler = new ItemStatusHandler<>((Class<? extends Scroll>[]) scrolls, images);
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}
	
	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>((Class<? extends Scroll>[]) scrolls, images, bundle);
	}
	
	public Scroll() {
		stackable     = true;
		setDefaultAction(CommonActions.AC_READ);
		
		if (this instanceof BlankScroll){
			return;
		}

		image = handler.index( this );
		rune  = Game.getVars(R.array.Scroll_Runes)[ItemStatusHandler.indexByImage(image,images)];
	}
	
	static public Scroll createRandomScroll(){
		try {
			return (Scroll) Random.element(inscribableScrolls).newInstance();
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_READ );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( CommonActions.AC_READ )) {
			
			if (hero.hasBuff( Blindness.class )) {
				GLog.w( Game.getVar(R.string.Scroll_Blinded) );
			} else {
				setCurUser(hero);
				curItem = detach( hero.belongings.backpack );
				
				doRead();
			}
			
		} else {
		
			super.execute( hero, action );
			
		}
	}
	
	abstract protected void doRead();
	
	public boolean isKnown() {
		return handler.isKnown( this );
	}
	
	public void setKnown() {
		if (!isKnown()) {
			handler.know( this );
		}
		
		Badges.validateAllScrollsIdentified();
	}
	
	@Override
	public Item identify() {
		setKnown();
		return super.identify();
	}
	
	@Override
	public String name() {
		return isKnown() ? name : Utils.format(Game.getVar(R.string.Scroll_Name), rune);
	}
	
	@Override
	public String info() {
		return isKnown() ? desc() : Utils.format(Game.getVar(R.string.Scroll_Info), rune);
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return isKnown();
	}
	
	public static HashSet<Class<? extends Scroll>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Scroll>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler.known().size() == scrolls.length;
	}
	
	@Override
	public int price() {
		return 15 * quantity();
	}
	
	@Override
	public Item burn(int cell){
		return null;
	}
}
