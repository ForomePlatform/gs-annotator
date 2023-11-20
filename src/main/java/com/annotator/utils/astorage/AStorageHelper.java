package com.annotator.utils.astorage;

import com.annotator.utils.Constants;

import java.net.URI;
import java.net.URISyntaxException;

public class AStorageHelper implements Constants {
	public static URI createUniversalVariantQuery(
		String refBuild,
		String chr,
		String pos,
		String ref,
		String alt
	) {
		try {
			return new URI(ASTORAGE_SERVER_PATH + "/query/universalvariant" +
				"?refBuild=" + refBuild +
				"&chr=" + chr +
				"&pos=" + pos +
				"&ref=" + ref +
				"&alt=" + alt);
		} catch (URISyntaxException e) {
			return null; // TODO: Handle...
		}
	}
}
