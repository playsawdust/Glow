/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl.shader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.AbstractIterator;

public class ShaderIO {
	
	public static HashMap<String, ShaderPass> load(InputStream in) throws IOException {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			//System.out.println(doc.getNodeName());
			Enclosure<ShaderProgram> result = new Enclosure<>();
			Enclosure<ShaderException> error = new Enclosure<>();
			
			HashMap<String, ShaderPass> passes = new HashMap<>();
			Nodelet document = new Nodelet(doc);
			for(Nodelet docNode : document) {
				if (docNode.getName().equals("root")) {
					Nodelet rootNode = docNode;
					
					for(Nodelet rootChild : rootNode) {
						if (rootChild.getName().equals("pass")) {
							Nodelet passNode = rootChild;
							
							String passName = passNode.getAttribute("name");
							String vertexShader = null;
							String fragmentShader = null;
							
							for(Nodelet passChild : passNode) {
								if (passChild.getName().equals("shader")) {
									Nodelet shaderNode = passChild;
									
									String stageName = passChild.getAttribute("stage");
									
									for(Nodelet shaderChild : shaderNode) {
										if (shaderChild.getName().equals("source")) {
											Nodelet sourceNode = shaderChild;
											String sourceLanguage = sourceNode.getAttribute("language");
											if (sourceLanguage.equals("GLSL")) {
												if (stageName.equalsIgnoreCase("vertex")) {
													vertexShader = sourceNode.innerText();
												} else if (stageName.equalsIgnoreCase("fragment")) {
													fragmentShader = sourceNode.innerText();
												}
											} else {
												throw new IOException("Unknown shader language '"+sourceLanguage+"'");
											}
											
										}
									}
								}
							}
							
							if (vertexShader!=null && fragmentShader!=null) {
								ShaderPass pass = new ShaderPass();
								pass.vertex = vertexShader;
								pass.fragment = fragmentShader;
								pass.name = passName;
								passes.put(passName, pass);
							}
						}
					}
				}
			}
			
			return passes;
		} catch (SAXException | ParserConfigurationException ex) {
			throw new IOException(ex);
		}
	}
	
	
	
	public static ShaderProgram loadOld(InputStream in) throws IOException, ShaderException {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			//System.out.println(doc.getNodeName());
			Enclosure<ShaderProgram> result = new Enclosure<>();
			Enclosure<ShaderException> error = new Enclosure<>();
			
			foreach(doc, (node)->{
				if (node.getNodeName().equals("root")) {
					foreach(node, (rootChild)->{
						if (rootChild.getNodeName().equals("pass")) {
							
							Enclosure<String> vertexShader = new Enclosure<>();
							Enclosure<String> fragmentShader = new Enclosure<>();
							
							String passName = rootChild.getAttributes().getNamedItem("name").getNodeValue();
							foreach(rootChild, (passChild)->{
								
								
								if (passChild.getNodeName().equals("shader")) {
									String stageName = passChild.getAttributes().getNamedItem("stage").getNodeValue();
									
									foreach(passChild, (shaderChild)->{
										if (shaderChild.getNodeName().equals("source")) {
											//TODO: Don't assume GLSL
											if (stageName.equalsIgnoreCase("vertex")) {
												vertexShader.set(shaderChild.getTextContent());
											} else if (stageName.equalsIgnoreCase("fragment")) {
												fragmentShader.set(shaderChild.getTextContent());
											}
										}
									});
								}
								
								if (vertexShader.isSet() && fragmentShader.isSet()) {
									if (!result.isSet()) {
										try {
											result.set(new ShaderProgram(vertexShader.value(), fragmentShader.value()));
										} catch (ShaderException e) {
											error.set(e);
											return;
										}
									}
								}
								
							});
						}
					});
				}
				
				
			});
			if (error.isSet()) throw error.value();
			return result.value();
		} catch (SAXException | ParserConfigurationException ex) {
			throw new IOException(ex);
		}
	}
	
	public static void foreach(Node n, Consumer<Node> f) {
		NodeList nodes = n.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++) {
			f.accept(nodes.item(i));
		}
	}
	
	private static class Enclosure<T> {
		private T value = null;
		
		public void set(T s) {
			this.value = s;
		}
		
		public boolean isSet() {
			return (value!=null);
		}
		
		public T value() {
			return value;
		}
	}
	
	public static class Nodelet implements Iterable<Nodelet> {
		private Node src;
		public Nodelet(Node src) { this.src = src; }
		
		public String getName() {
			return src.getNodeName();
		}
		
		public @NonNull String getAttribute(String attribute) {
			Node attrNode = src.getAttributes().getNamedItem(attribute);
			if (attrNode==null) return "";
			return attrNode.getNodeValue();
		}
		
		public String innerText() {
			return src.getTextContent();
		}
		
		@Override
		public Iterator<Nodelet> iterator() {
			NodeList nodes = src.getChildNodes();
			
			return new AbstractIterator<Nodelet>() {
				private int i = 0;
				@Override
				protected Nodelet computeNext() {
					if (i>=nodes.getLength()) return endOfData();
					
					Nodelet result = new Nodelet(nodes.item(i));
					i++;
					return result;
				}
				
			};
		}
		
	}
	
	public static class ShaderPass {
		private String name;
		private String vertex;
		private String fragment;
		
		public String getVertex() { return vertex; }
		public String getFragment() { return fragment; }
		
		public ShaderProgram compile() throws ShaderException {
			return new ShaderProgram(vertex, fragment);
		}
	}
}
