package com.playsawdust.chipper.glow.gl.shader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ShaderIO {
	public static ShaderProgram load(InputStream in) throws IOException, ShaderError {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			//System.out.println(doc.getNodeName());
			Enclosure<ShaderProgram> result = new Enclosure<>();
			Enclosure<ShaderError> error = new Enclosure<>();
			
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
										} catch (ShaderError e) {
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
}
