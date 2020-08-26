package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.google.common.io.CharStreams;

import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.ProgressReport;
import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.model.Vertex;

public class OBJLoader implements ModelLoader {
	private static NumberFormat floatFormat = NumberFormat.getInstance();
	static {
		floatFormat.setMinimumFractionDigits(6);
		floatFormat.setMaximumFractionDigits(6);
	}
	private static NumberFormat normalFormat = NumberFormat.getInstance();
	static {
		normalFormat.setMinimumFractionDigits(4);
		normalFormat.setMaximumFractionDigits(4);
	}
	
	@Override
	public Model tryLoad(InputStream in, Consumer<Integer> progressConsumer) throws IOException {
		ProgressReport progress = new ProgressReport(progressConsumer);
		ArrayList<Vector3d> positions = new ArrayList<>();
		ArrayList<Vector3d> normals = new ArrayList<>();
		ArrayList<Vector2d> texcoords = new ArrayList<>();
		
		ArrayList<IndexedFace> indexedFaces = new ArrayList<>();
		
		Mesh mesh = new Mesh();
		mesh.setMaterial(Material.BLANK);
		
		List<String> file = CharStreams.readLines(new InputStreamReader(in, StandardCharsets.UTF_8));
		int totalSize = file.size()*2;
		boolean confirmObj = false;
		//long lastProgress = System.nanoTime() / NANOS_PER_MILLI;
		int lineNum = -1;
		try {
			for(String line : file) {
				lineNum++;
				if (line.startsWith("#")) continue;
				if (line.isBlank()) continue;
				
				String[] parts = line.split(" ");
				if (parts[0].equals("v")) {
					if (parts.length>=4) {
						Vector3d vertexPos = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
						positions.add(vertexPos);
						confirmObj = true;
					} else return null;
				} else if (parts[0].equals("vn")) {
					if (parts.length>=4) {
						Vector3d vertexNormal = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
						normals.add(vertexNormal);
						confirmObj = true;
					} else return null;
				} else if (parts[0].equals("vt")) {
					//if (parts.length==4) {
					//	Vector3d vertexTexcoord = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
					//	texcoords.add(vertexTexcoord);
					//	confirmObj = true;
					//} else
					if (parts.length==3) {
						Vector2d vertexTexcoord = new Vector2d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
						texcoords.add(vertexTexcoord);
						confirmObj = true;
					} else return null;
				} else if (parts[0].equals("f")) {
					IndexedFace.parse(line, indexedFaces);
					confirmObj = true;
				} else if (parts[0].equals("g")) {
					confirmObj = true;
				}
				
				if (confirmObj) {
					progress.report(lineNum, totalSize);
				}
			}
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			return null;
		}
		
		if (positions.size()==0 || indexedFaces.size()==0) return null; //This probably wasn't an obj file
		
		
		HashMap<Vector3d, ArrayList<Vertex>> generatedVertices = new HashMap<>();
		
		totalSize = indexedFaces.size()*2;
		lineNum = indexedFaces.size();
		for(IndexedFace face : indexedFaces) {
			if (face.a==null || face.b==null || face.c==null) {
				//System.out.println("INVALID FACE");
				continue;
			}
			//System.out.println("Deindexing ("+face.a.v+"|"+face.a.vt+"|"+face.a.vn+"), ("+face.b.v+"|"+face.b.vt+"|"+face.b.vn+"), "+face.c.v+"|"+face.c.vt+"|"+face.c.vn+")");
			Face deIndexed = deref(face, positions, texcoords, normals, generatedVertices);
			mesh.addFace(deIndexed);
			
			lineNum++;
			progress.report(lineNum, totalSize);
		}
		
		Model result = new Model();
		result.addMesh(mesh);
		
		progressConsumer.accept(100);
		System.out.println("Faces: "+mesh.getFaceCount()); //+" Vertices: "+mesh.getVertexCount());
		
		return result;
	}
	
