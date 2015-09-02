package it.isislab.dmason.sim.field.network.kway.algo.jabeja;

import edu.cmu.graphchi.datablocks.BytesToValueConverter;

/**
 * Copyright [2012] [Aapo Kyrola, Guy Blelloch, Carlos Guestrin / Carnegie Mellon University]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Converts byte[4] ot Integer and vice versa.
 * @author  Aapo Kyrola
 */
public class IntArrayConverter implements  BytesToValueConverter<Integer[]> {
	int size;
	
	public IntArrayConverter(int size) {
		this.size = size;
	}
	
	public int sizeOf() {
		return (4 * size);
	}
	
	public void setValue(byte[] array, Integer[] val) {
		for (int i = 0; i < size; i++) {
	        array[i*4] = (byte) ((val[i]) & 0xff);
	        array[i*4 + 1] = (byte) ((val[i] >>> 8) & 0xff);
	        array[i*4 + 2] = (byte) ((val[i] >>> 16) & 0xff);
	        array[i*4 + 3] = (byte) ((val[i] >>> 24) & 0xff);
		}
	}
	
	
	public Integer[] getValue(byte[] array) {
		Integer[] val = new Integer[size];
		for (int i = 0; i < size; i++) {
			val[i] = ((array[i * 4 + 3]  & 0xff) << 24) + ((array[i * 4 + 2] & 0xff) << 16) + ((array[i * 4 + 1] & 0xff) << 8) + (array[i * 4] & 0xff);
		}
		return val;
	}
}