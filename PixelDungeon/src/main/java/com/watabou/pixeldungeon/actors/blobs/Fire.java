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
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Fire extends Blob {
	
	@Override
	protected void evolve() {

		boolean[] flamable = Dungeon.level.flammable;
		
		int from = getWidth() + 1;
		int to   = getLength() - getWidth() - 1;
		
		boolean observe = false;
		
		for (int pos=from; pos < to; pos++) {
			
			int fire;
			
			if (cur[pos] > 0) {
				
				burn( pos );
				
				fire = cur[pos] - 1;
				if (fire <= 0 && flamable[pos]) {
					
					Dungeon.level.set( pos, Terrain.EMBERS );
					
					observe = true;
					GameScene.updateMap( pos );
					if (Dungeon.visible[pos]) {
						GameScene.discoverTile( pos);
					}
				}
				
			} else {
				
				if (flamable[pos] && (cur[pos-1] > 0 || cur[pos+1] > 0 || cur[pos-getWidth()] > 0 || cur[pos+getWidth()] > 0)) {
					fire = 4;
					burn( pos );
				} else {
					fire = 0;
				}

			}
			
			volume += (off[pos] = fire);

		}
		
		if (observe) {
			Dungeon.observe();
		}
	}
	
	public static void burn( int pos ) {
		Char ch = Actor.findChar( pos );
		if (ch != null) {
			Buff.affect( ch, Burning.class ).reignite( ch );
		}
		
		Heap heap = Dungeon.level.getHeap( pos );
		if (heap != null) {
			heap.burn();
		}

		LevelObject levelObject = Dungeon.level.getLevelObject(pos);
		if (levelObject != null) {
			levelObject.burn();
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( FlameParticle.FACTORY, 0.03f, 0 );
	}
	
	@Override
	public String tileDesc() {
		return Game.getVar(R.string.Fire_Info);
	}
}
