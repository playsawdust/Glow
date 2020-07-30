package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;

import com.playsawdust.chipper.glow.model.Model;

public class OBJLoader implements ModelLoader {

	@Override
	public Model tryLoad(InputStream in) throws IOException {
		//TODO: Implement. For now it'll skip and say we're not the correct loader.
		return null;
	}

}