	private static final Vector3d ZERO = new Vector3d(0,0,0);
	private static Vector3d deref(int pos, ArrayList<Vector3d> list) {
		if (pos<0 || pos>=list.size()) return ZERO;
		return list.get(pos);
	}
	
	private static Vector2d deref2(int pos, ArrayList<Vector2d> list) {
		if (pos<0 || pos>=list.size()) return new Vector2d(0,0);
		return list.get(pos);
	}
	
	private static Vertex deref(IndexedVertex v,  ArrayList<Vector3d> positions, ArrayList<Vector2d> textures, ArrayList<Vector3d> normals, HashMap<Vector3d, ArrayList<Vertex>> dedupe) {
		//Negative indices count backwards from the end of the list, where -1 is the last element
		if (v.v<0) v.v = positions.size()+v.v;
		if (v.vt<0) v.vt = textures.size()+v.vt;
		if (v.vn<0) v.vn = normals.size()+v.vn;
		
		Vector3d pos = deref(v.v-1, positions);
		Vector2d tex = deref2(v.vt-1, textures);
		Vector3d col = new Vector3d(1,1,1);
		Vector3d normal = deref(v.vn-1, normals);
		
		Vertex result = new Vertex(pos, tex);
		result.putMaterialAttribute(MaterialAttribute.DIFFUSE_COLOR, col);
		result.putMaterialAttribute(MaterialAttribute.NORMAL, normal);
		
		
		ArrayList<Vertex> dupesList = dedupe.get(pos);
		if (dupesList==null) {
			dupesList = new ArrayList<>();
			dedupe.put(pos, dupesList);
		}
		int index = dupesList.indexOf(result);
		if (index!=-1) {
			result = dupesList.get(index);
		} else {
			dupesList.add(result);
		}
		
		return result;
	}
	
	private static Face deref(IndexedFace f, ArrayList<Vector3d> positions, ArrayList<Vector2d> textures, ArrayList<Vector3d> normals, HashMap<Vector3d, ArrayList<Vertex>> dedupe) {
		Vertex a = deref(f.a, positions, textures, normals, dedupe);
		Vertex b = deref(f.b, positions, textures, normals, dedupe);
		Vertex c = deref(f.c, positions, textures, normals, dedupe);
		
		//Face result = new Face(a,b,c);
		//result.genFaceNormal();
		//return result;
		return new Face(a,b,c);
	}
	
	
	private static class IndexedVertex {
		public int v  = -1; //pos
		public int vt = -1; //tex
		public int vn = -1; //norm
		
		public static IndexedVertex of(String def) {
			IndexedVertex result = new IndexedVertex();
			String[] parts = def.split("/");
			if (!parts[0].trim().isEmpty()) {
				result.v = Integer.parseInt(parts[0].trim());
			}
			if (parts.length>1 && !parts[1].trim().isEmpty()) {
				result.vt = Integer.parseInt(parts[1].trim());
			}
			if (parts.length>2 && !parts[2].trim().isEmpty()) {
				result.vn = Integer.parseInt(parts[2].trim());
			}
			return result;
		}
		
		public String asString() {
			if (vt==-1) {
				return (v+1)+"//"+(vn+1);
			} else {
				return (v+1)+"/"+(vt+1)+"/"+(vn+1);
			}
		}
	}
	
	private static class IndexedFace {
		public IndexedVertex a;
		public IndexedVertex b;
		public IndexedVertex c;
		
