package com.playsawdust.chipper.glow.pass;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.playsawdust.chipper.glow.gl.MeshFlattener;
import com.playsawdust.chipper.glow.gl.VertexBuffer;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;

public class MeshPass implements RenderPass {
	public static final long NANOS_PER_MILLI = 1_000_000L;
	public static final long MILLIS_PER_SECOND = 1_000L;
	
	private ArrayList<MeshEntry> scheduled = new ArrayList<>();
	private ArrayList<VertexBuffer> scheduledForDeletion = new ArrayList<>();
	private Cache<Mesh, VertexBuffer> generatedVBO = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.SECONDS)
			.<Mesh, VertexBuffer>removalListener((notification)-> {
				if (notification.getCause()==RemovalCause.EXPIRED) {
					scheduledForDeletion.add(notification.getValue());
				}
			})
			.build();
	//private HashMap<Mesh, VertexBuffer> generatedVBO = new HashMap<>();
	private VertexBuffer.Layout layout = new VertexBuffer.Layout();
	
	public void setLayout(VertexBuffer.Layout layout) {
		this.layout = layout;
	}
	
	//private MeshFlattener buffer = new MeshFlattener();
	
	@Override
	public void apply() {
		for(MeshEntry entry : scheduled) {
			if (entry.vbo==null) {
				
				try {
					entry.vbo = generatedVBO.get(entry.mesh, ()->MeshFlattener.uploadMesh(entry.mesh, layout) );
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
			if (entry.vbo!=null) {
				//TODO: DRAW
			}
			
			
		}
		
		scheduled.clear();
		
		//Clear out VBOs hit with cache eviction
		for(VertexBuffer buf : scheduledForDeletion) {
			buf.destroy();
		}
		scheduledForDeletion.clear();
	}

	@Override
	public boolean canEnqueue(Object o) {
		return (o instanceof Model) || (o instanceof Mesh); //&& matchesOtherOptions, e.g. shader, opacity
	}

	@Override
	public void enqueue(Object o, Vector3dc position) {
		if (o instanceof Model) {
			//Break into meshes
		} else if (o instanceof Mesh) {
			//FlatMesh cacheData = ((Mesh)o).getCache(this, FlatMesh.class);
			
			//if (cacheData==null) {
			//	cacheData = new FlatMesh();
			//	cacheData.mesh = (Mesh)o; //For now, we don't bother to flatten
			//}
			
			scheduled.add(new MeshEntry((Mesh)o, position));
		}
	}

	
	private static class MeshEntry {
		Mesh mesh; //TODO: Flattened data instead of mesh
		Vector3d position = new Vector3d(0,0,0);
		VertexBuffer vbo;
		/** The millisecond time at which the vbo was last used */
		long lastUsed;
		
		public MeshEntry(Mesh mesh, Vector3dc position) {
			this.mesh = mesh;
			this.position.set(position);
			this.vbo = null;
			this.lastUsed = System.nanoTime() / NANOS_PER_MILLI;
		}
	}

	
}
