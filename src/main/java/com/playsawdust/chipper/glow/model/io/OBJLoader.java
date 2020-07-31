package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joml.Vector2d;
import org.joml.Vector3d;

import com.google.common.io.CharStreams;

import com.playsawdust.chipper.glow.model.EditableMesh;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Model;

public class OBJLoader implements ModelLoader {

	@Override
	public Model tryLoad(InputStream in) throws IOException {
		ArrayList<Vector3d> positions = new ArrayList<>();
		ArrayList<Vector3d> normals = new ArrayList<>();
		ArrayList<Vector2d> texcoords = new ArrayList<>();
		
		ArrayList<IndexedFace> indexedFaces = new ArrayList<>();
		
		EditableMesh mesh = new EditableMesh();
		//ArrayList<EditableMesh.Edge> edges = new ArrayList<>();
		//ArrayList<EditableMesh.Face> faces = new ArrayList<>(); 
		
		List<String> file = CharStreams.readLines(new InputStreamReader(in, StandardCharsets.UTF_8));
		
		for(String line : file) {
			if (line.startsWith("#")) continue;
			if (line.trim().isEmpty()) continue;
			
			String[] parts = line.split(" ");
			if (parts[0].equals("v")) {
				if (parts.length>=4) {
					Vector3d vertexPos = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
					positions.add(vertexPos);
				}
			} else if (parts[0].equals("vn")) {
				if (parts.length>=4) {
					Vector3d vertexNormal = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
					normals.add(vertexNormal);
				}
			} else if (parts[0].equals("vt")) {
				if (parts.length==4) {
					Vector3d vertexTexcoord = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
					normals.add(vertexTexcoord);
				} else if (parts.length==3) {
					Vector2d vertexTexcoord = new Vector2d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
					texcoords.add(vertexTexcoord);
				}
			} else if (parts[0].equals("f")) {
				IndexedFace.parse(line, indexedFaces);
			}
			
		}
		
		System.out.println("Loaded "+positions+" vertex locations and "+indexedFaces.size()+" faces.");
		
		for(IndexedFace face : indexedFaces) {
			if (face.a==null || face.b==null || face.c==null) {
				System.out.println("INVALID FACE");
				continue;
			}
			//System.out.println("Deindexing ("+face.a.v+"|"+face.a.vt+"|"+face.a.vn+"), ("+face.b.v+"|"+face.b.vt+"|"+face.b.vn+"), "+face.c.v+"|"+face.c.vt+"|"+face.c.vn+")");
			EditableMesh.Face deIndexed = deref(face, positions, texcoords, normals);
			mesh.addFace(deIndexed);
		}
		
		//Generate data for vertexes which don't have it
		//for(EditableMesh.Face face : mesh.faces()) {
			//TODO: Log and remove degenerate triangles?
			//if (face.a().position().equals(face.b().position())) System.out.println("A-B Degenerate");
			//if (face.b().position().equals(face.c().position())) System.out.println("B-C Degenerate");
			//if (face.a().position().equals(face.c().position())) System.out.println("A-C Degenerate");
			
			
			//TODO: Calculate missing normals from face windings?
			//if (face.a().normal().length()==0 || face.b().normal().length()==0 || face.c().normal().length()==0) {
			//	face.genFaceNormal();
			//}
			
			//TODO: Project UVs if missing?
		//}
		
		Model result = new Model();
		result.addMesh(mesh);
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
	
	private static EditableMesh.Vertex deref(IndexedVertex v,  ArrayList<Vector3d> positions, ArrayList<Vector2d> textures, ArrayList<Vector3d> normals) {
		Vector3d pos = deref(v.v-1, positions);
		Vector2d tex = deref2(v.vt-1, textures);
		Vector3d col = new Vector3d(1,1,1);
		Vector3d normal = deref(v.vn-1, normals);
		
		EditableMesh.Vertex result = new EditableMesh.Vertex(pos, tex);
		result.putMaterialAttribute(MaterialAttribute.DIFFUSE_COLOR, col);
		result.putMaterialAttribute(MaterialAttribute.NORMAL, normal);
		
		return result;
	}
	
	private static EditableMesh.Face deref(IndexedFace f, ArrayList<Vector3d> positions, ArrayList<Vector2d> textures, ArrayList<Vector3d> normals) {
		EditableMesh.Vertex a = deref(f.a, positions, textures, normals);
		EditableMesh.Vertex b = deref(f.b, positions, textures, normals);
		EditableMesh.Vertex c = deref(f.c, positions, textures, normals);
		
		//Face result = new Face(a,b,c);
		//result.genFaceNormal();
		//return result;
		return new EditableMesh.Face(a,b,c);
	}
	
	
	private static class IndexedVertex {
		public int v  = -1; //pos
		public int vt = -1; //tex
		public int vn = -1; //norm
		
		public static IndexedVertex of(String def) {
			IndexedVertex result = new IndexedVertex();
			String[] parts = def.split("/");
			if (!parts[0].trim().isEmpty()) {
				result.v = Integer.parseUnsignedInt(parts[0].trim());
			}
			if (parts.length>1 && !parts[1].trim().isEmpty()) {
				result.vt = Integer.parseUnsignedInt(parts[1].trim());
			}
			if (parts.length>2 && !parts[2].trim().isEmpty()) {
				result.vn = Integer.parseUnsignedInt(parts[2].trim());
			}
			return result;
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
}
