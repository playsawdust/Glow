package com.playsawdust.chipper.glow.model.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.function.Consumer;

import com.google.common.io.ByteStreams;
import com.playsawdust.chipper.glow.model.Model;

public class ModelIO {
	private static ArrayDeque<ModelLoader> loaders = new ArrayDeque<>();
	
	static {
		loaders.add(new OBJLoader());
		
	}
	
	public static Model load(InputStream in) throws IOException {
		return load(in, (it)->{});
	}
	
	public static Model load(InputStream in, Consumer<Integer> progressConsumer) throws IOException {
		byte[] inBuffer = ByteStreams.toByteArray(in);
		for(ModelLoader loader : loaders) {
			ByteArrayInputStream bais = new ByteArrayInputStream(inBuffer);
			Model m = loader.tryLoad(bais, progressConsumer);
			if (m!=null) return m;
		}
		
		throw new IOException("Unknown model format");
	}
	
	public static void register(ModelLoader loader) {
		loaders.addFirst(loader); //The new loader will take precedence over older ones
	}
}
