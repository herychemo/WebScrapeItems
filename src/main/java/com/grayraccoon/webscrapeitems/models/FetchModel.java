package com.grayraccoon.webscrapeitems.models;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class FetchModel implements Serializable {

	private @Singular Set<String> sources;

	private Selector itemSelector;

	private Selector nextButtonSelector;

	private @Singular List<FieldGetter> fields;

	private FieldGetter plainObject;

	private boolean usingJavascript;

}
