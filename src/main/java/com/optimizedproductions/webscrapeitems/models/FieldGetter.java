package com.optimizedproductions.webscrapeitems.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class FieldGetter implements Serializable {

	public static final int GETTER_METHOD_OFF = -1;
	public static final int GETTER_METHOD_TEXT = 0;
	public static final int GETTER_METHOD_ATTR = 1;

	private String fieldName;

	private Selector selector;

	private int getterMethod;

	private String getterAttr;

	private @Singular List<PostProcessModel> postProcessModels;

	private @Singular List<CancelIfModel> cancelIfModels;

}
