package com.optimizedproductions.webscrapeitems.processing;

import java.util.List;

public class ContainsOrCancelIf extends CancelIf {

	public ContainsOrCancelIf() {}

	public ContainsOrCancelIf(List<String> args) {
		super(args);
	}

	@Override
	public boolean testCondition(String result) {
		if (getArgs() == null) {
			return false;
		}

		for (String arg: getArgs()) {
			if (result.contains(arg)) {
				return true;
			}
		}
		return false;
	}

}
