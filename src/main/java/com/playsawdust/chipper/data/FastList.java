/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Does not (quite) conform to the List interface (is missing "add(int, T)"). FastList keeps elements in insertion order unless the collection is
 * structurally modified.
 */
public class FastList<T> implements Collection<T> {
	
	/**
	 * Results in a growth curve of [ 10, 15, 22, 33, 49, 73, 109, 163, 244, 366 ], so if you initialize a list naively and fill
	 * it with 256 elements, you're doing 1 array allocation and 9 reallocs. We may need to increase the growth factor, but I
	 * want to benchmark it first.
	 */
	
	private static final int DEFAULT_CAPACITY = 10;
	
	protected Object[] data;
	protected int size = 0;
	
	public FastList() {
		this(DEFAULT_CAPACITY);
	}
	
	public FastList(List<T> other) {
		this.data = other.toArray(); //Will usually result in a blazing-fast Arrays.copyOf from other's backing array
		this.size = data.length;
	}
	
	public FastList(Iterable<T> other) {
		//Slower than we'd like, but we can completely skip going up the growth factor curve
		this(DEFAULT_CAPACITY);
		for(T t : other) {
			this.add(t);
		}
	}
	
	public FastList(int initialCapacity) {
		if (initialCapacity<=0) throw new IllegalArgumentException("List capacity must be at least 1");
		data = new Object[initialCapacity];
	}
	
	public void ensureCapacity(int requiredCapacity) {
		if (requiredCapacity<0) throw new OutOfMemoryError(); //requiredCapacity is probably an overflow
		if (data.length >= requiredCapacity) return; //We already have capacity for this
		
		final int expectedGrowSize = data.length + (data.length >> 1); //increased to 1.5x what it was
		if (expectedGrowSize < requiredCapacity) { //Also true if expectedGrowSize is an overflow but requiredCapacity is not
			//Resize to exactly the amount requested
			data = Arrays.copyOf(data, requiredCapacity);
		} else {
			//Resize to the growth factor
			data = Arrays.copyOf(data, expectedGrowSize);
		}
	}
	
	public T remove(int index) {
		Objects.checkIndex(index, size);
		
		@SuppressWarnings("unchecked")
		final T result = (T) data[index];
		
		data[index] = data[size-1];
		size--;
		data[size] = null;
		
		return result;
	}
	
	public T set(int index, T t) {
		Objects.checkIndex(index, size);
		@SuppressWarnings("unchecked")
		final T result = (T) data[index];
		data[index] = t;
		return result;
	}
	
	public int indexOf(Object t) {
		for(int i=0; i<size; i++) {
			if (Objects.equals(t, data[i])) return i;
		}
		return -1;
	}
	
	
	//implements Collection<T> {
		
		@Override
		public boolean add(T t) {
			ensureCapacity(size+1);
			data[size] = t;
			size++;
			return true;
		}
		
		@Override
		public boolean addAll(Collection<? extends T> other) {
			Object[] ts = (Object[]) other.toArray();
			if (ts.length==0) return false;
			
			ensureCapacity(this.size+ts.length);
			System.arraycopy(ts, 0, this.data, this.size, ts.length);
			this.size += ts.length;
			
			return true;
		}
		
		@Override
		public void clear() {
			Arrays.fill(data, null);
			size = 0;
		}
		
		@Override
		public boolean contains(Object obj) {
			return indexOf(obj) >= 0;
		}
		
		@Override
		public boolean containsAll(Collection<?> collection) {
			for(Object o : collection) {
				if (!contains(o)) return false;
			}
			return true;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			
			if (!(obj instanceof FastList)) return false; //also covers null. Note that this is not a full List and isn't equals-compatible with other Lists.
			
			FastList<?> other = (FastList<?>) obj;
			
			if (this.size!=other.size) return false;
			
			for(int i=0; i<this.size; i++) {
				if (!Objects.equals(this.data[i], other.data[i])) return false;
			}
			
			return true;
		}
		
		@Override
		public int hashCode() {
			int result = 1;
			
			for(int i=0; i<this.size; i++) {
				Object cur = this.data[i];
				int curHash = (cur==null) ? 0 : cur.hashCode();
				result = result * 31 + curHash;
			}
			
			return result;
		}
		
		@Override
		public boolean isEmpty() {
			return size==0;
		}
		
		@Override
		public Iterator<T> iterator() {
			return new FastListIterator<>(this);
		}
		
		@Override
		public boolean remove(Object t) {
			int index = indexOf(t);
			if (index>=0) {
				remove(index);
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public boolean removeAll(Collection<?> collection) {
			boolean result = false;
			for(Object o : collection) {
				result |= remove(o);
			}
			
			return result;
		}
		
		@Override
		public boolean retainAll(Collection<?> collection) {
			boolean result = false;
			
			for(int i=0; i<size; i++) {
				if (!collection.contains(i)) {
					result = true;
					remove(i);
					i--; //test this location again, since it has changed.
				}
			}
			
			return result;
		}
		
		@Override
		public int size() {
			return size;
		}
		
		@Override
		public Object[] toArray() {
			return Arrays.copyOf(data, size);
		}
		
		@Override
		public <U> U[] toArray(U[] us) {
			if (us.length<size) {
				us = Arrays.copyOf(us, size);
			}
			
			System.arraycopy(data, 0, us, 0, size);
			
			return us;
		};
		
	//}
	
	
	public static class FastListIterator<T> implements Iterator<T> {
		private final FastList<T> list;
		private final int initialFence;
		private int lastReturned = -1;
		
		public FastListIterator(FastList<T> list) {
			this.list = list;
			this.initialFence = list.size;
		}
		
		@Override
		public boolean hasNext() {
			return lastReturned < initialFence-1;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			if (list.size!=initialFence) throw new ConcurrentModificationException("This FastList cannot be structurally modified by two threads at once");
			
			lastReturned++;
			Objects.checkIndex(lastReturned, initialFence);
			
			return (T) list.data[lastReturned];
		}
		
	}
}
