package com.optimizedproductions.webscrapeitems.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class FetchModel implements Serializable {

	private @Singular Set<String> sources;

	private String itemSelector;

	private String nextButtonSelector;

	private @Singular List<FieldGetter> fields;

	private boolean usingJavascript;

}
