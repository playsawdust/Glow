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
	public static ShaderProgram load(InputStream in) throws IOException {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			//System.out.println(doc.getNodeName());
			Enclosure<ShaderProgram> result = new Enclosure<>();
			
			foreach(doc, (node)->{
				if (node.getNodeName().equals("root")) {
					System.out.println("root {");
					foreach(node, (rootChild)->{
						if (rootChild.getNodeName().equals("pass")) {
							
							Enclosure<String> vertexShader = new Enclosure<>();
							Enclosure<String> fragmentShader = new Enclosure<>();
							
							String passName = rootChild.getAttributes().getNamedItem("name").getNodeValue();
							System.out.println("    pass '"+passName+"' {");
							foreach(rootChild, (passChild)->{
								
								
								if (passChild.getNodeName().equals("shader")) {
									String stageName = passChild.getAttributes().getNamedItem("stage").getNodeValue();
									
									System.out.println("        shader '"+stageName+"' {");
									foreach(passChild, (shaderChild)->{
										if (shaderChild.getNodeName().equals("source")) {
											//TODO: Don't assume GLSL
											if (stageName.equalsIgnoreCase("vertex")) {
												vertexShader.set(shaderChild.getTextContent());
												System.out.println("VERTEX TextContent: "+vertexShader.value());
											} else if (stageName.equalsIgnoreCase("fragment")) {
												fragmentShader.set(shaderChild.getTextContent());
												System.out.println("FRAGMENT TextContent: "+fragmentShader.value());
											}
										}
									});
									
									System.out.println("        }");
								}
								
								if (vertexShader.isSet() && fragmentShader.isSet()) {
									if (!result.isSet()) {
										try {
											System.out.println("CREATING SHADER");
											result.set(new ShaderProgram(vertexShader.value(), fragmentShader.value()));
										} catch (ShaderError e) {
											e.printStackTrace();
										}
									}
								}
								
							});
							System.out.println("    }");
						}
					});
					System.out.println("}");
				}
				
				
			});
			
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
