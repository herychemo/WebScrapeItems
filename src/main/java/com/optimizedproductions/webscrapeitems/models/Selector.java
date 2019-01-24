package com.optimizedproductions.webscrapeitems.models;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class Selector implements Serializable {

	public static final int TYPE_CSS = 1;
	public static final int TYPE_XPATH = 2;

	private String selector;

	@Builder.Default private int type = TYPE_CSS;

}
