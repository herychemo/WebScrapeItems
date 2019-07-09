package com.grayraccoon.webscrapeitems.processing;

import java.util.ArrayList;
import java.util.List;

public abstract class CancelIf {

	private List<String> args;

	public CancelIf() {
		this.args = new ArrayList<>();
	}

	public CancelIf(List<String> args) {
		this();
		if (args != null) {
			this.args = args;
		}
	}

	public List<String> getArgs() {
		return args;
	}

	public abstract boolean testCondition(String result);
}
