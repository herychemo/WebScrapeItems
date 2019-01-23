package com.optimizedproductions.webscrapeitems.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class PostProcessModel implements Serializable {

	public static final int TYPE_TRIM = 1;
	public static final int TYPE_SPLIT = 2;

	private int type;

	private @Singular List<String> arguments;

}