		public static void parse(String def, Collection<IndexedFace> faces) {
			if (!def.startsWith("f ")) throw new IllegalArgumentException("Face declaration must start with f");
			String[] parts = def.split(" ");
			if (parts.length-1<3 || parts.length-1>4) throw new IllegalArgumentException("Face cannot have less than 3 or more than 4 vertices - this one has "+(parts.length-1));
			
			
			if (parts.length-1==3) {
				IndexedFace face = new IndexedFace();
				face.a = IndexedVertex.of(parts[1]);
				face.b = IndexedVertex.of(parts[2]);
				face.c = IndexedVertex.of(parts[3]);
				faces.add(face);
			} else {
				IndexedVertex a = IndexedVertex.of(parts[1]);
				IndexedVertex b = IndexedVertex.of(parts[2]);
				IndexedVertex c = IndexedVertex.of(parts[3]);
				IndexedVertex d = IndexedVertex.of(parts[4]);
				
				//TODO: No longer triangulate?
				IndexedFace face1 = new IndexedFace();
				face1.a = a;
				face1.b = b;
				face1.c = c;
				faces.add(face1);
				IndexedFace face2 = new IndexedFace();
				face2.a = a;
				face2.b = c;
				face2.c = d;
				faces.add(face2);
			}
		}
	}
	
	/** Write a model out to a stream in Wavefront OBJ format. The stream will not be closed. No .MTL will be written and no "usemtl" declaration will be made. */
	public static void save(Model model, OutputStream outputStream) {
		PrintWriter out = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
		
		ArrayList<Vector3dc> positions = new ArrayList<>();
		ArrayList<Vector3dc> normals = new ArrayList<>();
		ArrayList<Vector2dc> texcoords = new ArrayList<>();
		
		ArrayList<IndexedVertex> indexedVertices = new ArrayList<>();
		ArrayList<IndexedFace> indexedFaces = new ArrayList<>();
		
		for(Mesh m : model.meshes()) {
			for(Face face : m.faces()) {
				for(Vertex v : face) {
					IndexedVertex indexedVertex = new IndexedVertex();
					Vector3dc position = v.getMaterialAttribute(MaterialAttribute.POSITION);
					int posIndex = positions.indexOf(position);
					if (posIndex!=-1) {
						indexedVertex.v = posIndex;
					} else {
						indexedVertex.v = positions.size();
						positions.add(position);
					}
					
					Vector2dc uv = v.getMaterialAttribute(MaterialAttribute.UV);
					if (uv==null) uv = new Vector2d(0,0);
					int uvIndex = texcoords.indexOf(uv);
					if (uvIndex!=-1) {
						indexedVertex.vt = uvIndex;
					} else {
						indexedVertex.v = texcoords.size();
						texcoords.add(uv);
					}
					
					Vector3dc normal = v.getMaterialAttribute(MaterialAttribute.NORMAL);
					if (normal==null) normal = new Vector3d(0,0,0);
					int normalIndex = normals.indexOf(normal);
					if (normalIndex!=-1) {
						indexedVertex.vn = normalIndex;
					} else {
						indexedVertex.vn = normals.size();
						normals.add(normal);
					}
					
					indexedVertices.add(indexedVertex);
				}
				if (indexedVertices.size()>=3) { //TODO: Triangulate quads?
					IndexedFace indexedFace = new IndexedFace();
					indexedFace.a = indexedVertices.get(0);
					indexedFace.b = indexedVertices.get(1);
					indexedFace.c = indexedVertices.get(2);
					indexedFaces.add(indexedFace);
				}
				indexedVertices.clear();
			}
		}
		
		out.println("o GlowModel");
		out.println();
		for(Vector3dc pos : positions) {
			out.println("v "+floatFormat.format(pos.x())+" "+floatFormat.format(pos.y())+" "+floatFormat.format(pos.z()));
		}
		for(Vector2dc tex : texcoords) {
			out.println("vt "+floatFormat.format(tex.x())+" "+floatFormat.format(tex.y()));
		}
		for(Vector3dc norm : normals) {
			out.println("vn "+normalFormat.format(norm.x())+" "+normalFormat.format(norm.y())+" "+normalFormat.format(norm.z()));
		}
		out.println(); //TODO: Account for materials using usemtl
		for(IndexedFace face : indexedFaces) {
			out.println("f "+face.a.asString()+" "+face.b.asString()+" "+face.c.asString());
		}
		
		
		out.flush();
	}
}
