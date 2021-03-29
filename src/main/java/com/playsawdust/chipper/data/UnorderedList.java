/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.data;

import java.util.ArrayList;

/**
 * An ArrayList which performs removal in O(1) by swapping the removed element with the end of the list.
 * This means that UnorderedList does not maintain stable ordering over removals, hence the name.
 */
public class UnorderedList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 2562614395348486327L;
	
	@Override
	public T remove(int index) {
		T replacement = super.remove(this.size()-1);
		return this.set(index, replacement);
	}
	
	@Override
	public boolean remove(Object o) {
		int i = super.indexOf(o);
		if (i>=0) {
			remove(i);
			return true;
		} else {
			return false;
		}
	}
}
