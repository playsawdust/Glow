/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glimmer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class ModalContextTree {
	protected Node root;
	
	public ModalContextTree() {
		this.root = new Node(new BareModalContext());
	}
	
	public ModalContextTree(ModalContext root) {
		this.root = new Node(Preconditions.checkNotNull(root));
	}
	
	protected Node findNode(ModalContext context) {
		if (root==null) return null;
		
		// Return the root if that's what we're asking for
		if (root.context.equals(context)) return root;
		
		/* Do a breadth-first search of the tree for the node holding the specified context */
		ArrayDeque<Node> searchStack = new ArrayDeque<Node>();
		for(Node node : root.children) {
			searchStack.addLast(node);
		}
		
		while(!searchStack.isEmpty()) {
			Node node = searchStack.removeFirst();
			if (node.context.equals(context)) return node;
			
			for(Node child : node.children) {
				searchStack.addLast(child);
			}
		}
		
		return null;
	}
	
	/**
	 * Opens a new modal context.
	 * 
	 * <p>The new context, and its peers, will be the only controls which can receive and respond to user input
	 * until all children are closed.
	 * 
	 * @param parent an existing ModalContext that is open in this tree
	 * @param toOpen a new context which will prevent interaction with the parent context while open.
	 */
	/* Additional shadowing notes.
	 * 
	 * When a control is shadowed (becomes an internal node instead of a leaf node) it can no longer receive input.
	 * 
	 *    A
	 *   / \
	 *  B   C
	 *     / \
	 *    D  (E)
	 *    
	 * When E is added:
	 * 
	 * -> E will shadow C and A, which are already shadowed by D.
	 * 
	 * -> D, E, and B will all be able to receive input because they are leaf nodes.
	 * 
	 * -> B will remain unshadowed because it is a leaf node, and so
	 *    even though it is not an ancestor of C, it isn't in a position to be
	 *    shadowed by D or E. Tree depth does not matter.
	 * 
	 * -> once D and E are *both* closed, along with any other children of C, C
	 *    will regain the ability to receive user input.
	 * 
	 * -> A cannot receive input until D, E, C, and B are all closed. As a root it can only receive input when there
	 *    are no open contexts.
	 */
	public void openContext(ModalContext parent, ModalContext toOpen) {
		Node parentNode = findNode(parent);
		if (parentNode==null) throw new IllegalArgumentException("The parent context is not a part of this ModalContextTree.");
		parentNode.addChild(toOpen);
	}
	
	/**
	 * Opens a ModalContext, parented to the root context.
	 * @param context the window or dialog to open
	 */
	public void openContext(ModalContext context) {
		root.addChild(context);
	}
	
	/**
	 * Closes this context, potentially freeing up user interactions with its parent.
	 * @param context the window or dialog to close.
	 */
	public void closeContext(ModalContext context) {
		if (root.context.equals(context)) {
			throw new IllegalArgumentException("Cannot close the root context.");
		}
		
		/* Do a breadth-first search of the tree for the node holding the specified context *and its parent node* */
		ArrayDeque<Node> searchStack = new ArrayDeque<Node>();
		for(Node node : root.children) {
			searchStack.addLast(node);
		}
		
		while(!searchStack.isEmpty()) {
			Node node = searchStack.removeFirst();
			for(int i=0; i<node.children.size(); i++) {
				Node child = node.children.get(i);
				if (child.context.equals(context)) {
					node.children.remove(i); // Avoiding CME by using indexes and bailing once we need to structurally modify
					return;
				} else {
					searchStack.addLast(child);
				}
			}
		}
		
		throw new IllegalArgumentException("The specified context is not part of this ModalContextTree");
	}
	
	private class Node {
		public final ModalContext context;
		public final List<Node> children = new ArrayList<>();
		
		public Node(ModalContext context) {
			this.context = context;
		}
		
		public void addChild(ModalContext child) {
			children.add(new Node(child));
		}
		
		public void removeChild(ModalContext child) {
			for(int i=0; i<children.size(); i++) {
				Node cur = children.get(i);
				if (cur.context.equals(child)) {
					children.remove(i);
					return;
				}
			}
		}
		
		public void removeChild(Node node) {
			children.remove(node);
		}
	}
	
	private class BareModalContext implements ModalContext {
		
	}
}
