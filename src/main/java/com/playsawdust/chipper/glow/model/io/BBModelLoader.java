package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;

public class BBModelLoader {
	public static void load(InputStream in) throws IOException {
		try {
			JsonObject bbJson = Jankson.builder().build().load(in);
			JsonObject metaObject = bbJson.getObject("meta");
			if (metaObject==null || !"3.6".equals(metaObject.get(String.class, "format_version"))) throw new IOException("Not a valid bbmodel");
			
			System.out.println("Model name: "+bbJson.get(String.class, "name"));
			
		} catch (SyntaxError e) {
			System.err.println(e.getCompleteMessage());
		}
	}
}
