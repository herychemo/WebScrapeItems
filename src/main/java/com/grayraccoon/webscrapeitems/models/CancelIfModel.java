package com.grayraccoon.webscrapeitems.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class CancelIfModel implements Serializable {

	public static final int TYPE_CONTAINS_OR = 1;
	public static final int TYPE_CONTAINS_AND = 2;

	private int type;

	private @Singular List<String> arguments;

}
