package com.grayraccoon.webscrapeitems.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class BasicFetchModel implements Serializable {

    private String plainObjectName;

    private Selector itemSelector;

    private FieldGetter singlePlainObject;

}
